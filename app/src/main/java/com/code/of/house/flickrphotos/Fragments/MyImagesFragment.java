package com.code.of.house.flickrphotos.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.code.of.house.flickrphotos.FlickrImage;
import com.code.of.house.flickrphotos.R;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MyImagesFragment extends Fragment {

    public static final String API_KEY = "1566308a6e268a2e969dc8f09dbd11c5";
    private static final String SECRET = "f6b3331eeab4a159";

    /*
     * FlickrQuery = FlickrQuery_url
     * + FlickrQuery_per_page
     * + FlickrQuery_nojsoncallback
     * + FlickrQuery_format
     * + FlickrQuery_tag + q
     * + FlickrQuery_key + FlickrApiKey
     */

    String FlickrQuery_url = "https://api.flickr.com/services/rest/?method=flickr.photos.search";
    String FlickrQuery_per_page = "&per_page=1";
    String FlickrQuery_nojsoncallback = "&nojsoncallback=1";
    String FlickrQuery_format = "&format=json";
    String FlickrQuery_tag = "&tags=";
    String FlickrQuery_key = "&api_key=";

    // Apply your Flickr API:
// www.flickr.com/services/apps/create/apply/?
    String FlickrApiKey = API_KEY;

    EditText searchText;
    Button searchButton;
    TextView textQueryResult;
    ImageView imageFlickrPhoto;

    Bitmap bmFlickr;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_images, container, false);

        searchText = view.findViewById(R.id.edit_text);
        searchButton = view.findViewById(R.id.button);
        imageFlickrPhoto = view.findViewById(R.id.image_view);

        searchButton.setOnClickListener(searchButtonOnClickListener);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static MyImagesFragment newInstance(){
        MyImagesFragment fragment = new MyImagesFragment();
        return fragment;
    }

    private Button.OnClickListener searchButtonOnClickListener
            = new Button.OnClickListener(){

        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            String searchQ = searchText.getText().toString().replace(' ', '_');
            String searchResult = QueryFlickr(searchQ);
        }};

    private String QueryFlickr(String q){

        String qResult = null;

        String qString =
                FlickrQuery_url
                        + FlickrQuery_per_page
                        + FlickrQuery_nojsoncallback
                        + FlickrQuery_format
                        + FlickrQuery_tag + q
                        + FlickrQuery_key + FlickrApiKey;

        final HttpClient httpClient = new DefaultHttpClient();
        final HttpGet httpGet = new HttpGet(qString);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

                    if (httpEntity != null){
                        InputStream inputStream = httpEntity.getContent();
                        Reader in = new InputStreamReader(inputStream);
                        BufferedReader bufferedreader = new BufferedReader(in);
                        StringBuilder stringBuilder = new StringBuilder();

                        String stringReadLine = null;

                        while ((stringReadLine = bufferedreader.readLine()) != null) {
                            stringBuilder.append(stringReadLine + "\n");
                        }

                        final String qResult = stringBuilder.toString();
                        FlickrImage myFlickrImage = ParseJSON(qResult);
                        final Bitmap myFlickrImageBM = myFlickrImage.getBitmap();


                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if(myFlickrImageBM != null){
                                    imageFlickrPhoto.setImageBitmap(myFlickrImageBM);
                                }
                            }
                        });
                    }

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        thread.start();


        return qResult;
    }

    private FlickrImage ParseJSON(String json){

        bmFlickr = null;

        String flickrId;
        String flickrOwner;
        String flickrSecret;
        String flickrServer;
        String flickrFarm;
        String flickrTitle;

        FlickrImage flickrImage = null;

        try {

            JSONObject JsonObject = new JSONObject(json);
            JSONObject Json_photos = JsonObject.getJSONObject("photos");
            JSONArray JsonArray_photo = Json_photos.getJSONArray("photo");

            //We have only one photo in this exercise
            JSONObject FlickrPhoto = JsonArray_photo.getJSONObject(0);

            flickrId = FlickrPhoto.getString("id");
            flickrOwner = FlickrPhoto.getString("owner");
            flickrSecret = FlickrPhoto.getString("secret");
            flickrServer = FlickrPhoto.getString("server");
            flickrFarm = FlickrPhoto.getString("farm");
            flickrTitle = FlickrPhoto.getString("title");

            flickrImage = new FlickrImage(flickrId, flickrOwner, flickrSecret,
                    flickrServer, flickrFarm, flickrTitle);

        } catch (JSONException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }

        return flickrImage;

    }

    private Bitmap LoadPhotoFromFlickr(
            String id, String owner, String secret,
            String server, String farm, String title){
        Bitmap bm= null;

        String FlickrPhotoPath =
                "http://farm" + farm + ".static.flickr.com/"
                        + server + "/" + id + "_" + secret + "_m.jpg";

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