package com.example.muscletruth.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.muscletruth.R
import com.example.muscletruth.data.models.Weighting
import com.example.muscletruth.data.repositories.MealRepository
import com.example.muscletruth.data.repositories.WeightingRepository
import com.example.muscletruth.data.serviceClasses.CaloriesChartData
import com.example.muscletruth.utils.Period
import com.example.muscletruth.utils.Utils.DateUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale

class StatisticActivity : AppCompatActivity() {
    private var weightings = emptyList<Weighting>()
    private var rangePreference = Period.Week
    private lateinit var chart: LineChart
    private var displayedToast: Toast? = null

    private sealed class CaloriesData{
        data class ByWeek(val items: List<CaloriesChartData.ChartDataByWeek>): CaloriesData()
        data class ByDay(val items: List<CaloriesChartData.ChartDataByDay>): CaloriesData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_statistic)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chart = findViewById(R.id.statistics_chart)
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    val message = when(h?.dataSetIndex){
                        0 -> "Вес: ${"%.1f".format(it.y)} кг"
                        1 -> "Калорий: ${"%.2f".format(it.y)} ккал"
                        else -> "${it.y}"
                    }
                    displayedToast?.cancel()
                    displayedToast = Toast.makeText(
                        this@StatisticActivity,
                        message,
                        Toast.LENGTH_SHORT
                    )
                    displayedToast?.show()
                }
            }

            override fun onNothingSelected() {}})


        loadData()

        val backButton = findViewById<Button>(R.id.statistics_btn_back)
        backButton.setOnClickListener {
            finish()
        }

        val weekButton = findViewById<Button>(R.id.statistics_btn_week)
        weekButton.setOnClickListener {
            rangePreference = Period.Week
            loadData()
        }

        val monthButton = findViewById<Button>(R.id.statistics_btn_month)
        monthButton.setOnClickListener {
            rangePreference = Period.Month
            loadData()
        }

        val yearButton = findViewById<Button>(R.id.statistics_btn_year)
        yearButton.setOnClickListener {
            rangePreference = Period.Year
            loadData()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)
        chart.fitScreen()
        chart.setNoDataText("Данных нет")
        chart.setNoDataTextColor(Color.MAGENTA)
        chart.legend.textColor = Color.WHITE
        chart.legend.textSize = 14f

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.setGranularityEnabled(true)
        xAxis.enableGridDashedLine(10f, 1f, 0f)  // Makes grid less prominent
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(false)
        xAxis.setAvoidFirstLastClipping(false)
        xAxis.textColor = Color.WHITE
        xAxis.textSize = 14f

        val yAxis = chart.axisLeft
        yAxis.textColor = Color.WHITE
        yAxis.textSize = 14f
        yAxis.setDrawGridLines(true)
        chart.axisRight.isEnabled = false

        chart.axisRight.isEnabled = true
        chart.axisRight.textColor = Color.GREEN
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                weightings = withContext(Dispatchers.IO) {
                    var date = LocalDate.now()
                    date = when (rangePreference) {
                        Period.Week -> {
                            date.minusWeeks(1)
                        }
                        Period.Month -> {
                            date.minusMonths(1)
                        }
                        else -> {
                            date.minusYears(1)
                        }
                    }

                    WeightingRepository.getWeightings(startDate = date.toString())
                }

                setupChart()
                updateChartWithData(weightings)
            } catch (e: Exception) {
                Toast.makeText(this@StatisticActivity, "Ошибка!", Toast.LENGTH_LONG).show()
                Log.e("APP_DEBUG", "${e.toString()}")
                throw(e)
            }
        }
    }

    private fun updateChartWithData(weightings: List<Weighting>) {
        if (weightings.isEmpty()) {
            chart.clear()
            Toast.makeText(this, "Добавьте первое взвешивание!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            with(Dispatchers.IO){
                val sortedWeightings = mutableListOf<Weighting>()
                val calories: CaloriesData

                if(rangePreference == Period.Year){
                    with(Dispatchers.IO){
                        val data = WeightingRepository.getWeightingsYearChartData()
                        data.forEach {weekWeighting ->
                            sortedWeightings.add(Weighting(result = weekWeighting.average_weight, creationDate = weekWeighting.week_start))
                        }

                        calories = CaloriesData.ByWeek(MealRepository.getAverageCaloriesYearChartData().sortedBy {it -> it.week_start})
                    }
                }
                else if(rangePreference == Period.Month){
                    with(Dispatchers.IO){
                        val data = WeightingRepository.getWeightingsMonthChartData()
                        data.forEach {weighting ->
                            sortedWeightings.add(Weighting(result = weighting.average_weight, creationDate = weighting.day))
                        }

                        calories = CaloriesData.ByDay(MealRepository.getAverageCaloriesMonthChartData().sortedBy {it -> it.day})
                    }
                }
                else{
                    weightings.groupBy {LocalDate.parse(it.creationDate?.substringBefore('T'))}.forEach { group ->
                        var middleValue = 0.00
                        group.value.forEach { weighting ->
                            middleValue = middleValue + weighting.result
                        }
                        sortedWeightings.add(Weighting(result = middleValue / group.value.count(), creationDate = group.key.toString()))
                    }

                    calories = CaloriesData.ByDay(MealRepository.getAverageCaloriesWeekChartData().sortedBy {it -> it.day})
                }
                sortedWeightings.sortBy { it.creationDate }

                var dates = weightings.map{it-> it.creationDate.toString().split('T').first()}.toMutableList()
                when(calories){
                    is CaloriesData.ByWeek -> {
                        dates.addAll(calories.items.map{it-> it.week_start})
                    }
                    is CaloriesData.ByDay -> {
                        dates.addAll(calories.items.map{it-> it.day})
                    }
                }
                dates = dates.distinct()
                    .sortedBy{date ->
                        LocalDate.of(
                            date.split('-')[0].toInt(),
                            date.split('-')[1].toInt(),
                            date.split('-')[2].toInt()
                        )
                    }.map{date -> DateUtils.convertTimestamp(date)}.toMutableList()
                val dateIndexMap = dates.withIndex().associate { it.value to it.index }

                val weightingsEntries = ArrayList<Entry>()
                sortedWeightings.mapNotNull {weighting ->
                    dateIndexMap[DateUtils.convertTimestamp(weighting.creationDate)]?.let{ index ->
                        weightingsEntries.add( Entry(index.toFloat(), weighting.result.toFloat()))
                    }
                }

                val caloriesEntries = ArrayList<Entry>()
                when(calories) {
                    is CaloriesData.ByWeek -> {
                        calories.items.mapNotNull {data ->
                            dateIndexMap[DateUtils.convertTimestamp(data.week_start)]?.let{index ->
                                caloriesEntries.add(Entry(index.toFloat(), data.average_calories.toFloat()))
                            }
                        }

                    }
                    is CaloriesData.ByDay -> {
                        calories.items.mapNotNull {data ->
                            dateIndexMap[DateUtils.convertTimestamp(data.day)]?.let{index ->
                                caloriesEntries.add(Entry(index.toFloat(), data.total_calories.toFloat()))
                            }
                        }
                    }
                }

                weightingsEntries.sortBy{entry -> entry.x}
                caloriesEntries.sortBy {entry -> entry.x}

                val xAxis = chart.xAxis
                val weightingsDataSet = LineDataSet(weightingsEntries, "Взвешивания")
                val caloriesDataSet = LineDataSet(caloriesEntries, "Ккал")
                with(Dispatchers.Main){
                    weightingsDataSet.color = Color.WHITE
                    weightingsDataSet.lineWidth = 2f
                    weightingsDataSet.valueTextSize = 0f
                    weightingsDataSet.setDrawCircles(true)
                    weightingsDataSet.setCircleColor(Color.CYAN)
                    weightingsDataSet.circleRadius = 4f

                    caloriesDataSet.color = Color.GREEN
                    caloriesDataSet.lineWidth = 2f
                    caloriesDataSet.valueTextSize = 0f
                    caloriesDataSet.setDrawCircles(true)
                    caloriesDataSet.setCircleColor(Color.LTGRAY)
                    caloriesDataSet.circleRadius = 4f
                    caloriesDataSet.axisDependency = YAxis.AxisDependency.RIGHT

                    if(rangePreference == Period.Week){
                        xAxis.setLabelCount(7, false)
                        chart.axisRight.setLabelCount(7, false)
                    }
                    else if(rangePreference == Period.Month){
                        //31 / 2 = 15.5
                        xAxis.setLabelCount(16, false)
                        chart.axisRight.setLabelCount(16, false)
                    }
                    else if(rangePreference == Period.Year){
                        xAxis.setLabelCount(16, false)
                        weightingsDataSet.lineWidth = 1.5f
                        weightingsDataSet.circleRadius = 2.25f;
                        weightingsDataSet.setCircleColor(Color.BLACK)

                        chart.axisRight.setLabelCount(16, false)
                        caloriesDataSet.lineWidth = 1.5f
                        caloriesDataSet.circleRadius = 2.25f;
                        caloriesDataSet.setCircleColor(Color.WHITE)
                    }
                }

                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index in dates.indices) {
                            dates[index]
                        } else {
                            ""
                        }
                    }
                }

                with(Dispatchers.Main){
                    xAxis.labelRotationAngle = -90f
                    val yAxis = chart.axisLeft
                    yAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format(Locale.getDefault(), "%.2f кг", value)
                        }
                    }
                }

                chart.data = LineData(weightingsDataSet, caloriesDataSet)
                chart.invalidate()
            }
        }

    }
}