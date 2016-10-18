package neder.location;

import android.location.Location;

import com.google.gson.Gson;

/**
 * Created by Matheus on 17/10/2016.
 */

public class LocationConverter {

    private static Gson gson = new Gson();

    @org.jetbrains.annotations.Contract("null -> fail")
    public static LocationDTO toLocationDTO(Location location){
        LocationDTO result = new LocationDTO();
        result.latitude = location.getLatitude();
        result.longitude = location.getLongitude();
        if(location.hasAltitude())
            result.altitude = location.getAltitude();
        if(location.hasAccuracy())
            result.accuracy = location.getAccuracy();
        if(location.hasBearing())
            result.bearing = location.getBearing();
        if(location.hasSpeed())
            result.speed = location.getSpeed();
        result.time = location.getTime();
        result.provider = location.getProvider();
        return result;
    }

    public static String toJSON(LocationDTO locationDTO){
        String result = gson.toJson(locationDTO);
        return result;
    }

    public static String toJSON(Location location) {
        return toJSON(toLocationDTO(location));
    }

    public static LocationDTO fromJSON(String json){
        return gson.fromJson(json, LocationDTO.class);
    }
}
