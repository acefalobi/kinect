package com.kinectafrica.android.adapter.recycler;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kinectafrica.android.R;
import com.kinectafrica.android.activity.ImageViewActivity;

import java.util.List;

/**
 * Made by acefalobi on 5/13/2017.
 */

public class ProfilePhotosRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<String> photos;
    private String userId;

    public ProfilePhotosRecyclerAdapter(Context context, List<String> photos, String userId) {
        this.context = context;
        this.photos = photos;
        this.userId = userId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final String photo = photos.get(position);
        final ItemHolder itemHolder = (ItemHolder) holder;

        if (context != null)
            Glide.with(context).load(photo).thumbnail(.3f)
                    .placeholder(R.drawable.empty_image).into(itemHolder.imageView);

        itemHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImageViewActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("photo", photo);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        ItemHolder(View view) {
            super(view);

            imageView = (ImageView) view.findViewById(R.id.image_profile_item);
        }
    }
}
