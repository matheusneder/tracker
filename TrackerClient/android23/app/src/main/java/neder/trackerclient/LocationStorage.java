package neder.trackerclient;

import android.util.Log;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class LocationStorage {
    private static LocationStorage instance = null;

    public static  LocationStorage getInstance() {
        if(instance == null) {
            instance = new LocationStorage();
        }
        return instance;
    }

    private LocationModel lastLocation = null;

    public void add(LocationModel locationModel) {
        if (lastLocation == null) {
            Log.i("LocationStorage.add", "STORED: lastLocation was null, storing the new location.");
            lastLocation = locationModel;
        }else if(locationModel.time.after(lastLocation.time)){
            Log.i("LocationStorage.add", "STORED: lastLocation was older than provided one, storing the new location.");
            lastLocation = locationModel;
        }else{
            Log.i("LocationStorage.add", "REJECTED: lastLocation was newer than provided one, rejecting the new location.");
        }
    }

    public LocationModel getLast() {
        return lastLocation;
    }
}
