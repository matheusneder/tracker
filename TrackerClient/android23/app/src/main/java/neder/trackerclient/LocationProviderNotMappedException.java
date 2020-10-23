package neder.trackerclient;

import androidx.annotation.Nullable;

public class LocationProviderNotMappedException extends RuntimeException {
    private String provider;

    public LocationProviderNotMappedException(String provider) {
        this.provider = provider;
    }

    @Nullable
    @Override
    public String getMessage() {
        return "Could not map provider: '" + provider + "'";
    }
}
