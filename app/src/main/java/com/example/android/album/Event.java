package com.example.android.album;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;

public class Event implements Serializable {
    private ArrayList<String> url;
    private String caption;
    private String date;
    private String key;

    public Event() {
    }

    public Event(String caption, String date) {
        this.caption = caption;
        this.date = date;
    }

    public Event(ArrayList<String> url, String caption, String date) {
        this.url = url;
        this.caption = caption;
        this.date = date;
    }

    public ArrayList<String> getUrl() {
        return url;
    }

    public String getCaption() {
        return caption;
    }

    public String getDate() {
        return date;
    }

    public String getKey() {
        return key;
    }

    public void setUrl(ArrayList<String> url) {
        this.url = url;
    }
}
