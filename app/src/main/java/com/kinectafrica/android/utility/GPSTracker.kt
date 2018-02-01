package com.kinectafrica.android.utility

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.widget.Toast

/**
 * Made by acefalobi on 4/4/2017.
 */

class GPSTracker(private val context: Context) : Service(), LocationListener {

    private var isGPS = false
    private var isNetwork = false
    private var canGetLocation = false

    private var locationManager: LocationManager? = null

    private var location: Location? = null

    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    init {
        getLocation()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onLocationChanged(location: Location) {

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }
        return longitude
    }

    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }
        return latitude
    }

    fun canGetLocation(): Boolean = this.canGetLocation

    private fun getLocation(): Location? {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGPS = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetwork = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPS && !isNetwork) {
                Toast.makeText(context, "No Service Provider Available", Toast.LENGTH_SHORT).show()
            } else {
                canGetLocation = true
                if (isNetwork) {
                    try {
                        locationManager!!.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                        if (locationManager != null) {
                            location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        }

                        if (location != null) {
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    } catch (e: SecurityException) {
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }

                }
                if (isGPS) {
                    try {
                        locationManager!!.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                        if (locationManager != null) {
                            location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        }

                        if (location != null) {
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    } catch (e: SecurityException) {
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return location
    }

    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(context)


        alertDialog.setTitle("GPS Not Enabled")

        alertDialog.setMessage("Do you wants to turn On GPS")


        alertDialog.setPositiveButton("Yes") { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }


        alertDialog.setNegativeButton("No") { dialog, _ -> dialog.cancel() }


        alertDialog.show()
    }

    fun stopUsingGPS() {
        if (locationManager != null) {

            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }

    companion object {

        private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10

        private val MIN_TIME_BW_UPDATES = (1000 * 60).toLong()
    }

}
