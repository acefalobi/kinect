package com.kinectafrica.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kinectafrica.android.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ImageViewActivity extends AppCompatActivity {

    ImageView imageView;
    ProgressBar progressBar;
    CoordinatorLayout relativeLayout;

    String photo;
    String userId;

    FirebaseAuth firebaseAuth;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
            getSupportActionBar().setHomeActionContentDescription("Close");
        }

        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        photo = getIntent().getStringExtra("photo");
        userId = getIntent().getStringExtra("userId");

        imageView = (ImageView) findViewById(R.id.image_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_image_view);
        relativeLayout = (CoordinatorLayout) findViewById(R.id.main_content);

        Glide.with(ImageViewActivity.this).load(photo).asBitmap().thumbnail(.2f).into(new BitmapImageViewTarget(imageView) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                super.onResourceReady(resource, glideAnimation);
                int color = Palette.from(resource).generate().getDominantColor(getResources().getColor(R.color.white));
                Drawable[] drawables = {new ColorDrawable(getResources().getColor(R.color.white)), (new ColorDrawable(color))};
                TransitionDrawable transitionDrawable = new TransitionDrawable(drawables);
                relativeLayout.setBackground(transitionDrawable);
                transitionDrawable.startTransition(200);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (firebaseAuth.getCurrentUser() == null)
            finish();
        else {
            if (userId.equals(firebaseAuth.getCurrentUser().getUid())) {
                getMenuInflater().inflate(R.menu.menu_image_view, menu);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (firebaseAuth.getCurrentUser() == null)
                finish();
            else {
                Intent home_intent;
                if (userId.equals(firebaseAuth.getCurrentUser().getUid()))
                    home_intent = new Intent(this, MainActivity.class);
                else
                    home_intent = new Intent(this, ProfileActivity.class);

                home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(home_intent);
                finish();
            }
            return true;
        }

        if (id == R.id.action_set_profile_photo) {
            databaseReference.child("users").child(userId).child("profilePicture")
                    .setValue(photo, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null)
                                Toast.makeText(ImageViewActivity.this, "Profile Photo could not be changed. Try again later",
                                        Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(ImageViewActivity.this, "Profile Photo changed successfully",
                                        Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
