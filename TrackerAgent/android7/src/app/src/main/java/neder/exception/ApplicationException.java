package neder.exception;

import android.content.Context;

/**
 * Created by Matheus on 17/10/2016.
 */

public class ApplicationException extends Exception {

    private String errorCode;

    public ApplicationException(String errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public ApplicationException(String errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
