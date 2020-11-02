package neder.location;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

public class AuditLogger {
    private static final String LOG_CAT = "TRACKERAUDIT";
    private static Gson serializer = new Gson();

    public static void l(String message, Object data){
        Log.i(LOG_CAT, message + " || data: " + serializer.toJson(data));
    }

    public static void l(String message){
        Log.i(LOG_CAT, message);
    }

    public static void l(String message, Throwable error){
        Log.i(LOG_CAT, message, error);
    }

    public static void l(String message, Location data){
        l(message, LocationConverter.toLocationDTO(data));
    }

    public static void start(String directory) {
        try {
            Runtime.getRuntime().exec("logcat -c");
            Runtime.getRuntime().exec("logcat -f " + directory + "/tracker." + System.currentTimeMillis() + ".log " +
                    LOG_CAT + ":I *:S");
        } catch ( IOException e ) {
            Log.e("", "logcat error", e);
        }
    }
}
