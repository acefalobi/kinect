package com.kinectafrica.android.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kinectafrica.android.R;
import com.kinectafrica.android.activity.LoginActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

    FirebaseAuth firebaseAuth;

    DatabaseReference databaseReference;

    Preference logoutPreference;
    Preference ageRangePreference;

    SharedPreferences settingsPreferences;
    SharedPreferences userPreferences;

    SwitchPreference preferenceMen;
    SwitchPreference preferenceWomen;

    EditTextPreference preferenceDisplayName;
    EditTextPreference preferenceBio;
    EditTextPreference preferenceRadius;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null)
            getActivity().finish();
        else {
            settingsPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            userPreferences = getActivity().getSharedPreferences("currentUser", Context.MODE_PRIVATE);

            logoutPreference = findPreference("pref_logout");
            ageRangePreference = findPreference("pref_age_range");

            preferenceMen = (SwitchPreference) findPreference("pref_men");
            preferenceWomen = (SwitchPreference) findPreference("pref_women");

            preferenceDisplayName = (EditTextPreference) findPreference("pref_display_name");
            preferenceBio = (EditTextPreference) findPreference("pref_bio");
            preferenceRadius = (EditTextPreference) findPreference("pref_radius");


            databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(firebaseAuth.getCurrentUser().getUid());

            logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid())
                            .child("fcmId").setValue(null);
                    firebaseAuth.signOut();
                    LoginManager.getInstance().logOut();
                    Toast.makeText(getActivity(), "Signed Out Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                }
            });


            final int[] ageMin = {settingsPreferences.getInt("ageMin", 17)};
            final int[] ageMax = {settingsPreferences.getInt("ageMax", 29)};
            ageRangePreference.setSummary("Show people within ages " + ageMin[0] + " - " + ageMax[0]);
            ageRangePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ageMin[0] = settingsPreferences.getInt("ageMin", 17);
                    ageMax[0] = settingsPreferences.getInt("ageMax", 29);
                    @SuppressLint("InflateParams")
                    final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_age_range, null);
                    RangeBar rangeBar = (RangeBar) dialogView.findViewById(R.id.range_age);
                    rangeBar.setRangePinsByValue(ageMin[0], ageMax[0]);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setView(dialogView)
                            .setCancelable(false)
                            .setTitle("Choose Age Range")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    RangeBar rangeBar = (RangeBar) dialogView.findViewById(R.id.range_age);
                                    int ageMin = Integer.parseInt(rangeBar.getLeftPinValue());
                                    int ageMax = Integer.parseInt(rangeBar.getRightPinValue());
                                    ageRangePreference.setSummary("Show people within ages " + ageMin + " - " + ageMax);
                                    SharedPreferences.Editor editor = settingsPreferences.edit();
                                    editor.putInt("ageMin", ageMin);
                                    editor.putInt("ageMax", ageMax);
                                    editor.apply();
                                    Toast.makeText(getActivity(), "Age Range Changed Successfully. Restart might be required",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                    builder.create().show();
                    return true;
                }
            });

            String interest = userPreferences.getString("interest", "male");
            if (interest.equals("male")) {
                preferenceMen.setChecked(true);
                preferenceWomen.setChecked(false);
            } else {
                preferenceMen.setChecked(false);
                preferenceWomen.setChecked(true);
            }

            preferenceMen.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals("true")) {
                        preferenceWomen.setChecked(false);
                        databaseReference.child("interest").setValue("male",
                                new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null)
                                            Toast.makeText(getActivity().getBaseContext(), databaseError.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        else {
                                            Toast.makeText(getActivity().getBaseContext(),
                                                    "Interest Changed Successfully. Restart might be required",
                                                    Toast.LENGTH_SHORT).show();
                                            SharedPreferences.Editor editor = userPreferences.edit();
                                            editor.putString("interest", "male");
                                            editor.apply();
                                        }
                                    }
                                });
                    } else {
                        preferenceWomen.setChecked(true);
                        databaseReference.child("interest").setValue("female",
                                new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null)
                                            Toast.makeText(getActivity().getBaseContext(), databaseError.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        else {
                                            Toast.makeText(getActivity().getBaseContext(),
                                                    "Interest Changed Successfully. Restart might be required",
                                                    Toast.LENGTH_SHORT).show();
                                            SharedPreferences.Editor editor = userPreferences.edit();
                                            editor.putString("interest", "female");
                                            editor.apply();
                                        }
                                    }
                                });
                    }
                    return true;
                }
            });

            preferenceWomen.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals("false")) {
                        preferenceMen.setChecked(true);
                        databaseReference.child("interest").setValue("male",
                                new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null)
                                            Toast.makeText(getActivity().getBaseContext(), databaseError.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        else {
                                            Toast.makeText(getActivity().getBaseContext(),
                                                    "Interest Changed Successfully. Restart might be required",
                                                    Toast.LENGTH_SHORT).show();
                                            SharedPreferences.Editor editor = userPreferences.edit();
                                            editor.putString("interest", "male");
                                            editor.apply();
                                        }
                                    }
                                });
                    } else {
                        preferenceMen.setChecked(false);
                        databaseReference.child("interest").setValue("female",
                                new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null)
                                            Toast.makeText(getActivity().getBaseContext(), databaseError.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        else {
                                            Toast.makeText(getActivity().getBaseContext(),
                                                    "Interest Changed Successfully. Restart might be required",
                                                    Toast.LENGTH_SHORT).show();
                                            SharedPreferences.Editor editor = userPreferences.edit();
                                            editor.putString("interest", "female");
                                            editor.apply();
                                        }
                                    }
                                });
                    }
                    return true;
                }
            });

            String displayName = userPreferences.getString("displayName", "");
            preferenceDisplayName.setSummary(displayName);
            preferenceDisplayName.setText(displayName);
            preferenceDisplayName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, final Object newValue) {
                    databaseReference.child("displayName").setValue(newValue.toString(),
                            new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null)
                                        Toast.makeText(getActivity().getBaseContext(),
                                                databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    else {
                                        Toast.makeText(getActivity().getBaseContext(), "Display Name Changed Successfully",
                                                Toast.LENGTH_SHORT).show();
                                        preferenceDisplayName.setText(newValue.toString());
                                        preferenceDisplayName.setSummary(newValue.toString());
                                        SharedPreferences.Editor editor = userPreferences.edit();
                                        editor.putString("displayName", newValue.toString());
                                        editor.apply();
                                    }
                                }
                            });
                    return true;
                }
            });

            String bio = userPreferences.getString("description", "");
            preferenceBio.setSummary(bio);
            preferenceBio.setText(bio);
            preferenceBio.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, final Object newValue) {
                    databaseReference.child("description").setValue(newValue.toString(),
                            new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null)
                                        Toast.makeText(getActivity().getBaseContext(),
                                                databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    else {
                                        Toast.makeText(getActivity().getBaseContext(),
                                                "Bio Changed Successfully", Toast.LENGTH_SHORT).show();
                                        preferenceBio.setText(newValue.toString());
                                        preferenceBio.setSummary(newValue.toString());
                                        SharedPreferences.Editor editor = userPreferences.edit();
                                        editor.putString("description", newValue.toString());
                                        editor.apply();
                                    }
                                }
                            });
                    return true;
                }
            });

            int radius = settingsPreferences.getInt("radius", 80);
            preferenceRadius.setSummary("Show people within " + String.valueOf(radius) + "km from me");
            preferenceRadius.setText(String.valueOf(radius));
            preferenceRadius.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (Integer.parseInt(newValue.toString()) >= 1 && Integer.parseInt(newValue.toString()) <= 160) {
                        preferenceRadius.setText(newValue.toString());
                        preferenceRadius.setSummary("Show people within " + newValue.toString() + "km from me");
                        SharedPreferences.Editor editor = settingsPreferences.edit();
                        editor.putInt("radius", Integer.parseInt(newValue.toString()));
                        editor.apply();
                        Toast.makeText(getActivity().getBaseContext(), "Radius Changed Successfully. Restart might be required",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity().getBaseContext(),
                                "Distance must be between 1km to 160km", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
    }
}
