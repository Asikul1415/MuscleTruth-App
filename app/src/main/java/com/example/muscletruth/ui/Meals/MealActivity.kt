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
import com.example.muscletruth.data.serviceClasses.ServingItem
import com.example.muscletruth.data.repository.MealRepository
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
    private var servings = mutableListOf<ServingItem>()
    private var addedServings = mutableListOf<ServingItem>()
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
            val serving = data?.getParcelableExtra<ServingItem>("serving")
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
                                val meal = Meal(
                                    localID = mealItem.localID,
                                    serverID = mealItem.id,
                                    mealTypeID = mealItem.mealTypeID
                                )
                                MealRepository.deleteMeal(meal)
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
        adapter = ServingAdapter(lifecycleScope = lifecycleScope, onItemClick = { serving ->
            //WIP to be filled
        }, context = this)
        adapter.items = emptyList()
        productsList.adapter = adapter
    }
}