package com.example.weachieve;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chris on 10/17/13.
 */
public class UpComingTasks extends Activity {
    DBHandler db = new DBHandler(this);
    String user;
    TaskListAdapter taskListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        user = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user", new StringBuilder().toString());

        db.open();
        ArrayList<Task> tasks = db.getUserSessions(user);

        ListView taskList = (ListView) findViewById(R.id.upcomingTasks);
        taskListAdapter = new TaskListAdapter(this, tasks);
        taskList.setAdapter(taskListAdapter);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        ListView taskList = (ListView) findViewById(R.id.upcomingTasks);
                        taskListAdapter = new TaskListAdapter(UpComingTasks.this, db.getUserSessions(user));
                        taskList.setAdapter(taskListAdapter);
                    }
                }.execute();
            }
        }, 0, 1000);

        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent in = new Intent(getApplicationContext(), TaskDetailView.class);
                in.putExtra("id", db.getUserSessions(user).get(position).getId());
                startActivity(in);
            }
        });
    }
}