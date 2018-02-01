package com.kinectafrica.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.kinectafrica.android.R;
import com.kinectafrica.android.adapter.recycler.ProfilePhotosRecyclerAdapter;
import com.kinectafrica.android.model.User;
import com.kinectafrica.android.utility.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileActivity extends AppCompatActivity {

    DatabaseReference databaseReference;

    ProfilePhotosRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    TextView textNameAge, textDescription, textLocation;
    FloatingActionButton btnYes, btnNo;
    CircularImageView imageView;
    ProgressBar progressBar;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        progressBar = findViewById(R.id.progress_profile_photos);

        layout = findViewById(R.id.layout_no_photos);

        textNameAge = findViewById(R.id.text_name_age);
        textDescription = findViewById(R.id.text_description);
        textLocation = findViewById(R.id.text_location);

        btnNo = findViewById(R.id.btn_no);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });

        btnYes = findViewById(R.id.btn_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                if (firebaseAuth.getCurrentUser() != null) {
                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    Map<String, Object> userValues = user != null ? user.toMap() : null;

                                    SharedPreferences sharedPreferences
                                            = getSharedPreferences("currentUser", Context.MODE_PRIVATE);

                                    List<String> yesUsers;
                                    if ((user != null ? user.getYesUsers() : null) != null) {
                                        yesUsers = user.getYesUsers();
                                    } else {
                                        yesUsers = new ArrayList<>();
                                    }
                                    yesUsers.add(getIntent().getStringExtra("userId"));
                                    if (userValues != null) {
                                        userValues.put("yesUsers", yesUsers);
                                    }

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    Set<String> yesUsersSet;
                                    yesUsersSet = new HashSet<>(yesUsers);
                                    editor.putStringSet("yesUsers", yesUsersSet);
                                    editor.apply();

                                    if (userValues != null) {
                                        dataSnapshot.getRef().updateChildren(userValues);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        });

        imageView = findViewById(R.id.profile_image);

        recyclerView = findViewById(R.id.recycler_profile_photos);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(getIntent().getStringExtra("userId"))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);

                        SharedPreferences sharedPreferences
                                = getSharedPreferences("currentUser", Context.MODE_PRIVATE);

                        List<String> yesUsers;
                        yesUsers = Lists.newArrayList((sharedPreferences.getStringSet("yesUsers", new HashSet<String>())));

                        boolean hasYes = false;

                        for (String userId : yesUsers) {
                            if (userId.equals(getIntent().getStringExtra("userId"))) {
                                hasYes = true;
                            }
                        }

                        if (!hasYes) {
                            btnNo.setVisibility(View.VISIBLE);
                            btnYes.setVisibility(View.VISIBLE);
                            if (getSupportActionBar() != null)
                                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        }

                        if (getSupportActionBar() != null)
                            getSupportActionBar().setTitle(user != null ? user.getDisplayName() : null);

                        long today = System.currentTimeMillis();
                        long age = today - (user != null ? user.getDateOfBirth() : 0);
                        long divider = (long) (86340862 * 365.25);
                        Long ageYears = age / divider;

                        double distance = 0;
                        if (user != null) {
                            distance = Utils.INSTANCE.getLongLatDistance(user.getLatitude(), user.getLongitude(),
                                    getSharedPreferences("currentUser", Context.MODE_PRIVATE).getFloat("latitude", (float) 0.0),
                                    getSharedPreferences("currentUser", Context.MODE_PRIVATE).getFloat("longitude", (float) 0.0));
                        }

                        DecimalFormat decimalFormat = new DecimalFormat("#.#");

                        textNameAge.setText(user.getDisplayName() + ", " + ageYears);
                        textLocation.setText(user.getLocation() + " ("
                                + String.valueOf(decimalFormat.format(distance )) + "km)");

                        textDescription.setText(user.getDescription());

                        if (!isFinishing())
                            Glide.with(ProfileActivity.this)
                                    .load(user.getProfilePicture())
                                    .placeholder(R.drawable.empty_image)
                                    .thumbnail(.3f).into(imageView);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ProfileActivity.this, ImageViewActivity.class);
                                intent.putExtra("userId", getIntent().getStringExtra("userId"));
                                intent.putExtra("photo", user.getProfilePicture());
                                startActivity(intent);
                            }
                        });

                        if (user.getPhotos() == null)
                            user.setPhotos(new ArrayList<String>());

                        recyclerAdapter = new ProfilePhotosRecyclerAdapter(ProfileActivity.this,
                                user.getPhotos(), getIntent().getStringExtra("userId"));
                        recyclerView.setAdapter(recyclerAdapter);
                        progressBar.setVisibility(View.GONE);

                        List<String> photos = user.getPhotos();
                        if ((photos != null ? photos.size() : 0) <= 0)
                            layout.setVisibility(View.VISIBLE);
                        else
                            layout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
