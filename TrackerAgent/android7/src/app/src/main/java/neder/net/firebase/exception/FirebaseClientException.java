package neder.net.firebase.exception;

import neder.exception.ApplicationException;

/**
 * Created by Matheus on 18/10/2016.
 */

public class FirebaseClientException extends ApplicationException {

    public FirebaseClientException(String errorCode) {
        super(errorCode);
    }

    public FirebaseClientException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
