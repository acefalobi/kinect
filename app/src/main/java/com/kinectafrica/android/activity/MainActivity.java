package com.kinectafrica.android.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kinectafrica.android.R;
import com.kinectafrica.android.adapter.pager.MainPagerAdapter;
import com.kinectafrica.android.model.Message;
import com.kinectafrica.android.model.MessageThread;
import com.kinectafrica.android.model.User;
import com.kinectafrica.android.utility.Utils;
import com.kinectafrica.android.view.CustomViewPager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    ProgressDialog mProgressDialog;

    FirebaseAuth firebaseAuth;

    MainPagerAdapter pagerAdapter;
    Button toolbarButton;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    CustomViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, SplashScreenActivity.class);
            startActivity(intent);
            finish();
        }

        String intentAction = getIntent().getStringExtra("action");
        String intentId = getIntent().getStringExtra("id");

        if (intentAction != null && intentId != null) {
            switch (intentAction) {
                case "chat": {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("threadId", intentId);
                    startActivity(intent);
                    break;
                }
                case "profile": {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra("userId", intentId);
                    startActivity(intent);
                    break;
                }
            }
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_forum_grey_24dp);
            getSupportActionBar().setHomeActionContentDescription("Messages");
        }

        // Set up ProgressDialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Uploading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null)
            finish();
        else {
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(firebaseAuth.getCurrentUser().getUid()).child("fcmId")
                    .setValue(FirebaseInstanceId.getInstance().getToken());
        }
        updateProfile();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (CustomViewPager) findViewById(R.id.container);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);

        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Snackbar.make(viewPager, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
        }

        toolbarButton = (Button) findViewById(R.id.text_toolbar);
        toolbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Egyptian Nights.ttf");
        toolbarButton.setTypeface(typeface);

        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot child : dataSnapshot.getChildren()) {

                    SharedPreferences sharedPreferences = getSharedPreferences("currentUser", Context.MODE_PRIVATE);

                    final User user = child.getValue(User.class);

                    List<String> yesUsers;
                    yesUsers = Lists.newArrayList((sharedPreferences.getStringSet("yesUsers", new HashSet<String>())));
                    boolean hasYes = false;

                    for (String userId: yesUsers) {
                        if (userId.equals(child.getKey())) {
                            hasYes = true;
                        }
                    }

                    List<String> yesUsers2;
                    if (user.getYesUsers() != null)
                        yesUsers2 = user.getYesUsers();
                    else
                        yesUsers2 = new ArrayList<>();

                    boolean hasYes2 = false;

                    for (String userId: yesUsers2) {
                        if (firebaseAuth.getCurrentUser() != null) {
                            if (userId.equals(firebaseAuth.getCurrentUser().getUid())) {
                                hasYes2 = true;
                            }
                        }
                    }

                    boolean hasMatch = false;

                    List<String> matchedUsers;
                    matchedUsers = Lists.newArrayList((sharedPreferences.getStringSet("matchedUsers",
                            new HashSet<String>())));

                    for (String userId: matchedUsers) {
                        if (userId.equals(child.getKey())) {
                            hasMatch = true;
                        }
                    }
                    if (hasYes && hasYes2 && !hasMatch) {
                        TextView textProfile1 = (TextView) findViewById(R.id.text_profile1);
                        TextView textProfile2 = (TextView) findViewById(R.id.text_profile2);

                        TextView textMatchMessage = (TextView) findViewById(R.id.text_match_message);
                        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Egyptian Nights.ttf");
                        textMatchMessage.setTypeface(typeface);

                        TextView textMatchMessage2 = (TextView) findViewById(R.id.text_match_message_2);
                        textMatchMessage2.setText(user.getDisplayName() + " swiped right too");

                        ImageView imageProfile1 = (ImageView) findViewById(R.id.image_profile1);
                        ImageView imageProfile2 = (ImageView) findViewById(R.id.image_profile2);

                        textProfile1.setText(sharedPreferences.getString("displayName", ""));
                        textProfile2.setText(user.getDisplayName());

                        Glide.with(MainActivity.this).load(sharedPreferences.getString("profilePicture",
                                "http://showmie.com/kinect/avatar/default_avatar.png")).thumbnail(.4f)
                                .placeholder(R.drawable.empty_image).into(imageProfile1);
                        Glide.with(MainActivity.this).load(user.getProfilePicture()).thumbnail(.4f)
                                .placeholder(R.drawable.empty_image).into(imageProfile2);

                        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.match_layout);
                        Utils.INSTANCE.fadeIn(linearLayout, 1000);
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Utils.INSTANCE.fadeOut(linearLayout, 500);
                            }
                        });

                        Button button = (Button) findViewById(R.id.btn_goto_messages);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Utils.INSTANCE.fadeOut(linearLayout, 500);
                                viewPager.setCurrentItem(0);
                            }
                        });

                        matchedUsers.add(child.getKey());

                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(firebaseAuth.getCurrentUser().getUid()).child("matchedUsers")
                                .setValue(matchedUsers);

                        FirebaseDatabase.getInstance().getReference().child("messageThreads")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        boolean hasBeenInitialized = false;
                                        for (DataSnapshot child1 : dataSnapshot.getChildren()) {
                                            MessageThread thread = child1.getValue(MessageThread.class);
                                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                            if (firebaseAuth.getCurrentUser() != null) {
                                                if ((thread.getUserId1().equals(child.getKey())
                                                        && thread.getUserId2().equals(firebaseAuth.getCurrentUser().getUid()))
                                                        || (thread.getUserId2().equals(child.getKey())
                                                        && thread.getUserId1().equals(firebaseAuth.getCurrentUser().getUid()))) {
                                                    hasBeenInitialized = true;
                                                }
                                            }
                                        }
                                        if (!hasBeenInitialized) {
                                            final MessageThread messageThread = new MessageThread(child.getKey(),
                                                    firebaseAuth.getCurrentUser().getUid(),
                                                    new ArrayList<Message>());
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("messageThreads").push().setValue(messageThread);
                                            Utils.INSTANCE.sendFCM(MainActivity.this, user.getFcmId(),
                                                    user.getDisplayName() + " has swiped right too",
                                                    "main", "");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(MainActivity.this,
                                                databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putStringSet("matchedUsers", new HashSet<>(matchedUsers));
                        editor.apply();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        if (firebaseAuth.getCurrentUser() != null) {
            FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final User user = dataSnapshot.getValue(User.class);
                            SharedPreferences sharedPreferences = getSharedPreferences("currentUser", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("displayName", user.getDisplayName());
                            editor.putString("email", user.getEmail());
                            editor.putString("gender", user.getGender());
                            editor.putLong("dateOfBirth", user.getDateOfBirth());
                            editor.putString("interest", user.getInterest());
                            editor.putString("location", user.getLocation());
                            editor.putFloat("longitude", (float) user.getLongitude());
                            editor.putFloat("latitude", (float) user.getLatitude());
                            editor.putString("description", user.getDescription());
                            editor.putString("profilePicture", user.getProfilePicture());
                            Set<String> yesUsers;
                            if (user.getYesUsers() != null) {
                                yesUsers = new HashSet<>(user.getYesUsers());
                            } else {
                                yesUsers = new HashSet<>();
                            }
                            editor.putStringSet("yesUsers", yesUsers);

                            Set<String> matchedUsers;
                            if (user.getMatchedUsers() != null) {
                                matchedUsers = new HashSet<>(user.getMatchedUsers());
                            } else {
                                matchedUsers = new HashSet<>();
                            }
                            editor.putStringSet("matchedUsers", matchedUsers);

                            Set<String> photos;
                            if (user.getPhotos() != null) {
                                photos = new HashSet<>(user.getPhotos());
                            } else {
                                photos = new HashSet<>();
                            }
                            editor.putStringSet("photos", photos);
                            editor.apply();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("TwitterKit", "Login failure", databaseError.toException());
                            Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        } else {
            firebaseAuth.signOut();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        menu.findItem(R.id.action_settings).setIcon(R.drawable.ic_person_grey_24dp);
                        toolbarButton.setTextColor(getResources().getColor(R.color.gray_transparent));
                        if (getSupportActionBar() != null)
                            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_forum_black_24dp);
                        break;
                    case 1:
                        menu.findItem(R.id.action_settings).setIcon(R.drawable.ic_person_grey_24dp);
                        toolbarButton.setTextColor(getResources().getColor(R.color.white));
                        if (getSupportActionBar() != null)
                            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_forum_grey_24dp);
                        break;
                    case 2:
                        menu.findItem(R.id.action_settings).setIcon(R.drawable.ic_person_white_24dp);
                        toolbarButton.setTextColor(getResources().getColor(R.color.gray_transparent));
                        if (getSupportActionBar() != null)
                            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_forum_grey_24dp);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                viewPager.setCurrentItem(2);
                return true;
            case android.R.id.home:
                viewPager.setCurrentItem(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
