package neder.location.exception;

/**
 * Created by Matheus on 17/10/2016.
 */

public class NetworkNotEnabledException extends LocationException {
    public NetworkNotEnabledException() {
        super("NETWORK_NOT_ENABLED");
    }
}
