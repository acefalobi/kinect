package com.kinectafrica.android.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log

import com.google.firebase.messaging.RemoteMessage
import com.kinectafrica.android.R
import com.kinectafrica.android.activity.ChatActivity
import com.kinectafrica.android.activity.MainActivity
import com.kinectafrica.android.utility.Utils

/**
 * Made by acefalobi on 5/16/2017.
 */

class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {

    private var notifyId = 0
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.d("Service", "From: " + remoteMessage!!.from)

        if (remoteMessage.data.isNotEmpty()) {
            Log.d("Service", "Message data payload: " + remoteMessage.data)
        }

        if (remoteMessage.notification != null) {
            Log.d("Service", "Message notification: " + remoteMessage.notification!!.body!!)
        }
        sendNotification(remoteMessage.data!!["message"],
                remoteMessage.data!!["action"], remoteMessage.data!!["id"])
    }

    private fun sendNotification(message: String?, action: String?, id: String?) {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle("Kinect")
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setWhen(System.currentTimeMillis())
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(uri)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setVibrate(longArrayOf(100, 500, 100, 500, 100))

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        when (action) {
            "chat" -> if (!Utils.isRunning(this, ChatActivity::class.java.`package`.name)) {
                intent.putExtra("action", action)
                intent.putExtra("id", id)
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                builder.setContentIntent(pendingIntent)

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notifyId, builder.build())
                notifyId++
            } else {
                RingtoneManager.getRingtone(this, uri).play()
            }
            else -> {
                intent.putExtra("action", action)
                intent.putExtra("id", id)
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                builder.setContentIntent(pendingIntent)

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notifyId, builder.build())
                notifyId++
            }
        }
    }
}
