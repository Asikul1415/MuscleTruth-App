package com.example.muscletruth.data.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import androidx.lifecycle.lifecycleScope

class Serving {
    data class ServingBase(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("user_id") val userID: Int? = null,
        @SerializedName("meal_id") val mealID: Int? = null,
        @SerializedName("product_id") val productID: Int,
        @SerializedName("product_amount") val productAmount: Int,
    )

    @Parcelize
    data class ServingItem(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("product_id") val productID: Int,
        @SerializedName("products") var product: Product.ProductBase? = null,
        @SerializedName("product_amount") val productAmount: Int
    ) : Parcelable

    data class ServingResponse(
        @SerializedName("id") val id: Int
    )
}