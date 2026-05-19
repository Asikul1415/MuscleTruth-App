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
    var creationDate: String? = null,

    @SerializedName("origin_meal_id")
    @ColumnInfo("server_origin_meal_id")
    var serverOriginMealID: Int? = null,

    @ColumnInfo("local_origin_meal_id")
    var localOriginMealID: String? = null,

    @ColumnInfo("was_updated")
    var wasUpdated: Int = 0,
): Parcelable

@Parcelize
@Entity("saved_meals")
data class SavedMeal(
    @SerializedName("title")
    @ColumnInfo(name = "title")
    var title: String,

    @SerializedName("meal_id")
    @ColumnInfo(name = "meal_server_id")
    var mealServerID: Int = -1,

    @PrimaryKey
    @ColumnInfo(name = "meal_local_id")
    var mealLocalID: String,

    @SerializedName("user_id")
    @ColumnInfo("user_id")
    var userID: Int? = null,

    var totalProteins: Double = 0.00,
    var totalFats: Double = 0.00,
    var totalCarbs: Double = 0.00,
    var totalCalories: Double = 0.00,
    var localImage: String? = null,
    var serverImage: String? = null
): Parcelable