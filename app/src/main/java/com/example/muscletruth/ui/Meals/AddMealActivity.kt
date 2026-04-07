package com.example.muscletruth.ui.Meals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
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
import com.example.muscletruth.data.serviceClasses.ServingItem
import com.example.muscletruth.data.repository.MealRepository
import com.example.muscletruth.data.repository.ServingRepository
import com.example.muscletruth.ui.Servings.AddServingActivity
import com.example.muscletruth.ui.Servings.ServingAdapter
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class AddMealActivity : AppCompatActivity() {
    private var servings = mutableListOf<ServingItem>()
    private lateinit var productsList: RecyclerView
    private lateinit var adapter: ServingAdapter
    private lateinit var spinner: Spinner
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    var imageURI: Uri? = null

    private val startProductActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val serving = data?.getParcelableExtra<ServingItem>("serving")
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
                    val mealResponse = MealRepository.addMeal(Meal(mealTypeID=mealTypeID), imagePart)

                    mealResponse.onSuccess {meal ->
                        servings.forEach { serving ->
                            val servingBase = Serving(
                                mealID = meal.serverID,
                                productID = serving.productID,
                                productAmount = serving.productAmount
                            )
                            Log.d("APP_DEBUG", "${mealResponse} ${meal} ${servingBase} ${meal.serverID}")
                            ServingRepository.addServing(meal.serverID, servingBase)
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
        adapter = ServingAdapter(lifecycleScope = lifecycleScope, onItemClick = { serving ->
            //WIP to be filled
        }, context = this)
        adapter.items = emptyList()
        productsList.adapter = adapter
    }
}

