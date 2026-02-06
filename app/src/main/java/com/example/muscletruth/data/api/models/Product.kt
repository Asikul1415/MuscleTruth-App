package com.example.muscletruth.data.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

class Product {
    @Parcelize
    data class ProductBase(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("user_id") val userID: Int? = null,
        @SerializedName("title") val title: String,
        @SerializedName("proteins") val proteins: Int,
        @SerializedName("fats") val fats: Int,
        @SerializedName("carbs") val carbs: Int,
        @SerializedName("picture") val picture: String? = null
    ) : Parcelable

    data class ProductResponse(
        @SerializedName("id") val id: Int
    )
}