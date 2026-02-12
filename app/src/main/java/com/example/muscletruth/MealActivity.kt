package com.example.muscletruth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muscletruth.data.api.models.Meal
import com.example.muscletruth.data.api.models.Serving
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class MealActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private var servings = mutableListOf<Serving.ServingItem>()
    private var addedServings = mutableListOf<Serving.ServingItem>()
    private lateinit var productsList: RecyclerView
    private lateinit var adapter: ServingAdapter
    private lateinit var spinner: Spinner
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var picture: ImageView
    var imageURI: Uri? = null

    private val startProductActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val serving = data?.getParcelableExtra<Serving.ServingItem>("serving")
            if(serving != null){
                servings.add(serving)
                addedServings.add(serving)
                loadData()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val addProduct = findViewById<Button>(R.id.meal_btn_product_add)
        addProduct.setOnClickListener {
            val intent = Intent(this, AddServingActivity::class.java)
            startProductActivityForResult.launch(intent)
        }

        spinner = findViewById<Spinner>(R.id.meal_sp)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.meals,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        val addImage = findViewById<Button>(R.id.meal_btn_picture)
        addImage.setOnClickListener {
            Utils.ImageUtils.openGallery(selectImageLauncher)
        }

        picture = findViewById<ImageView>(R.id.meal_iv)
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == Activity.RESULT_OK){
                val selectedImageUri: Uri? = result.data?.data

                picture.setImageURI(selectedImageUri)
                imageURI = selectedImageUri
            }
        }

       lifecycleScope.launch {
            with(Dispatchers.IO){
                val mealID = intent.getIntExtra("mealID", 0)
                if(mealID != 0){
                    servings = userRepository.getServings(mealID)
                    loadData()

                    val meal = userRepository.getMeal(mealID)
                    if(meal?.picture != null){
                        Glide.with(this@MealActivity)
                            .load(Utils.ImageUtils.getImagePath(meal.picture))
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(picture)
                    }
                    if(meal != null){
                        spinner.setSelection(meal.mealTypeID - 1)
                    }
                }

            }
       }

        val deleteButton = findViewById<Button>(R.id.meal_btn_delete)
        deleteButton.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    val mealID = intent.getIntExtra("mealID", 0)
                    if(mealID != 0){
                        userRepository.deleteMeal(mealID)
                        finish()
                    }
                }
            }
        }

        val saveButton = findViewById<Button>(R.id.meal_btn_save)
        saveButton.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    var imagePart: MultipartBody.Part? = null
                    if(imageURI != null){
                        imagePart = imageURI?.let { uri ->
                            Utils.ImageUtils.createImagePart(this@MealActivity, uri)
                        }
                    }

                    val mealTypeID = spinner.selectedItemPosition + 1;
                    val mealID = intent.getIntExtra("mealID", 0)
                    val mealResponse = userRepository.updateMeal(mealID = mealID, meal = Meal.MealBase(mealTypeID=mealTypeID), imagePart)

                    mealResponse.onSuccess {meal ->
                        addedServings.forEach { serving ->
                            val servingBase = Serving.ServingBase(
                                mealID = mealID,
                                productID = serving.productID,
                                productAmount = serving.productAmount
                            )
                            userRepository.addServing(mealID, servingBase)
                        }
                        finish()
                    }
                }
            }
        }



        productsList = findViewById<RecyclerView>(R.id.meal_rv)
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
                Log.e("APP_DEBUG", "${e.toString()}")
            }
        }
    }

    private fun setupList() {
        productsList.layoutManager = LinearLayoutManager(this)
        adapter = ServingAdapter(lifecycleScope = lifecycleScope, onItemClick = {serving ->
            //WIP to be filled
        }, context = this)
        adapter.items = emptyList()
        productsList.adapter = adapter
    }
}