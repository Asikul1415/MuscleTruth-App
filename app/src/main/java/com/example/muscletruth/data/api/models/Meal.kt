package com.example.muscletruth.data.api.models

import com.example.muscletruth.data.repository.UserRepository
import com.google.gson.annotations.SerializedName

class Meal {
    data class MealBase(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("user_id") val userID: Int? = null,
        @SerializedName("meal_type_id") val mealTypeID: Int,
        @SerializedName("picture") val picture: String? = null,
        @SerializedName("creation_date") val creationDate: String? = null
    )

    data class MealResponse(
        @SerializedName("id") val id: Int
    )

    class MealItem(
        val id: Int,
        var isExpanded: Boolean = true,
        var servings: List<Serving.ServingItem> = emptyList<Serving.ServingItem>(),
        var mealTypeID: Int,
        val picture: String? = null,
        val creationDate: String? = null,
    )
}