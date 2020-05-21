package com.goqii.goqiisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.goqii.goqiisdk.model.TemperatureModel;
import com.goqii.goqiisdk.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Nitish-GOQii on 06-06-2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private final static int DATABASE_VERSION = 1;
    private final static String DATABASE_NAME = "DB_Sdk.db";

    public final static String TABLE_TEMPERATURE = "table_temperature";
    private static DatabaseHandler mDatabaseHandler;

    private final String COL_LOCAL_ID = "local_id";
    private final String COL_LOGDATE = "log_date";
    private final String COL_LOGDATETIME = "log_date_time";
    private final String COL_TEMPERATURE = "log_temperature";
    private final String COL_HEART_RATE = "heart_rate";
    private final String COL_STATUS = "status";


    private String createConferanceTable = "CREATE TABLE IF NOT EXISTS " + TABLE_TEMPERATURE + " (" +
            COL_LOCAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_LOGDATE + " TEXT," +
            COL_LOGDATETIME + " TEXT DEFAULT ''," +
            COL_TEMPERATURE + " TEXT DEFAULT '0'," +
            COL_STATUS + " TEXT DEFAULT 'new'," +
            COL_HEART_RATE + " TEXT DEFAULT '0')";


    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    public static DatabaseHandler getInstance(Context context) {
        if (mDatabaseHandler == null)
            mDatabaseHandler = new DatabaseHandler(context.getApplicationContext());
        return mDatabaseHandler;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createConferanceTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertTemperature(Context context) {
        String heartRate = (String) Utils.getPreferences(context, Utils.HEARTRATE, Utils.PREFTYPE_STRING);
        String temperature = (String) Utils.getPreferences(context, Utils.TEMPERATURE, Utils.PREFTYPE_STRING);
        Calendar calendar = Calendar.getInstance();
        String logDateTime = Utils.formatDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
        String logDate = Utils.formatDate(calendar.getTime(), "yyyy-MM-dd");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_LOGDATE, logDate);
        contentValues.put(COL_LOGDATETIME, logDateTime);
        contentValues.put(COL_TEMPERATURE, temperature);
        contentValues.put(COL_HEART_RATE, heartRate);
        contentValues.put(COL_STATUS, "new");
        return db.insertWithOnConflict(TABLE_TEMPERATURE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        //return db.insert(TABLE_TEMPERATURE, null, contentValues);
    }

    public ArrayList<TemperatureModel> getAllNewRecords() {
        Cursor cursor = null;
        SQLiteDatabase db;
        ArrayList<TemperatureModel> conferenceList = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM " + TABLE_TEMPERATURE + " where " + COL_STATUS + " ='" + "new" + "'";
            db = this.getWritableDatabase();
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    TemperatureModel temperatureModel = new TemperatureModel();
                    temperatureModel.setLocalId(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_ID)));
                    temperatureModel.setLogDate(cursor.getString(cursor.getColumnIndex(COL_LOGDATE)));
                    temperatureModel.setLogDate(cursor.getString(cursor.getColumnIndex(COL_LOGDATE)));
                    temperatureModel.setLogDateTime(cursor.getString(cursor.getColumnIndex(COL_LOGDATETIME)));
                    temperatureModel.setLogTemperature(cursor.getString(cursor.getColumnIndex(COL_TEMPERATURE)));
                    temperatureModel.setHeartRate(cursor.getString(cursor.getColumnIndex(COL_HEART_RATE)));
                    temperatureModel.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
                    // Adding contact to list
                    conferenceList.add(temperatureModel);
                } while (cursor.moveToNext());
            }

            return conferenceList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return conferenceList;
    }

    public void updateStatus() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_STATUS, "old");
        db.update(TABLE_TEMPERATURE, contentValues, null, null);
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TEMPERATURE, null, null);

    }
}
