package com.code.of.house.flickrphotos.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.code.of.house.flickrphotos.Activities.MainActivity;
import com.code.of.house.flickrphotos.Model.FlickrImage;
import com.code.of.house.flickrphotos.MosaicAapter;
import com.code.of.house.flickrphotos.R;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.code.of.house.flickrphotos.Activities.MainActivity.accessToken;

public class PublicImagesFragment extends Fragment {

    //Variables for more easily building a query to FlickrAPI
    public static final String API_KEY = "1566308a6e268a2e969dc8f09dbd11c5";
    public static final String SECRET = "f6b3331eeab4a159";
    public static final String FlickrQuery_url = "https://api.flickr.com/services/rest/?method=flickr.photos.search";
    public static final String FlickrQuery_per_page = "&per_page=20";
    public static final String FlickrQuery_nojsoncallback = "&nojsoncallback=1";
    public static final String FlickrQuery_format = "&format=json";
    public static final String FlickrQuery_tag = "&tags=";
    public static final String FlickrQuery_page = "&page=";

    //Variables for keeping track on what images to load
    int currentPage = 0;
    Boolean hasMoreResults = true;

    //List containing all the loaded images
    List<FlickrImage> flickrImageList = new ArrayList<>();

    ProgressDialog progressDialog;
    LoadImagesThread loadImagesThread;

    MosaicAapter mosaicAdapter; // The addapter for the mosaic-grid. Used for updating the grid on new data loaded.

    //Variables for the views
    EditText searchText;
    Button searchButton;
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_public_images, container, false);

        //Finds the views
        searchText = view.findViewById(R.id.edit_text);
        searchButton = view.findViewById(R.id.button);
        recyclerView = view.findViewById(R.id.public_recyclerview);

        //Prepare the recycler view
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager _sGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(_sGridLayoutManager);

        //Prepares the adapter for the recycler view
        mosaicAdapter = new MosaicAapter(getContext(), flickrImageList);
        recyclerView.setAdapter(mosaicAdapter);

        //Sets the needed listeners
        searchButton.setOnClickListener(searchButtonOnClickListener);
        recyclerView.addOnScrollListener(recyclerViewOnClickListener);

        return view;
    }

    private Button.OnClickListener searchButtonOnClickListener
            = new Button.OnClickListener() {

        public void onClick(View view) {
            //Opens a progress dialog box to inform the user that images is being loaded in
            progressDialog = ProgressDialog.show(getActivity(),
                    "", "Indlæser billeder!");

            //resets the values to start new search
            flickrImageList.clear();
            currentPage = 1;
            hasMoreResults = true;

            //Call the data on a background thread
            loadImagesThread = new LoadImagesThread();
            loadImagesThread.setRunning(true);
            loadImagesThread.start();
        }
    };

    private RecyclerView.OnScrollListener recyclerViewOnClickListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if(recyclerView.getLayoutManager() != null){
                //Finds values about the users location of the recyclerView
                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int pastVisibleItems = ((StaggeredGridLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPositions(null)[0];

                //A check to see if the user is near the end of the list and that new images is not being loading in already
                if ((pastVisibleItems + visibleItemCount + 10 >= totalItemCount)& !loadImagesThread.running) {
                    if (hasMoreResults) {
                        //Increment page for the search and starts loading more pictures
                        currentPage++;
                        loadImagesThread = new LoadImagesThread();
                        loadImagesThread.setRunning(true);
                        loadImagesThread.start();
                    } else {
                        Toast.makeText(getActivity(), "No more pictures to load", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static PublicImagesFragment newInstance() {
        return new PublicImagesFragment();
    }


    private String QueryFlickr(String tag) {

        final String qString =
                FlickrQuery_url
                        + FlickrQuery_per_page
                        + FlickrQuery_nojsoncallback
                        + FlickrQuery_format
                        + FlickrQuery_tag + tag
                        + FlickrQuery_page + currentPage;

        String result = null;

        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, qString);
            MainActivity.service.signRequest(accessToken, request);
            final Response response = MainActivity.service.execute(request);

            result = response.getBody();

            //TODO make code for Exceptions (All places)
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<FlickrImage> ParseJSON(String json) {

        List<FlickrImage> flickrImage = new ArrayList<>();

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

            for (int i = 0; i < JsonArray_photo.length(); i++) {
                JSONObject FlickrPhoto = JsonArray_photo.getJSONObject(i);
                flickrId = FlickrPhoto.getString("id");
                flickrOwner = FlickrPhoto.getString("owner");
                flickrSecret = FlickrPhoto.getString("secret");
                flickrServer = FlickrPhoto.getString("server");
                flickrFarm = FlickrPhoto.getString("farm");
                flickrTitle = FlickrPhoto.getString("title");
                flickrImage.add(new FlickrImage(flickrId, flickrOwner, flickrSecret,
                        flickrServer, flickrFarm, flickrTitle));
            }

        } catch (JSONException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }
        return flickrImage;
    }

    public class LoadImagesThread extends Thread {
        volatile boolean running = false;

        void setRunning(boolean b) {
            running = b;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            String searchQ = searchText.getText().toString().replace(' ', '_');
            String searchResult = QueryFlickr(searchQ);
            hasMoreResults = false;
            List<FlickrImage> myFlickrImage = ParseJSON(searchResult);

            flickrImageList.addAll(myFlickrImage);

            if(!myFlickrImage.isEmpty())
                hasMoreResults = true;

            handler.sendMessage(handler.obtainMessage());
            setRunning(false);
        }

        Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub

                progressDialog.dismiss();
                mosaicAdapter.notifyDataSetChanged();
                if(flickrImageList.isEmpty())
                {
                    Toast.makeText(getActivity(), "Der var ingen resultater", Toast.LENGTH_LONG).show();
                }
            }

        };
    }

}
