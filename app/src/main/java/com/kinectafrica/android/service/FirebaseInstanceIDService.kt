package com.kinectafrica.android.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Made by acefalobi on 5/16/2017.
 */

class FirebaseInstanceIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        sendRegistrationToServer(refreshedToken)
    }

    private fun sendRegistrationToServer(refreshedToken: String?) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser.uid).child("fcmId")
                    .setValue(refreshedToken)
        }
    }
}
