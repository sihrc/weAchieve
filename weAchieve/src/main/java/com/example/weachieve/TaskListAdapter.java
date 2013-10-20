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
public class TaskListAdapter extends ArrayAdapter<Task> {
    private final Context context;
    private ArrayList<Task> tasks;

    public TaskListAdapter(Context context, ArrayList<Task> tasks){
        super(context, R.layout.task_list_item, tasks);
        this.context = context;
        this.tasks = tasks;
    }

    private class TaskHolder{
        TextView title,location,date,time,course;
        ImageView people;
    }

    public void refill(ArrayList<Task> tasks){
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        TaskHolder holder;
        View taskRow = convertView;

        if(taskRow == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            taskRow = inflater.inflate(R.layout.task_list_item, parent, false);
            holder = new TaskHolder();

            holder.people = (ImageView) taskRow.findViewById(R.id.people);
            holder.title = (TextView) taskRow.findViewById(R.id.title);
            holder.course = (TextView) taskRow.findViewById(R.id.course);

            holder.location = (TextView) taskRow.findViewById(R.id.location);
            holder.date = (TextView) taskRow.findViewById(R.id.date);
            holder.time = (TextView) taskRow.findViewById(R.id.time);

            taskRow.setTag(holder);
        } else {
            holder = (TaskHolder) taskRow.getTag();
        }

        Task task = this.tasks.get(position);

        holder.title.setText(task.getName());
        holder.course.setText(task.getClassName());

        holder.location.setText(task.getLocation());
        holder.date.setText(task.getDate());
        if (!task.getStart().equals("")){
            holder.time.setText(task.getStart() + " - " + task.getEnd());
        }
        else{
            holder.time.setText("");}

        switch(task.numPeople()){
            case 0:
                holder.people.setImageResource(R.drawable.people);
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
        return taskRow;
    }
}
