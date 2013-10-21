package com.example.weachieve;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chris on 10/17/13.
 */
public class UpComingSessions extends Activity {
    //Database Handler
    DBHandler db = new DBHandler(this);

    //Username
    String fullName;

    //ListAdapter
    SessionListAdapter sessionListAdapter;

    //List of Sessions
    ArrayList<Session> sessions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setting Activity Content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming);

        //Enabling Up Button
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //Getting User's Name
        Intent in = getIntent();
        this.fullName = in.getStringExtra("fullName");


        //Opening Database
        db.open();

        //Grabbing all the sessions
        getSessions();

        //Setting up list view
        ListView sessionList = (ListView) findViewById(R.id.upcomingSessions);
        sessionListAdapter = new SessionListAdapter(this, this.sessions);
        sessionList.setAdapter(sessionListAdapter);

        //Setting up refresh
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                rePopulate();
            }
        }, 0, 1000);

        //Setting on Item Click Listener
        sessionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent in = new Intent(getApplicationContext(), SessionDetailView.class);
                in.putExtra("id", UpComingSessions.this.sessions.get(position).getId());
                startActivity(in);
            }
        });
    }

    //Syncing with Server (attending/un-attending)
    public void rePopulate(){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getSessions();
                ListView sessionList = (ListView) findViewById(R.id.upcomingSessions);
                sessionListAdapter = new SessionListAdapter(UpComingSessions.this, UpComingSessions.this.sessions);
                sessionList.setAdapter(sessionListAdapter);
            }
        }.execute();
    }
    //Getting sessions from database
    public void getSessions(){
        UpComingSessions.this.sessions = db.getUserSessions(UpComingSessions.this.fullName);
    }
}