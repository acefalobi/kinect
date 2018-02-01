package com.kinectafrica.android.adapter.recycler;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kinectafrica.android.R;
import com.kinectafrica.android.activity.ChatActivity;
import com.kinectafrica.android.model.MessageThread;
import com.kinectafrica.android.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Made by acefalobi on 4/10/2017.
 */

public class MatchesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<MessageThread> messageThreads;
    private List<String> messageThreadIds;

    public MatchesRecyclerAdapter(Context context, List<MessageThread> messageThreads, List<String> messageThreadIds) {
        this.context = context;
        this.messageThreads = messageThreads;
        this.messageThreadIds = messageThreadIds;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            final ItemHolder itemHolder = (ItemHolder) holder;
            final MessageThread messageThread = messageThreads.get(position);
            final String id = messageThreadIds.get(position);
            if (messageThread.getUserId1().equals(firebaseAuth.getCurrentUser().getUid())) {
                FirebaseDatabase.getInstance().getReference().child("users").child(messageThread.getUserId2())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                itemHolder.textMatchUser.setText(user.getDisplayName());

                                if (context != null)
                                    Glide.with(context)
                                            .load(user.getProfilePicture()).thumbnail(.4f)
                                            .placeholder(R.drawable.empty_image).into(itemHolder.imageMatchUser);

                                itemHolder.layoutMatch.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(context, ChatActivity.class);
                                        intent.putExtra("threadId",id);
                                        context.startActivity(intent);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                FirebaseDatabase.getInstance().getReference().child("users").child(messageThread.getUserId1())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                itemHolder.textMatchUser.setText(user.getDisplayName());

                                if (context != null)
                                    Glide.with(context)
                                            .load(user.getProfilePicture()).thumbnail(.4f)
                                            .placeholder(R.drawable.empty_image).into(itemHolder.imageMatchUser);

                                itemHolder.layoutMatch.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(context, ChatActivity.class);
                                        intent.putExtra("threadId",id);
                                        context.startActivity(intent);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageThreads.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutMatch;
        TextView textMatchUser;
        ImageView imageMatchUser;

        ItemHolder(View itemView) {
            super(itemView);

            layoutMatch = (LinearLayout) itemView.findViewById(R.id.layout_match);
            textMatchUser = (TextView) itemView.findViewById(R.id.text_match_user);
            imageMatchUser = (ImageView) itemView.findViewById(R.id.image_match_user);
        }
    }
}
