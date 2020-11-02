package neder.trackerclient;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import neder.location.LocationConverter;
import static neder.location.AuditLogger.l;

public class LocationStorage {
    private static LocationStorage instance = null;
    private Context context;
    private SQLiteDatabase db;

    private LocationStorage(Context context) {
        this.context = context;

        synchronized (context) {
            SQLiteDatabase db = getDatabase();
            Cursor resultSet = db.rawQuery("SELECT data FROM AgentLocations ORDER by time DESC LIMIT 1",
                    null);
            if(resultSet.moveToFirst()) {
                String jsonData = resultSet.getString(resultSet.getColumnIndex("data"));
                LocationModel locationModel = LocationConverter2.fromJson(jsonData);
                lastLocation = locationModel;
            }
            resultSet.close();
            db.close();
        }
    }

    private SQLiteDatabase getDatabase() {
        if(db == null) {
            db = context.openOrCreateDatabase("TrackerCLient", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS AgentLocations(id VARCHAR PRIMARY KEY, data TEXT, time INTEGER);");
        } else if(!db.isOpen()) {
            db = context.openOrCreateDatabase("TrackerCLient", Context.MODE_PRIVATE, null);
        }
        return db;
    }

    public static  LocationStorage getInstance(Context context) {
        if(instance == null) {
            instance = new LocationStorage(context);
        }
        return instance;
    }

    private LocationModel lastLocation = null;

    private Object dbLockPad = new Object();

    public void add(LocationModel locationModel) {
        if (lastLocation == null) {
            l("NEW LOCATION STORED: lastLocation was null, storing the new location.");
            lastLocation = locationModel;
        }else if(locationModel.time.after(lastLocation.time)){
            l("NEW LOCATION STORED: lastLocation was older than provided one, storing the new location.");
            lastLocation = locationModel;
        }else{
            l("NEW LOCATION REJECTED: lastLocation was newer than provided one, rejecting the new location.");
        }

        synchronized (dbLockPad) {
            SQLiteDatabase db = getDatabase();
            db.execSQL("DELETE FROM AgentLocations WHERE time < " + (System.currentTimeMillis() - 60000));
            SQLiteStatement stmt = db.compileStatement("INSERT INTO AgentLocations(id, data, time) VALUES (?, ? , ?)");
            stmt.bindString(1, locationModel.id);
            stmt.bindString(2, LocationConverter2.toJSON(locationModel));
            stmt.bindLong(3, locationModel.time.getTime());
            try {
                stmt.execute();
            }catch(SQLiteConstraintException e){
                Log.w("LocationStorage.add", "Has SQLiteConstraintException", e) ;
            }
            db.close();
        }
    }

    public LocationModel getLast() {
        return lastLocation;
    }

//    public float getSpeedAvgSince(long time) {
//        float acc = 0;
//        long count = 0;
//        synchronized (dbLockPad) {
//            SQLiteDatabase db = getDatabase();
//            Cursor resultSet = db.rawQuery("SELECT data FROM AgentLocations WHERE time >= " + time,
//                    null);
//            if(resultSet.moveToFirst()) {
//                do {
//                    String jsonData = resultSet.getString(resultSet.getColumnIndex("data"));
//                    LocationModel locationModel = LocationConverter2.fromJson(jsonData);
//                    acc += locationModel.speed;
//                    count++;
//                }while(resultSet.moveToNext());
//            }
//            resultSet.close();
//            db.close();
//        }
//        if(count > 0)
//            return acc / (float)count;
//
//        return 0.0F;
//    }
}
