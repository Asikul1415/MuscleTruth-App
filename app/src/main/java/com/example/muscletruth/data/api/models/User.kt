package com.example.muscletruth.data.api.models
import com.google.gson.annotations.SerializedName

class User {
    data class UserCreate(
        @SerializedName("name") val name: String,
        @SerializedName("email") val email: String,
        @SerializedName("password") val password: String,
        @SerializedName("age") val age: Int,
        @SerializedName("profile_picture") val profilePicture: String? = null
    )

    data class UserResponse(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("email") val email: String,
        @SerializedName("age") val age: Int,
        @SerializedName("profile_picture") val profilePicture: String? = null
    )

    data class UserLogin(
        @SerializedName("email") val email: String,
        @SerializedName("password") val password: String
    )

    data class LoginResponse(
        @SerializedName("access_token") val accessToken: String,
        @SerializedName("token_type") val tokenType: String = "bearer",
        @SerializedName("user") val user: UserResponse
    )
}