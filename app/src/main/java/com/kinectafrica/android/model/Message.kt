package com.kinectafrica.android.model

import com.google.firebase.database.ServerValue

/**
 * Made by acefalobi on 4/10/2017.
 */

class Message {
    var userId: String = ""
    var message: String = ""

    constructor()

    constructor(userId: String, message: String) {
        this.userId = userId
        this.message = message
        this.timeSent = ServerValue.TIMESTAMP
    }

    var timeSent: Any? = null

}
