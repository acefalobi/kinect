package com.kinectafrica.android.utility

import android.animation.Animator
import android.app.ActivityManager
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Made by acefalobi on 4/1/2017.
 */

object Utils {

    private val RADIUS_OF_EARTH = 6367.0

    fun isRunning(ctx: Context, packageName: String): Boolean {
        val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(Integer.MAX_VALUE)

        return tasks.any { packageName.equals(it.baseActivity.packageName, ignoreCase = true) }
    }

    fun covertMonthToString(month: Int): String = when (month) {
        1 -> "JANUARY"
        2 -> "FEBRUARY"
        3 -> "MARCH"
        4 -> "APRIL"
        5 -> "MAY"
        6 -> "JUNE"
        7 -> "JULY"
        8 -> "AUGUST"
        9 -> "SEPTEMBER"
        10 -> "OCTOBER"
        11 -> "NOVEMBER"
        12 -> "DECEMBER"
        else -> ""
    }

    fun sendFCM(context: Context, fcmId: String, message: String, action: String, id: String) {
        val requestQueue = Volley.newRequestQueue(context)
        val url = "https://fcm.googleapis.com/fcm/send"

        try {
            val jsonText = ("{\"to\": \"" + fcmId + "\"," +
                    "\"data\": {\"message\": \"" + message + "\", \"action\": \""
                    + action + "\", \"id\": \"" + id + "\"}" + "}")
            val jsonObject = JSONObject(jsonText)
            val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    Response.Listener { }, Response.ErrorListener { error -> Toast.makeText(context, "Error: " + error.message, Toast.LENGTH_LONG).show() }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("Content-Type", "application/json")
                    params.put("Authorization", "key=AIzaSyDq4IrnMOyqbVPb7g9099wXzCdnar3_giw")

                    return params
                }
            }
            requestQueue.add(jsonObjectRequest)
        } catch (e: JSONException) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }

    }

    fun getDisplaySize(windowManager: WindowManager): Point {
        return try {
            if (Build.VERSION.SDK_INT > 16) {
                val display = windowManager.defaultDisplay
                val displayMetrics = DisplayMetrics()
                display.getMetrics(displayMetrics)
                Point(displayMetrics.widthPixels, displayMetrics.heightPixels)
            } else {
                Point(0, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Point(0, 0)
        }

    }

    fun fadeBackground(view: View, duration: Long) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate().alpha(1f).setDuration(duration).setListener(null)
    }

    fun fadeIn(view: View, duration: Long) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate().alpha(1f).setDuration(duration).setListener(null)
    }

    fun fadeOut(view: View, duration: Long) {
        view.animate().alpha(0f).setDuration(duration).setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
    }

    private fun degreeToRadian(degrees: Double): Double = degrees * (Math.PI / 180)

    fun getLongLatDistance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val latitudeDistance = degreeToRadian(lat2 - lat1)
        val longitudeDistance = degreeToRadian(long2 - long1)

        val a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2) + (Math.cos(degreeToRadian(lat1)) * Math.cos(degreeToRadian(lat2))
                * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return RADIUS_OF_EARTH * c
    }
}
