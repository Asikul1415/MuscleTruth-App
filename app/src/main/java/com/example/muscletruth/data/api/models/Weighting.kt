package com.example.muscletruth.data.api.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName
import java.sql.Timestamp


class  Weighting {
    @Parcelize
    data class WeightingBase(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("user_id") val userID: Int? = null,
        @SerializedName("result") val result: Number,
        @SerializedName("picture") val picture: String? = null,
        @SerializedName("creation_date") val creationDate: Timestamp? = null
    ) : Parcelable

    data class WeightingResponse(
        @SerializedName("id") val id: Int,
    )
}