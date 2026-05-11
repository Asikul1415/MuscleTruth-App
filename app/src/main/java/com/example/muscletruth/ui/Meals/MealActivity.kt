package com.example.muscletruth.ui.Meals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muscletruth.R
import com.example.muscletruth.data.models.Meal
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.data.repository.MealRepository
import com.example.muscletruth.data.repository.ProductRepository
import com.example.muscletruth.data.repository.ServingRepository
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.data.serviceClasses.MealItem
import com.example.muscletruth.ui.Servings.AddServingActivity
import com.example.muscletruth.ui.Servings.ServingAdapter
import com.example.muscletruth.utils.Utils
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import java.util.UUID

class MealActivity : AppCompatActivity() {
    private var servings = mutableListOf<Serving>()
    private var addedServings = mutableListOf<Serving>()
    private lateinit var productsList: RecyclerView
    private lateinit var adapter: ServingAdapter
    private lateinit var spinner: Spinner
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var picture: ImageView
    private lateinit var proteinsField: TextView
    private lateinit var fatsField: TextView
    private lateinit var carbsField: TextView
    private lateinit var caloriesField: TextView
    var imageURI: Uri? = null

    private val startProductActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val serving = data?.getParcelableExtra<Serving>("serving")
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
            R.layout.item_spinner_meal_type
        )
        adapter.setDropDownViewResource(R.layout.item_spinner_meal_type)
        spinner.adapter = adapter
        spinner.setSelection(0)

        val addImage = findViewById<Button>(R.id.meal_btn_picture)
        addImage.setOnClickListener {
            Utils.ImageUtils.openGallery(selectImageLauncher)
        }

        picture = findViewById<ImageView>(R.id.meal_iv)
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == RESULT_OK){
                val selectedImageUri: Uri? = result.data?.data

                picture.setImageURI(selectedImageUri)
                imageURI = selectedImageUri
            }
        }

        proteinsField = findViewById<TextView>(R.id.meal_tv_proteins_val)
        fatsField = findViewById<TextView>(R.id.meal_tv_fats_val)
        carbsField = findViewById<TextView>(R.id.meal_tv_carbs_val)
        caloriesField = findViewById<TextView>(R.id.meal_tv_calories_val)


        val mealItem = intent.getParcelableExtra<MealItem>("meal")
       lifecycleScope.launch {
            with(Dispatchers.IO){
                if(mealItem !== null){
                    servings = ServingRepository.getServings(mealItem.id, mealItem.localID)
                    loadData()

                    val meal = MealRepository.getMeal(mealItem.id)
                    if(checkForInternetConnection() && meal?.serverPicture !== null){
                        val path = meal.serverPicture
                        Glide.with(this@MealActivity)
                            .load(Utils.ImageUtils.getImagePath(path!!))
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(picture)
                    }
                    else if(meal?.localPicture !== null){
                        val path = meal.localPicture
                        Glide.with(this@MealActivity)
                            .load(path)
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
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_empty, null)
            val dialog = AlertDialog.Builder(this)
                .setTitle("Вы точно хотите удалить приём пищи?")
                .setView(dialogView)
                .setPositiveButton("Да", null)
                .setNegativeButton("Нет", null)
                .create()
            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

                positiveButton.setOnClickListener {
                    Log.d("APP_DEBUG", "SERVING DELETE")
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO){
                            Log.d("APP_DEBUG", "SERVING DELETE: mealItem from intent $mealItem")
                            if(mealItem !== null && mealItem.localID !== null){
                                MealRepository.deleteMeal(mealItem.id, mealItem.localID)
                                Log.d("APP_DEBUG", "SERVING DELETE: serving was deleted")
                                finish()
                            }
                        }
                    }
                }

                negativeButton.setOnClickListener {
                    dialog.cancel()
                }
            }

            dialog.show()

        }

        val backButton = findViewById<Button>(R.id.meal_btn_back)
        backButton.setOnClickListener {
            finish()
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

                    if(mealItem !== null){
                        val isMealUpdateSuccessful = MealRepository.updateMeal(
                            meal = Meal(
                                localID = mealItem.localID ?: UUID.randomUUID().toString(),
                                serverID = mealItem.id ?: -1,
                                mealTypeID=mealTypeID,
                                creationDate = mealItem.creationDate),
                            localImage = imageURI,
                            image = imagePart,
                            context = this@MealActivity)

                        if(isMealUpdateSuccessful){
                            //Setting wasUpdated flag to sync changes later
                            if(checkForInternetConnection() === false && mealItem.localID !== null){
                                val updatedMeal = UserRepository.localDb.mealDao().getLocalMeal(mealItem.localID)
                                if(updatedMeal !== null){
                                    updatedMeal.wasUpdated = 1;
                                    UserRepository.localDb.mealDao().update(updatedMeal)
                                }
                            }


                            addedServings.forEach { serving ->
                                val servingBase = Serving(
                                    mealID = mealItem.id,
                                    localMealID = mealItem.localID,
                                    productID = serving.productID,
                                    localProductID = serving.localProductID,
                                    productAmount = serving.productAmount
                                )
                                ServingRepository.addServing(MealRepository.getMeal(mealItem.id, mealItem.localID!!)!!, servingBase)
                            }
                            finish()
                        }
                    }
                    else{
                        Toast.makeText(this@MealActivity, "Ошибка!",
                            Toast.LENGTH_LONG).show()
                        Log.e("APP_DEBUG", "MEAL ACTIVITY ERROR: MEAL ITEM IS NULL")
                        finish()
                    }

                }
            }
        }



        productsList = findViewById<RecyclerView>(R.id.meal_rv)
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

        Log.d("APP_DEBUG", "TEST 123")
    }

    private fun loadData(){
        lifecycleScope.launch {
            try{
                adapter.items = servings
                updateMacros()

                adapter.notifyDataSetChanged()
            }
            catch(e: Exception){
                Log.e("APP_DEBUG", "${e.toString()}")
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