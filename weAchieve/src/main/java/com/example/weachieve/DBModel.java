package com.example.weachieve;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by chris on 10/12/13.
 */
public class DBModel extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "tasks";
    public static final String TASK_ID = "id";
    public static final String TASK_NAME = "name";
    public static final String TASK_PEOPLE = "people";
    public static final String TASK_DATE = "date";
    public static final String TASK_START = "startTime";
    public static final String TASK_END = "endTime";
    public static final String TASK_LOCATION = "location";
    public static final String TASK_CLASS = "class";

    private static final String DATABASE_NAME = "weAchieve";
    private static final int DATABASE_VERSION = 1;

    // DBModel creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "("
            + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TASK_ID + " TEXT NOT NULL, "
            + TASK_NAME + " TEXT NOT NULL, "
            + TASK_PEOPLE + " TEXT NOT NULL, "
            + TASK_START + " TEXT NOT NULL, "
            + TASK_END + " TEXT NOT NULL, "
            + TASK_DATE + " TEXT NOT NULL, "
            + TASK_LOCATION + " TEXT NOT NULL, "
            + TASK_CLASS + " TEXT NOT NULL);";

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
