package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.data.api.models.Meal
import com.example.muscletruth.data.api.models.MealType
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MealsActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private var meals: List<Meal.MealItem> = emptyList()

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

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.meals,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val addButton = findViewById<Button>(R.id.meals_btn_add)
        addButton.setOnClickListener {
            val intent = Intent(this, AddMealActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onResume () {
        super.onResume()
        val todayDate = LocalDate.now().toString()
        loadData(startDate = todayDate, endDate = todayDate)
    }

    private fun loadData(startDate:String? = null, endDate: String? = null, mealTypeID: Int? = null){
        adapter.items = mutableListOf<Any>()


        lifecycleScope.launch {
            try{
                withContext(Dispatchers.IO){
                    val breakfastTotal = userRepository.getMealTypeTotal(1)
                    adapter.items.add(MealType.MealTypeBase(
                        id=1,
                        title="Завтрак",
                        proteins = breakfastTotal?.proteins,
                        fats = breakfastTotal?.fats,
                        carbs = breakfastTotal?.carbs,
                        totalCalories = breakfastTotal?.totalCalories))

                    val lunchTotal = userRepository.getMealTypeTotal(2)
                    adapter.items.add(MealType.MealTypeBase(
                        id=2,
                        title="Обед",
                        proteins = lunchTotal?.proteins,
                        fats = lunchTotal?.fats,
                        carbs = lunchTotal?.carbs,
                        totalCalories = lunchTotal?.totalCalories))

                    val dinnerTotal = userRepository.getMealTypeTotal(3)
                    adapter.items.add(MealType.MealTypeBase(
                        id=3,
                        title="Ужин",
                        proteins = dinnerTotal?.proteins,
                        fats = dinnerTotal?.fats,
                        carbs = dinnerTotal?.carbs,
                        totalCalories = dinnerTotal?.totalCalories))

                    val snacksTotal = userRepository.getMealTypeTotal(4)
                    adapter.items.add(MealType.MealTypeBase(
                        id=4,
                        title="Перекусы",
                        proteins = snacksTotal?.proteins,
                        fats = snacksTotal?.fats,
                        carbs = snacksTotal?.carbs,
                        totalCalories = snacksTotal?.totalCalories))
                }
                meals = withContext(Dispatchers.IO){
                    userRepository.getMeals(startDate, endDate).map{ meal ->
                        Meal.MealItem(
                            id=meal.id!!,
                            servings = userRepository.getServings(meal.id),
                            mealTypeID = meal.mealTypeID,
                            creationDate = meal.creationDate)
                    }
                }
                meals.groupBy {it.mealTypeID }.forEach {group ->
                    when(group.key){
                        1 -> {
                            var insertionIndex = adapter.items.indexOf(adapter.items.find {item ->
                                item is MealType.MealTypeBase && item.id == 1
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
                                item is MealType.MealTypeBase && item.id == 2
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
                                item is MealType.MealTypeBase && item.id == 3
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
                                item is MealType.MealTypeBase && item.id == 4
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
            }
            catch(e: Exception){
                Log.e("APP_DEBUG", e.toString())
                throw e
            }
        }
    }

    private fun setupList() {
        mealsList.layoutManager = LinearLayoutManager(this)
        adapter = MealAdapter(lifecycleScope)
        adapter.items = mutableListOf<Any>()
        mealsList.adapter = adapter
    }
}