package com.example.muscletruth.ui.Weightings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.graphics.Color
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.R
import com.example.muscletruth.data.localDB.offlineModels.WeightingDao
import com.example.muscletruth.data.models.Weighting
import com.example.muscletruth.data.repository.WeightingRepository
import com.example.muscletruth.utils.Period
import com.example.muscletruth.utils.Utils.DateUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
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
import java.time.ZonedDateTime
import java.util.Locale

class WeightingsActivity : AppCompatActivity() {
    private var weightings = emptyList<Weighting>()
    private var rangePreference = Period.Week
    private lateinit var weightingsList: RecyclerView
    private lateinit var chart: LineChart
    private lateinit var adapter: WeightingAdapter
    private var displayedToast: Toast? = null
    private val addActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
        if(result.resultCode === RESULT_OK){
            loadData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weightings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chart = findViewById(R.id.weightings_chart)
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    displayedToast?.cancel()
                    displayedToast = Toast.makeText(
                        this@WeightingsActivity,
                        "Вес: ${"%.1f".format(it.y)} кг",
                        Toast.LENGTH_SHORT
                    )
                    displayedToast?.show()
                }
            }

            override fun onNothingSelected() {}})

        weightingsList = findViewById(R.id.weightings_rv)

        loadData()
        setupList()

        val addWeightingButton = findViewById<Button>(R.id.weightings_btn_add)
        addWeightingButton.setOnClickListener {
            val intent = Intent(this@WeightingsActivity, AddWeightingActivity::class.java)
            addActivityLauncher.launch(intent);
        }

        val weekButton = findViewById<Button>(R.id.weightings_btn_week)
        weekButton.setOnClickListener {
            rangePreference = Period.Week
            loadData()
        }

        val monthButton = findViewById<Button>(R.id.weightings_btn_month)
        monthButton.setOnClickListener {
            rangePreference = Period.Month
            loadData()
        }

        val yearButton = findViewById<Button>(R.id.weightings_btn_year)
        yearButton.setOnClickListener {
            rangePreference = Period.Year
            loadData()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupList() {
        weightingsList.layoutManager = LinearLayoutManager(this)
        adapter = WeightingAdapter {weighting ->
            val intent = Intent(this, WeightingActivity::class.java)
            intent.putExtra("weighting", weighting)
            startActivity(intent)
        }
        adapter.items = emptyList()
        weightingsList.adapter = adapter
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)
        chart.fitScreen()
        chart.setNoDataText("Нет взвешиваний")
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
                adapter.items = weightings.sortedByDescending { ZonedDateTime.parse(it.creationDate)}
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Toast.makeText(this@WeightingsActivity, "Ошибка!", Toast.LENGTH_LONG).show()
                Log.d("APP_DEBUG", "${e.toString()}")
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
                if(rangePreference == Period.Year){
                    with(Dispatchers.IO){
                        val data = WeightingRepository.getWeightingsYearChartData()
                        data.forEach {weekWeighting ->
                            sortedWeightings.add(Weighting(result = weekWeighting.average_weight, creationDate = weekWeighting.week_start))
                        }
                    }
                }
                else if(rangePreference == Period.Month){
                    with(Dispatchers.IO){
                        val data = WeightingRepository.getWeightingsMonthChartData()
                        data.forEach {weighting ->
                            sortedWeightings.add(Weighting(result = weighting.average_weight, creationDate = weighting.day))
                        }
                    }
                }
                else{
                    weightings.groupBy {LocalDate.parse(it.creationDate?.substringBefore('T'))}.forEach { group ->
                        var middleValue = 0.00
                        group.value.forEach { weighting ->
                            middleValue = middleValue + weighting.result.toDouble()
                        }
                        sortedWeightings.add(Weighting(result = middleValue / group.value.count(), creationDate = group.key.toString()))
                    }
                }
                sortedWeightings.sortBy { it.creationDate }

                val entries = ArrayList<Entry>()
                sortedWeightings.forEachIndexed { index, weighting ->
                    entries.add(Entry(index.toFloat(), weighting.result.toFloat()))
                }

                val xAxis = chart.xAxis
                val dataSet = LineDataSet(entries, "Взвешивания")
                with(Dispatchers.Main){
                    dataSet.color = Color.MAGENTA
                    dataSet.lineWidth = 2f
                    dataSet.valueTextSize = 0f
                    dataSet.setDrawCircles(true)
                    dataSet.setCircleColor(Color.GRAY)
                    dataSet.circleRadius = 4f

                    if(rangePreference == Period.Week){
                        xAxis.setLabelCount(7, false)
                    }
                    else if(rangePreference == Period.Month){
                        //31 / 2 = 15.5
                        xAxis.setLabelCount(16, false)
                    }
                    else if(rangePreference == Period.Year){
                        xAxis.setLabelCount(16, false)
                        dataSet.lineWidth = 1.5f
                        dataSet.circleRadius = 2.25f;
                        dataSet.setCircleColor(Color.BLACK)
                    }
                }

                val dates = sortedWeightings.map{DateUtils.convertTimestamp(it.creationDate)}
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

                chart.data = LineData(dataSet)
                chart.invalidate()
            }
        }


    }
}