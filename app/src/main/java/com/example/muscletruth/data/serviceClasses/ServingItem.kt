package com.example.muscletruth.data.serviceClasses

import android.os.Parcelable
import com.example.muscletruth.data.models.Product
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServingItem(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("product_id") val productID: Int,
    @SerializedName("meal_id") val mealID: Int? = null,
    @SerializedName("products") var product: Product? = null,
    @SerializedName("product_amount") val productAmount: Int
) : Parcelable