package com.example.android.album;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ListViewAdapter extends ArrayAdapter<Event> {

    private String[] MONTHS = new String[]{"January","February","March","April","May","June","July","August","September","October","November","December"};

    public ListViewAdapter(@NonNull Activity context, ArrayList<Event> events) {
        super(context,0,events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent,false);
        }

        Event currentEvent = getItem(position);

        //instantiate widgets
        TextView year = listItemView.findViewById(R.id.year_text_view);
        TextView month = listItemView.findViewById(R.id.month_text_view);
        TextView day = listItemView.findViewById(R.id.day_text_view);
        TextView caption = listItemView.findViewById(R.id.caption_text_view);

        //Set caption font
        Typeface typeFace =Typeface.createFromAsset(getContext().getAssets(),"font/roboto_light.ttf");
        caption.setTypeface(typeFace);

        //update UI
        year.setText(currentEvent.getDate().substring(0,4));

        String monthStr = currentEvent.getDate().substring(4,6);
        if(monthStr.charAt(0) == 0){
            monthStr = MONTHS[Integer.parseInt(Character.toString(monthStr.charAt(1))) - 1];
        }else{
            monthStr = MONTHS[Integer.parseInt(monthStr) - 1];
        }
        month.setText(monthStr);

        day.setText(currentEvent.getDate().substring(6,8));
        caption.setText(currentEvent.getCaption());


        return listItemView;
    }
}
