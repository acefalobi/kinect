package com.kinectafrica.android.model

import com.google.firebase.database.Exclude

import java.util.HashMap

/**
 * Made by acefalobi on 4/10/2017.
 */

class MessageThread {

    var userId1: String = ""
    var userId2: String = ""
    var messages: List<Message>? = null

    constructor()

    constructor(userId1: String, userId2: String, messages: List<Message>) {
        this.userId1 = userId1
        this.userId2 = userId2
        this.messages = messages
    }

    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["userId1"] = userId1
        result["userId2"] = userId2
        result["messages"] = messages!!

        return result
    }
}
