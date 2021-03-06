package com.example.weachieve;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    //Database Handler
    DBHandler db = new DBHandler(this);

    //Authentication Information
    String username;
    String password;
    String fullName;

    //Courses user is enrolled in
    ArrayList<String> courses;

    //HashMap for course sessions
    HashMap<String, ArrayList<Session>> courseSessions = new HashMap<String, ArrayList<Session>>();

    //ArrayList for user sessions
    ArrayList<Session> userSessions = new ArrayList<Session>();

    //Adapters
    ListView upcomingSessions;
    CourseExpListAdapter expAdapter;
    SessionListAdapter sessionAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        //Setting the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //First Run
        firstRun();

        //Get Enrolled Courses before user to show dialog on top.
        getCourses();

        //Check for User Authentication
        getUser();

        //Grab Database Sessions for Courses
        db.open();
        getCourseSessions();
        getUserSessions();

        //Populate Upcoming Sessions
        //ListView Adapter
        upcomingSessions = (ListView) findViewById(R.id.upcomingSessions);
        MainActivity.this.sessionAdapter = new SessionListAdapter(this, userSessions);
        upcomingSessions.setAdapter(sessionAdapter);

        //Populate Courses
        //Expandable ListView Adapter
        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.allSessions);
        expAdapter = new CourseExpListAdapter(this, courses, courseSessions);
        expListView.setAdapter(expAdapter);

        //Set See All Button
        Button seeAll = (Button) findViewById(R.id.upcoming);
        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(MainActivity.this, UpComingSessions.class);
                in.putExtra("fullName", MainActivity.this.fullName);
                startActivity(in);
                rePopulate();
            }
        });

        //Set ItemClick on ListView
        upcomingSessions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent in = new Intent(getApplicationContext(), SessionDetailView.class);
                in.putExtra("id", MainActivity.this.userSessions.get(i).getId());
                startActivity(in);
                rePopulate();

            }
        });

        //Set ItemClick on Expandable List View
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                Intent in;
                Session session = MainActivity.this.courseSessions.get(MainActivity.this.courses.get(i)).get(i2);
                if (session.getName().equals("Add New Session")) {
                    in = new Intent(getApplicationContext(), CreateNewSession.class);
                    in.putExtra("course", session.getClassName());
                    in.putExtra("people", MainActivity.this.fullName);
                } else {
                    in = new Intent(getApplicationContext(), SessionDetailView.class);
                    in.putExtra("id", session.getId());
                }
                startActivity(in);
                rePopulate();
                return false;
            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                syncWithServer();
            }
        },0,1000);


    }

    //Syncing the Server with the Database
    public void syncWithServer(){
        new AsyncTask<Void, Void, Void>(){
            //Initializing Clients
            HttpResponse response;
            InputStream inputStream = null;
            String result = "";
            HttpClient client = new DefaultHttpClient();


            @Override
            protected void onPreExecute(){
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }

            protected Void doInBackground(Void... voids){
                //Website URL and header configuration
                String website = "http://weachieveserver1.herokuapp.com/sessions";
                HttpGet all_sessions = new HttpGet(website);
                all_sessions.setHeader("Content-type", "application/json");
                try{response = client.execute(all_sessions);}catch(Exception e){e.printStackTrace();}

                //Parsing the response

                HttpEntity entity = response.getEntity();

                try{inputStream = entity.getContent();}catch(Exception e){e.printStackTrace();}
                try{BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),8);
                StringBuilder sb = new StringBuilder();

                String line;
                String nl = System.getProperty("line.separator");
                while ((line = reader.readLine())!= null){
                    sb.append(line);
                    sb.append(nl);
                }
                result = sb.toString();}catch(Exception e){e.printStackTrace();}

                try{if(inputStream != null)inputStream.close();}catch(Exception squish){squish.printStackTrace();}
                if (!result.equals("")){
                    JSONArray jArray = new JSONArray();
                    JSONObject jsonObj;
                    try{
                        jsonObj = new JSONObject(result);
                        jArray = jsonObj.getJSONArray("sessions");
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                    db.deleteSessions();
                    for (int i=0; i < jArray.length(); i++) {
                        try {
                            JSONObject sessionObject = jArray.getJSONObject(i);
                            // Pulling items from the array
                            StringBuilder userList = new StringBuilder();

                            JSONArray userArray = sessionObject.getJSONArray("usersAttending");
                            for (int j=0; j < userArray.length(); j++) {
                                userList.append(userArray.getString(j));
                                userList.append("#");
                            }
                            db.addSession(new Session(
                                    sessionObject.getString("_id"),
                                    sessionObject.getString("task"),
                                    userList.toString(),
                                    sessionObject.getString("startTime"),
                                    sessionObject.getString("endTime"),
                                    sessionObject.getString("date"),
                                    sessionObject.getString("place"),
                                    sessionObject.getString("course")
                            ));}
                        catch (JSONException e){e.printStackTrace();}
                    }
                }
                return null;
            }

            protected void onPostExecute(Void voids){
                rePopulate();
            }
        }.execute();

    }

    //First Run
    public void firstRun(){
        getCourses();
        getUser();
        if (MainActivity.this.courses.size() < 1)
            addCourse();
        if (MainActivity.this.fullName.equals(""))
            userLogin();
    }

    // Get Course Sessions
    public void getCourseSessions(){
        for (String course : this.courses){
            ArrayList<Session> sessions = db.getCourseSessions(course);
            sessions.add(new Session("","Add New Session","","","","","",course));
            this.courseSessions.put(course, sessions);
        }
    }

    // Get User Sessions
    public void getUserSessions(){
        MainActivity.this.userSessions = db.getUserSessions(MainActivity.this.fullName);
    }

    //Authentication Methods
    public void getUser(){
        MainActivity.this.fullName = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("fullName", "");
    }
    public void userLogin(){
        //Inflate Dialog View
        final View view = MainActivity.this.getLayoutInflater().inflate(R.layout.signin_main,null);
        //Prompt for username and password
        new AlertDialog.Builder(MainActivity.this)
                .setView(view)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText userInput = (EditText) view.findViewById(R.id.username);
                        EditText passInput = (EditText) view.findViewById(R.id.password);
                        MainActivity.this.username = userInput.getText().toString();
                        MainActivity.this.password = passInput.getText().toString();
                        //Save to preference
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("username", userInput.getText().toString())
                                .commit();

                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("password", passInput.getText().toString())
                                .commit();
                        authenticate();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
        //Get User Login
        MainActivity.this.username = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("username","");
        MainActivity.this.password = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("password","");
    }
    public void authenticate(){
        new AsyncTask<Void, Void, String>() {
            HttpResponse response;
            InputStream inputStream = null;
            String result = "";
            HttpClient client = new DefaultHttpClient();

            @Override
            protected void onPreExecute(){
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }
            protected String doInBackground(Void... voids) {
                //Website URL and header configuration
                String website = "https://olinapps.herokuapp.com/api/exchangelogin";
                HttpPost get_auth = new HttpPost(website);
                get_auth.setHeader("Content-type","application/json");

                //Create and execute POST with JSON Post Package
                JSONObject auth = new JSONObject();
                try{
                    auth.put("username",MainActivity.this.username);
                    auth.put("password",MainActivity.this.password);
                    StringEntity se = new StringEntity(auth.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                    get_auth.setEntity(se);
                }catch(Exception e){e.printStackTrace();}
                try{response = client.execute(get_auth);}catch(Exception e){e.printStackTrace();}

                //Read the response
                HttpEntity entity = response.getEntity();

                try{inputStream = entity.getContent();}catch(Exception e){e.printStackTrace();}
                try{BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),8);
                    StringBuilder sb = new StringBuilder(); String line; String nl = System.getProperty("line.separator");

                    while ((line = reader.readLine())!= null){
                        sb.append(line);
                        sb.append(nl);
                    }
                    result = sb.toString();}catch(Exception e){e.printStackTrace();}

                //Convert Result to JSON
                String username = "";
                try{
                    auth = new JSONObject(result);
                    JSONObject userID = auth.getJSONObject("user");
                    username = userID.getString("id");
                }catch(Exception e){e.printStackTrace();}
                return username;
            }
            protected void onPostExecute(String fullName){
                MainActivity.this.fullName = fullName;
                //Save FullName
                getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                        .edit()
                        .putString("fullName", MainActivity.this.fullName)
                        .commit();
                Toast.makeText(MainActivity.this, "You have logged in as " + MainActivity.this.fullName, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    //Getting Courses
    public ArrayList<String> getUnique (ArrayList<String> raw){
        LinkedHashSet<String> unique = new LinkedHashSet<String>(raw);
        return new ArrayList(Arrays.asList(unique.toArray()));
    }
    public void getCourses(){
        String rawString = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("courses", "");
        //Check if Initial "" is at the beginning
        if (rawString.equals(""))
            this.courses = new ArrayList<String>();
        else
            this.courses = getUnique(new ArrayList<String>(Arrays.asList(rawString.split("#"))));
    }
    public void addCourse(){
        //Fake Classes for now, should get from server
        final ArrayList <String> databaseCourses = new ArrayList<String>(){};
        databaseCourses.add("ModSim");
        databaseCourses.add("MobilePrototyping");
        databaseCourses.add("ModCon");
        for (String course : this.courses)databaseCourses.remove(course);

        //Single Course Input
        final AutoCompleteTextView courseList = new AutoCompleteTextView(MainActivity.this);
        courseList.setAdapter(new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_dropdown_item_1line, databaseCourses));

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Add A Course")
                .setMessage("Choose from the existing list, or create a new course")
                .setView(courseList)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newCourse = courseList.getText().toString();
                        if (newCourse.length() < 1) {
                            Toast.makeText(MainActivity.this,"Give the course a name!",Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        } else Toast.makeText(MainActivity.this, "You have successfully added " + newCourse, Toast.LENGTH_LONG).show();

                        //Add course to server, if it doesn't exist on the server
                        if (!databaseCourses.contains(newCourse))
                            addCourseToServer(newCourse);

                        //Save to preference
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("courses", getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("courses", "") + newCourse + "#")
                                .commit();
                        rePopulate();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();

    }
    public void addCourseToServer(String addCourse){
        //Post Request to Server...
    } //UnImplemented

    //Repopulate Views
    public void rePopulate(){
        db.open();
        getUser();
        getCourses();
        getUserSessions();
        getCourseSessions();
        Log.i("Username",MainActivity.this.fullName);
        if (!MainActivity.this.fullName.equals(""))
        {
            sessionAdapter = new SessionListAdapter(this, this.userSessions);
            upcomingSessions.setAdapter(sessionAdapter);
            expAdapter.refill(MainActivity.this.courses,MainActivity.this.courseSessions);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.getItem(0).setTitle("Logged in as: " + MainActivity.this.fullName);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_Remove_Course:

                ListView listView = new ListView(MainActivity.this);
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(listView);
                dialog.setTitle("Remove a Course");

                ArrayAdapter<String> ad = new ArrayAdapter<String>(this, R.layout.course_list_item , R.id.singleItem, courses);
                listView.setAdapter(ad);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        //do something on click
                        MainActivity.this.courses.remove(arg2);
                        StringBuilder csvList = new StringBuilder();
                        csvList.append("");
                        for(String item : MainActivity.this.courses){
                            csvList.append(item);
                            csvList.append("#");
                        }
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("courses", csvList.toString())
                                .commit();

                        getUserSessions();
                        getCourseSessions();
                        getCourses();
                        expAdapter.refill(MainActivity.this.courses,MainActivity.this.courseSessions);
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;

            case R.id.action_Add_Course:
                addCourse();
                getUserSessions();
                getCourseSessions();
                getCourses();
                expAdapter.refill(MainActivity.this.courses,MainActivity.this.courseSessions);
                return true;

            case R.id.action_change_user:
                item.setTitle("Logged in as: " + MainActivity.this.fullName);
                userLogin();
                return true;
        }
        return true;
    }

}
