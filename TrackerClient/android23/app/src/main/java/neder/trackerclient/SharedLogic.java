package neder.trackerclient;

import android.location.Location;

public class SharedLogic {
    public static boolean useNewLocation(LocationProvider newLocationProvider,
                                         float newLocationAccuracy,
                                         float oldLocationAccuracy,
                                         long newLocationTime,
                                         long oldLocationTime) {
        if(newLocationProvider == LocationProvider.NETWORK || newLocationProvider == LocationProvider.STORED_NETWORK) {
            if(oldLocationTime > (newLocationTime - 60000) && oldLocationAccuracy < newLocationAccuracy + 50.0F)
            {
                return false;
            }
        }

        return true;
    }
}
