package com.example.muscletruth.data.serviceClasses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MealItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("localID")
    val localID: String? = null,

    @SerializedName("isExpanded")
    var isExpanded: Boolean = true,

    @SerializedName("servings")
    var servings: List<ServingItem> = emptyList<ServingItem>(),

    @SerializedName("mealTypeID")
    var mealTypeID: Int,

    @SerializedName("picture")
    val picture: String? = null,

    @SerializedName("creationDate")
    val creationDate: String? = null,
): Parcelable