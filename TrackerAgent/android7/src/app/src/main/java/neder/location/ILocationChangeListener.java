package neder.location;

import android.location.Location;

/**
 * Created by Matheus on 16/10/2016.
 */
public interface ILocationChangeListener {
    void onLocationChanged(Location location);
    //void onStatusChanged(String s);
}
