package com.code.of.house.flickrphotos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.code.of.house.flickrphotos.Model.FlickrImage;
import com.code.of.house.flickrphotos.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullsceenImageActivity extends AppCompatActivity {


    private ImageView imageView;
    private FlickrImage flickrImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullsceen_image);

        imageView = findViewById(R.id.fulscreen_image);

        final Intent intent = getIntent();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                flickrImage = (FlickrImage) intent.getParcelableExtra("object");

                flickrImage.GetBigBitmap();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(flickrImage.GetBigBitmap());
                    }
                });
            }
        });
        thread.start();

    }
}
