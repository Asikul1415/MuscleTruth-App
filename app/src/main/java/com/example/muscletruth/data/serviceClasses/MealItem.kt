package com.example.muscletruth.data.serviceClasses

import android.os.Parcelable
import com.example.muscletruth.data.models.Serving
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
    var servings: List<Serving> = emptyList<Serving>(),

    @SerializedName("mealTypeID")
    var mealTypeID: Int,

    @SerializedName("picture")
    val picture: String? = null,

    @SerializedName("creationDate")
    val creationDate: String? = null,

    val serverOriginMealID: Int? = null,
    val localOriginMealID: String? = null,
): Parcelable