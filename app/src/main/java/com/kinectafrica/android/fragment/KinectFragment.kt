package com.kinectafrica.android.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.google.common.collect.Lists
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kinectafrica.android.R
import com.kinectafrica.android.model.User
import com.kinectafrica.android.utility.KinectApplication
import com.kinectafrica.android.utility.Utils
import com.kinectafrica.android.view.KinectCard
import com.mindorks.placeholderview.SwipeDecor
import com.mindorks.placeholderview.SwipePlaceHolderView
import java.util.*

class KinectFragment : Fragment() {

    internal var sharedPreferences: SharedPreferences? = null
    private var settingsPreferences: SharedPreferences? = null

    private var databaseReference: DatabaseReference? = null

    internal var firebaseAuth: FirebaseAuth? = null

    internal var swipePlaceHolderView: SwipePlaceHolderView? = null

    private var btnNo: FloatingActionButton? = null
    private var btnYes: FloatingActionButton? = null
    private var btnKinect: FloatingActionButton? = null

    internal var layoutProgress: LinearLayout? = null
    internal var layoutError: LinearLayout? = null

    private var fragmentView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_kinect, container, false)

        databaseReference = FirebaseDatabase.getInstance().reference

        firebaseAuth = FirebaseAuth.getInstance()

        layoutProgress = fragmentView!!.findViewById(R.id.layout_progress_kinect)
        layoutError = fragmentView!!.findViewById(R.id.layout_no_users)

        sharedPreferences = context!!.getSharedPreferences("currentUser", Context.MODE_PRIVATE)
        sharedPreferences!!.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "interest")
                getUsers()
        }

        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        settingsPreferences!!.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "range" || key == "ageMin" || key == "ageMax")
                getUsers()
        }

        swipePlaceHolderView = fragmentView!!.findViewById(R.id.swipe_view) as SwipePlaceHolderView

        val windowSize = Utils.getDisplaySize(activity!!.windowManager)

        swipePlaceHolderView!!.builder
                .setDisplayViewCount(3)
                .setSwipeDecor(SwipeDecor()
                        .setViewWidth(windowSize.x)
                        .setViewHeight(windowSize.y - 140)
                        .setViewGravity(Gravity.TOP)
                        .setPaddingTop(20)
                        .setRelativeScale(.01f)
                        .setSwipeInMsgLayoutId(R.layout.swipe_message_yes)
                        .setSwipeOutMsgLayoutId(R.layout.swipe_message_no))

        btnNo = fragmentView!!.findViewById(R.id.btn_no) as FloatingActionButton
        btnNo!!.setOnClickListener { swipePlaceHolderView!!.doSwipe(false) }

        btnYes = fragmentView!!.findViewById(R.id.btn_yes) as FloatingActionButton
        btnYes!!.setOnClickListener { swipePlaceHolderView!!.doSwipe(true) }

        btnKinect = fragmentView!!.findViewById(R.id.btn_kinect) as FloatingActionButton
        btnKinect!!.setOnClickListener {
            if (sharedPreferences!!.getInt("kinectLeft", 0) <= 0) {
                val builder = AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("Out Of Kinects!")
                        .setMessage("Sorry, but you've used up all of your kinects for today")
                        .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
                val alertDialog = builder.create()
                alertDialog.show()
            } else {
                val editor = sharedPreferences!!.edit()
                editor.putInt("kinectLeft", sharedPreferences!!.getInt("kinectLeft", 0) - 1)
                editor.apply()
                KinectApplication.instance!!.kinectLike = true
                swipePlaceHolderView!!.doSwipe(true)
            }
        }

        getUsers()

        return fragmentView
    }

    override fun onResume() {
        super.onResume()
        if (activity!!.intent.getBooleanExtra("refresh", false)) {
            getUsers()
        }
    }

    private fun getUsers() {
        val radius = settingsPreferences!!.getInt("radius", 80)

        val ageMin = settingsPreferences!!.getInt("ageMin", 17)
        val ageMax = settingsPreferences!!.getInt("ageMax", 29)

        swipePlaceHolderView!!.removeAllViews()

        layoutProgress!!.visibility = View.VISIBLE
        layoutError!!.visibility = View.GONE

        databaseReference!!.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val snapshots = Lists.newArrayList(dataSnapshot.children)

                Collections.shuffle(snapshots)

                val users = ArrayList<String>()

                for (child in snapshots) {
                    if (firebaseAuth!!.currentUser == null)
                        activity!!.finish()
                    else {
                        if (child.key != firebaseAuth!!.currentUser!!.uid) {
                            val user = child.getValue(User::class.java)

                            val yesUsers: List<String>
                            yesUsers = Lists.newArrayList(sharedPreferences!!.getStringSet("yesUsers", HashSet()))
                            val hasYes = yesUsers.any { it == child.key }

                            val today = System.currentTimeMillis()
                            val age = today - user!!.dateOfBirth
                            val divider = (86340862 * 365.25).toLong()
                            val ageYears = age / divider

                            val distance = Utils.getLongLatDistance(user.latitude, user.longitude,
                                    sharedPreferences!!.getFloat("latitude", 0.0.toFloat()).toDouble(),
                                    sharedPreferences!!.getFloat("longitude", 0.0.toFloat()).toDouble())
                            if (distance < radius && user.gender == sharedPreferences!!.getString("interest", "")
                                    && user.interest == sharedPreferences!!.getString("gender", "") && !hasYes
                                    && ageYears > ageMin && ageYears < ageMax) {
                                layoutProgress!!.visibility = View.GONE
                                users.add(child.key)
                                swipePlaceHolderView!!.addView(KinectCard(context!!, user,
                                        child.ref, swipePlaceHolderView!!))
                            }
                        }
                    }
                }

                if (users.size <= 0) {
                    layoutProgress!!.visibility = View.GONE
                    layoutError!!.visibility = View.VISIBLE
                } else
                    layoutError!!.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(activity, databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })

    }
}// Required empty public constructor
