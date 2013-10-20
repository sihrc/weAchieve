package com.example.weachieve;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.text.InputType;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    //Database Handler
    DBHandler db = new DBHandler(this);

    //Authentication Information
    String user;

    //Courses user is enrolled in
    ArrayList<String> courses;

    //HashMap for course sessions
    HashMap<String, ArrayList<Task>> courseSessions = new HashMap<String, ArrayList<Task>>();

    //ArrayList for user sessions
    ArrayList<Task> userSessions = new ArrayList<Task>();

    //Adapters
    CourseExpListAdapter expAdapter;
    TaskListAdapter taskAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        //Setting the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //First Run
        firstRun();

        //Get Enrolled Courses before user to show dialog on top.
        courses = getCourses();

        //Check for User Authentication
        user = getUser();

        //Grab Database Sessions for Courses
        db.open();
        getCourseSessions();
        getUserSessions();

        //Populate Upcoming Tasks
        //ListView Adapter
        ListView upcomingSessions = (ListView) findViewById(R.id.upcomingTasks);
        taskAdapter = new TaskListAdapter(this, this.userSessions);
        upcomingSessions.setAdapter(taskAdapter);

        //Populate Courses
        //Expandable ListView Adapter
        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.allTasks);
        expAdapter = new CourseExpListAdapter(this, courses, courseSessions);
        expListView.setAdapter(expAdapter);

        //Set See All Button
        Button seeAll = (Button) findViewById(R.id.upcoming);
        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(MainActivity.this,UpComingTasks.class);
                startActivity(in);
            }
        });

        //Set ItemClick on ListView
        upcomingSessions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent in = new Intent(getApplicationContext(), TaskDetailView.class);
                in.putExtra("id", MainActivity.this.userSessions.get(i).getId());
                startActivity(in);
            }
        });

        //Set ItemClick on Expandable List View
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                Intent in;
                Task task = MainActivity.this.courseSessions.get(MainActivity.this.courses.get(i)).get(i2);
                if (task.getName().equals("Add New Session")) {
                    in = new Intent(getApplicationContext(), CreateNewTask.class);
                    in.putExtra("course", task.getClassName());
                    in.putExtra("people", user);
                } else {
                    in = new Intent(getApplicationContext(), TaskDetailView.class);
                    in.putExtra("id", task.getId());
                }
                startActivity(in);
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
                HttpGet all_tasks = new HttpGet(website);
                all_tasks.setHeader("Content-type","application/json");
                try{response = client.execute(all_tasks);}catch(Exception e){e.printStackTrace();}

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
                        Log.i("JSONPARSER", "ERROR PARSING JSON");
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

                            Log.i("userList", userList.toString());
                            db.addTask(new Task(
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
                getUserSessions();
                getCourseSessions();
                expAdapter.refill(getCourses(),MainActivity.this.courseSessions);
                taskAdapter.refill(MainActivity.this.userSessions);
            }
        }.execute();

    }


    //First Run
    public void firstRun(){
        if (getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("courses", "^").equals("^"))
            addCourse();
        if (getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user", "").equals(""))
            userLogin();
    }

    // Get Course Sessions
    public void getCourseSessions(){
        for (String course : this.courses){
            ArrayList<Task> tasks = db.getCourseSessions(course);
            tasks.add(new Task("","Add New Session","","","","","",course));
            this.courseSessions.put(course,tasks);
        }
    }

    // Get User Sessions
    public void getUserSessions(){
        this.userSessions = db.getUserSessions(getUser());
    }

    //Authentication Methods
    public String getUser(){
        return getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user", "");
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

                        //Save to preference
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("user", userInput.getText().toString())
                                .commit();

                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("password", passInput.getText().toString())
                                .commit();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }

    //Getting Courses
    ArrayList<String> getCourses(){
        String rawString = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("courses", "^");

        //Check if Initial "^" is at the beginning
        if (rawString.charAt(0) == '^') Log.i("COURSE ERROR","Something might be wrong with the courses string.");

        return new ArrayList<String>(Arrays.asList(rawString.substring(1).split("#")));
    }
    public void addCourse(){
        //Fake Classes for now, should get from server
        final ArrayList <String> databaseCourses = new ArrayList<String>(){};
        databaseCourses.add("ModSim");
        databaseCourses.add("MobilePrototyping");
        databaseCourses.add("ModCon");

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
                        }

                        //Add course to server, if it doesn't exist on the server
                        if (!databaseCourses.contains(newCourse))
                            addCourseToServer(newCourse);

                        //Save to preference
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("courses", getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("course", "^") + newCourse + "#")
                                .commit();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_Remove_Course:

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.course_list);
                dialog.setTitle("Remove Course");
                ListView listView = (ListView) dialog.findViewById(R.id.list);

                ArrayAdapter<String> ad = new ArrayAdapter<String>(this, R.layout.course_list_item , R.id.singleItem, courses);
                listView.setAdapter(ad);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        //do something on click
                        ArrayList myCourses = new ArrayList(courses);
                        myCourses.remove(arg2);
                        Log.i("removeCourse1", myCourses.toString());
                        courses = myCourses;
                        Log.i("removeCourse2", courses.toString());
                        StringBuilder csvList = new StringBuilder();
                        csvList.append("");
                        for(String item : courses){
                            csvList.append(item);
                            csvList.append("#");
                        }
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("courses", csvList.toString())
                                .commit();

                        getUserSessions();
                        getCourseSessions();
                        expAdapter.refill(getCourses(),MainActivity.this.courseSessions);
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;

            case R.id.action_Add_Course:
                addCourse();
                getUserSessions();
                getCourseSessions();
                expAdapter.refill(getCourses(),MainActivity.this.courseSessions);
                return true;

            case R.id.action_change_user:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Enter User Name");

                final EditText input = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userName= input.getText().toString();
                        MainActivity.this.user = userName;
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("user", userName)
                                .commit();
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .edit()
                                .putString("courses", "")
                                .commit();

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
                return true;
        }
        return true;
    }

}
