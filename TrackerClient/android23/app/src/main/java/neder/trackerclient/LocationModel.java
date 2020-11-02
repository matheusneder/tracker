package neder.trackerclient;

import com.google.gson.Gson;

import java.util.Date;

public class LocationModel {
    public double latitude = 0;
    public double longitude = 0;
    public Double altitude = null;
    public Float accuracy = null;
    public Float bearing = null;
    public Float speed = null;
    public Date time = null;
    public LocationProvider provider = null;
    public String id;
    public boolean parked = false;
}
