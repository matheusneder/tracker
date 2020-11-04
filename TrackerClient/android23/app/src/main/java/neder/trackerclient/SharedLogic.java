package neder.trackerclient;
import static neder.location.AuditLogger.l;

import android.location.Location;

import java.util.Date;

public class SharedLogic {
    public static boolean useNewLocation(LocationProvider newLocationProvider,
                                         LocationProvider oldLocationProvider,
                                         float newLocationAccuracy,
                                         float oldLocationAccuracy,
                                         long newLocationTime,
                                         long oldLocationTime)
    {
        if(oldLocationProvider == LocationProvider.GPS || oldLocationProvider == LocationProvider.STORED_GPS) {
            if (newLocationProvider == LocationProvider.NETWORK || newLocationProvider == LocationProvider.STORED_NETWORK) {
                if (oldLocationTime > (newLocationTime - 60000) && oldLocationAccuracy < newLocationAccuracy + 50.0F) {
                    l("useNewLocation: REJECTING location",
                            new RejectedLocationData(newLocationProvider,
                                    oldLocationProvider,
                                    newLocationAccuracy,
                                    oldLocationAccuracy,
                                    newLocationTime,
                                    oldLocationTime));
                    return false;
                }
            }
        }

        return true;
    }

    public static class RejectedLocationData
    {
        public RejectedLocationData(LocationProvider newLocationProvider,
                                    LocationProvider oldLocationProvider,
                                    float newLocationAccuracy,
                                    float oldLocationAccuracy,
                                    long newLocationTime,
                                    long oldLocationTime)
        {
            this.newLocationProvider = newLocationProvider;
            this.oldLocationProvider = oldLocationProvider;
            this.newLocationAccuracy = newLocationAccuracy;
            this.oldLocationAccuracy = oldLocationAccuracy;
            this.newLocationTime = new Date(newLocationTime);
            this.oldLocationTime = new Date(oldLocationTime);
        }
        public LocationProvider newLocationProvider;
        public LocationProvider oldLocationProvider;
        public float newLocationAccuracy;
        public float oldLocationAccuracy;
        public Date newLocationTime;
        public Date oldLocationTime;
    }
}
