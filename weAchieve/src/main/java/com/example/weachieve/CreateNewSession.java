package com.example.weachieve;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by chris on 10/15/13.
 */
public class CreateNewSession extends Activity {
    String name, location, people, start, end, date, course;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_session);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupUI(findViewById(R.id.parent));

        //Unpacking Intent
        Intent in = getIntent();
        course = in.getStringExtra("course");
        people = in.getStringExtra("people");

        //Grabbing Views
        TextView title = (TextView) findViewById(R.id.addTitle);
        title.setText(course + " Session");

        final EditText editDate = (EditText) findViewById(R.id.inputDate);
        final EditText editStart = (EditText) findViewById(R.id.inputStart);
        final EditText editEnd = (EditText) findViewById(R.id.inputEnd);

        //Save and Cancel Buttons
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText editName = (EditText) findViewById(R.id.inputName);
                final EditText editLocation = (EditText) findViewById(R.id.inputLocation);
                final EditText editDate = (EditText) findViewById(R.id.inputDate);
                final EditText editStart = (EditText) findViewById(R.id.inputStart);
                final EditText editEnd = (EditText) findViewById(R.id.inputEnd);

                CreateNewSession.this.name =  editName.getText().toString();
                CreateNewSession.this.start = editStart.getText().toString();
                CreateNewSession.this.end = editEnd.getText().toString();
                CreateNewSession.this.date = editDate.getText().toString();
                CreateNewSession.this.location = editLocation.getText().toString();

                new AsyncTask<Void, Void, List<String>>() {
                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response;

                    @Override
                    protected void onPreExecute() {
                        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                    }

                    protected List<String> doInBackground(Void... voids) {
                        String user = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("fullName", "");
                        try {
                            String website = "http://weachieveserver1.herokuapp.com/createSession";
                            HttpPost createSessions = new HttpPost(website);

                            JSONObject json = new JSONObject();
                            json.put("course", course);
                            json.put("session",name);
                            json.put("date",date);
                            json.put("startTime",start);
                            json.put("endTime",end);
                            json.put("place",location);
                            json.put("user", user);


                            StringEntity se = new StringEntity(json.toString());
                            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                            createSessions.setEntity(se);

                            response = client.execute(createSessions);
                        }
                        catch (Exception e) {e.printStackTrace();}
                        String result = "";
                        try{
                            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"),8);
                            StringBuilder sb = new StringBuilder();

                             String line;
                             String nl = System.getProperty("line.separator");
                             while ((line = reader.readLine())!= null){
                                 sb.append(line + nl);
                             }

                            JSONObject jObject = new JSONObject(sb.toString());
                            result = jObject.getString("error");
                         }catch (Exception e){e.printStackTrace();}

                    return Arrays.asList(new String[]{result, name, user, start, end, date, location, course});
                    }

                    protected void onPostExecute(List<String> data){
                    DBHandler db = new DBHandler(getApplicationContext());
                       db.open();
                       db.addSession(new Session(data.get(0), data.get(1), data.get(2), data.get(3), data.get(4), data.get(5), data.get(6), data.get(7)));
                }
            }.execute();finish();}
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Making the EditText essentially a button...
        editDate.setFocusable(false);   editStart.setFocusable(false);  editEnd.setFocusable(false);
        editDate.setClickable(true);    editStart.setClickable(true);   editEnd.setClickable(true);

        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar myCalendar = Calendar.getInstance();
                new DatePickerDialog(CreateNewSession.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                                myCalendar.set(Calendar.YEAR, i);
                                myCalendar.set(Calendar.MONTH, i2);
                                myCalendar.set(Calendar.DAY_OF_MONTH, i3);
                                String myFormat = "MM/dd/yy";
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                                editDate.setText(sdf.format(myCalendar.getTime()));
                            }
                        },
                        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        editStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar start = Calendar.getInstance();
                int hour = start.get(Calendar.HOUR_OF_DAY);
                int minute = start.get(Calendar.MINUTE);
                TimePickerDialog timePicker = new TimePickerDialog(CreateNewSession.this,new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute){
                        String time, AMPM;
                        if (selectedHour >= 12){
                            if (selectedHour%12 == 0) time = "12";
                            else time = String.valueOf(selectedHour%12);
                            AMPM = "PM";}
                        else{
                            if (selectedHour%12 == 0) time = "12";
                            else time = String.valueOf(selectedHour%12);
                            AMPM = "AM";}
                        time = time + ":" + String.valueOf(selectedMinute) + AMPM;
                        editStart.setText(time);
                    }},hour,minute,false);
                timePicker.setTitle("Select Start Time");
                timePicker.show();
            }
        });

        editEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar end = Calendar.getInstance();
                int hour = end.get(Calendar.HOUR_OF_DAY);
                int minute = end.get(Calendar.MINUTE);
                TimePickerDialog timePicker = new TimePickerDialog(CreateNewSession.this,new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute){
                        String time, AMPM;
                        if (selectedHour >= 12){
                            if (selectedHour%12 == 0) time = "12";
                            else time = String.valueOf(selectedHour%12);
                            AMPM = " PM";}
                        else{
                            if (selectedHour%12 == 0) time = "12";
                            else time = String.valueOf(selectedHour%12);
                            AMPM = " AM";}
                        time = time + ":" + String.valueOf(selectedMinute) + AMPM;
                        editEnd.setText(time);
                    }},hour,minute,false);
                timePicker.setTitle("Select End Time");
                timePicker.show();
            }
        });
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(CreateNewSession.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
