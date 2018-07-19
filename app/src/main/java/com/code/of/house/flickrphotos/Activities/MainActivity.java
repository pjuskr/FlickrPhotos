package com.code.of.house.flickrphotos.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.code.of.house.flickrphotos.Model.FlickrUser;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;

    static public FlickrUser flickrUser;

    //Variables for more easily building a query to FlickrAPI
    public static final String FlickrQuery_url = "https://api.flickr.com/services/rest/";
    public static final String FlickrQuery_method = "?method=flickr.people.getInfo";
    public static final String FlickrQuery_nojsoncallback = "&nojsoncallback=1";
    public static final String FlickrQuery_format = "&format=json";
    public static final String FlickrQuery_user_id = "&user_id=";

    static public OAuth10aService service;
    static public OAuth1RequestToken requestToken;
    static public OAuth1AccessToken accessToken;
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
        if (data != null) {
            token = data.toString().substring(data.toString().indexOf("?oauth_token=") + 13, data.toString().indexOf("&oauth_verifier="));
            verifier = data.toString().substring(data.toString().indexOf("oauth_verifier=") + 15);
        }

        final String consumerKey = "1566308a6e268a2e969dc8f09dbd11c5"; //api key
        final String consumerSecret = "f6b3331eeab4a159"; //api secret

        final String finalVerifier = verifier;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (service == null) {
                        service = new ServiceBuilder(consumerKey)
                                .apiSecret(consumerSecret).callback("flickrphotos:///").build(FlickrApi.instance());
                        requestToken = service.getRequestToken();
                    }

                    String authUrl = service.getAuthorizationUrl(requestToken);

                    if (!finalVerifier.isEmpty()) {
                        accessToken = service.getAccessToken(requestToken, finalVerifier);

                        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.flickr.com/services/rest/?method=flickr.test.login&format=json&nojsoncallback=1");
                        service.signRequest(accessToken, request);
                        final Response response = service.execute(request);


                        String body = response.getBody();

                        JSONObject JsonObject = new JSONObject(body);
                        JSONObject Json_photos = JsonObject.getJSONObject("user");

                        String id = Json_photos.getString("id");

                        JSONObject Json_photos2 = Json_photos.getJSONObject("username");
                        String username = Json_photos2.getString("_content");


                        String query = FlickrQuery_url + FlickrQuery_method + FlickrQuery_format + FlickrQuery_nojsoncallback + FlickrQuery_user_id + id;

                        final OAuthRequest request2 = new OAuthRequest(Verb.GET, query);
                        service.signRequest(accessToken, request2);
                        final Response response2 = service.execute(request2);

                        String body2 = response2.getBody();

                        JSONObject JsonObject3 = new JSONObject(body2).getJSONObject("person");

                        String nsid = JsonObject3.getString("nsid");
                        String farm = JsonObject3.getString("iconfarm");
                        String server = JsonObject3.getString("iconserver");
                        Bitmap bla = null;
                        if (!farm.equals("0")) { //safety check in case the user have the default icon
                            String q = "http://farm" + farm + ".staticflickr.com/" + server + "/buddyicons/" + nsid + "_m.jpg";

                            final OAuthRequest request3 = new OAuthRequest(Verb.GET, q);
                            service.signRequest(accessToken, request3);
                            final Response response3 = service.execute(request3);

                            bla = BitmapFactory.decodeStream(response3.getStream());
                        }

                        flickrUser = new FlickrUser(username ,id, nsid, bla);


                    } else {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                        startActivity(browserIntent);
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void openFragment(String text) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        String fragnmentName = "";
        Fragment fragment = null;

        switch (menuItem.getItemId()) {
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

        if (fragment != null) {
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
