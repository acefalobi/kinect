package com.kinectafrica.android.utility

import com.twitter.sdk.android.core.models.User

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UsersService {
    @GET("/1.1/users/show.json")
    fun show(@Query("user_id") userId: Long?,
             @Query("screen_name") screenName: String,
             @Query("include_entities") includeEntities: Boolean?): Call<User>
}
