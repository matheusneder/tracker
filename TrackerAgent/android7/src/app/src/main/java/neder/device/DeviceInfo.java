package neder.device;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.security.AccessController;
// teste commit
/**
 * Created by Matheus on 18/10/2016.
 */

public class DeviceInfo {

    private Context context;

    public DeviceInfo(Context context){
        this.context = context;
    }

    public String getDeviceName() {
        String result;
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            result = model;
        } else {
            result = manufacturer + " " + model;
        }
        return result;
    }

    public String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

}
