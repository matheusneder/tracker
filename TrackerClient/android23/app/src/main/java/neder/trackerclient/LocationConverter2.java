package neder.trackerclient;

import android.location.Location;

import com.google.gson.Gson;

import java.time.Instant;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;

import neder.location.LocationDTO;
import neder.location.LocationPackageDTO;

public class LocationConverter2 {
    public static LocationModel toLocationModel(LocationPackageDTO locationPackage) {
        if(locationPackage == null) {
            throw new RuntimeException("locationPackage is null");
        }

        LocationDTO locationDTO = locationPackage.location;

        if(locationDTO == null) {
            throw new RuntimeException("locationPackage.location is null");
        }
        LocationModel locationModel = new LocationModel();
        locationModel.id = locationPackage.id;
        locationModel.accuracy = locationDTO.accuracy;
        locationModel.altitude = locationDTO.altitude;
        locationModel.latitude = locationDTO.latitude;
        locationModel.longitude = locationDTO.longitude;
        locationModel.bearing = locationDTO.bearing;
        locationModel.provider = toLocationProvider(locationDTO.provider);
        locationModel.speed = locationDTO.speed;
        locationModel.time = new Date(locationDTO.time);
        locationModel.parked = locationDTO.parked;
        return locationModel;
    }

    public static LocationProvider toLocationProvider(String provider) {
        switch (provider){
            case "gps":
                return LocationProvider.GPS;
            case "network":
                return LocationProvider.NETWORK;
            case "stored:gps":
                return LocationProvider.STORED_GPS;
            case "stored:network":
                return LocationProvider.STORED_NETWORK;
            default:
                throw new LocationProviderNotMappedException(provider);
        }
    }

    public static String fromLocationProvider(LocationProvider locationProvider) {
        switch (locationProvider){
            case GPS:
                return "gps";
            case NETWORK:
                return "network";
            case STORED_GPS:
                return "stored:gps";
            case STORED_NETWORK:
                return "stored:network";
            default:
                throw new RuntimeException("Invalid LocationProvider");
        }
    }

    public static String toLogString(LocationModel locationModel){
        return "id: " + locationModel.id + ", " +
                "latitude: " + locationModel.latitude + ", " +
                "longitude: " + locationModel.longitude + ", " +
                "accuracy: " + locationModel.accuracy + ", " +
                "altitude: " + locationModel.altitude + ", " +
                "bearing: " + locationModel.bearing + ", " +
                "provider: " + locationModel.provider.name() + ", " +
                "speed: " + locationModel.speed + ", " +
                "time: " + locationModel.time.toString();
    }

    public static String toLogString(LocationPackageDTO locationPackage) {
        return toLogString(toLocationModel(locationPackage));
    }

    public static Location toLocation(LocationModel locationModel) {
        Location location = new Location(fromLocationProvider(locationModel.provider));
        location.setLongitude(locationModel.longitude);
        location.setLatitude(locationModel.latitude);

        if(locationModel.accuracy != null) {
            location.setAccuracy(locationModel.accuracy);
        }
        if(locationModel.altitude != null) {
            location.setAltitude(locationModel.altitude);
        }
        if(locationModel.bearing != null) {
            location.setBearing(locationModel.bearing);
        }
        if(locationModel.speed != null) {
            location.setSpeed(locationModel.speed);
        }
        if(locationModel.time != null) {
            location.setTime(locationModel.time.getTime());
        }

        return location;
    }

    private static Gson gson = new Gson();

    public static String toJSON(LocationModel locationModel) {
        return gson.toJson(locationModel);
    }

    public static LocationModel fromJson(String data) {
        return gson.fromJson(data, LocationModel.class);
    }
}
