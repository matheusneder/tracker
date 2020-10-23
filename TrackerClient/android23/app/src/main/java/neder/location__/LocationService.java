package neder.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

import neder.location.exception.GpsNotEnabledException;
import neder.location.exception.NetworkNotEnabledException;

/**
 * Created by Matheus on 16/10/2016.
 */

public class LocationService implements LocationListener {

    private static final long GPS_MIN_TIME = 5000;
    private static final float GPS_MIN_DISTANCE = 5F;
    private static final long NETWORK_MIN_TIME = 50000;
    private static final float NETWORK_MIN_DISTANCE = 50F;

    // se receber update por network e o gps estiver sem resposta por tempo > q o definido nesta
    // constante (em milliseconds), atualiza com a localizacao do NETWORK
    private static final long UPDATE_WITH_NETWORK_GPS_TIMEOUT = 30000;

    private Context context;
    private LocationManager locationManager;
    private Location location;
    private ArrayList<LocationChangeListener> locationChangeListeners = new ArrayList<LocationChangeListener>();

    public Location getCurrentLocation() {
        return location;
    }

    public LocationService(Context context) throws GpsNotEnabledException, NetworkNotEnabledException {
        this.context = context;
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //throw new GpsNotEnabledException();
        }
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //throw new NetworkNotEnabledException();
        }

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NETWORK_MIN_TIME, NETWORK_MIN_DISTANCE, this);
    }

    public LocationChangeListener addLocationChangeListener(LocationChangeListener listener) {
        locationChangeListeners.add(listener);
        return listener;
    }

    public boolean RemoveLocationChangeListener(LocationChangeListener listener){
        return locationChangeListeners.remove(listener);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v("LocationService", "onLocationChanged Triggered");
        boolean acceptNewLocation = false;
        // possuo uma localizacao anterior proveniente do gps (guardada em this.location) e
        // estou recebendo uma nova localizacao por rede
        if(this.location != null && this.location.getProvider().equals(LocationManager.GPS_PROVIDER) &&
                location.getProvider().equals(LocationManager.NETWORK_PROVIDER))
        {
            // a localizacao por rede sera aceita (e atualizada) somente se o momento da utltima
            // atualizacao tiver sido a mais do que UPDATE_WITH_NETWORK_GPS_TIMEOUT atras
            if(System.currentTimeMillis() - UPDATE_WITH_NETWORK_GPS_TIMEOUT > this.location.getTime()) {
                acceptNewLocation = true;
            }
        } else {
            acceptNewLocation = true;
        }
        if(acceptNewLocation) {
            this.location = location;
            for (LocationChangeListener listener : locationChangeListeners) {
                listener.onLocationChanged(location);
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.v("LocationService", "onStatusChanged: " + s + ", " + i);
//        for (ILocationChangeListener listener : locationChangeListeners) {
//            listener.onStatusChanged(s);
//        }
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.v("LocationService", "onProviderEnabled: " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.v("LocationService", "onProviderDisabled: " + s);
    }
}
