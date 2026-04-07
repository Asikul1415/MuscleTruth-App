package com.example.muscletruth.data.serviceClasses

class WeightingsChartData {
    data class YearChartData(
        val week_start: String,  // or Date type
        val average_weight: Double)

    data class MonthChartData(
        val day: String,  // or Date type
        val average_weight: Double)
}