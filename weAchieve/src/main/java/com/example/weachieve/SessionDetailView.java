package com.example.weachieve;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by chris on 10/14/13.
 */
public class SessionDetailView extends Activity {
    HttpClient client = new DefaultHttpClient();
    Session session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //Unpacking Intent
        Intent in = getIntent();
        String id = in.getStringExtra("id");

        DBHandler db = new DBHandler(this);
        db.open();

        session = db.getSessionById(id);
        ((TextView) findViewById(R.id.course)).setText(session.getClassName());
        ((TextView) findViewById(R.id.session)).setText(session.getName());
        ((TextView) findViewById(R.id.location)).setText(session.getLocation());
        ((TextView) findViewById(R.id.date)).setText(session.getDate());
        ((TextView) findViewById(R.id.time)).setText(session.getStart() + " - " + session.getEnd());

        List<String> peopleList = session.getAttendees();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.simple_item_list, peopleList);
        ListView people = (ListView) findViewById(R.id.peopleList);
        people.setAdapter(adapter);

        final Button attendance = (Button) findViewById(R.id.attend);
        final String user =  getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("fullName", "");
        if (session.isAttending(user)){
            attendance.setText("Un-attend");
            attendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AsyncTask<Void, Void, Void>() {
                        HttpResponse response;

                        @Override
                        protected void onPreExecute() {
                            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                        }

                        protected Void doInBackground(Void... voids) {
                            try {
                                String user = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user", "");
                                String website = "http://weachieveserver1.herokuapp.com/" + session.getId() + "/removeUser";
                                HttpPost createSessions = new HttpPost(website);

                                JSONObject json = new JSONObject();
                                json.put("username",user);
                                StringEntity se = new StringEntity(json.toString());
                                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                                createSessions.setEntity(se);

                                response = client.execute(createSessions);
                            }
                            catch (Exception e) {e.printStackTrace();}
                            return null;
                        }

                        protected void onPostExecute(Void nameless){
                            session.unattend(user);
                            DBHandler db = new DBHandler(getApplicationContext());
                            db.open();
                            db.updateSession(session);
                            finish();
                            adapter.notifyDataSetChanged();
                            attendance.setText("Attend");
                        }
                    }.execute();
                }
            });
        }
        else{
            attendance.setText("Attend");
            attendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    session.unattend(user);

                    new AsyncTask<Void, Void, Void>() {
                        HttpResponse response;

                        @Override
                        protected void onPreExecute() {
                            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);

                        }

                        protected Void doInBackground(Void... voids) {
                            try {
                                String user = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("fullName", "");
                                String website = "http://weachieveserver1.herokuapp.com/" + session.getId() + "/addUser";
                                HttpPost createSessions = new HttpPost(website);



                                JSONObject json = new JSONObject();
                                json.put("username",user);
                                StringEntity se = new StringEntity(json.toString());
                                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                                createSessions.setEntity(se);

                                response = client.execute(createSessions);
                            }
                            catch (Exception e) {e.printStackTrace();}
                            return null;
                        }

                        protected void onPostExecute(Void nameless){
                            session.attend(user);
                            DBHandler db = new DBHandler(getApplicationContext());
                            db.open();
                            db.updateSession(session);
                            finish();
                            adapter.notifyDataSetChanged();
                            attendance.setText("Un-attend");
                        }
                    }.execute();
                }
            });
        }

    }
}