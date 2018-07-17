package com.code.of.house.flickrphotos.Activities;

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
import com.code.of.house.flickrphotos.Fragments.MapFragment;
import com.code.of.house.flickrphotos.Fragments.MyImagesFragment;
import com.code.of.house.flickrphotos.Fragments.PublicImagesFragment;
import com.code.of.house.flickrphotos.R;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private  DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void openFragment(String text){

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        String fragnmentName = "";
        Fragment fragment = null;

        switch (menuItem.getItemId()){
            case R.id.nav_public_images:
                fragment = PublicImagesFragment.newInstance();
                break;
            case R.id.nav_my_images:
                fragment = MyImagesFragment.newInstance();
                break;
            case R.id.nav_map:
                fragment = MapFragment.newInstance();
                break;
            case R.id.nav_profile:
                fragment = AccountFragment.newInstance();
                break;
        }

        if(fragment != null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            transaction.addToBackStack(null);
            transaction.add(R.id.fragment_container, fragment, fragnmentName).commit();
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
