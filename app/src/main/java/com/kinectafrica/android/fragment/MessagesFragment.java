package com.kinectafrica.android.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kinectafrica.android.R;
import com.kinectafrica.android.adapter.recycler.MatchesRecyclerAdapter;
import com.kinectafrica.android.model.MessageThread;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    DatabaseReference databaseReference;

    MatchesRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    LinearLayout layoutProgress, layoutError;

    View fragmentView;


    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_messages, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        layoutProgress = (LinearLayout) fragmentView.findViewById(R.id.layout_progress_messages);
        layoutError = (LinearLayout) fragmentView.findViewById(R.id.layout_no_messages);

        databaseReference.child("messageThreads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<MessageThread> messageThreads = new ArrayList<>();
                List<String> ids = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    MessageThread thread = child.getValue(MessageThread.class);
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    if (firebaseAuth.getCurrentUser() != null) {
                        if (thread.getUserId1().equals(firebaseAuth.getCurrentUser().getUid())
                                || thread.getUserId2().equals(firebaseAuth.getCurrentUser().getUid())) {
                            child.getRef().keepSynced(true);
                            messageThreads.add(thread);
                            ids.add(child.getKey());
                        }
                    }
                }

                layoutProgress.setVisibility(View.GONE);

                if (messageThreads.size() <= 0)
                    layoutError.setVisibility(View.VISIBLE);
                else
                    layoutError.setVisibility(View.GONE);

                recyclerAdapter = new MatchesRecyclerAdapter(getActivity(), messageThreads, ids);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_messages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);

        return fragmentView;
    }

}
