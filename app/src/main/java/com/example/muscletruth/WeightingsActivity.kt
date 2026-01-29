package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.graphics.Color
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.data.api.models.Weighting
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.utils.Utils.DateUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class WeightingsActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private var weightings = emptyList<Weighting.WeightingBase>()
    private lateinit var weightingsList: RecyclerView
    private lateinit var chart: LineChart
    private lateinit var adapter: WeightingAdapter

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
        weightingsList = findViewById(R.id.weightings_rv)

        setupChart()
        setupList()

        loadData()

        val addWeightingButton = findViewById<Button>(R.id.weightings_btn_add)
        addWeightingButton.setOnClickListener {
            val intent = Intent(this, AddWeightingActivity::class.java)
            startActivity(intent)
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

        val xAxis = chart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.granularity = 1f
        xAxis.setLabelCount(weightings.count(), true)
        xAxis.setAvoidFirstLastClipping(true)

        val yAxis = chart.axisLeft
        yAxis.setDrawGridLines(true)
        chart.axisRight.isEnabled = false
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                weightings = withContext(Dispatchers.IO) {
                    userRepository.getWeightings()
                }

                updateChartWithData(weightings)
                adapter.items = weightings
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Toast.makeText(this@WeightingsActivity, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateChartWithData(weightings: List<Weighting.WeightingBase>) {
        if (weightings.isEmpty()) {
            chart.clear()
            Toast.makeText(this, "Добавьте первое взвешивание!", Toast.LENGTH_SHORT).show()
            return
        }

        val sortedWeightings = weightings.sortedBy { it.creationDate }

        val entries = ArrayList<Entry>()
        sortedWeightings.forEachIndexed { index, weighting ->
            entries.add(Entry(index.toFloat(), weighting.result.toFloat()))
        }

        val xAxis = chart.xAxis
        val dates = sortedWeightings.map{DateUtils.convertTimestamp(it.creationDate)}
        xAxis.setLabelCount(dates.size, true)
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

        val yAxis = chart.axisLeft
        yAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format(Locale.getDefault(), "%.2f кг", value)
            }
        }

        val dataSet = LineDataSet(entries, "Взвешивания")
        dataSet.color = Color.MAGENTA
        dataSet.lineWidth = 2f
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.setDrawCircles(true)
        dataSet.setCircleColor(Color.GRAY)
        dataSet.circleRadius = 4f

        chart.data = LineData(dataSet)
        chart.invalidate()
    }
}