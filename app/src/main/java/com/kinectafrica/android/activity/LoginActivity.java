package com.kinectafrica.android.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kinectafrica.android.R;
import com.kinectafrica.android.model.User;
import com.kinectafrica.android.utility.GPSTracker;
import com.kinectafrica.android.utility.KinectReceiver;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    GPSTracker gpsTracker;

    AuthCredential credential;

    String location = "";
    double longitude;
    double latitude;
    String userKey;
    String fullName;
    String profilePicture;

    String loginProvider;

    User currentUser;

    Map<String, Object> userValues;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    LoginButton facebookLoginButton;
    TwitterLoginButton loginButton;
    AppCompatButton facebookButton;

    ProgressDialog progressDialog;

    com.twitter.sdk.android.core.models.User twitterUser;
    CallbackManager callbackManager;

    Calendar calendar;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        calendar = Calendar.getInstance();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating");

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null)
            LoginManager.getInstance().logOut();


        databaseReference = FirebaseDatabase.getInstance().getReference();

        callbackManager = CallbackManager.Factory.create();

        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(final Result<TwitterSession> result) {
                progressDialog.show();
                Twitter.getApiClient(result.data).getAccountService().verifyCredentials(true, false)
                        .enqueue(new retrofit2.Callback<com.twitter.sdk.android.core.models.User>() {
                            @Override
                            public void onResponse(Call<com.twitter.sdk.android.core.models.User> call,
                                                   Response<com.twitter.sdk.android.core.models.User> response) {
                                twitterUser = response.body();
                                credential = TwitterAuthProvider.getCredential(
                                        result.data.getAuthToken().token,
                                        result.data.getAuthToken().secret);

                                if (ContextCompat.checkSelfPermission(LoginActivity.this,
                                        Manifest.permission.ACCESS_COARSE_LOCATION)
                                        == PackageManager.PERMISSION_GRANTED
                                        && ContextCompat.checkSelfPermission(LoginActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION)
                                        == PackageManager.PERMISSION_GRANTED) {
                                    storeLocation();
                                } else {
                                    progressDialog.dismiss();
                                    ActivityCompat.requestPermissions(LoginActivity.this,
                                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
                                }

                                loginProvider = "twitter";
                            }

                            @Override
                            public void onFailure(Call<com.twitter.sdk.android.core.models.User> call, Throwable t) {
                                Log.d("TwitterKit", "Login with Twitter failure", t);
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this,
                                        "Unable to connect. Try again later", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        facebookButton = (AppCompatButton) findViewById(R.id.login_button);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLoginButton.performClick();
            }
        });
        facebookLoginButton = (LoginButton) findViewById(R.id.facebook_button);
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                progressDialog.show();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    fullName = object.getString("first_name") + " " + object.getString("last_name");
                                    profilePicture = object.getJSONObject("picture")
                                            .getJSONObject("data").getString("url");
                                    loginProvider = "facebook";

                                    credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());

                                    if (ContextCompat.checkSelfPermission(LoginActivity.this,
                                            Manifest.permission.ACCESS_COARSE_LOCATION)
                                            == PackageManager.PERMISSION_GRANTED
                                            && ContextCompat.checkSelfPermission(LoginActivity.this,
                                            Manifest.permission.ACCESS_FINE_LOCATION)
                                            == PackageManager.PERMISSION_GRANTED) {
                                        storeLocation();
                                    } else {
                                        progressDialog.dismiss();
                                        ActivityCompat.requestPermissions(LoginActivity.this,
                                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                                        Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
                                    }

                                } catch (JSONException e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity.this,
                                            "Unable to connect. Try again later", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "first_name,last_name,picture.type(large)");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Login Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogin() {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            email = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getEmail() : "";
                            databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    boolean isRegistered = false;
                                    DataSnapshot userDataSnapshot = null;
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                        //noinspection ConstantConditions
                                        if (child.getKey().equals(firebaseAuth.getCurrentUser().getUid())) {
                                            isRegistered = true;
                                            userDataSnapshot = child;
                                        }
                                    }
                                    if (!isRegistered) {
                                        progressDialog.dismiss();
                                        openDatePickerDialog();
                                    } else {
                                        final User user = userDataSnapshot.getValue(User.class);
                                        userValues = user.toMap();
                                        userValues.put("location", location);
                                        userValues.put("longitude", longitude);
                                        userValues.put("latitude", latitude);
                                        userKey = userDataSnapshot.getKey();
                                        userDataSnapshot.getRef().updateChildren(userValues);

                                        SharedPreferences sharedPreferences =
                                                getSharedPreferences("currentUser", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putInt("kinectLeft", 5);
                                        editor.putString("displayName", user.getDisplayName());
                                        editor.putString("email", user.getEmail());
                                        editor.putString("gender", user.getGender());
                                        editor.putLong("dateOfBirth", user.getDateOfBirth());
                                        editor.putString("interest", user.getInterest());
                                        editor.putString("location", location);
                                        editor.putFloat("longitude", (float) longitude);
                                        editor.putFloat("latitude", (float) latitude);
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

                                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                                        Intent intent = new Intent(LoginActivity.this, KinectReceiver.class);

                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(LoginActivity.this, 0, intent, 0);

                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTimeInMillis(System.currentTimeMillis());
                                        calendar.set(Calendar.HOUR_OF_DAY, 12);
                                        calendar.set(Calendar.MINUTE, 0);
                                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

                                        progressDialog.dismiss();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    firebaseAuth.signOut();
                                    LoginManager.getInstance().logOut();
                                    Log.d("TwitterKit", "Login failure", databaseError.toException());
                                    Toast.makeText(LoginActivity.this, databaseError.getMessage(),
                                            Toast.LENGTH_LONG).show();

                                }
                            });
                        } else {
                            firebaseAuth.signOut();
                            LoginManager.getInstance().logOut();
                            progressDialog.dismiss();
                            //noinspection ThrowableResultOfMethodCallIgnored
                            if (task.getException() != null) {
                                //noinspection ThrowableResultOfMethodCallIgnored
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Unable To Sign In", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) { }

    private void openDatePickerDialog() {
        @SuppressLint("InflateParams")
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_date_picker, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .setTitle("Select Date Of Birth")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.datePicker);
                        Calendar dob = Calendar.getInstance();
                        dob.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                        long dateOfBirth = dob.getTimeInMillis();
                        openGenderInterestDialog(dateOfBirth);
                    }
                });
        builder.create().show();
    }

    private void openGenderInterestDialog(final long dateOfBirth) {
        @SuppressLint("InflateParams")
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_interest, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .setTitle("Almost done")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.setMessage("Registering");
                        progressDialog.show();
                        RadioButton radioGenderMale =
                                (RadioButton) dialogView.findViewById(R.id.radio_gender_male);
                        RadioButton radioInterestMale =
                                (RadioButton) dialogView.findViewById(R.id.radio_interest_male);

                        final String gender = radioGenderMale.isChecked() ? "male" : "female";
                        final String interest = radioInterestMale.isChecked() ? "male" : "female";

                        if (loginProvider.equals("twitter"))
                            currentUser = new User(twitterUser.name, email, gender,
                                    dateOfBirth, interest, location, longitude, latitude,
                                    twitterUser.profileImageUrl.replace("_normal", ""), twitterUser.description);
                        else
                            currentUser = new User(fullName, email, gender,
                                    dateOfBirth, interest, location, longitude, latitude,
                                    profilePicture, "");


                        //noinspection ConstantConditions
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid())
                                .setValue(currentUser, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(final DatabaseError databaseError,
                                                           final DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Toast.makeText(LoginActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            firebaseAuth.signOut();
                                            LoginManager.getInstance().logOut();
                                        } else {
                                            SharedPreferences sharedPreferences =
                                                    getSharedPreferences("currentUser", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt("kinectLeft", 5);
                                            editor.putString("displayName", currentUser.getDisplayName());
                                            editor.putString("email", currentUser.getEmail());
                                            editor.putString("gender", currentUser.getGender());
                                            editor.putLong("dateOfBirth", currentUser.getDateOfBirth());
                                            editor.putString("interest", currentUser.getInterest());
                                            editor.putString("location", currentUser.getLocation());
                                            editor.putFloat("longitude", (float) currentUser.getLongitude());
                                            editor.putFloat("latitude", (float) currentUser.getLatitude());
                                            editor.putString("description", currentUser.getDescription());
                                            editor.putString("profilePicture", currentUser.getProfilePicture());
                                            Set<String> yesUsers = new HashSet<>();
                                            editor.putStringSet("yesUsers", yesUsers);
                                            Set<String> matchedUsers = new HashSet<>();
                                            editor.putStringSet("matchedUsers", matchedUsers);
                                            Set<String> photos = new HashSet<>();
                                            editor.putStringSet("photos", photos);
                                            editor.apply();
                                            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                                            Intent intent = new Intent(LoginActivity.this, KinectReceiver.class);

                                            PendingIntent pendingIntent = PendingIntent.getBroadcast(LoginActivity.this, 0, intent, 0);

                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTimeInMillis(System.currentTimeMillis());
                                            calendar.set(Calendar.HOUR_OF_DAY, 12);
                                            calendar.set(Calendar.MINUTE, 0);
                                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                                            progressDialog.dismiss();
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    }
                                });

                    }
                });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1234) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access Granted", Toast.LENGTH_SHORT).show();
                progressDialog.show();
                storeLocation();
            } else {
                Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void storeLocation() {
        gpsTracker = new GPSTracker(LoginActivity.this);
        if (gpsTracker.canGetLocation()) {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            Geocoder geoCoder = new Geocoder(LoginActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geoCoder.getFromLocation(gpsTracker.getLatitude(), gpsTracker.getLongitude(), 1);
            } catch (IOException ignored) { }
            if (addresses != null && addresses.size() > 0) {
                location = "";
                for (int i = addresses.get(0).getMaxAddressLineIndex() - 2;
                     i <= addresses.get(0).getMaxAddressLineIndex(); i++) {
                    if (i == addresses.get(0).getMaxAddressLineIndex() - 2) {
                        if (i >= 0) location += addresses.get(0).getAddressLine(i);
                    } else {
                        if (i >= 0) location += ", " + addresses.get(0).getAddressLine(i);
                    }
                }
                handleLogin();
            } else {
                RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                        + String.valueOf(latitude) + "," + String.valueOf(longitude)
                        + "&key=AIzaSyCWlddxmoF2_de4khTcUe_lZeKdPbmPjMo";
                JsonObjectRequest jsonObjectRequest =
                        new JsonObjectRequest(Request.Method.POST, url, new JSONObject(),
                                new com.android.volley.Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            JSONArray results = response.getJSONArray("results");
                                            JSONObject result = results.getJSONObject(0);
                                            String[] addresses = result.getString("formatted_addresses").split(", ");
                                            List<String> locationArray = new ArrayList<>();

                                            if (addresses.length < 2) {
                                                locationArray.addAll(Arrays.asList(addresses).
                                                        subList(addresses.length - 1, addresses.length));
                                            } else if (addresses.length < 3) {
                                                locationArray.addAll(Arrays.asList(addresses).
                                                        subList(addresses.length - 2, addresses.length));
                                            } else if (addresses.length >= 3) {
                                                locationArray.addAll(Arrays.asList(addresses).
                                                        subList(addresses.length - 3, addresses.length));
                                            }

                                            location = "";

                                            for (int i = 0; i < locationArray.size(); i++) {
                                                location += locationArray.get(i);
                                                if (i != locationArray.size() - 1) {
                                                    location += ", ";
                                                }
                                            }

                                            handleLogin();
                                        } catch (JSONException e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this, "Unable to get location. Try again", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }, new com.android.volley.Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Unable to connect. Try again", Toast.LENGTH_LONG).show();
                            }
                        });
                requestQueue.add(jsonObjectRequest);
            }
        } else {
            gpsTracker.showSettingsAlert();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        LoginManager.getInstance().logOut();
        finish();
    }
}
