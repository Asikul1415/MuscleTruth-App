package com.example.muscletruth.ui.Meals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
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
import androidx.core.widget.addTextChangedListener
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
import com.example.muscletruth.ui.Servings.AddServingActivity
import com.example.muscletruth.ui.Servings.ServingAdapter
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import java.io.File
import java.util.UUID

class AddMealActivity : AppCompatActivity() {
    private var servings = mutableListOf<Serving>()
    private lateinit var productsList: RecyclerView
    private lateinit var adapter: ServingAdapter
    private lateinit var spinner: Spinner
    private lateinit var proteinsField: TextView
    private lateinit var fatsField: TextView
    private lateinit var carbsField: TextView
    private lateinit var caloriesField: TextView
    private lateinit var picture: ImageView
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

    private val startSavedMealsActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val meal = data?.getParcelableExtra<Meal>("meal")
            if(meal !== null){
                Log.d("APP_DEBUG!", "meal get $meal")
                lifecycleScope.launch {
                    servings = ServingRepository.getMealServings(meal.serverID, meal.localID)
                    if(meal.localPicture !== null){
                        Glide.with(this@AddMealActivity)
                            .load(meal.localPicture)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(picture)
                        imageURI = Uri.fromFile(File(meal.localPicture))
                    }
                    else if(meal.serverPicture !== null){
                        val path = Utils.ImageUtils.getImagePath(meal.serverPicture!!)
                        val localPicture = Utils.ImageUtils.saveImageFromServer(this@AddMealActivity, path)
                        Glide.with(this@AddMealActivity)
                            .load(localPicture)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(picture)
                        imageURI = Uri.fromFile(File(localPicture))
                    }
                    setupList()
                    loadData()
                }
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

        val savedMealsButton = findViewById<Button>(R.id.add_meal_btn_saved_meals)
        savedMealsButton.setOnClickListener {
            val intent = Intent(this, SavedMealsActivity::class.java)
            startSavedMealsActivityForResult.launch(intent)
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

        picture = findViewById<ImageView>(R.id.add_meal_iv)
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == RESULT_OK){
                val selectedImageUri: Uri? = result.data?.data

                picture.setImageURI(selectedImageUri)
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
                            //Regenerate localID to prevent serving just being replaced. With it it would be inserted, not replaced in offline
                            serving.localID = UUID.randomUUID().toString()
                            ServingRepository.addServing(meal, serving).onSuccess {serverServing ->
                                //If se
                                if(serverServing.localID !== null){
                                    ServingRepository.addRecentServing(serverServing.serverID, serverServing.localID)
                                }
                                else{
                                    ServingRepository.addRecentServing(serverServing.serverID, serving.localID)
                                }

                            }
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
            showServingActionsDialog(serving)
        }, context = this)
        adapter.items = emptyList()
        productsList.adapter = adapter
    }

    private fun showServingActionsDialog(serving: Serving): Unit{

        val servingActionsDialogView = LayoutInflater.from(this@AddMealActivity).inflate(R.layout.dialog_empty, null)
        val servingActionsDialog = AlertDialog.Builder(this@AddMealActivity)
            .setTitle("Что вы желаете?")
            .setView(servingActionsDialogView)
            .setPositiveButton("Удалить", null)
            .setNegativeButton("Изменить", null)
            .create()

        servingActionsDialog.setOnShowListener {
            val deleteButton = servingActionsDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            deleteButton.setOnClickListener {
                showDeleteServingDialog(serving, servingActionsDialog)
            }

            val changeButton = servingActionsDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            changeButton.setOnClickListener {
                showChangeServingDialog(serving, servingActionsDialog)
            }
        }

        servingActionsDialog.show()
    }

    private fun showChangeServingDialog(serving: Serving, servingActionsDialog: AlertDialog){
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 1).toInt()
        val height = (displayMetrics.heightPixels * 0.55).toInt()

        val changeServingDialogView = LayoutInflater.from(this@AddMealActivity).inflate(R.layout.dialog_product_amount, null)
        val changeServingDialog = AlertDialog.Builder(this@AddMealActivity)
            .setTitle("Введите количество продукта:")
            .setView(changeServingDialogView)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отменить", null)
            .create()


        changeServingDialog.setOnShowListener {
            lifecycleScope.launch {
                val product = with(Dispatchers.IO) {
                    ProductRepository.getProduct(serving.productID, serving.localProductID)
                }

                if(product === null) return@launch

                val saveButton = changeServingDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                val cancelButton = changeServingDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                val productTitleField = changeServingDialog.findViewById<TextView>(R.id.dg_product_amount_tv_product_title)
                productTitleField?.text = product.title

                val amount = changeServingDialogView.findViewById<EditText>(R.id.dialog_product_amount_et_amount)
                amount.setText(serving.productAmount.toString())

                val proteinsField = changeServingDialogView.findViewById<TextView>(R.id.dg_product_amount_tv_proteins_val)
                val carbsField = changeServingDialogView.findViewById<TextView>(R.id.dg_product_amount_tv_carbs_val)
                val fatsField = changeServingDialogView.findViewById<TextView>(R.id.dg_product_amount_tv_fats_val)
                val caloriesField = changeServingDialogView.findViewById<TextView>(R.id.dg_product_amount_tv_calories_val)

                fun updateMacros() {
                    val productAmount = amount.text.toString().toDouble()
                    val proteinsAmount = product.proteins * (productAmount / 100)
                    val carbsAmount = product.carbs * (productAmount / 100)
                    val fatsAmount = product.fats * (productAmount / 100)
                    val caloriesAmount = proteinsAmount * 4 + carbsAmount * 4 + fatsAmount * 9

                    proteinsField.text = "%.2f".format(proteinsAmount)
                    carbsField.text = "%.2f".format(carbsAmount)
                    fatsField.text = "%.2f".format(fatsAmount)
                    caloriesField.text = "%.2f".format(caloriesAmount)
                }

                updateMacros()

                amount.addTextChangedListener {
                    if (amount.length() > 4) {
                        amount.setText(amount.text.dropLast(1))
                        amount.setSelection(amount.text.length)
                    } else if (amount.length() == 0) {
                        amount.error = "Введите кол-во продукта!"
                    }
                    else{
                        updateMacros()
                    }
                }
                saveButton.setOnClickListener {
                    //9999 max
                    if (amount.length() > 0 && amount.length() <= 4) {
                        val intAmount = amount.text.toString().toInt()
                        if (intAmount <= 0) {
                            amount.error = "Кол-во продукта должно быть больше 0!"
                        } else if (intAmount > 9999) {
                            amount.error = "Кол-во продукта должно быть меньше 9999!"
                        } else {
                            servingActionsDialog.dismiss()
                            changeServingDialog.dismiss()

                            val index = servings.indexOf(serving)
                            if(index != -1){
                                servings[index].productAmount = intAmount
                            }
                            loadData()

                            Toast.makeText(this@AddMealActivity, "Порция была изменена успешно!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        amount.error = "Введите корректное кол-во продукта!"
                    }
                }
                cancelButton.setOnClickListener {
                    changeServingDialog.dismiss()
                }
            }
        }

        changeServingDialog.show()
        changeServingDialog.window?.setLayout(width, height)
    }

    private fun showDeleteServingDialog(serving: Serving, servingActionsDialog: AlertDialog): Unit{

        val dialogView = LayoutInflater.from(this@AddMealActivity).inflate(R.layout.dialog_empty, null)
        val dialog = AlertDialog.Builder(this@AddMealActivity)
            .setTitle("Вы хотите удалить эту порцию?")
            .setView(dialogView)
            .setPositiveButton("Да", null)
            .setNegativeButton("Нет", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                lifecycleScope.launch {
                    with(Dispatchers.IO){
                        servings.remove(serving)
                        loadData()
                        Toast.makeText(this@AddMealActivity, "Порция успешно удалена!", Toast.LENGTH_LONG).show()

                        dialog.dismiss()
                        servingActionsDialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }
}

