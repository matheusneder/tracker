package neder.trackerclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import neder.location.AuditLogger;
import neder.location.LocationPackageDTO;
import neder.location.LocationService;
import neder.location.SharedConstants;
import neder.location.exception.LocationException;
import static neder.location.AuditLogger.l;

public class MainActivity extends AppCompatActivity {

    private TextView agentLatitudeView;
    private TextView agentLongitudeView;
    private TextView agentTimeView;
    private TextView agentSpeedView;
    private TextView agentProviderView;
    private TextView agentAccuracyView;
    private TextView agentParkedView;
    private TextView agentBearingView;

    private TextView deviceLatitudeView;
    private TextView deviceLongitudeView;
    private TextView deviceTimeView;
    private TextView deviceSpeedView;
    private TextView deviceProviderView;
    private TextView deviceAccuracyView;
    private TextView deviceBearingView;

    private TextView distanceView;
    private TextView lagView;
    private TextView compensationView;
    private TextView compensatedDistanceView;
    private TextView potentialThefView;

    private Location deviceLocation = null;
    private ArrayList<Location> deviceLocationHistory = new ArrayList<>();

    private long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        initializeFirebase();
        AuditLogger.start(getExternalFilesDir(null).toString());
        l("Service starting...");
        try {

            LocationService locationService = new LocationService(this, SharedConstants.CLIENT_GPS_MIN_TIME, SharedConstants.CLIENT_GPS_MIN_DISTANCE);
//
//            //updateLocationView(toLocationDTO(locationService.getCurrentLocation()));
//            Location currentLocation = locationService.getCurrentLocation();
//            if(currentLocation != null) {
//                handleNewLocation(currentLocation);
//            }
            //wakeup();

            deviceLocation = locationService.getCurrentLocation();

            locationService.addLocationChangeListener(location -> {
                l("Device location changed (" + location.getTime() + ")", location);

                if(deviceLocation == null || SharedLogic.useNewLocation(
                        LocationConverter2.toLocationProvider(location.getProvider()),
                        location.getAccuracy(),
                        deviceLocation.getAccuracy(),
                        location.getTime(),
                        deviceLocation.getTime()))
                {
                    deviceLocation = location;
                    performLocComp();
                    synchronized (deviceLocationHistoryLockPad) {
                        deviceLocationHistory.add(location);
                    }
                } else {
                    l("New location REJECTED (" + location.getTime() + ")");
                }
            });

            //startOldMessageLoop();

        } catch (LocationException e) {
            //showGpsDisabledAlert(e.getMessageFromResource(this));
            e.printStackTrace();
        }        
        
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        agentLatitudeView = (TextView)findViewById(R.id.agentLatitudeValue);
        agentLongitudeView = (TextView)findViewById(R.id.agentLongitudeValue);
        agentTimeView = (TextView)findViewById(R.id.agentTimeValue);
        agentSpeedView = (TextView)findViewById(R.id.agentSpeedValue);
        agentProviderView = (TextView)findViewById(R.id.agentProviderValue);
        agentAccuracyView = (TextView)findViewById(R.id.agentAccuracyValue);
        agentParkedView = (TextView)findViewById(R.id.agentParkedValue);
        agentBearingView = (TextView)findViewById(R.id.agentBearingValue);

        deviceLatitudeView = (TextView)findViewById(R.id.deviceLatitudeValue);
        deviceLongitudeView = (TextView)findViewById(R.id.deviceLongitudeValue);
        deviceTimeView = (TextView)findViewById(R.id.deviceTimeValue);
        deviceSpeedView = (TextView)findViewById(R.id.deviceSpeedValue);
        deviceProviderView = (TextView)findViewById(R.id.deviceProviderValue);
        deviceAccuracyView = (TextView)findViewById(R.id.deviceAccuracyValue);
        deviceBearingView = (TextView)findViewById(R.id.deviceBearingValue);

        distanceView = (TextView)findViewById(R.id.distanceValue);
        lagView = (TextView)findViewById(R.id.lagValue);
        compensationView = (TextView)findViewById(R.id.compensationValue);
        compensatedDistanceView = (TextView)findViewById(R.id.compensatedDistanceValue);
        potentialThefView = (TextView)findViewById(R.id.potentialThefValue);
    }

    private void initializeFirebase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // motorolaMotoG5-e8e76a185f1707ab
        // unknownAndroidSDKbuiltforx86-a7977c739bcfe84
        String deviceId = "motorolaMotoG5-e8e76a185f1707ab";
        String path = "tracked-devices/" + deviceId + "/tracks";

        DatabaseReference myRef = database.getReference(path);

        myRef.limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                LocationPackageDTO locationPackage = snapshot.getValue(LocationPackageDTO.class);
                l("Package received", locationPackage);

                // NOTE: storeLocation returns false if the locationPackage is older than the
                // last known agent location (it will be stored anyway -or- an exception will be thrown)
                if(storeLocation(locationPackage)) {
                    performLocComp();
                    removeOldDeviveLocationHistory(locationPackage.location.time);
                }
                snapshot.getRef().removeValue();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChildEventListenerError", error.getMessage(), error.toException());
            }
        });

//        myRef.addValueEventListener(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                //Object value = snapshot.getValue(Object.class);
//
//                GenericTypeIndicator<HashMap<String, LocationPackageDTO>> gti = new GenericTypeIndicator<HashMap<String, LocationPackageDTO>>() {};
//                HashMap<String, LocationPackageDTO> value = snapshot.getValue(gti);
//
//                for(HashMap.Entry<String, LocationPackageDTO> item : value.entrySet()) {
//                    LocationDTO location = item.getValue().location;
//                    Log.d("X", "Latitude: " + location.latitude +
//                            " Longitude: " + location.longitude +
//                            " Time: " + new java.util.Date(location.time));
//                }
//
//                ///snapshot.get
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Exception ex = error.toException();
//            }
//        });
    }

    private long logScopeId = 0;
    private Object logScopeIdLockPad = new Object();

    private void performLocComp() {
        synchronized (logScopeIdLockPad) {
            final long logScopeId = this.logScopeId++;
            l(logScopeId + "performLocComp triggered.");
            LocationModel agentLocationModel = LocationStorage.getInstance(this).getLast();
            Location agentLocation = null;
            Location deviceLocation = this.deviceLocation;
            l(logScopeId + "performLocComp: agentLocationModel", agentLocationModel);
            l(logScopeId + "performLocComp: deviceLocation", deviceLocation);
            if (agentLocationModel != null) {
                agentLocation = LocationConverter2.toLocation(agentLocationModel);
                agentLatitudeView.setText(String.valueOf(agentLocation.getLatitude()));
                agentLongitudeView.setText(String.valueOf(agentLocation.getLongitude()));
                agentTimeView.setText(new Date(agentLocation.getTime()).toString());
                agentSpeedView.setText(String.valueOf(agentLocation.getSpeed() * 3.6));
                agentProviderView.setText(String.valueOf(agentLocation.getProvider()));
                agentBearingView.setText(String.valueOf(agentLocation.getBearing()));
                agentAccuracyView.setText(String.valueOf(agentLocation.getAccuracy()));
                agentParkedView.setText(String.valueOf(agentLocationModel.parked));
            }

            if (deviceLocation != null) {
                deviceLatitudeView.setText(String.valueOf(deviceLocation.getLatitude()));
                deviceLongitudeView.setText(String.valueOf(deviceLocation.getLongitude()));
                deviceTimeView.setText(new Date(deviceLocation.getTime()).toString());
                deviceSpeedView.setText(String.valueOf(deviceLocation.getSpeed() * 3.6));
                deviceProviderView.setText(String.valueOf(deviceLocation.getProvider()));
                deviceBearingView.setText(String.valueOf(deviceLocation.getBearing()));
                deviceAccuracyView.setText(String.valueOf(deviceLocation.getAccuracy()));
            }

            if (agentLocationModel != null && deviceLocation != null) {
                float distance = deviceLocation.distanceTo(agentLocation);
                float lagInSeconds = Math.abs((float) (deviceLocation.getTime() - agentLocation.getTime()) / 1000.0F);
                Log.i("onLocationChanged", "Distance: " + distance);

                float deviceAvgSpeed = getDeviceAvgSpeedSince(agentLocation.getTime());
                float estimatedDistanceCompensation = 0.0F;
                if (!agentLocationModel.parked) {
                    estimatedDistanceCompensation = (deviceAvgSpeed * lagInSeconds) * 1.5F;
                }
                Log.i(logScopeId + "performLocComp", "estimatedDistanceCompensation: " + estimatedDistanceCompensation);
                float compensatedDistance = distance - estimatedDistanceCompensation -
                        Math.max(agentLocation.getAccuracy(), deviceLocation.getAccuracy()) - SharedConstants.DISTANCE_TOLERANCE;

                distanceView.setText(String.valueOf(distance));
                lagView.setText(String.valueOf(lagInSeconds));
                compensationView.setText(String.valueOf(estimatedDistanceCompensation));
                if (compensatedDistance >= 0) {
                    compensatedDistanceView.setTextColor(Color.BLUE);
                } else {
                    compensatedDistanceView.setTextColor(Color.RED);
                }
                compensatedDistanceView.setText(String.valueOf(compensatedDistance));
                l(logScopeId + "performLocComp: computed data", new AuditData(distance, lagInSeconds, deviceAvgSpeed, estimatedDistanceCompensation, compensatedDistance));
                // the main rule
                if (!agentLocationModel.parked && compensatedDistance > 0 && startTime + 5000 < System.currentTimeMillis()) {
                    l(logScopeId + "performLocComp: the condition has been met");
                    synchronized (incidenceReportTimerLockPad) {
                        if (!incidenceReportTimerScheduled) {
                            incidenceReportTimerScheduled = true;
                            incidenceReportTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    synchronized (incidenceReportTimerLockPad) {
                                        hlr.postDelayed(() -> {
                                            l(logScopeId + "performLocComp: calling reportIncident");
                                            reportIncident();
                                        }, 500);
                                    }
                                }
                            }, SharedConstants.INCIDENT_REPORT_DELAY);
                            l(logScopeId + "performLocComp: incidenceReportTimerScheduled");
                        }
                    }
                } else {
                    synchronized (incidenceReportTimerLockPad) {
                        incidenceReportTimer.cancel();
                        incidenceReportTimer = new Timer();
                        incidenceReportTimerScheduled = false;
                        l(logScopeId + "performLocComp: incidenceReportTimerCanceled");
                    }
                }
            } else {
                l(logScopeId + "performLocComp: Agent lastLocation or device location is null");
            }
        }
    }

    private Handler hlr = new Handler();

    private void reportIncident() {

        potentialThefView.setTextColor(Color.RED);
        potentialThefView.setText(String.valueOf(true));
    }

    private Object incidenceReportTimerLockPad = new Object();
    private Timer incidenceReportTimer = new Timer();
    private  boolean incidenceReportTimerScheduled = false;

    private Object deviceLocationHistoryLockPad = new Object();

    private float getDeviceAvgSpeedSince(long time) {
        float acc = 0.0F;
        int count = 0;
        synchronized (deviceLocationHistoryLockPad) {
            for (Location deviceLocation : deviceLocationHistory) {
                if (deviceLocation.getTime() >= time) {
                    acc += deviceLocation.getSpeed();
                    count++;
                }
            }
        }
        if(count > 0)
            return acc / (float)count;
        return 0.0F;
    }

    private void removeOldDeviveLocationHistory(long time) {
        ArrayList<Location> newDeviceLocationHistory = new ArrayList<>();
        synchronized (deviceLocationHistoryLockPad) {
            for (Location deviceLocation : deviceLocationHistory) {
                if (deviceLocation.getTime() >= time) {
                    newDeviceLocationHistory.add(deviceLocation);
                }
            }
            deviceLocationHistory = newDeviceLocationHistory;
        }
    }

    /**
     *
     * @param locationPackage
     * @return returns false if the locationPackage is older than the lastKnowedLocation or true otherwise
     */
    private boolean storeLocation(LocationPackageDTO locationPackage) {
        LocationStorage store = LocationStorage.getInstance(this);
        return store.add(LocationConverter2.toLocationModel(locationPackage));
    }
}