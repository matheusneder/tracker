package neder.location.exception;

import neder.trackeragent.R;

/**
 * Created by Matheus on 16/10/2016.
 */
public class GpsNotEnabledException extends LocationException {
    public GpsNotEnabledException() {
        super(R.string.GpsNotEnabledException);
    }
}
