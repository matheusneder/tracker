package neder.trackerclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import neder.location.LocationChangeListener;
import neder.location.LocationPackageDTO;
import neder.location.LocationService;
import neder.location.exception.LocationException;

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

    private Location deviceLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeFirebase();

        try {

            LocationService locationService = new LocationService(this);
//
//            //updateLocationView(toLocationDTO(locationService.getCurrentLocation()));
//            Location currentLocation = locationService.getCurrentLocation();
//            if(currentLocation != null) {
//                handleNewLocation(currentLocation);
//            }
            //wakeup();

            deviceLocation = locationService.getCurrentLocation();

            locationService.addLocationChangeListener(new LocationChangeListener() {
                @Override
                public void onLocationChanged(Location location) {
                    deviceLocation = location;
                    doTheStuff();
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
    }

    private void initializeFirebase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        String deviceId = "motorolaMotoG5-e8e76a185f1707ab";//"unknownAndroidSDKbuiltforx86-a7977c739bcfe84";
        String path = "tracked-devices/" + deviceId + "/tracks";

        DatabaseReference myRef = database.getReference(path);

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                LocationPackageDTO locationPackage = snapshot.getValue(LocationPackageDTO.class);
                Log.i("onChildAdded", "key: " + key + "; " + LocationConverter2.toLogString(locationPackage));
                storeLocation(locationPackage);
                doTheStuff();
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

    private void doTheStuff() {
        LocationModel agentLocationModel = LocationStorage.getInstance().getLast();
        Location agentLocation = null;
        //float distance = 0.0F;

        if(agentLocationModel != null) {
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

        if(deviceLocation != null) {
            deviceLatitudeView.setText(String.valueOf(deviceLocation.getLatitude()));
            deviceLongitudeView.setText(String.valueOf(deviceLocation.getLongitude()));
            deviceTimeView.setText(new Date(deviceLocation.getTime()).toString());
            deviceSpeedView.setText(String.valueOf(deviceLocation.getSpeed() * 3.6));
            deviceProviderView.setText(String.valueOf(deviceLocation.getProvider()));
            deviceBearingView.setText(String.valueOf(deviceLocation.getBearing()));
            deviceAccuracyView.setText(String.valueOf(deviceLocation.getAccuracy()));
        }

        if(agentLocationModel != null && deviceLocation != null) {
            float distance = deviceLocation.distanceTo(agentLocation);
            float lagInSeconds = Math.abs((deviceLocation.getTime() - agentLocation.getTime()) / (float)1000);
            Log.i("onLocationChanged", "Distance: " + distance);

            float estimatedDistanceCompensation = deviceLocation.getSpeed() * lagInSeconds;
            float compensatedDistance = distance - estimatedDistanceCompensation - Math.max(agentLocation.getAccuracy(), deviceLocation.getAccuracy());

            distanceView.setText(String.valueOf(distance));
            lagView.setText(String.valueOf(lagInSeconds));
            compensationView.setText(String.valueOf(estimatedDistanceCompensation));
            if(compensatedDistance >= 0){
                compensatedDistanceView.setTextColor(Color.BLUE);
            }else{
                compensatedDistanceView.setTextColor(Color.RED);
            }
            compensatedDistanceView.setText(String.valueOf(compensatedDistance));
        }else{
            Log.i("doTheStuff", "Agent lastLocation or device location is null");
        }
    }

    private void storeLocation(LocationPackageDTO locationPackage) {
        LocationStorage store = LocationStorage.getInstance();
        store.add(LocationConverter2.toLocationModel(locationPackage));
    }
}