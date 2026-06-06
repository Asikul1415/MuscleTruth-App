package com.example.muscletruth.ui.Meals

import android.content.Intent
import android.os.Bundle
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
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.SavedMeal
import com.example.muscletruth.data.repositories.MealRepository
import com.example.muscletruth.data.repositories.ProductRepository
import com.example.muscletruth.data.repositories.ServingRepository
import kotlinx.coroutines.launch

class SavedMealsActivity : AppCompatActivity() {

    private var meals: List<SavedMeal> = emptyList()

    private lateinit var mealsList: RecyclerView
    private var meal: Meal? = null
    private lateinit var adapter: SavedMealAdapter
    private lateinit var noMealsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved_meals)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        noMealsText = findViewById<TextView>(R.id.saved_meals_no_meals_tv)

        mealsList = findViewById<RecyclerView>(R.id.saved_meals_meals_rv)
        setupList()
        loadData()

        val backButton = findViewById<Button>(R.id.saved_meals_btn_back)
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupList() {
        mealsList.layoutManager = LinearLayoutManager(this)
        adapter = SavedMealAdapter(this@SavedMealsActivity, {item ->
            val intent = Intent()
            lifecycleScope.launch {
                val meal = MealRepository.getMeal(item.mealServerID, item.mealLocalID)
                if(meal !== null){
                    intent.putExtra("meal", meal)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }, {item ->
            lifecycleScope.launch {
                MealRepository.deleteSavedMeal(item.mealServerID, item.mealLocalID)
            }
        })
        adapter.items = mutableListOf<SavedMeal>()
        mealsList.adapter = adapter
    }

    private fun loadData() {
        adapter.items = mutableListOf<SavedMeal>()

        lifecycleScope.launch {
            meals = MealRepository.getSavedMeals().map{savedMeal ->
                meal = MealRepository.getMeal(savedMeal.mealServerID, savedMeal.mealLocalID)

                var totalProteins: Double = 0.00
                var totalFats: Double = 0.00
                var totalCarbs: Double = 0.00
                if(meal !== null){
                    val servings = ServingRepository.getMealServings(meal!!.serverID, meal!!.localID)
                    servings.forEach {serving ->
                        val product = ProductRepository.getProduct(serving.productID, serving.localProductID)
                        if(product !== null){
                            totalProteins += (product.proteins * serving.productAmount) / 100.00
                            totalFats += (product.fats * serving.productAmount) / 100.00
                            totalCarbs += (product.carbs * serving.productAmount) / 100.00
                        }
                    }
                }
                val totalCalories = totalProteins * 4 + totalFats * 9 + totalCarbs * 4

                savedMeal.copy(
                    totalProteins = totalProteins,
                    totalFats = totalFats,
                    totalCarbs = totalCarbs,
                    totalCalories = totalCalories,
                    serverImage = meal?.serverPicture,
                    localImage = meal?.localPicture
                )
            }

            if(meals.isEmpty()){
                mealsList.visibility = View.GONE
                noMealsText.visibility = View.VISIBLE
            }
            else{
                mealsList.visibility = View.VISIBLE
                noMealsText.visibility = View.GONE
                adapter.items = meals.toMutableList()
                adapter.notifyDataSetChanged()
            }
        }
    }

}