package neder.transmition.exception;

import neder.exception.ApplicationException;
import neder.trackeragent.R;

/**
 * Created by Matheus on 17/10/2016.
 */

public class TooMuchTransmitFailsException extends ApplicationException {
    public TooMuchTransmitFailsException() {
        super("tooMuchTransmitFails");
    }
}
