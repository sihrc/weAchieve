package com.example.weachieve;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 10/14/13.
 */
public class CourseExpListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<String> courses;
    private HashMap<String, ArrayList<Session>> sessions;

    public CourseExpListAdapter(Context context, ArrayList<String> courses, HashMap<String, ArrayList<Session>> sessions){
        this.context = context;
        this.courses = courses;
        this.sessions = sessions;
    }

    public void refill(ArrayList<String> courses, HashMap<String, ArrayList<Session>> sessions){
        this.courses = courses;
        this.sessions = sessions;
        notifyDataSetChanged();
    }

    @Override
    public Session getChild(int groupPosition, int childPosition){
        return this.sessions.get(this.courses.get(groupPosition)).get(childPosition);
    }
    @Override
    public long getChildId(int groupPosition, int childPosition){
        return childPosition;
    }

    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
        final Session session = getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.session_list_item, null);
        }

        ImageView people = (ImageView) convertView.findViewById(R.id.people);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView course = (TextView) convertView.findViewById(R.id.course);

        TextView location = (TextView) convertView.findViewById(R.id.location);
        TextView date = (TextView) convertView.findViewById(R.id.date);
        TextView time = (TextView) convertView.findViewById(R.id.time);

        title.setText(session.getName());
        course.setText(session.getClassName());

        location.setText(session.getLocation());
        date.setText(session.getDate());
        if (!session.getStart().equals("")){
            time.setText(session.getStart() + " - " + session.getEnd());
        }
        else{
            time.setText("");}

        switch(session.numPeople()){
            case 0:
                people.setImageResource(R.drawable.people_0);
                break;
            case 1:
                people.setImageResource(R.drawable.people_1);
                break;
            case 2:
                people.setImageResource(R.drawable.people_2);
                break;
            case 3:
                people.setImageResource(R.drawable.people_3);
                break;
            case 4:
                people.setImageResource(R.drawable.people_4);
                break;
            case 5:
                people.setImageResource(R.drawable.people_5);
                break;
            case 6:
                people.setImageResource(R.drawable.people_6);
                break;
            default:
                people.setImageResource(R.drawable.people_6plus);
                break;
        }
        return convertView;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.sessions.get(this.courses.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.courses.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.courses.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.session_group, null);
        }

        TextView course = (TextView) convertView.findViewById(R.id.sessionGroup);
        course.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}