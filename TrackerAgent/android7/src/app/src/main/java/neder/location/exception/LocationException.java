package neder.location.exception;

import neder.exception.ApplicationException;

/**
 * Created by Matheus on 17/10/2016.
 */

public abstract class LocationException extends ApplicationException {
    public LocationException(String errorCode) {
        super(errorCode);
    }
}
