package com.example.muscletruth.data.models

import android.os.Parcelable
import androidx.room.*
import java.util.UUID
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


//Вроде все перенес с порциями, но не тестил совсем.
@Parcelize
@Entity("servings")
data class Serving(
    @PrimaryKey
    @ColumnInfo(name = "local_id")
    var localID: String = UUID.randomUUID().toString(),

    @SerializedName("id")
    @ColumnInfo(name = "server_id")
    var serverID: Int = -1,

    @SerializedName("user_id")
    @ColumnInfo("user_id")
    var userID: Int? = null,

    @SerializedName("meal_id")
    @ColumnInfo(name="meal_id")
    var mealID: Int? = null,

    @SerializedName("product_id")
    @ColumnInfo(name="product_id")
    var productID: Int,

    @SerializedName("product_amount")
    @ColumnInfo(name="product_amount")
    var productAmount: Int,
): Parcelable