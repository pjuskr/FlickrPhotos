package com.code.of.house.flickrphotos.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.code.of.house.flickrphotos.Activities.MainActivity;
import com.code.of.house.flickrphotos.FlickrImage;
import com.code.of.house.flickrphotos.MosaicAapter;
import com.code.of.house.flickrphotos.R;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.code.of.house.flickrphotos.Activities.MainActivity.accessToken;

public class PublicImagesFragment extends Fragment {

    public static final String API_KEY = "1566308a6e268a2e969dc8f09dbd11c5";
    private static final String SECRET = "f6b3331eeab4a159";
    //private StaggeredGridLayoutManager _sGridLayoutManager;
    List<FlickrImage> fList = new ArrayList<>();
    MosaicAapter mAdapter;

    /*
     * FlickrQuery = FlickrQuery_url
     * + FlickrQuery_per_page
     * + FlickrQuery_nojsoncallback
     * + FlickrQuery_format
     * + FlickrQuery_tag + q
     * + FlickrQuery_key + FlickrApiKey
     */

    String FlickrQuery_url = "https://api.flickr.com/services/rest/?method=flickr.photos.search";
    String FlickrQuery_per_page = "&per_page=20";
    String FlickrQuery_nojsoncallback = "&nojsoncallback=1";
    String FlickrQuery_format = "&format=json";
    String FlickrQuery_tag = "&tags=";
    String FlickrQuery_key = "&api_key=";

    // Apply your Flickr API:
// www.flickr.com/services/apps/create/apply/?
    String FlickrApiKey = API_KEY;

    EditText searchText;
    Button searchButton;
    FlickrImage[] myFlickrImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_public_images, container, false);

        searchText = view.findViewById(R.id.edit_text);
        searchButton = view.findViewById(R.id.button);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.public_recyclerview);
        recyclerView.setHasFixedSize(true);

        StaggeredGridLayoutManager _sGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(_sGridLayoutManager);

        mAdapter = new MosaicAapter(getContext(), fList);
        recyclerView.setAdapter(mAdapter);
        searchButton.setOnClickListener(searchButtonOnClickListener);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static PublicImagesFragment newInstance(){
        PublicImagesFragment fragment = new PublicImagesFragment();
        return fragment;
    }

    private Button.OnClickListener searchButtonOnClickListener
            = new Button.OnClickListener(){

        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            String searchQ = searchText.getText().toString().replace(' ', '_');
            QueryFlickr(searchQ);
            fList.clear();
        }};

    private void QueryFlickr(String q){

        final String qString =
                FlickrQuery_url
                        + FlickrQuery_per_page
                        + FlickrQuery_nojsoncallback
                        + FlickrQuery_format
                        + FlickrQuery_tag + q;

        final URL[] flickrQueryURL = {null};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{

                    final OAuthRequest request = new OAuthRequest(Verb.GET, qString);
                    MainActivity.service.signRequest(accessToken, request);
                    final Response response = MainActivity.service.execute(request);

                    String body = response.getBody();

                    final FlickrImage myFlickrImage[] = ParseJSON(body);


                    for (final FlickrImage f: myFlickrImage) {
                        final Bitmap myFlickrImageBM = f.getBitmap();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if(myFlickrImageBM != null){
                                    fList.add(f);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }

                }catch (MalformedURLException e){

                }catch (IOException e){

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private FlickrImage[] ParseJSON(String json){

        FlickrImage[] flickrImage = null;

        String flickrId;
        String flickrOwner;
        String flickrSecret;
        String flickrServer;
        String flickrFarm;
        String flickrTitle;

        try {

            JSONObject JsonObject = new JSONObject(json);
            JSONObject Json_photos = JsonObject.getJSONObject("photos");
            JSONArray JsonArray_photo = Json_photos.getJSONArray("photo");

            flickrImage = new FlickrImage[JsonArray_photo.length()];
            for (int i = 0; i < JsonArray_photo.length(); i++){
                JSONObject FlickrPhoto = JsonArray_photo.getJSONObject(i);
                flickrId = FlickrPhoto.getString("id");
                flickrOwner = FlickrPhoto.getString("owner");
                flickrSecret = FlickrPhoto.getString("secret");
                flickrServer = FlickrPhoto.getString("server");
                flickrFarm = FlickrPhoto.getString("farm");
                flickrTitle = FlickrPhoto.getString("title");
                flickrImage[i] = new FlickrImage(flickrId, flickrOwner, flickrSecret,
                        flickrServer, flickrFarm, flickrTitle);
            }

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
