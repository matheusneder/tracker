package neder.location.exception;

import neder.trackeragent.R;

/**
 * Created by Matheus on 17/10/2016.
 */
public class LocationNullException extends LocationException {
    public LocationNullException() {
        super(R.string.LocationNullException);
    }
}
