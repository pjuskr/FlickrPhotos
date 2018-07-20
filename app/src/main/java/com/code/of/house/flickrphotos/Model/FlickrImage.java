package com.code.of.house.flickrphotos.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//Class for holding Flickr images
public class FlickrImage implements Parcelable{ //Implemented Parcelable to allow an FlickrImage to be send with an intent

    private static String MEDIUM = "_m";
    private static String BIG = "_b";

    private String Id;
    private String Owner;
    private String Secret;
    private String Server;
    private String Farm;
    private String Title;


    private Bitmap FlickrBitmapM;
    private Bitmap FlickrBitmapBigest;

    public FlickrImage(String _Id, String _Owner, String _Secret,
                String _Server, String _Farm, String _Title){
        this.Id = _Id;
        this.Owner = _Owner;
        this.Secret = _Secret;
        this.Server = _Server;
        this.Farm = _Farm;
        this.Title = _Title;

        this.FlickrBitmapM = fetchBitmap(MEDIUM);
    }

    public FlickrImage(Parcel in){
        this.Id = in.readString();
        this.Owner = in.readString();
        this.Secret = in.readString();
        this.Server = in.readString();
        this.Farm = in.readString();
        this.Title = in.readString();

        this.FlickrBitmapM = fetchBitmap(MEDIUM);
    }

    public Bitmap getBitmap(){
        return this.FlickrBitmapM;
    }

    public Bitmap GetBigBitmap(){

        if(FlickrBitmapBigest != null)
            return FlickrBitmapBigest;
        else
            return FlickrBitmapBigest = getBiggetsAvailable();
    }

    private Bitmap fetchBitmap(String size){

        String FlickrPhotoPath =
                "http://farm" + Farm + ".static.flickr.com/"
                        + Server + "/" + Id + "_" + Secret + size + ".jpg";

        return FlickrAPiManager.QueryGetBitmap(FlickrPhotoPath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(this.Id);
        parcel.writeString(this.Owner);
        parcel.writeString(this.Secret);
        parcel.writeString(this.Server);
        parcel.writeString(this.Farm);
        parcel.writeString(this.Title);
    }

    public static final Parcelable.Creator<FlickrImage>CREATOR = new Parcelable.Creator<FlickrImage>(){
        public FlickrImage createFromParcel(Parcel in) {
            return new FlickrImage(in);
    }
        public FlickrImage[] newArray(int size) {
            return new FlickrImage[size];
        }
    };

    public Bitmap getBiggetsAvailable() {

        //Prepares the query to get all sizes
        String query = FlickrAPiManager.Query_url
                + FlickrAPiManager.Query_Method_sizes
                + FlickrAPiManager.Query_Format_json
                + FlickrAPiManager.Query_noJsonCallback
                + FlickrAPiManager.Query_photo_id + this.Id;

        //Get the json with the result
        String jsonResult = FlickrAPiManager.QueryGetString(query);

        try {
            //Go reads the json and find the source for the biggets version
            JSONObject JsonObject = new JSONObject(jsonResult);
            JSONObject JsonObject_sizes = JsonObject.getJSONObject("sizes");
            JSONArray JsonArray_size = JsonObject_sizes.getJSONArray("size");
            // Only gets the last in the list, as this is always the biggest one
            JSONObject JsonObject_biggets = JsonArray_size.getJSONObject(JsonArray_size.length()-1);
            String sourceBiggest = JsonObject_biggets.getString("source");

            //Gets the bitmap using the link provided by the call
            this.FlickrBitmapBigest = FlickrAPiManager.QueryGetBitmap(sourceBiggest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return FlickrBitmapBigest;
    }
}
