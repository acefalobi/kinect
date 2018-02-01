package com.kinectafrica.android.fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kinectafrica.android.R;
import com.kinectafrica.android.activity.ImageViewActivity;
import com.kinectafrica.android.activity.SettingsActivity;
import com.kinectafrica.android.adapter.recycler.ProfilePhotosRecyclerAdapter;
import com.kinectafrica.android.model.User;
import com.kinectafrica.android.utility.KinectApplication;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    ProgressDialog mProgressDialog;

    FirebaseAuth firebaseAuth;
    FirebaseStorage storage;

    StorageReference mStorageReference;
    StorageReference userPhotosReference;

    DatabaseReference databaseReference;

    ProfilePhotosRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    TextView textNameAge, textDescription, textLocation;
    CircularImageView imageView;
    Button btnSettings, btnAddPhoto;
    ProgressBar progressBar;
    LinearLayout layout;

    View fragmentView;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        storage = FirebaseStorage.getInstance();

        mStorageReference = storage.getReferenceFromUrl("gs://kinect-2c46c.appspot.com");

        userPhotosReference = mStorageReference.child("user_photos");

        // Set up ProgressDialog
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage("Uploading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        progressBar = fragmentView.findViewById(R.id.progress_profile_photos);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null)
            if (getActivity() != null) getActivity().finish();
        else {
            layout = fragmentView.findViewById(R.id.layout_no_photos);

            textNameAge = fragmentView.findViewById(R.id.text_name_age);
            textDescription = fragmentView.findViewById(R.id.text_description);
            textLocation = fragmentView.findViewById(R.id.text_location);

            btnSettings = fragmentView.findViewById(R.id.btn_settings);
            btnSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), SettingsActivity.class));
                }
            });

            btnAddPhoto = fragmentView.findViewById(R.id.btn_add_photo);
            btnAddPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPhotoChooser();
                }
            });

            imageView = fragmentView.findViewById(R.id.profile_image);

            recyclerView = fragmentView.findViewById(R.id.recycler_profile_photos);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setHasFixedSize(true);

            if (getContext() != null) {

                final SharedPreferences sharedPreferences = getContext().getSharedPreferences("currentUser", Context.MODE_PRIVATE);

                List<String> photos = Lists.newArrayList(sharedPreferences.getStringSet("photos", new HashSet<String>()));

                recyclerAdapter = new ProfilePhotosRecyclerAdapter(getActivity(), photos, firebaseAuth.getCurrentUser().getUid());
                recyclerView.setAdapter(recyclerAdapter);

                if (photos.size() <= 0)
                    layout.setVisibility(View.VISIBLE);
                else
                    layout.setVisibility(View.GONE);

                long today = System.currentTimeMillis();
                long age = today - sharedPreferences.getLong("dateOfBirth", today);
                long divider = (long) (86340862 * 365.25);
                Long ageYears = age / divider;

                textNameAge.setText(sharedPreferences.getString("displayName", "") + ", " + ageYears);
                textLocation.setText(sharedPreferences.getString("location", "Nigeria"));
                textDescription.setText(sharedPreferences.getString("description", ""));

                Glide.with(getActivity())
                        .load(sharedPreferences.getString("profilePicture", "http://showmie.com/kinect/avatar/default_avatar.png"))
                        .thumbnail(.4f).placeholder(R.drawable.empty_image).into(imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ImageViewActivity.class);
                        intent.putExtra("userId", firebaseAuth.getCurrentUser().getUid());
                        intent.putExtra("photo", sharedPreferences.getString("profilePicture",
                                "http://showmie.com/kinect/avatar/default_avatar.png"));
                        startActivity(intent);
                    }
                });

                databaseReference = FirebaseDatabase.getInstance().getReference();

                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (firebaseAuth.getCurrentUser() != null) {
                                    final User user = dataSnapshot.getValue(User.class);

                                    long today = System.currentTimeMillis();
                                    long age = today - user.getDateOfBirth();
                                    long divider = (long) (86340862 * 365.25);
                                    Long ageYears = age / divider;

                                    textNameAge.setText(user.getDisplayName() + ", " + ageYears);
                                    textLocation.setText(user.getLocation());
                                    textDescription.setText(user.getDescription());

                                    if (getContext() != null && getActivity() != null)
                                        Glide.with(getContext()).load(user.getProfilePicture()).thumbnail(.4f)
                                                .placeholder(R.drawable.empty_image).into(imageView);

                                    imageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getContext(), ImageViewActivity.class);
                                            intent.putExtra("userId", firebaseAuth.getCurrentUser().getUid());
                                            intent.putExtra("photo", user.getProfilePicture());
                                            startActivity(intent);
                                        }
                                    });

                                    if (user.getPhotos() == null)
                                        user.setPhotos(new ArrayList<String>());

                                    if (firebaseAuth.getCurrentUser() == null)
                                        getActivity().finish();
                                    else {
                                        recyclerAdapter = new ProfilePhotosRecyclerAdapter(getActivity(), user.getPhotos(),
                                                firebaseAuth.getCurrentUser().getUid());
                                        recyclerView.setAdapter(recyclerAdapter);
                                    }
                                    if (user.getPhotos().size() <= 0)
                                        layout.setVisibility(View.VISIBLE);
                                    else
                                        layout.setVisibility(View.GONE);

                                    progressBar.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        return fragmentView;
    }

    private void openPhotoChooser() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), KinectApplication.Companion.getUPLOAD_IMAGE());
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1234) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Access Granted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), KinectApplication.Companion.getUPLOAD_IMAGE());
            } else {
                Toast.makeText(getContext(), "Access Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final Uri resultUri = result.getUri();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setCancelable(false)
                        .setTitle("Add Photo")
                        .setMessage("Are you sure you want to add this photo to your photos?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mProgressDialog.show();

                                ContentResolver contentResolver = getContext().getContentResolver();
                                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

                                String type = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(resultUri));

                                StorageReference storageReference =
                                        userPhotosReference.child(String.valueOf(System.currentTimeMillis()) + "." + type);

                                uploadMedia(storageReference, resultUri);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                try {
                    throw result.getError();
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (requestCode == KinectApplication.Companion.getUPLOAD_IMAGE()) {
            if (resultCode == RESULT_OK) {
                CropImage.activity(data.getData())
                        .setAspectRatio(1, 1)
                        .start(getContext(), this);
            }
        }
    }

    private void uploadMedia(final StorageReference storageReference, Uri resultUri) {
        if (firebaseAuth.getCurrentUser() == null)
            getActivity().finish();
        else {
            UploadTask uploadTask = storageReference.putFile(resultUri);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final User user = dataSnapshot.getValue(User.class);
                                    if (user.getPhotos() == null) {
                                        user.setPhotos(new ArrayList<String>());
                                    }
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            user.getPhotos().add(uri.toString());
                                            FirebaseDatabase.getInstance().getReference().child("users")
                                                    .child(firebaseAuth.getCurrentUser().getUid())
                                                    .child("photos").setValue(user.getPhotos());
                                            mProgressDialog.dismiss();
                                            Snackbar.make(btnAddPhoto, "Photo Added Successfully!",
                                                    Snackbar.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
    }
}
