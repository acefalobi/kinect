package com.kinectafrica.android.utility

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.kinectafrica.android.R
import com.kinectafrica.android.activity.MainActivity

/**
 * Made by acefalobi on 5/21/2017.
 */

class KinectReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferences = context.getSharedPreferences("currentUser", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("kinectLeft", 5)
        editor.apply()


        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle("Kinect")
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setWhen(System.currentTimeMillis())
                .setContentText("Your Kinects have been refilled. Now get to swiping")
                .setAutoCancel(true)
                .setSound(uri)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("Your Kinects and Likes have been refilled. Now get to swiping"))
                .setVibrate(longArrayOf(100, 500, 100, 500, 100))

        val targetIntent = Intent(context, MainActivity::class.java)
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        targetIntent.putExtra("action", "main")
        targetIntent.putExtra("id", "")
        val pendingIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_ONE_SHOT)
        builder.setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build())
    }
}
