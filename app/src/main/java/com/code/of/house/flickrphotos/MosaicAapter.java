package com.code.of.house.flickrphotos;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.code.of.house.flickrphotos.Model.FlickrImage;

import java.util.List;

public class MosaicAapter extends RecyclerView.Adapter<SampleViewHolders> {
    private List<FlickrImage> itemList;
    private Context context;

    public MosaicAapter(Context context, List<FlickrImage> itemList)
    {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public SampleViewHolders onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.mosaic_layout, null);
        SampleViewHolders rcv = new SampleViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(SampleViewHolders holder, int position)
    {
        holder.flickrImage = itemList.get(position);
        holder.Image.setImageBitmap(itemList.get(position).getBitmap());
    }

    @Override
    public int getItemCount()
    {
        return this.itemList.size();
    }
}
