package com.code.of.house.flickrphotos.Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.code.of.house.flickrphotos.Activities.MainActivity;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import static com.code.of.house.flickrphotos.Activities.MainActivity.accessToken;

public class FlickrImage implements Parcelable{

    private String Id;
    private String Owner;
    private String Secret;
    private String Server;
    private String Farm;
    private String Title;
    private static String MEDIUM = "_m";
    private static String BIG = "_b";

    private Bitmap FlickrBitmapM;
    private Bitmap FlickrBitmapB;

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
        if(FlickrBitmapB != null)
            return FlickrBitmapB;
        else
            return FlickrBitmapB = fetchBitmap(BIG);
    }

    private Bitmap fetchBitmap(String size){

        Bitmap bm= null;

        String FlickrPhotoPath =
                "http://farm" + Farm + ".static.flickr.com/"
                        + Server + "/" + Id + "_" + Secret + size + ".jpg";

        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, FlickrPhotoPath);
            MainActivity.service.signRequest(accessToken, request);
            final Response response = MainActivity.service.execute(request);

            bm = BitmapFactory.decodeStream(response.getStream());

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return bm;
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
}
