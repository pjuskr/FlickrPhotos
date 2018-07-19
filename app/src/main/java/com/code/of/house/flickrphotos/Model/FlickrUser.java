package com.code.of.house.flickrphotos.Model;

import android.graphics.Bitmap;

public class FlickrUser {
    public String username;
    public String user_id;
    public String nsid;
    public Bitmap user_icon;

    public FlickrUser(String username, String user_id, String nsid, Bitmap user_icon){
        this.username = username;
        this.user_id = user_id;
        this.nsid = nsid;
        this.user_icon = user_icon;
    }

    public FlickrUser(){

    }
}
