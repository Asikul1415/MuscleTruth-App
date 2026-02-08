package com.example.muscletruth.data.api.models

import com.google.gson.annotations.SerializedName

class MealType {
    data class MealTypeBase(
        @SerializedName("id") val id: Int,
        @SerializedName("title") val title: String?,
        @SerializedName("proteins") val proteins: Float?,
        @SerializedName("fats") val fats: Float?,
        @SerializedName("carbs") val carbs: Float?,
        @SerializedName("total_calories") val totalCalories: Float?,
    )
}