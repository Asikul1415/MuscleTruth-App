package com.example.muscletruth.ui.Meals

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.R
import com.example.muscletruth.data.serviceClasses.MealType
import com.example.muscletruth.data.serviceClasses.MealItem
import com.example.muscletruth.data.repository.MealRepository
import com.example.muscletruth.data.repository.ServingRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar

class MealsActivity : AppCompatActivity() {
    private var meals: List<MealItem> = emptyList()

    private lateinit var mealsList: RecyclerView
    private lateinit var pieChart: PieChart
    private lateinit var tvDate: TextView
    private lateinit var adapter: MealAdapter
    private var mealsDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meals)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mealsList = findViewById<RecyclerView>(R.id.meals_rv)
        setupList()

        pieChart = findViewById<PieChart>(R.id.meals_pie_chart)

        val addButton = findViewById<Button>(R.id.meals_btn_add)
        addButton.setOnClickListener {
            val intent = Intent(this, AddMealActivity::class.java)
            startActivity(intent)

        }

        val backButton = findViewById<Button>(R.id.meals_btn_back)
        backButton.setOnClickListener {
            finish()
        }

        tvDate = findViewById<TextView>(R.id.meals_tv_date)

        val calendarButton = findViewById<ImageButton>(R.id.meals_btn_calendar)
        calendarButton.setOnClickListener {
            showDatePickerDialog()
        }
    }

    override fun onResume () {
        super.onResume()
        loadData(mealsDate)
    }

    private fun loadData(date: String? = null){
        adapter.items.clear()
        meals = mutableListOf()
        setupList(date)

        try{
            if(date === null){
                loadTodayMeals()
            }
            else{
                loadDateMeals(date)
            }
        }
        catch(e: Exception){
            Log.e("APP_DEBUG", e.toString())
            throw e
        }
    }

    private fun setupList(date: String? = null) {
        mealsList.layoutManager = LinearLayoutManager(this)
        adapter = MealAdapter(lifecycleScope, this@MealsActivity, {
            loadData(date)
            adapter.notifyDataSetChanged()
        }, mealsDate)
        adapter.items.clear()
        mealsList.adapter = adapter
    }

    private fun setupPieChart() {
        val entries = mutableListOf<PieEntry>()
        var totalCalories = 0.00f
        adapter.items.forEach {mealType ->
            //Filtering total for all day header
            if(mealType is MealType && mealType.id !== 999 && mealType.totalCalories !== null && mealType.totalCalories > 0){
                entries.add(PieEntry(mealType.totalCalories, mealType.title))
            }
            else if(mealType is MealType && mealType.id == 999){
                totalCalories = mealType.totalCalories ?: 0.00f
            }
        }

        // Создаём датасет и задаём цвета
        val dataSet = PieDataSet(entries, "Рацион")
        dataSet.colors = listOf(Color.rgb(230, 156, 46), Color.GRAY, Color.MAGENTA, Color.LTGRAY)
        dataSet.valueTextSize = 24f
        dataSet.valueTextColor = Color.YELLOW

    // Формируем данные для отображения
        pieChart.data = PieData(dataSet)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(14f)
        pieChart.legend.isEnabled = false
        pieChart.holeRadius = 40f
        pieChart.centerText = totalCalories.toString() + " ккал"
        pieChart.setCenterTextSize(24f)
        pieChart.setCenterTextColor(Color.BLACK)
        pieChart.description.isEnabled = false

    // Обновляем диаграмму
        pieChart.invalidate()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)      // 0-11
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                val today = LocalDate.now().toString()
                Log.d("APP_DEBUG!", "DATE $formattedDate $today")
                if(formattedDate == today){
                    tvDate.text = "Сегодня"
                    mealsDate = null
                    loadData()
                }
                else{
                    tvDate.text = formattedDate
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                    formatter.timeZone = selectedCalendar.timeZone
                    mealsDate = formatter.format(selectedCalendar.time)
                    loadData(mealsDate)
                }
            },
            year, month, day
        )

         datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    private fun loadTodayMeals(){
        lifecycleScope.launch {
            meals = withContext(Dispatchers.IO){
                MealRepository.getTodayMeals().map{ meal ->
                    Log.d("APP_DEBUG", "MEAL: ${meal}")
                    MealItem(
                        id=meal.serverID!!,
                        localID = meal.localID,
                        servings = ServingRepository.getMealServings(meal.serverID, meal.localID),
                        mealTypeID = meal.mealTypeID,
                        creationDate = meal.creationDate,
                        serverOriginMealID = meal.serverOriginMealID,
                        localOriginMealID = meal.localOriginMealID)
                }
            }
            meals.forEach {meal ->
                Log.d("APP_DEBUG", "MEAL_ITEM: ${meal.servings}")
            }

            val noMealsText = findViewById<TextView>(R.id.meals_tv_no_meals)
            if(meals.size == 0){
                mealsList.visibility = View.GONE
                pieChart.visibility = View.GONE
                noMealsText.visibility = View.VISIBLE
                return@launch
            } else{
                mealsList.visibility = View.VISIBLE
                pieChart.visibility = View.VISIBLE
                noMealsText.visibility = View.GONE
            }

            withContext(Dispatchers.IO){
                val breakfastTotal = MealRepository.getMealTypeTotal(1)
                val lunchTotal = MealRepository.getMealTypeTotal(2)
                val dinnerTotal = MealRepository.getMealTypeTotal(3)
                val snacksTotal = MealRepository.getMealTypeTotal(4)

                val totalProteins = breakfastTotal?.proteins!! + lunchTotal?.proteins!! + dinnerTotal?.proteins!! + snacksTotal?.proteins!!
                val totalFats = breakfastTotal?.fats!! + lunchTotal?.fats!! + dinnerTotal?.fats!! + snacksTotal?.fats!!
                val totalCarbs = breakfastTotal?.carbs!! + lunchTotal?.carbs!! + dinnerTotal?.carbs!! + snacksTotal?.carbs!!
                val totalCalories = totalProteins * 4 + totalFats * 9 + totalCarbs * 4

                adapter.items.add(MealType(
                    id=999,
                    title="Итого",
                    proteins = totalProteins,
                    fats = totalFats,
                    carbs = totalCarbs,
                    totalCalories = totalCalories,
                ))

                adapter.items.add(MealType(
                    id=1,
                    title="Завтрак",
                    proteins = breakfastTotal?.proteins,
                    fats = breakfastTotal?.fats,
                    carbs = breakfastTotal?.carbs,
                    totalCalories = breakfastTotal?.totalCalories))

                adapter.items.add(MealType(
                    id=2,
                    title="Обед",
                    proteins = lunchTotal?.proteins,
                    fats = lunchTotal?.fats,
                    carbs = lunchTotal?.carbs,
                    totalCalories = lunchTotal?.totalCalories))

                adapter.items.add(MealType(
                    id=3,
                    title="Ужин",
                    proteins = dinnerTotal?.proteins,
                    fats = dinnerTotal?.fats,
                    carbs = dinnerTotal?.carbs,
                    totalCalories = dinnerTotal?.totalCalories))

                adapter.items.add(MealType(
                    id=4,
                    title="Перекусы",
                    proteins = snacksTotal?.proteins,
                    fats = snacksTotal?.fats,
                    carbs = snacksTotal?.carbs,
                    totalCalories = snacksTotal?.totalCalories))
            }

            meals.groupBy {it.mealTypeID }.forEach {group ->
                when(group.key){
                    1 -> {
                        var insertionIndex = adapter.items.indexOf(adapter.items.find {item ->
                            item is MealType && item.id == 1
                        }) + 1

                        group.value.forEach {meal ->
                            adapter.items.add(index=insertionIndex, element = meal)
                            insertionIndex += 1
                            meal.servings.forEach {serving ->
                                Log.d("APP_DEBUG", "SERVING - $serving")
                                adapter.items.add(index = insertionIndex, element = serving)
                                insertionIndex += 1
                            }
                        }
                    }
                    2 -> {
                        var insertionIndex = adapter.items.indexOf(adapter.items.find {item ->
                            item is MealType && item.id == 2
                        }) + 1

                        group.value.forEach {meal ->
                            adapter.items.add(index=insertionIndex, element = meal)
                            insertionIndex += 1
                            meal.servings.forEach {serving ->
                                Log.d("APP_DEBUG", "SERVING - $serving")
                                adapter.items.add(index = insertionIndex, element = serving)
                                insertionIndex += 1
                            }
                        }
                    }
                    3 -> {
                        var insertionIndex = adapter.items.indexOf(adapter.items.find {item ->
                            item is MealType && item.id == 3
                        }) + 1

                        group.value.forEach {meal ->
                            adapter.items.add(index=insertionIndex, element = meal)
                            insertionIndex += 1
                            meal.servings.forEach {serving ->
                                Log.d("APP_DEBUG", "SERVING - $serving")
                                adapter.items.add(index = insertionIndex, element = serving)
                                insertionIndex += 1
                            }
                        }
                    }
                    4 -> {
                        var insertionIndex = adapter.items.indexOf(adapter.items.find {item ->
                            item is MealType && item.id == 4
                        }) + 1

                        group.value.forEach {meal ->
                            adapter.items.add(index=insertionIndex, element = meal)
                            insertionIndex += 1
                            meal.servings.forEach {serving ->
                                Log.d("APP_DEBUG", "SERVING - $serving")
                                adapter.items.add(index = insertionIndex, element = serving)
                                insertionIndex += 1
                            }
                        }
                    }
                }
            }

            adapter.notifyDataSetChanged()
            setupPieChart()
        }
    }

    private fun loadDateMeals(date: String){
        lifecycleScope.launch {
            meals = withContext(Dispatchers.IO){
                MealRepository.getMeals(date).map{ meal ->
                    Log.d("APP_DEBUG", "MEAL: ${meal}")
                    MealItem(
                        id=meal.serverID!!,
                        localID = meal.localID,
                        servings = ServingRepository.getMealServings(meal.serverID, meal.localID),
                        mealTypeID = meal.mealTypeID,
                        creationDate = meal.creationDate,
                        serverOriginMealID = meal.serverOriginMealID,
                        localOriginMealID = meal.localOriginMealID)
                }
            }

            val noMealsText = findViewById<TextView>(R.id.meals_tv_no_meals)
            if(meals.size == 0){
                mealsList.visibility = View.GONE
                noMealsText.visibility = View.VISIBLE
                pieChart.visibility = View.GONE
                return@launch
            } else{
                mealsList.visibility = View.VISIBLE
                pieChart.visibility = View.VISIBLE
                noMealsText.visibility = View.GONE
            }

            withContext(Dispatchers.IO){
                val breakfastTotal = MealRepository.getMealTypeTotal(1, date)
                val lunchTotal = MealRepository.getMealTypeTotal(2, date)
                val dinnerTotal = MealRepository.getMealTypeTotal(3, date)
                val snacksTotal = MealRepository.getMealTypeTotal(4, date)

                val totalProteins = breakfastTotal?.proteins!! + lunchTotal?.proteins!! + dinnerTotal?.proteins!! + snacksTotal?.proteins!!
                val totalFats = breakfastTotal?.fats!! + lunchTotal?.fats!! + dinnerTotal?.fats!! + snacksTotal?.fats!!
                val totalCarbs = breakfastTotal?.carbs!! + lunchTotal?.carbs!! + dinnerTotal?.carbs!! + snacksTotal?.carbs!!
                val totalCalories = totalProteins * 4 + totalFats * 9 + totalCarbs * 4

                adapter.items.add(MealType(
                    id=999,
                    title="Итого",
                    proteins = totalProteins,
                    fats = totalFats,
                    carbs = totalCarbs,
                    totalCalories = totalCalories,
                ))

                adapter.items.add(MealType(
                    id=1,
                    title="Завтрак",
                    proteins = breakfastTotal?.proteins,
                    fats = breakfastTotal?.fats,
                    carbs = breakfastTotal?.carbs,
                    totalCalories = breakfastTotal?.totalCalories))

                adapter.items.add(MealType(
                    id=2,
                    title="Обед",
                    proteins = lunchTotal?.proteins,
                    fats = lunchTotal?.fats,
                    carbs = lunchTotal?.carbs,
                    totalCalories = lunchTotal?.totalCalories))

                adapter.items.add(MealType(
                    id=3,
                    title="Ужин",
                    proteins = dinnerTotal?.proteins,
                    fats = dinnerTotal?.fats,
                    carbs = dinnerTotal?.carbs,
                    totalCalories = dinnerTotal?.totalCalories))

                adapter.items.add(MealType(
                    id=4,
                    title="Перекусы",
                    proteins = snacksTotal?.proteins,
                    fats = snacksTotal?.fats,
                    carbs = snacksTotal?.carbs,
                    totalCalories = snacksTotal?.totalCalories))
            }

            meals.groupBy {it.mealTypeID }.forEach {group ->
                when(group.key){
                    1 -> {
                        var insertionIndex = adapter.items.indexOf(adapter.items.find {item ->
                            item is MealType && item.id == 1
                        }) + 1

                        group.value.forEach {meal ->
                            adapter.items.add(index=insertionIndex, element = meal)
                            insertionIndex += 1
                            meal.servings.forEach {serving ->
                                adapter.items.add(index = insertionIndex, element = serving)
                                insertionIndex += 1
                            }
                        }
                    }
                    2 -> {
                        var insertionIndex = adapter.items.indexOf(adapter.items.find {item ->
                            item is MealType && item.id == 2
                        }) + 1

                        group.value.forEach {meal ->
                            adapter.items.add(index=insertionIndex, element = meal)
                            insertionIndex += 1
                            meal.servings.forEach {serving ->
                                adapter.items.add(index = insertionIndex, element = serving)
                                insertionIndex += 1
                            }
                        }
                    }
                    3 -> {
                        var insertionIndex = adapter.items.indexOf(adapter.items.find {item ->
                            item is MealType && item.id == 3
                        }) + 1

                        group.value.forEach {meal ->
                            adapter.items.add(index=insertionIndex, element = meal)
                            insertionIndex += 1
                            meal.servings.forEach {serving ->
                                adapter.items.add(index = insertionIndex, element = serving)
                                insertionIndex += 1
                            }
                        }
                    }
                    4 -> {
                        var insertionIndex = adapter.items.indexOf(adapter.items.find {item ->
                            item is MealType && item.id == 4
                        }) + 1

                        group.value.forEach {meal ->
                            adapter.items.add(index=insertionIndex, element = meal)
                            insertionIndex += 1
                            meal.servings.forEach {serving ->
                                adapter.items.add(index = insertionIndex, element = serving)
                                insertionIndex += 1
                            }
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged()
            setupPieChart()
        }
    }
}