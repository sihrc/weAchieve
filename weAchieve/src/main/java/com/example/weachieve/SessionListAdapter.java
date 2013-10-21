package com.example.weachieve;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by chris on 10/14/13.
 */
public class SessionListAdapter extends ArrayAdapter<Session> {
    private final Context context;
    private ArrayList<Session> sessions;

    public SessionListAdapter(Context context, ArrayList<Session> sessions){
        super(context, R.layout.session_list_item, sessions);
        this.context = context;
        this.sessions = sessions;
    }

    private class SessionHolder {
        TextView title,location,date,time,course;
        ImageView people;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        SessionHolder holder;
        View sessionRow = convertView;

        if(sessionRow == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            sessionRow = inflater.inflate(R.layout.session_list_item, parent, false);
            holder = new SessionHolder();

            holder.people = (ImageView) sessionRow.findViewById(R.id.people);
            holder.title = (TextView) sessionRow.findViewById(R.id.title);
            holder.course = (TextView) sessionRow.findViewById(R.id.course);

            holder.location = (TextView) sessionRow.findViewById(R.id.location);
            holder.date = (TextView) sessionRow.findViewById(R.id.date);
            holder.time = (TextView) sessionRow.findViewById(R.id.time);

            sessionRow.setTag(holder);
        } else {
            holder = (SessionHolder) sessionRow.getTag();
        }

        Session session = this.sessions.get(position);

        holder.title.setText(session.getName());
        holder.course.setText(session.getClassName());

        holder.location.setText(session.getLocation());
        holder.date.setText(session.getDate());
        if (!session.getStart().equals("")){
            holder.time.setText(session.getStart() + " - " + session.getEnd());
        }
        else{
            holder.time.setText("");}

        switch(session.numPeople()){
            case 0:
                holder.people.setImageResource(R.drawable.people_0);
                break;
            case 1:
                holder.people.setImageResource(R.drawable.people_1);
                break;
            case 2:
                holder.people.setImageResource(R.drawable.people_2);
                break;
            case 3:
                holder.people.setImageResource(R.drawable.people_3);
                break;
            case 4:
                holder.people.setImageResource(R.drawable.people_4);
                break;
            case 5:
                holder.people.setImageResource(R.drawable.people_5);
                break;
            case 6:
                holder.people.setImageResource(R.drawable.people_6);
                break;
            default:
                holder.people.setImageResource(R.drawable.people_6plus);
                break;
        }
        return sessionRow;
    }
}
