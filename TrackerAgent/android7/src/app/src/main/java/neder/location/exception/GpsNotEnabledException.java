package neder.location.exception;

/**
 * Created by Matheus on 16/10/2016.
 */
public class GpsNotEnabledException extends LocationException {
    public GpsNotEnabledException() {
        super("GPS_NOT_ENABLED");
    }
}
