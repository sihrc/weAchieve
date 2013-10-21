package com.example.weachieve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chris on 10/12/13.
 */
public class Session {
    private String id;
    private String name;
    private String people;
    private String date;
    private String start;
    private String end;
    private String location;
    private String className;

    public Session(String id, String name, String people, String start, String end, String date, String location, String className){
        this.name = name;
        this.people = people;
        this.date = date;
        this.start = start;
        this.end = end;
        this.location = location;
        this.className = className;
        this.id = id;
    }

    //Session attendance
    public List<String> getAttendees(){
        ArrayList<String> people = new ArrayList<String>();
        if (this.people.equals(""))
            return people;

        people.addAll(Arrays.asList(this.people.split("#")));
        return people;
    }

    public void setAttendees(List<String> people){
        this.people = people.toString().replace(", ", "#").replace("[","").replace("]","");
    }

    public boolean isAttending(String user){
        return getAttendees().contains(user);
    }

    public void unattend(String user){
        List <String> attendees = getAttendees();
        if (isAttending(user))
            attendees.remove(attendees.indexOf(user));
        setAttendees(attendees);
    }
    public void attend(String user){
        List<String> attendees = getAttendees();
        attendees.add(user);
        setAttendees(attendees);
    }

    public int numPeople(){
        return getAttendees().size();
    }

    //Get Data
    public String getId(){return this.id;}
    public String getName(){return this.name;}
    public String getPeople(){return String.valueOf(this.people);}
    public String getDate(){return this.date;}
    public String getStart(){return this.start;}
    public String getEnd(){return this.end;}
    public String getLocation(){return this.location;}
    public String getClassName(){return this.className;}

    //Set Data
    public void setId(String value){this.id = value;}
    public void setName(String value){this.name = value;}
}
