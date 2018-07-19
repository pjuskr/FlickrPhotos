package com.code.of.house.flickrphotos.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.code.of.house.flickrphotos.Fragments.AccountFragment;
import com.code.of.house.flickrphotos.Fragments.MyImagesFragment;
import com.code.of.house.flickrphotos.Fragments.PublicImagesFragment;
import com.code.of.house.flickrphotos.Model.FlickrAPiManager;
import com.code.of.house.flickrphotos.R;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //prepares the views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //Gets the verifier from the intent returned by the browser
        FlickrAPiManager.verifier = "";
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            FlickrAPiManager.verifier = data.toString().substring(data.toString().indexOf("oauth_verifier=") + 15);
        }

        //Use the activity to allow calling the browser
        final Activity f = this;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FlickrAPiManager.login(f);}
        });
        thread.start();

        //opens the PublicImagesFragment such that the user is not welcomed with a blank screen from start
        if(!FlickrAPiManager.verifier.isEmpty())
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,new PublicImagesFragment()).commit();
    }

    public void openFragment(String text) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        String fragnmentName = "";
        Fragment fragment = null;
        Boolean isMaps = false;

        //Finds out with fragment to open
        switch (menuItem.getItemId()) {
            case R.id.nav_public_images:
                fragment = PublicImagesFragment.newInstance();
                break;
            case R.id.nav_my_images:
                fragment = MyImagesFragment.newInstance();
                break;
            case R.id.nav_map:
                //fragment = MapFragment.newInstance();
                isMaps = true;
                break;
            case R.id.nav_profile:
                fragment = AccountFragment.newInstance();
                break;
        }

        //Opens the selected fragment
        if (fragment != null & !isMaps) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            transaction.addToBackStack(null);
            transaction.add(R.id.fragment_container, fragment, fragnmentName).commit();
        }
        if(isMaps){
            Intent intent = new Intent(this , MapsActivity.class);
            this.startActivity(intent);
        }


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
