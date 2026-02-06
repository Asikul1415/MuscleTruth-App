package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.data.api.models.Meal
import com.example.muscletruth.data.api.models.Serving
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.collections.toMutableList

class MealsActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private var meals: List<Meal.MealItem> = emptyList()
    private lateinit var spinner: Spinner

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

        spinner = findViewById<Spinner>(R.id.meals_sp)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.meals,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0) {
                    val selectedMeal = parent?.getItemAtPosition(position).toString()
                    updateMealInfo(selectedMeal)
                } else {

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val addButton = findViewById<Button>(R.id.meals_btn_add)
        addButton.setOnClickListener {
            val intent = Intent(this, AddMealActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onResume() {
        super.onResume()
        loadData(mealTypeID = spinner.selectedItemPosition + 1)
    }

    private fun loadData(startDate:String? = null, endDate: String? = null, mealTypeID: Int? = null){
        lifecycleScope.launch {
            try{
                meals = withContext(Dispatchers.IO){
                    userRepository.getMeals(startDate, endDate, mealTypeID).map{ meal ->
                        Meal.MealItem(id=meal.id!!, servings = userRepository.getServings(meal.id), creationDate = meal.creationDate)
                    }
                }
                adapter.items = meals.toMutableList()
                adapter.notifyDataSetChanged()
            }
            catch(e: Exception){

            }
        }
    }

    private fun setupList() {
        mealsList.layoutManager = LinearLayoutManager(this)
        adapter = MealAdapter(lifecycleScope)
        adapter.items = mutableListOf<Meal.MealItem>()
        mealsList.adapter = adapter
    }

    private fun updateMealInfo(meal: String) {
        val todayDate = LocalDate.now().toString()
        when (meal) {
            "Завтрак" -> {
                loadData(mealTypeID = 1, startDate = todayDate, endDate = todayDate)
            }
            "Обед" -> {
                loadData(mealTypeID = 2, startDate = todayDate, endDate = todayDate)
            }
            "Ужин" -> {
                loadData(mealTypeID = 3, startDate = todayDate, endDate = todayDate)
            }
            "Перекусы" -> {
                loadData(mealTypeID = 4, startDate = todayDate, endDate = todayDate)
            }
        }
    }
}