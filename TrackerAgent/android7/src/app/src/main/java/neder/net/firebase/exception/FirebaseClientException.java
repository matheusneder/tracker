package neder.net.firebase.exception;

import neder.exception.ApplicationException;

/**
 * Created by Matheus on 18/10/2016.
 */

public class FirebaseClientException extends ApplicationException {

    public FirebaseClientException(int messageCode) {
        super(messageCode);
    }

    public FirebaseClientException(int messageCode, Throwable cause) {
        super(messageCode, cause);
    }
}
