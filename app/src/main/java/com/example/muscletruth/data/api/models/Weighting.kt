package com.example.muscletruth.data.api.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime


class  Weighting {
    @Parcelize
    data class WeightingBase(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("user_id") val userID: Int? = null,
        @SerializedName("result") val result: Number,
        @SerializedName("picture") val picture: String? = null,
        @SerializedName("creation_date") val creationDate: String? = null
    ) : Parcelable

    data class WeightingRequest(
        @SerializedName("start_date") val startDate: String?,
        @SerializedName("end_date") val endDate: String? = LocalDate.now().toString()
    )

    data class WeightingResponse(
        @SerializedName("id") val id: Int,
    )
}