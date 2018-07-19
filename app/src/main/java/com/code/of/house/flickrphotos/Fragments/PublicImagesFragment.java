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

import com.code.of.house.flickrphotos.FlickrAPiManager;
import com.code.of.house.flickrphotos.Model.FlickrImage;
import com.code.of.house.flickrphotos.MosaicAapter;
import com.code.of.house.flickrphotos.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PublicImagesFragment extends Fragment {

    private static final String PER_PAGE = "20";

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

            if (recyclerView.getLayoutManager() != null) {
                //Finds values about the users location of the recyclerView
                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int pastVisibleItems = ((StaggeredGridLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPositions(null)[0];

                //A check to see if the user is near the end of the list and that new images is not being loading in already
                if ((pastVisibleItems + visibleItemCount + 10 >= totalItemCount) & !loadImagesThread.running) {
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
                FlickrAPiManager.Query_url
                        + FlickrAPiManager.Query_Method_search
                        + FlickrAPiManager.Query_per_page + PER_PAGE
                        + FlickrAPiManager.Query_noJsonCallback
                        + FlickrAPiManager.Query_Format_json
                        + FlickrAPiManager.Query_tag + tag
                        + FlickrAPiManager.Query_page + currentPage;

        return FlickrAPiManager.QueryGetString(qString);
    }

    private List<FlickrImage> ParseJSON(String json) {

        List<FlickrImage> flickrImage = new ArrayList<>();

        try {
            JSONObject JsonObject = new JSONObject(json);
            JSONObject Json_photos = JsonObject.getJSONObject("photos");
            JSONArray JsonArray_photo = Json_photos.getJSONArray("photo");

            for (int i = 0; i < JsonArray_photo.length(); i++) {
                JSONObject FlickrPhoto = JsonArray_photo.getJSONObject(i);
                String flickrId = FlickrPhoto.getString("id");
                String flickrOwner = FlickrPhoto.getString("owner");
                String flickrSecret = FlickrPhoto.getString("secret");
                String flickrServer = FlickrPhoto.getString("server");
                String flickrFarm = FlickrPhoto.getString("farm");
                String flickrTitle = FlickrPhoto.getString("title");
                flickrImage.add(new FlickrImage(flickrId, flickrOwner, flickrSecret,
                        flickrServer, flickrFarm, flickrTitle));
            }

        } catch (JSONException e) {
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

            String searchQ = searchText.getText().toString().replace(' ', '_');
            String searchResult = QueryFlickr(searchQ);
            hasMoreResults = false;
            List<FlickrImage> myFlickrImage = ParseJSON(searchResult);

            flickrImageList.addAll(myFlickrImage);

            if (!myFlickrImage.isEmpty())
                hasMoreResults = true;

            handler.sendMessage(handler.obtainMessage());
            setRunning(false);
        }

        Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                mosaicAdapter.notifyDataSetChanged(); //Notify the adapter that new data has been added to the view

                if (progressDialog.isShowing()) //Dismisses the prograsDialog after the initial search
                    progressDialog.dismiss();

                if (flickrImageList.isEmpty()) { //A Toast for when the search query ended with no results
                    Toast.makeText(getActivity(), "Der var ingen resultater", Toast.LENGTH_LONG).show();
                }
            }

        };
    }

}
