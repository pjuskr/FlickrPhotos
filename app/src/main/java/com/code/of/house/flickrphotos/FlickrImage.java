package com.code.of.house.flickrphotos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FlickrImage {
    private String Id;
    private String Owner;
    private String Secret;
    private String Server;
    private String Farm;
    private String Title;

    private Bitmap FlickrBitmap;

    public FlickrImage(String _Id, String _Owner, String _Secret,
                String _Server, String _Farm, String _Title){
        this.Id = _Id;
        this.Owner = _Owner;
        this.Secret = _Secret;
        this.Server = _Server;
        this.Farm = _Farm;
        this.Title = _Title;

        this.FlickrBitmap = preloadBitmap();
    }

    public Bitmap getBitmap(){
        return this.FlickrBitmap;
    }

    private Bitmap preloadBitmap(){
        Bitmap bm= null;

        String FlickrPhotoPath =
                "http://farm" + Farm + ".static.flickr.com/"
                        + Server + "/" + Id + "_" + Secret + "_m.jpg";

        URL FlickrPhotoUrl = null;

        try {
            FlickrPhotoUrl = new URL(FlickrPhotoPath);

            HttpURLConnection httpConnection
                    = (HttpURLConnection) FlickrPhotoUrl.openConnection();
            httpConnection.setDoInput(true);
            httpConnection.connect();
            InputStream inputStream = httpConnection.getInputStream();
            bm = BitmapFactory.decodeStream(inputStream);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bm;
    }
}
