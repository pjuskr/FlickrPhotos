package com.code.of.house.flickrphotos.Activities;

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
import com.code.of.house.flickrphotos.Fragments.MapFragment;
import com.code.of.house.flickrphotos.Fragments.MyImagesFragment;
import com.code.of.house.flickrphotos.Fragments.PublicImagesFragment;
import com.code.of.house.flickrphotos.R;
import com.github.scribejava.apis.FlickrApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private  DrawerLayout drawer;

    static private OAuth10aService service;
    static private OAuth1RequestToken requestToken;
    static private OAuth1AccessToken accessToken;
    static public String verifier;

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

        String verifier = "";
        String token = "";
        Intent intent = getIntent();
        Uri data = intent.getData();
        if(data != null){
            token = data.toString().substring(data.toString().indexOf("?oauth_token=") + 13,data.toString().indexOf("&oauth_verifier="));
            verifier = data.toString().substring(data.toString().indexOf("oauth_verifier=") + 15);
        }

        final String consumerKey    = "1566308a6e268a2e969dc8f09dbd11c5"; //api key
        final String consumerSecret = "f6b3331eeab4a159"; //api secret
        final String requestUrl = "https://www.flickr.com/services/oauth/request_token";

        final String finalVerifier = verifier;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(service == null){
                        service = new ServiceBuilder(consumerKey)
                                .apiSecret(consumerSecret).callback("flickrphotos:///").build(FlickrApi.instance());
                        requestToken = service.getRequestToken();
                    }

                    String authUrl = service.getAuthorizationUrl(requestToken);

                    URL url = new URL(authUrl);

                    if(!finalVerifier.isEmpty())
                    {
                        accessToken = service.getAccessToken(requestToken, finalVerifier);

                        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.flickr.com/services/rest/?method=flickr.test.login&format=json&nojsoncallback=1");
                        service.signRequest(accessToken, request);
                        final Response response = service.execute(request);
                        String body = response.getBody();
                    }

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                    startActivity(browserIntent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
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
