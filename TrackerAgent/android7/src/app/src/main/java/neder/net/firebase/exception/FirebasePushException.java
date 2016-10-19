package neder.net.firebase.exception;

import neder.exception.ApplicationException;
import neder.trackeragent.R;

/**
 * Created by Matheus on 18/10/2016.
 */

public class FirebasePushException extends FirebaseClientException {
    public FirebasePushException(Throwable cause) {
        super(R.string.firebasePushException, cause);
    }
}
