package com.example.muscletruth.data.models

import android.os.Parcelable
import androidx.room.*
import java.util.UUID
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity("products")
data class Product(
    @PrimaryKey
    @ColumnInfo(name = "local_id")
    var localID: String = UUID.randomUUID().toString(),

    @SerializedName("id")
    @ColumnInfo(name = "server_id")
    var serverID: Int = -1,

    @SerializedName("user_id")
    @ColumnInfo("user_id")
    var userID: Int? = null,

    @SerializedName("title")
    @ColumnInfo(name = "title")
    var title: String,

    @SerializedName("proteins")
    @ColumnInfo(name = "proteins")
    var proteins: Int,

    @SerializedName("fats")
    @ColumnInfo(name = "fats")
    var fats: Int,

    @SerializedName("carbs")
    @ColumnInfo(name = "carbs")
    var carbs: Int,

    @SerializedName("picture")
    @ColumnInfo("server_picture")
    var serverPicture: String? = null,

    @ColumnInfo("local_picture")
    var localPicture: String? = null,

    @SerializedName("creation_date")
    @ColumnInfo("creation_date")
    var creationDate: String? = null
): Parcelable

@Parcelize
@Entity("favourite_products")
data class FavouriteProduct(
    @PrimaryKey
    @ColumnInfo(name = "product_local_id")
    var productLocalID: String,

    @SerializedName("product_id")
    @ColumnInfo(name = "product_server_id")
    var productServerID: Int = -1,
): Parcelable

@Parcelize
@Entity("products_history")
data class ProductsHistory(
    @PrimaryKey
    @ColumnInfo(name = "product_local_id")
    var productLocalID: String,

    @SerializedName("product_id")
    @ColumnInfo(name = "product_server_id")
    var productServerID: Int = -1,

    @SerializedName("use_date")
    @ColumnInfo("use_date")
    var useDate: String? = null
): Parcelable