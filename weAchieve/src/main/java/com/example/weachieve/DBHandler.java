package com.example.weachieve;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by chris on 10/12/13.
 */
public class DBHandler {
    private DBModel model;
    private SQLiteDatabase database;

    private String[] allColumns = {DBModel.TASK_ID,
            DBModel.TASK_NAME,
            DBModel.TASK_PEOPLE,
            DBModel.TASK_START,
            DBModel.TASK_END,
            DBModel.TASK_DATE,
            DBModel.TASK_LOCATION,
            DBModel.TASK_CLASS};

    //Default Constructor
    public DBHandler(Context context){
        model = new DBModel(context);
    }

    public void deleteSessions(){
        database.delete(DBModel.TABLE_NAME,null,null);
    }
    //Add a new Task
    public void addTask(Task task){
        ContentValues values = new ContentValues();
        Log.i("add?", "task add in DBHandler");
        //Unpacking data from Task
        values.put(DBModel.TASK_ID,task.getId());
        values.put(DBModel.TASK_NAME,task.getName());
        values.put(DBModel.TASK_PEOPLE,task.getPeople());
        values.put(DBModel.TASK_DATE,task.getDate());
        values.put(DBModel.TASK_START,task.getStart());
        values.put(DBModel.TASK_END,task.getEnd());
        values.put(DBModel.TASK_LOCATION,task.getLocation());
        values.put(DBModel.TASK_CLASS,task.getClassName());
        database.insert(DBModel.TABLE_NAME, null, values);
    }

    //Get Only Specific Tasks by Course
    public ArrayList<Task> getCourseSessions(String course){
        ArrayList<Task> allTasks = new ArrayList<Task>();
        Log.i("add?", "get course tasks");
        Cursor cursor = database.query(DBModel.TABLE_NAME,allColumns, DBModel.TASK_CLASS + " like '%" + course + "%'", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            allTasks.add(cursorToTask(cursor));
            cursor.moveToNext();
            Log.v("CURSOR","GETTING A CURSOR");
        }

        cursor.close();
        return allTasks;
    }

    //Get Task by ID
    public Task getTaskById(String id){
        Cursor cursor = database.query(DBModel.TABLE_NAME,allColumns,DBModel.TASK_ID + " like '%" + id + "%'",null,null,null,null);
        cursor.moveToFirst();
        Task task = (cursorToTask(cursor));
        cursor.close();
        return task;
    }

    public void updateTask(Task task){
        database.delete(DBModel.TABLE_NAME,DBModel.TASK_ID + " like '%" + task.getId() + "%'",null);
        addTask(task);
    }
    //Get Only Specific Tasks by User
    public ArrayList<Task> getUserSessions(String user){
        ArrayList<Task> allTasks = new ArrayList<Task>();
        Cursor cursor = database.query(DBModel.TABLE_NAME,allColumns, DBModel.TASK_PEOPLE + " like '%" + user + "%'", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            allTasks.add(cursorToTask(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return allTasks;
    }

    public Task cursorToTask(Cursor cursor){
        return new Task(
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
