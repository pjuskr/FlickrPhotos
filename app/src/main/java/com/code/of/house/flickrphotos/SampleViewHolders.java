package com.code.of.house.flickrphotos;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class SampleViewHolders extends RecyclerView.ViewHolder implements
        View.OnClickListener
{
    public ImageView Image;

    public SampleViewHolders(View itemView)
    {
        super(itemView);
        itemView.setOnClickListener(this);
        Image = (ImageView) itemView.findViewById(R.id.mosaic_image);
    }

    @Override
    public void onClick(View view)
    {
        Toast.makeText(view.getContext(),
                "Clicked Position = " + getPosition(), Toast.LENGTH_SHORT)
                .show();
    }
}
