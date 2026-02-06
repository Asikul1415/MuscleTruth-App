package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.data.api.ApiService
import com.example.muscletruth.data.api.models.Meal
import com.example.muscletruth.data.api.models.Product
import com.example.muscletruth.data.api.models.Serving
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddMealActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private var servings = emptyList<Serving.ServingItem>()
    private lateinit var productsList: RecyclerView
    private lateinit var adapter: ServingAdapter

    private lateinit var spinner: Spinner

    private val startProductActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val serving = data?.getParcelableExtra<Serving.ServingItem>("serving")
                if(serving != null){
                    servings = servings + serving
                    loadData()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_meal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val addProduct = findViewById<Button>(R.id.add_meal_btn_product_add)
        addProduct.setOnClickListener {
            val intent = Intent(this, ProductsActivity::class.java)
            startProductActivityForResult.launch(intent)
        }

        spinner = findViewById<Spinner>(R.id.add_meal_sp)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.meals,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        val saveButton = findViewById<Button>(R.id.add_meal_btn_save)
        saveButton.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    val mealTypeID = spinner.selectedItemPosition + 1;
                    val mealResponse = userRepository.addMeal(Meal.MealBase(mealTypeID=mealTypeID))

                    mealResponse.onSuccess {meal ->
                        servings.forEach { serving ->
                            if(serving.productID != null){
                                val servingBase = Serving.ServingBase(
                                    mealID = meal.id,
                                    productID = serving.productID,
                                    productAmount = serving.productAmount
                                )
                                userRepository.addServing(meal.id, servingBase)
                                finish()
                            }
                        }
                    }
                }
            }
        }

        productsList = findViewById<RecyclerView>(R.id.add_meal_rv)
        setupList()
        loadData()
    }

    private fun loadData(){
        lifecycleScope.launch {
            try{
                adapter.items = servings
                adapter.notifyDataSetChanged()
            }
            catch(e: Exception){

            }
        }
    }

    private fun setupList() {
        productsList.layoutManager = LinearLayoutManager(this)
        adapter = ServingAdapter(lifecycleScope = lifecycleScope, onItemClick = {serving ->
            //WIP to be filled
        })
        adapter.items = emptyList()
        productsList.adapter = adapter
    }
}

