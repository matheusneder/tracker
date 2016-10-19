package neder.exception;

import android.content.Context;

/**
 * Created by Matheus on 17/10/2016.
 */

public class ApplicationException extends Exception {

    private int messageCode;

    public ApplicationException(int messageCode) {
        super();
        this.messageCode = messageCode;
    }

    public ApplicationException(int messageCode, Throwable cause) {
        super(cause);
    }

    public String getMessageFromResource(Context context) {
        return  context.getString(messageCode);
    }

    public int getMessageCode() {
        return messageCode;
    }
}
