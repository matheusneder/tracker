package neder.location;

import android.location.Location;

/**
 * Created by Matheus on 17/10/2016.
 */

public class LocationDTO implements Cloneable {
    public double latitude = 0;
    public double longitude = 0;
    public Double altitude = null;
    public Float accuracy = null;
    public Float bearing = null;
    public Float speed = null;
    public Long time = null;
    public String provider = null;
    public boolean parked = false;
    public Float originSpeed = null;
    public Long originTime = null;

    public LocationDTO clone() {
        LocationDTO result = new LocationDTO();

        result.latitude = latitude;
        result.longitude = longitude;
        if(altitude != null)
            result.altitude = altitude.doubleValue();
        if(accuracy != null)
            result.accuracy = accuracy.floatValue();
        if(bearing != null)
            result.bearing = bearing.floatValue();
        if(speed != null)
            result.speed = speed.floatValue();
        if(time != null)
            result.time = time.longValue();
        result.provider = provider;
        result.parked = parked;

        return  result;
    }
}
