package com.code.of.house.flickrphotos.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.code.of.house.flickrphotos.Activities.FullscreenImageActivity;
import com.code.of.house.flickrphotos.Model.FlickrImage;
import com.code.of.house.flickrphotos.R;

public class SampleViewHolders extends RecyclerView.ViewHolder implements
        View.OnClickListener
{
    public ImageView Image;
    public FlickrImage flickrImage;
    private Context context;

    public SampleViewHolders(View itemView)
    {
        super(itemView);
        context = itemView.getContext();
        itemView.setOnClickListener(this);
        Image = (ImageView) itemView.findViewById(R.id.mosaic_image);
    }

    @Override
    public void onClick(View view)
    {
        Intent intent = new Intent(context , FullscreenImageActivity.class);
        intent.putExtra("object", flickrImage);
        context.startActivity(intent);
    }
}
