package com.example.muscletruth.data.api.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName
import java.time.LocalDate


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
        @SerializedName("start_date") val startDate: String? = null,
        @SerializedName("end_date") val endDate: String? = LocalDate.now().toString()
    )

    data class WeightingResponse(
        @SerializedName("id") val id: Int,
    )

    data class YearChartData(
        val week_start: String,  // or Date type
        val average_weight: Double)

    data class MonthChartData(
        val day: String,  // or Date type
        val average_weight: Double)
}