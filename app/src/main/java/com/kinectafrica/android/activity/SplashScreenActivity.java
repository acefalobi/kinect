package com.kinectafrica.android.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kinectafrica.android.R;
import com.kinectafrica.android.utility.KinectReceiver;

import java.util.Calendar;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("fcmId")
                        .setValue(FirebaseInstanceId.getInstance().getToken());
            }
            int SPLASH_TIMEOUT = 1000;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, SPLASH_TIMEOUT);
        } else {
            int SPLASH_TIMEOUT = 1000;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                }
            }, SPLASH_TIMEOUT);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
