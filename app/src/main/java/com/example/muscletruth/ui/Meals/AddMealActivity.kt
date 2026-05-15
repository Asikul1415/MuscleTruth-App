package com.example.muscletruth.ui.Meals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.R
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.data.repository.MealRepository
import com.example.muscletruth.data.repository.ProductRepository
import com.example.muscletruth.data.repository.ServingRepository
import com.example.muscletruth.ui.Servings.AddServingActivity
import com.example.muscletruth.ui.Servings.ServingAdapter
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class AddMealActivity : AppCompatActivity() {
    private var servings = mutableListOf<Serving>()
    private lateinit var productsList: RecyclerView
    private lateinit var adapter: ServingAdapter
    private lateinit var spinner: Spinner
    private lateinit var proteinsField: TextView
    private lateinit var fatsField: TextView
    private lateinit var carbsField: TextView
    private lateinit var caloriesField: TextView
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    var imageURI: Uri? = null

    private val startProductActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val serving = data?.getParcelableExtra<Serving>("serving")
                if(serving != null){
                    servings.add(serving)
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
            val intent = Intent(this, AddServingActivity::class.java)
            startProductActivityForResult.launch(intent)
        }

        val backButton = findViewById<Button>(R.id.add_meal_btn_back)
        backButton.setOnClickListener {
            finish()
        }

        spinner = findViewById<Spinner>(R.id.add_meal_sp)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.meals,
            R.layout.item_spinner_meal_type
        )
        adapter.setDropDownViewResource(R.layout.item_spinner_meal_type)
        spinner.adapter = adapter
        spinner.setSelection(0)

        val addImage = findViewById<Button>(R.id.add_meal_btn_picture)
        addImage.setOnClickListener {
            Utils.ImageUtils.openGallery(selectImageLauncher)
        }

        val image = findViewById<ImageView>(R.id.add_meal_iv)
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == RESULT_OK){
                val selectedImageUri: Uri? = result.data?.data

                image.setImageURI(selectedImageUri)
                imageURI = selectedImageUri
            }
        }

        proteinsField = findViewById<TextView>(R.id.add_meal_tv_proteins_val)
        fatsField = findViewById<TextView>(R.id.add_meal_tv_fats_val)
        carbsField = findViewById<TextView>(R.id.add_meal_tv_carbs_val)
        caloriesField = findViewById<TextView>(R.id.add_meal_tv_calories_val)

        val saveButton = findViewById<Button>(R.id.add_meal_btn_save)
        saveButton.setOnClickListener {
            if(servings.size <= 0){
                Toast.makeText(this, "В приёме пищи должна быть хотя бы 1 порция!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    var imagePart: MultipartBody.Part? = null
                    if(imageURI != null){
                        imagePart = imageURI?.let { uri ->
                            Utils.ImageUtils.createImagePart(this@AddMealActivity, uri)
                        }
                    }

                    val mealTypeID = spinner.selectedItemPosition + 1;
                    val mealResponse = MealRepository.addMeal(Meal(mealTypeID=mealTypeID), imagePart, localImage = imageURI, context = this@AddMealActivity)

                    mealResponse.onSuccess {meal ->
                        servings.forEach { serving ->
                            serving.mealID = meal.serverID
                            ServingRepository.addServing(meal, serving)
                            ProductRepository.addRecentProduct(serving.productID, serving.localProductID)
                            finish()
                        }
                    }
                }
            }

        }

        productsList = findViewById<RecyclerView>(R.id.add_meal_rv)
        setupList()
        loadData()
    }

    private suspend fun updateMacros(){
        var totalProteins: Double = 0.00
        var totalFats: Double = 0.00
        var totalCarbs: Double = 0.00
        var totalCalories: Double = 0.00

        servings.forEach { serving ->
            val product = ProductRepository.getProduct(serving.productID, serving.localProductID)

            if (product !== null) {
                totalProteins += product.proteins * (serving.productAmount / 100.00)
                totalFats += product.fats * (serving.productAmount / 100.00)
                totalCarbs += product.carbs * (serving.productAmount / 100.00)
            }
        }
        totalCalories += totalProteins * 4 + totalFats * 9 + totalCarbs * 4

        proteinsField.text = "%.2f".format(totalProteins)
        fatsField.text = "%.2f".format(totalFats)
        carbsField.text = "%.2f".format(totalCarbs)
        caloriesField.text = "%.2f".format(totalCalories)
    }

    private fun loadData(){
        lifecycleScope.launch {
            try{
                adapter.items = servings
                updateMacros()

                adapter.notifyDataSetChanged()
            }
            catch(e: Exception){

            }
        }
    }

    private fun setupList() {
        productsList.layoutManager = LinearLayoutManager(this)
        adapter = ServingAdapter(lifecycleScope = lifecycleScope, onItemClick = { serving ->
            //WIP to be filled
        }, context = this)
        adapter.items = emptyList()
        productsList.adapter = adapter
    }
}

