package com.example.muscletruth.ui.Meals

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MealsActivity : AppCompatActivity() {
    private var meals: List<MealItem> = emptyList()

    private lateinit var mealsList: RecyclerView
    private lateinit var adapter: MealAdapter

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

        val addButton = findViewById<Button>(R.id.meals_btn_add)
        addButton.setOnClickListener {
            val intent = Intent(this, AddMealActivity::class.java)
            startActivity(intent)

        }

        val backButton = findViewById<Button>(R.id.meals_btn_back)
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume () {
        super.onResume()
        val todayDate = LocalDate.now().toString()
        loadData()
    }

    private fun loadData(){
        adapter.items = mutableListOf<Any>()

        lifecycleScope.launch {
            try{
                meals = withContext(Dispatchers.IO){
                    MealRepository.getTodayMeals().map{ meal ->
                        Log.d("APP_DEBUG", "MEAL: ${meal}")
                        MealItem(
                            id=meal.serverID!!,
                            localID = meal.localID,
                            servings = ServingRepository.getMealServings(meal.serverID, meal.localID),
                            mealTypeID = meal.mealTypeID,
                            creationDate = meal.creationDate)
                    }
                }
                meals.forEach {meal ->
                    Log.d("APP_DEBUG", "MEAL_ITEM: ${meal.servings}")
                }

                val noMealsText = findViewById<TextView>(R.id.meals_tv_no_meals)
                if(meals.size == 0){
                    mealsList.visibility = View.GONE
                    noMealsText.visibility = View.VISIBLE
                    return@launch
                } else{
                    mealsList.visibility = View.VISIBLE
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
            }
            catch(e: Exception){
                Log.e("APP_DEBUG", e.toString())
                throw e
            }
        }
    }

    private fun setupList() {
        mealsList.layoutManager = LinearLayoutManager(this)
        adapter = MealAdapter(lifecycleScope, this@MealsActivity, {
            adapter.notifyDataSetChanged()
            loadData()
        })
        adapter.items = mutableListOf<Any>()
        mealsList.adapter = adapter
    }
}