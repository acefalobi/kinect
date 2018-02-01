package com.kinectafrica.android.utility

import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.database.FirebaseDatabase
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import io.fabric.sdk.android.Fabric
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

/**
 * Made by acefalobi on 4/3/2017.
 */

class KinectApplication : MultiDexApplication() {

    var kinectLike = false

    override fun onCreate() {
        super.onCreate()
        val authConfig = TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET)
        Fabric.with(this, Twitter(authConfig), Crashlytics())
        instance = this
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.keepSynced(true)

        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(uk.co.chrisjenx.calligraphy.R.attr.fontPath).build())
    }

    companion object {

        // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
        private val TWITTER_KEY = "N4KGbSd4qSa7FZIIJsyFuSmlH"
        private val TWITTER_SECRET = "OR80qqtvNADihfDVA160DrvC3PlPxphpNAbwY41AxterAEY2RX"

        val UPLOAD_IMAGE = 1000

        @get:Synchronized
        var instance: KinectApplication? = null
            private set
    }
}
