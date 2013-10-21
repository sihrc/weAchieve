package com.example.weachieve;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by chris on 10/12/13.
 */
public class DBHandler {
    private DBModel model;
    private SQLiteDatabase database;

    private String[] allColumns = {DBModel.SESSION_ID,
            DBModel.SESSION_NAME,
            DBModel.SESSION_PEOPLE,
            DBModel.SESSION_START,
            DBModel.SESSION_END,
            DBModel.SESSION_DATE,
            DBModel.SESSION_LOCATION,
            DBModel.SESSION_CLASS
    };

    //Default Constructor
    public DBHandler(Context context){
        model = new DBModel(context);
    }

    public void deleteSessions(){
        database.delete(DBModel.TABLE_NAME,null,null);
    }
    //Add a new Session
    public void addSession(Session session){
        ContentValues values = new ContentValues();
        //Unpacking data from Session
        values.put(DBModel.SESSION_ID, session.getId());
        values.put(DBModel.SESSION_NAME, session.getName());
        values.put(DBModel.SESSION_PEOPLE, session.getPeople());
        values.put(DBModel.SESSION_DATE, session.getDate());
        values.put(DBModel.SESSION_START, session.getStart());
        values.put(DBModel.SESSION_END, session.getEnd());
        values.put(DBModel.SESSION_LOCATION, session.getLocation());
        values.put(DBModel.SESSION_CLASS, session.getClassName());
        database.insert(DBModel.TABLE_NAME, null, values);
    }

    //Get Only Specific Sessions by Course
    public ArrayList<Session> getCourseSessions(String course){
        ArrayList<Session> allSessions = new ArrayList<Session>();
        Cursor cursor = database.query(DBModel.TABLE_NAME,allColumns, DBModel.SESSION_CLASS + " like '%" + course + "%'", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            allSessions.add(cursorToSession(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return allSessions;
    }

    //Get Session by ID
    public Session getSessionById(String id){
        Cursor cursor = database.query(DBModel.TABLE_NAME,allColumns,DBModel.SESSION_ID + " like '%" + id + "%'",null,null,null,null);
        cursor.moveToFirst();
        Session session = (cursorToSession(cursor));
        cursor.close();
        return session;
    }

    public void updateSession(Session session){
        database.delete(DBModel.TABLE_NAME,DBModel.SESSION_ID + " like '%" + session.getId() + "%'",null);
        addSession(session);
    }
    //Get Only Specific Sessions by User
    public ArrayList<Session> getUserSessions(String user){
        ArrayList<Session> allSessions = new ArrayList<Session>();
        Cursor cursor = database.query(DBModel.TABLE_NAME,allColumns, DBModel.SESSION_PEOPLE + " like '%" + user + "%'", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            allSessions.add(cursorToSession(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return allSessions;
    }

    public Session cursorToSession(Cursor cursor){
        return new Session(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7)
        );
    }


    //Open Database Access
    public void open(){database = model.getWritableDatabase();}
}
