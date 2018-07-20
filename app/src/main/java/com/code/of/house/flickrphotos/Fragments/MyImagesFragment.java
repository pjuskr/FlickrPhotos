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
import android.widget.Toast;

import com.code.of.house.flickrphotos.Adapters.MosaicAapter;
import com.code.of.house.flickrphotos.Model.FlickrAPiManager;
import com.code.of.house.flickrphotos.Model.FlickrImage;
import com.code.of.house.flickrphotos.R;

import java.util.ArrayList;
import java.util.List;

public class MyImagesFragment extends Fragment {

    //Here to make easy modification on how many pictures are loaded at a time
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
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_images, container, false);

        //Finds the views
        recyclerView = view.findViewById(R.id.my_recyclerview);

        //Prepare the recycler view
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager _sGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(_sGridLayoutManager);

        //Prepares the adapter for the recycler view
        mosaicAdapter = new MosaicAapter(getContext(), flickrImageList);
        recyclerView.setAdapter(mosaicAdapter);

        //Sets the needed listeners
        recyclerView.addOnScrollListener(recyclerViewOnClickListener);

        //resets the values to start new search
        flickrImageList.clear();
        currentPage = 1;
        hasMoreResults = true;

        //Starts a progress dialog informing the user that images are being loaded in and starts the background thread
        progressDialog = ProgressDialog.show(getActivity(),
                "", "Indlæser billeder!");

        loadImagesThread = new LoadImagesThread();
        loadImagesThread.setRunning(true);
        loadImagesThread.start();

        return view;
    }

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
                        if (!recyclerView.canScrollVertically(1))
                            Toast.makeText(getActivity(), "Ikke flere billeder", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static MyImagesFragment newInstance() {
        return new MyImagesFragment();
    }

    //Makes a call to get a set number of images given the search word
    private String QueryFlickr(String user_id) {

        final String qString =
                FlickrAPiManager.Query_url
                        + FlickrAPiManager.Query_Method_getPhotos
                        + FlickrAPiManager.Query_per_page + PER_PAGE
                        + FlickrAPiManager.Query_noJsonCallback
                        + FlickrAPiManager.Query_Format_json
                        + FlickrAPiManager.Query_user_id + user_id
                        + FlickrAPiManager.Query_page + currentPage;

        return FlickrAPiManager.QueryGetString(qString);

    }

    //Thread handling loading images in the background
    public class LoadImagesThread extends Thread {
        volatile boolean running = false;

        void setRunning(boolean b) {
            running = b;
        }

        @Override
        public void run() {

            String searchResult = QueryFlickr(FlickrAPiManager.flickrUser.user_id);
            hasMoreResults = false;
            List<FlickrImage> myFlickrImage = FlickrAPiManager.ParseJSONToFlickrImage(searchResult);

            flickrImageList.addAll(myFlickrImage);

            if (!myFlickrImage.isEmpty())
                hasMoreResults = true;

            handler.sendMessage(handler.obtainMessage());
            setRunning(false);
        }

        //Handler that informs the view about the result of the thread
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
