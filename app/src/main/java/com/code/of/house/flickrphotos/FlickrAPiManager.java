package com.code.of.house.flickrphotos;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.code.of.house.flickrphotos.Model.FlickrUser;
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
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class FlickrAPiManager {
    public static final String Query_url = "https://api.flickr.com/services/rest/";

    public static final String Query_Method_getInfo = "?method=flickr.people.getInfo";
    public static final String Query_Method_getPhotos = "?method=flickr.people.getPhotos";
    public static final String Query_Method_search = "?method=flickr.photos.search";
    public static final String Query_Method_testlogin = "?method=flickr.test.login";

    public static final String Query_noJsonCallback = "&nojsoncallback=1";
    public static final String Query_Format_json = "&format=json";

    public static final String Query_user_id = "&user_id=";
    public static final String Query_tag = "&tags=";

    public static final String Query_per_page = "&per_page=";
    public static final String Query_page = "&page=";

    private static final String consumerKey = "1566308a6e268a2e969dc8f09dbd11c5"; //api key
    private static final String consumerSecret = "f6b3331eeab4a159"; //api secret

    static public FlickrUser flickrUser = new FlickrUser();

    static private OAuth10aService service;
    static private OAuth1RequestToken requestToken;
    static private OAuth1AccessToken accessToken;

    static public String verifier;

    public static void login(Activity activity) {

        getService();

        if(!verifier.isEmpty()){
            getAccessToken();
            getUserInfo();
        } else{
            getVerifier(activity);
        }
    }

    private static void getService(){
        try {
            if (service == null) {
                service = new ServiceBuilder(consumerKey)
                        .apiSecret(consumerSecret).callback("flickrphotos:///").build(FlickrApi.instance());
                requestToken = service.getRequestToken();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void getAccessToken(){
        try {
            accessToken = service.getAccessToken(requestToken, verifier);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getVerifier(Activity activity){
        String authUrl = service.getAuthorizationUrl(requestToken);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
        activity.startActivity(browserIntent);
    }

    private static void getUserInfo(){

        getBasicUserInfo();
        getAdditionalUserInfo();
    }

    //This method gets the user_id and username for the user
    private static void getBasicUserInfo(){

        try{
            String queryLoginTest = Query_url
                    + Query_Method_testlogin
                    + Query_Format_json
                    + Query_noJsonCallback;

            String accountInfoJson = QueryGetString(queryLoginTest);

            JSONObject JsonObject = new JSONObject(accountInfoJson);

            JSONObject Json_user = JsonObject.getJSONObject("user");
            flickrUser.user_id = Json_user.getString("id");

            JSONObject Json_username = Json_user.getJSONObject("username");
            flickrUser.username = Json_username.getString("_content");

        }  catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //This method gets additional info about the user like the nsid and user icon
    private static void getAdditionalUserInfo(){

        try {
            String queryUserIfo = Query_url
                    + Query_Method_getInfo
                    + Query_Format_json
                    + Query_noJsonCallback
                    + Query_user_id + flickrUser.user_id;

            String userInfoJson = QueryGetString(queryUserIfo);

            JSONObject JsonObject = new JSONObject(userInfoJson).getJSONObject("person");

            flickrUser.nsid = JsonObject.getString("nsid");
            String farm = JsonObject.getString("iconfarm");
            String server = JsonObject.getString("iconserver");

            if (!farm.equals("0")) { //safety check in case the user have the default icon, as then it will fail
                String q = "http://farm" + farm + ".staticflickr.com/" + server + "/buddyicons/" + flickrUser.nsid + "_m.jpg";

                flickrUser.user_icon = QueryGetBitmap(q);
            }

        }  catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Method for calling the api with a query that returns it all as a String
    public static String QueryGetString(String query){

        Response response = QueryGetResponce(query);
        try{
            if(response != null)
                return response.getBody();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //Method for calling the api with a query that returns it all as an InputsStream
    public static InputStream QueryGetStream(String query){

        Response response = QueryGetResponce(query);

        if(response != null)
            return response.getStream();
        else
            return null;
    }

    public static Bitmap QueryGetBitmap(String Query){

        Response response = QueryGetResponce(Query);
        if(response != null)
            return BitmapFactory.decodeStream(response.getStream());
        else
            return null;

    }
    private static Response QueryGetResponce(String Query){
        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, Query);
            service.signRequest(accessToken, request);
            return service.execute(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }





}
