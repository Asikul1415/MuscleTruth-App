package com.example.muscletruth.data.serviceClasses

import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String = "bearer",
    @SerializedName("user_id") val userID: Int
)