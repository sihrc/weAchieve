package com.example.weachieve;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by chris on 10/12/13.
 */
public class DBModel extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "sessions";
    public static final String SESSION_ID = "id";
    public static final String SESSION_NAME = "name";
    public static final String SESSION_PEOPLE = "people";
    public static final String SESSION_DATE = "date";
    public static final String SESSION_START = "startTime";
    public static final String SESSION_END = "endTime";
    public static final String SESSION_LOCATION = "location";
    public static final String SESSION_CLASS = "class";

    private static final String DATABASE_NAME = "weAchieve";
    private static final int DATABASE_VERSION = 1;

    // DBModel creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "("
            + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SESSION_ID + " TEXT NOT NULL, "
            + SESSION_NAME + " TEXT NOT NULL, "
            + SESSION_PEOPLE + " TEXT NOT NULL, "
            + SESSION_START + " TEXT NOT NULL, "
            + SESSION_END + " TEXT NOT NULL, "
            + SESSION_DATE + " TEXT NOT NULL, "
            + SESSION_LOCATION + " TEXT NOT NULL, "
            + SESSION_CLASS + " TEXT NOT NULL);";

    //Default Constructor
    public DBModel(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    //OnCreate Method - creates the DBModel
    public void onCreate(SQLiteDatabase database){
        database.execSQL(DATABASE_CREATE);

    }
    @Override
    //OnUpgrade Method - upgrades DBModel if applicable
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        Log.w(DBModel.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

}
