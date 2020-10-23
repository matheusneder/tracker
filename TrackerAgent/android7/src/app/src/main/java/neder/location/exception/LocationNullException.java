package neder.location.exception;

/**
 * Created by Matheus on 17/10/2016.
 */
public class LocationNullException extends LocationException {
    public LocationNullException() {
        super("LOCATION_IS_NULL");
    }
}
