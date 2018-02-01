package com.kinectafrica.android.model

import com.google.firebase.database.Exclude

import java.util.HashMap

/**
 * Made by acefalobi on 4/1/2017.
 */

class User {
    var displayName: String = ""
    var email: String = ""
    var gender: String = ""
    var dateOfBirth: Long = 0
    var interest: String = ""
    var location: String = ""
    var longitude: Double = 0.toDouble()
    var latitude: Double = 0.toDouble()
    var profilePicture: String = ""
    var photos: List<String>? = null
    var description: String = ""

    var yesUsers: List<String>? = null
    private var noUsers: List<String>? = null

    var matchedUsers: List<String>? = null
    var fcmId = ""

    constructor()

    constructor(displayName: String, email: String, gender: String, dateOfBirth: Long, interest: String, location: String,
                longitude: Double, latitude: Double, profilePicture: String, description: String, yesUsers: List<String>,
                matchedUsers: List<String>, photos: List<String>) {
        this.displayName = displayName
        this.email = email
        this.gender = gender
        this.dateOfBirth = dateOfBirth
        this.interest = interest
        this.location = location
        this.longitude = longitude
        this.latitude = latitude
        this.profilePicture = profilePicture
        this.description = description
        this.yesUsers = yesUsers
        this.matchedUsers = matchedUsers
        this.photos = photos
    }

    constructor(displayName: String, email: String, gender: String, dateOfBirth: Long, interest: String, location: String,
                longitude: Double, latitude: Double, profilePicture: String, description: String) {
        this.displayName = displayName
        this.email = email
        this.gender = gender
        this.dateOfBirth = dateOfBirth
        this.interest = interest
        this.location = location
        this.longitude = longitude
        this.latitude = latitude
        this.profilePicture = profilePicture
        this.description = description
    }

    @Exclude
    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["email"] = email
        result["displayName"] = displayName
        result["gender"] = gender
        result["profilePicture"] = profilePicture
        result["interest"] = interest
        result["location"] = location
        result["longitude"] = longitude
        result["latitude"] = latitude
        result["dateOfBirth"] = dateOfBirth
        result["description"] = description
        yesUsers?.let { result.put("yesUsers", it) }
        noUsers?.let { result.put("noUsers", it) }
        matchedUsers?.let { result.put("matchedUsers", it) }
        photos?.let { result.put("photos", it) }
        fcmId.let { result.put("fcmId", it) }

        return result
    }
}
