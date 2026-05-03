package com.example.muscletruth.data.models

import android.os.Parcelable
import androidx.room.*
import java.util.UUID
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity("users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "local_id")
    var localID: String = UUID.randomUUID().toString(),

    @SerializedName("id")
    @ColumnInfo(name = "server_id")
    var serverID: Int = -1,

    @SerializedName("name")
    @ColumnInfo("name")
    var name: String,

    @SerializedName("email")
    @ColumnInfo("email")
    var email: String,

    @SerializedName("password")
    @ColumnInfo("password")
    var password: String,

    @SerializedName("age")
    @ColumnInfo("age")
    var age: Int,

    @SerializedName("profile_picture")
    @ColumnInfo("server_picture")
    var serverPicture: String? = null,

    @ColumnInfo("local_picture")
    var localPicture: String? = null,
): Parcelable

@Parcelize
data class UserUpdate(
    @SerializedName("name")
    var name: String? = null,

    @SerializedName("email")
    var email: String? = null,

    @SerializedName("password")
    var password: String? = null,

    @SerializedName("age")
    var age: Int? = null,

    @SerializedName("profile_picture")
    var serverPicture: String? = null,
): Parcelable