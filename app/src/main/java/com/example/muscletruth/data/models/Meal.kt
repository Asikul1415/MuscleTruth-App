package com.example.muscletruth.data.models

import android.os.Parcelable
import androidx.room.*
import java.util.UUID
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity("meals")
data class Meal(
    @PrimaryKey
    @ColumnInfo(name = "local_id")
    var localID: String = UUID.randomUUID().toString(),

    @SerializedName("id")
    @ColumnInfo(name = "server_id")
    var serverID: Int = -1,

    @SerializedName("user_id")
    @ColumnInfo("user_id")
    var userID: Int? = null,

    @SerializedName("meal_type_id")
    @ColumnInfo(name = "meal_type_id")
    var mealTypeID: Int,

    @SerializedName("picture")
    @ColumnInfo("server_picture")
    var serverPicture: String? = null,

    @ColumnInfo("local_picture")
    var localPicture: String? = null,

    @SerializedName("creation_date")
    @ColumnInfo("creation_date")
    var creationDate: String? = null
): Parcelable