package neder.location.exception;

import neder.trackeragent.R;

/**
 * Created by Matheus on 17/10/2016.
 */

public class NetworkNotEnabledException extends LocationException {
    public NetworkNotEnabledException() {
        super(R.string.NetworkNotEnabledException);
    }
}
