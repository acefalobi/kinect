package com.kinectafrica.android.utility

import com.twitter.sdk.android.core.TwitterApiClient
import com.twitter.sdk.android.core.TwitterSession

/**
 * Made by acefalobi on 4/3/2017.
 */

class CustomTwitterApiClient(session: TwitterSession) : TwitterApiClient(session) {

    val usersService: UsersService
        get() = getService(UsersService::class.java)
}

