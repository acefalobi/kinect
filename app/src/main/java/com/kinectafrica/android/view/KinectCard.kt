package com.kinectafrica.android.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kinectafrica.android.R
import com.kinectafrica.android.activity.ProfileActivity
import com.kinectafrica.android.model.User
import com.kinectafrica.android.utility.KinectApplication
import com.kinectafrica.android.utility.Utils
import com.mindorks.placeholderview.SwipePlaceHolderView
import com.mindorks.placeholderview.annotations.Layout
import com.mindorks.placeholderview.annotations.Resolve
import com.mindorks.placeholderview.annotations.View
import com.mindorks.placeholderview.annotations.swipe.*
import java.text.DecimalFormat
import java.util.*

/**
 * Made by acefalobi on 4/1/2017.
 */

@Layout(R.layout.item_swipe_card)
class KinectCard(private val context: Context, private val user: User, private val userReference: DatabaseReference, private val swipePlaceHolderView: SwipePlaceHolderView) {

    @View(R.id.kinect_profile_image)
    private val kinectProfileImageView: ImageView? = null

    @View(R.id.progress_kinect_card)
    private val progressBar: ProgressBar? = null

    @View(R.id.text_name_age)
    private val textNameAge: TextView? = null

    @View(R.id.text_location)
    private val textLocation: TextView? = null

    @View(R.id.text_description)
    private val textDescription: TextView? = null

    @Resolve
    private fun onResolved() {
        Glide.with(context).load(user.profilePicture).into(object : GlideDrawableImageViewTarget(kinectProfileImageView!!) {
            override fun onResourceReady(resource: GlideDrawable, animation: GlideAnimation<in GlideDrawable>?) {
                super.onResourceReady(resource, animation)
                progressBar!!.visibility = android.view.View.GONE
            }
        })
        kinectProfileImageView.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("userId", userReference.key)
            context.startActivity(intent)
        }

        val today = System.currentTimeMillis()
        val age = today - user.dateOfBirth
        val divider = (86340862 * 365.25).toLong()
        val ageYears = age / divider

        val distance = Utils.getLongLatDistance(user.latitude, user.longitude,
                context.getSharedPreferences("currentUser", Context.MODE_PRIVATE).getFloat("latitude", 0.0.toFloat()).toDouble(),
                context.getSharedPreferences("currentUser", Context.MODE_PRIVATE).getFloat("longitude", 0.0.toFloat()).toDouble())

        val decimalFormat = DecimalFormat("#.#")

        textNameAge!!.text = user.displayName + ", " + ageYears
        textLocation!!.text = user.location + " | " + decimalFormat.format(distance).toString() + "km away"
        textDescription!!.text = user.description
    }

    @SwipeOut
    private fun onSwipedOut() {
        val sharedPreferences = context.getSharedPreferences("currentUser", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("isFirstNo", true)) {
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstNo", false)
            editor.apply()
            val builder = AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("Don't like " + user.displayName + "?")
                    .setMessage("A right swipe means you are not interested")
                    .setPositiveButton("OK") { _, _ -> swipePlaceHolderView.addView(this@KinectCard) }
            val alertDialog = builder.create()
            alertDialog.show()
        } else
            swipePlaceHolderView.addView(this@KinectCard)
    }

    @SwipeCancelState
    private fun onSwipeCancelState() {

    }

    @SwipeIn
    private fun onSwipeIn() {
        Log.d("EVENT", "onSwipedIn")

        val sharedPreferences = context.getSharedPreferences("currentUser", Context.MODE_PRIVATE)

        if (KinectApplication.instance!!.kinectLike) {
            if (sharedPreferences.getBoolean("isFirstKinect", true)) {
                val editor = sharedPreferences.edit()
                editor.putBoolean("isFirstKinect", false)
                editor.apply()
                val builder = AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("Get Kinectic with " + user.displayName + "?")
                        .setMessage("This means you want " + user.displayName + " to be notified that you're interested")
                        .setPositiveButton("OK") { _, _ ->
                            val databaseReference = FirebaseDatabase.getInstance().reference
                            val firebaseAuth = FirebaseAuth.getInstance()

                            if (firebaseAuth.currentUser != null) {
                                databaseReference.child("users").child(firebaseAuth.currentUser!!.uid)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                val userValues = dataSnapshot.getValue(User::class.java)!!.toMap()
                                                val yesUsers: MutableList<String> = if (dataSnapshot.getValue(User::class.java)!!.yesUsers != null) {
                                                    dataSnapshot.getValue(User::class.java)!!.yesUsers as MutableList<String>
                                                } else {
                                                    ArrayList()
                                                }
                                                yesUsers.add(userReference.key)
                                                userValues["yesUsers"] = yesUsers

                                                val yesUsersSet: Set<String>
                                                yesUsersSet = HashSet(yesUsers)
                                                editor.putStringSet("yesUsers", yesUsersSet)
                                                editor.apply()

                                                dataSnapshot.ref.updateChildren(userValues)

                                                Utils.sendFCM(context, user.fcmId,
                                                        user.displayName + " wants to get kinectic you",
                                                        "profile", userReference.key)
                                                KinectApplication.instance!!.kinectLike = false
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {

                                            }
                                        })
                            }
                        }
                val alertDialog = builder.create()
                alertDialog.show()
            } else {
                val databaseReference = FirebaseDatabase.getInstance().reference
                val firebaseAuth = FirebaseAuth.getInstance()

                if (firebaseAuth.currentUser != null) {
                    databaseReference.child("users").child(firebaseAuth.currentUser!!.uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val userValues = dataSnapshot.getValue(User::class.java)!!.toMap()

                                    val yesUsers: MutableList<String> = if (dataSnapshot.getValue(User::class.java)!!.yesUsers != null) {
                                        dataSnapshot.getValue(User::class.java)!!.yesUsers as MutableList<String>
                                    } else {
                                        ArrayList()
                                    }
                                    yesUsers.add(userReference.key)
                                    userValues["yesUsers"] = yesUsers

                                    val editor = sharedPreferences.edit()
                                    val yesUsersSet: Set<String>
                                    yesUsersSet = HashSet(yesUsers)
                                    editor.putStringSet("yesUsers", yesUsersSet)
                                    editor.apply()

                                    dataSnapshot.ref.updateChildren(userValues)
                                    Utils.sendFCM(context, user.fcmId,
                                            user.displayName + " wants to get kinectic you",
                                            "profile", userReference.key)
                                    KinectApplication.instance!!.kinectLike = false
                                }

                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            })
                }
            }
        } else {
            if (sharedPreferences.getBoolean("isFirstYes", true)) {
                val editor = sharedPreferences.edit()
                editor.putBoolean("isFirstYes", false)
                editor.apply()
                val builder = AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("Like " + user.displayName + "?")
                        .setMessage("A left swipe means you are interested")
                        .setPositiveButton("OK") { _, _ ->
                            val databaseReference = FirebaseDatabase.getInstance().reference
                            val firebaseAuth = FirebaseAuth.getInstance()

                            if (firebaseAuth.currentUser != null) {
                                databaseReference.child("users").child(firebaseAuth.currentUser!!.uid)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                val userValues = dataSnapshot.getValue(User::class.java)!!.toMap()

                                                val yesUsers: MutableList<String> = if (dataSnapshot.getValue(User::class.java)!!.yesUsers != null) {
                                                    dataSnapshot.getValue(User::class.java)!!.yesUsers as MutableList<String>
                                                } else {
                                                    ArrayList()
                                                }
                                                yesUsers.add(userReference.key)
                                                userValues["yesUsers"] = yesUsers

                                                val yesUsersSet: Set<String>
                                                yesUsersSet = HashSet(yesUsers)
                                                editor.putStringSet("yesUsers", yesUsersSet)
                                                editor.apply()

                                                dataSnapshot.ref.updateChildren(userValues)
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {

                                            }
                                        })
                            }
                        }
                val alertDialog = builder.create()
                alertDialog.show()
            } else {
                val databaseReference = FirebaseDatabase.getInstance().reference
                val firebaseAuth = FirebaseAuth.getInstance()

                if (firebaseAuth.currentUser != null) {
                    databaseReference.child("users").child(firebaseAuth.currentUser!!.uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val userValues = dataSnapshot.getValue(User::class.java)!!.toMap()

                                    val yesUsers: MutableList<String> = if (dataSnapshot.getValue(User::class.java)!!.yesUsers != null) {
                                        dataSnapshot.getValue(User::class.java)!!.yesUsers as MutableList<String>
                                    } else {
                                        ArrayList()
                                    }
                                    yesUsers.add(userReference.key)
                                    userValues["yesUsers"] = yesUsers

                                    val editor = sharedPreferences.edit()
                                    val yesUsersSet: Set<String>
                                    yesUsersSet = HashSet(yesUsers)
                                    editor.putStringSet("yesUsers", yesUsersSet)
                                    editor.apply()

                                    dataSnapshot.ref.updateChildren(userValues)
                                }

                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            })
                }
            }
        }
    }

    @SwipeInState
    private fun onSwipeInState() {
        Log.d("EVENT", "onSwipeInState")
    }

    @SwipeOutState
    private fun onSwipeOutState() {
        Log.d("EVENT", "onSwipeOutState")
    }
}
