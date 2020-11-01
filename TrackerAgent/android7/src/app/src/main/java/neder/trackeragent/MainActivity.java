package neder.trackeragent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import neder.device.DeviceInfo;
import neder.location.LocationChangeListener;
import neder.location.LocationConverter;
import neder.location.LocationDTO;
import neder.location.LocationPackageDTO;
import neder.location.SharedConstants;
import neder.location.exception.LocationException;
import neder.location.LocationService;
import neder.net.firebase.FirebaseClient;
import neder.net.firebase.exception.FirebaseClientException;

import static neder.location.LocationConverter.toLocationDTO;

public class MainActivity extends Activity {

    private static final long DELAY_TO_START_TRANSMITING_OLD_LOCATIONS = 10000;
    private static final long INTERVAL_TO_RETRY_TO_TRANSMIT_OLD_LOCATION = 30000;
    private static final long TRANSMIT_OLD_STORED_LOCATION_PAK_LIMIT = 100;
    private static final long TRASMIT_FAILS_TO_STOP = 4;
    private TextView latitudeView;
    private TextView longitudeView;
    private TextView altitudeView;
    private TextView accuracyView;
    private TextView bearingView;
    private TextView speedView;
    private TextView timeView;
    private TextView providerView;
    private TextView locationUpdatesView;
    private EditText statusLogView;

    private long locationUpdateCount = 0;
    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initializeDatabase();
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        latitudeView = (TextView)findViewById(R.id.latitudeValue);
        longitudeView = (TextView)findViewById(R.id.longitudeValue);
        altitudeView =  (TextView)findViewById(R.id.altitudeValue);
        accuracyView = (TextView)findViewById(R.id.accuracyValue);
        bearingView = (TextView)findViewById(R.id.bearingValue);
        speedView = (TextView)findViewById(R.id.speedValue);
        timeView = (TextView)findViewById(R.id.timeValue);
        providerView = (TextView)findViewById(R.id.providerValue);
        locationUpdatesView = (TextView)findViewById(R.id.locationUpdateCountValue);

        //statusLogView = (EditText)findViewById(R.id.statusLog);

        try {

            locationService = new LocationService(this);
//
//            //updateLocationView(toLocationDTO(locationService.getCurrentLocation()));
//            Location currentLocation = locationService.getCurrentLocation();
//            if(currentLocation != null) {
//                handleNewLocation(currentLocation);
//            }
            wakeup();
            locationService.addLocationChangeListener(new LocationChangeListener() {
                @Override
                public void onLocationChanged(Location location) {
                    handleNewLocation(location);
                }
            });

        } catch (LocationException e) {
            showGpsDisabledAlert(e.getErrorCode());
            Log.e("Inicializacao", "LocationException ao inicializar", e);
        }
    }

    private enum OldMessageLoopState{
        STOPPED,
        WAITING,
        RUNNING
    }

    private OldMessageLoopState oldMessageLoopState = OldMessageLoopState.STOPPED;


    private void wakeup() {
        if(oldMessageLoopState == OldMessageLoopState.STOPPED) {
            Log.v("MainActivity", "acordando");
            serviceState = ServiceState.NORMAL;
            tryTransmitLocationPackageFailCount = 0;
            Location currentLocation = locationService.getCurrentLocation();
            boolean haveTakenCurrentLocationSuccessful = currentLocation != null;
            if (haveTakenCurrentLocationSuccessful) {
                handleNewLocation(currentLocation);
            }
            // se nao tiver pego a localizacao corrente tenta enviar as armazenadas imediatamente,
            // caso contrario inicia com atraso
            startOldMessageLoop(!haveTakenCurrentLocationSuccessful);
        } else {
            Log.e("MainActivity", "Calling wakeup with illegal oldMessageLoopState: " + oldMessageLoopState.toString());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    final Handler handler = new Handler();
    Runnable runnableForTransmitOldStoredLocations;

    private void startOldMessageLoop(boolean startImadiately) {
        // transmite dados pendentes se houver (com delay para priorizar as localizacoes mais atuais)
        if(oldMessageLoopState == OldMessageLoopState.STOPPED) {
            oldMessageLoopState = OldMessageLoopState.WAITING;
            runnableForTransmitOldStoredLocations = new Runnable() {
                @Override
                public void run() {
                    boolean keepPosting = true;
                    try {
                        oldMessageLoopState = OldMessageLoopState.RUNNING;
                        transmitOldStoredLocations();
//                    } catch (TooMuchTransmitFailsException e) {
//                        oldMessageLoopState = OldMessageLoopState.STOPPED;
//                        keepPosting = false;
                    } finally {
                        if(keepPosting && oldMessageLoopState != OldMessageLoopState.STOPPED) {
                            oldMessageLoopState = OldMessageLoopState.WAITING;
                            // torna a executar (com atraso)
                            handler.postDelayed(this, INTERVAL_TO_RETRY_TO_TRANSMIT_OLD_LOCATION);
                        }
                    }
                }
            };
            handler.postDelayed(runnableForTransmitOldStoredLocations, startImadiately ? 0 : DELAY_TO_START_TRANSMITING_OLD_LOCATIONS);

        } else {
            Log.e("MainActivity", "Calling startOldMessageLoop with illegal oldMessageLoopState: " + oldMessageLoopState.toString());
        }
    }

    private void stopOldMessageLoop(){
        switch (oldMessageLoopState){
            case WAITING:
                handler.removeCallbacks(runnableForTransmitOldStoredLocations);
                oldMessageLoopState = OldMessageLoopState.STOPPED;
                break;
            case RUNNING:
                oldMessageLoopState = OldMessageLoopState.STOPPED;
                break;
            case STOPPED:
                Log.e("MainActivity", "Calling stopOldMessageLoop but oldMessageLoopState state is already STOPPED");
                break;
        }
    }

    private Timer parkNotificationTimer = new Timer();
    private final Object lockPad = new Object();

    private void handleNewLocation(Location location) {
        synchronized (lockPad) {
            parkNotificationTimer.cancel();
            parkNotificationTimer = new Timer();

            locationUpdateCount++;
            locationUpdatesView.setText(Long.toString(locationUpdateCount));
            final LocationDTO locationDTO = toLocationDTO(location);
            storeAndTryTransmitLocationPackage(locationDTO);
            updateLocationView(locationDTO);
            parkNotificationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (lockPad) {
                        Log.v("handleNewLocation:timer", "parkNotificationTimer fired!");
                        LocationDTO parkedLocationDTO = locationDTO.clone();
                        parkedLocationDTO.parked = true;
                        parkedLocationDTO.originSpeed = locationDTO.speed;
                        parkedLocationDTO.speed = 0.0F;
                        parkedLocationDTO.originTime = locationDTO.time;
                        parkedLocationDTO.time = System.currentTimeMillis();
                        storeAndTryTransmitLocationPackage(parkedLocationDTO);
                    }
                }
            }, SharedConstants.PARKED_TIMER_DELAY, SharedConstants.PARKED_TIMER_INTERVAL);
        }
    }

    private void storeAndTryTransmitLocationPackage(LocationDTO locationDTO) {
        String packageId = storeLocation(locationDTO);
        tryTransmitLocationPackage(packageId, locationDTO);
    }

    private SQLiteDatabase db;

    private SQLiteDatabase getDatabase() {
        if(db == null) {
            db = openOrCreateDatabase("TrackerAgent", MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS Packages(id VARCHAR PRIMARY KEY, data TEXT, sent BOOLEAN);");
        } else if(!db.isOpen()) {
            db = openOrCreateDatabase("TrackerAgent", MODE_PRIVATE, null);
        }
        return db;
    }

    private int sequenceForPackageIds = 0;
    private Random randomGeneratorForPackageIds = new Random();

    private String generateSequentialUniqueId() {
        String time = String.format("%016X", System.currentTimeMillis());
        String seq  = String.format("%08X", sequenceForPackageIds++);
        String rand = String.format("%08X", randomGeneratorForPackageIds.nextInt());
        return time + "-" + seq + "-" + rand;
    }

    /**
     *
     * @param locationDTO
     * @return the unique identifier
     */
    private String storeLocation(LocationDTO locationDTO) {
        String id = generateSequentialUniqueId();
        LocationDTO storedLocationDTO = locationDTO.clone();
        SQLiteDatabase db = getDatabase();
        SQLiteStatement stmt = db.compileStatement("INSERT INTO Packages(id, data, sent) VALUES (?, ? , 0)");
        stmt.bindString(1, id);
        storedLocationDTO.provider = String.format("stored:%s", locationDTO.provider);
        stmt.bindString(2, LocationConverter.toJSON(storedLocationDTO));
        stmt.execute();
        db.close();
        return id;
    }

    int tryTransmitLocationPackageFailCount = 0;

    private void tryTransmitLocationPackage(String id, LocationDTO data){
        if(tryTransmitLocationPackageFailCount < TRASMIT_FAILS_TO_STOP) {
            if (doTheTransmitionOfLocationPackageThroughCloud(id, data)) {
                tryTransmitLocationPackageFailCount = 0;
                // marca o pacote como enviado no controle local
                SQLiteDatabase db = getDatabase();
                SQLiteStatement stmt = db.compileStatement("UPDATE Packages SET sent = 1 WHERE id = ?");
                stmt.bindString(1, id);
                stmt.execute();
                db.close();
            } else {
                tryTransmitLocationPackageFailCount++;
                Log.e("MainActivity", "Fail to trasmit #" + tryTransmitLocationPackageFailCount);
            }
        } else {
            SwitchToCantTransmitPackagesState();
        }
    }

    private void SwitchToCantTransmitPackagesState() {
        if(serviceState != ServiceState.CANT_TRANSMIT_PACKAGES){
            Log.v("MainActivity", "mudando para estado sem conexao");
            serviceState = ServiceState.CANT_TRANSMIT_PACKAGES;
            stopOldMessageLoop();
            startConnectionCheckerTimer();
        }
    }

    private void startConnectionCheckerTimer() {
        final Handler connectionCheckerHandler = new Handler();

        connectionCheckerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean keepTring = true;
                try {
                    Log.v("MainActivity", "Verificando se ha conexao");
                    if(isNetworkAvailable()) {
                        keepTring = false;
                        wakeup();
                    }
                } finally {
                    if(keepTring) {
                        connectionCheckerHandler.postDelayed(this, 1000);
                    }
                }
            }
        }, 1000);
    }

    private enum ServiceState {
        NORMAL,
        CANT_TRANSMIT_PACKAGES
    }

    private ServiceState serviceState = ServiceState.NORMAL;

    private void transmitOldStoredLocations() {
        Log.v("MainActivity", "transmitOldStoredLocations called");
        if(tryTransmitLocationPackageFailCount >= TRASMIT_FAILS_TO_STOP) {
            Log.e("MainActivity", "transmitOldStoredLocations, tryTransmitLocationPackageFailCount = " + tryTransmitLocationPackageFailCount);
            SwitchToCantTransmitPackagesState();
        } else {
            SQLiteDatabase db = getDatabase();
            try {
                // pega do mais recente para o mais antigo
                Cursor resultSet = db.rawQuery(
                        "SELECT id, data FROM Packages WHERE sent = 0 ORDER BY id DESC LIMIT " +
                                TRANSMIT_OLD_STORED_LOCATION_PAK_LIMIT, null);
                if (resultSet.moveToFirst()) {
                    do {
                        String id = resultSet.getString(resultSet.getColumnIndex("id"));
                        String jsonData = resultSet.getString(resultSet.getColumnIndex("data"));
                        LocationDTO data = LocationConverter.fromJSON(jsonData);
                        tryTransmitLocationPackage(id, data);
                    }
                    while (resultSet.moveToNext());
                }
                resultSet.close();

                db.execSQL("DELETE FROM Packages WHERE sent = 1"); // limpa (localmente) os pacotes enviados
            }catch (IllegalStateException e){
                Log.v("transmitOldStoredLoc", "transmitOldStoredLocations thrown IllegalStateException :/", e);
            }
            db.close();
        }
    }

    private boolean doTheTransmitionOfLocationPackageThroughCloud(String id, LocationDTO data) {

        try {
            DeviceInfo deviceInfo = new DeviceInfo(this);
            LocationPackageDTO locationPackage = new LocationPackageDTO(id, data);
            String fbServiceUrl = getResources().getString(R.string.default_fb_account_service_url);

            if(fbServiceUrl == null || !fbServiceUrl.matches("^https://.*\\.firebaseio\\.com/?$"))
            {
                throw new RuntimeException("Cannot read default_fb_account_service_url resource value or its value dont match regex ^https://.*\\.firebaseio\\.com/?$. Look for local.properties.");
            }

            FirebaseClient client = new FirebaseClient(fbServiceUrl);
            String deviceIdPathSegment = (deviceInfo.getDeviceName() + "-" + deviceInfo.getDeviceId()).replaceAll("[^a-zA-Z0-9_-]", "");
            client.push("/tracked-devices/" + deviceIdPathSegment + "/tracks", locationPackage);
            return true;
        }catch(FirebaseClientException e) {
            Log.e("MainActivity", "FirebaseClientException thrown");
            Log.getStackTraceString(e);
            return false;
        }
    }

    public void showGpsDisabledAlert(String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.GpsDisabledDialogTitle)
                .setMessage(message)
                .show();
    }

    public void updateLocationView(LocationDTO locationDTO){
        latitudeView.setText(Double.toString(locationDTO.latitude));
        longitudeView.setText(Double.toString(locationDTO.longitude));
        if(locationDTO.altitude != null)
            altitudeView.setText(Double.toString(locationDTO.altitude));
        else
            altitudeView.setText("n/a");
        if(locationDTO.accuracy != null)
            accuracyView.setText(Double.toString(locationDTO.accuracy));
        else
            accuracyView.setText("n/a");
        if(locationDTO.bearing != null)
            bearingView.setText(Double.toString(locationDTO.bearing));
        else
            bearingView.setText("n/a");
        if(locationDTO.speed != null)
            speedView.setText(Double.toString(locationDTO.speed));
        else
            speedView.setText("n/a");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZZ");
        timeView.setText(simpleDateFormat.format(new Date(locationDTO.time)));
        providerView.setText(locationDTO.provider);
    }
}
