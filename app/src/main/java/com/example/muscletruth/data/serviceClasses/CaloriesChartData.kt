package com.example.muscletruth.data.serviceClasses

class CaloriesChartData {
    data class ChartDataByWeek(
        val week_start: String,  // or Date type
        val average_calories: Double)

    data class ChartDataByDay(
        val day: String,  // or Date type
        val total_calories: Double)
}