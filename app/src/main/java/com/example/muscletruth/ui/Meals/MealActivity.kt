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
    private var deletedServings = mutableListOf<Serving>()
    private lateinit var productsList: RecyclerView
    private lateinit var adapter: ServingAdapter
    private lateinit var spinner: Spinner
    private lateinit var mealItem: MealItem
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
                serving.mealID = mealItem.id
                serving.localMealID = mealItem.localID
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

        mealItem = intent.getParcelableExtra<MealItem>("meal")!!
        lifecycleScope.launch {
            with(Dispatchers.IO){
                if(mealItem !== null){
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
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO){
                            if(mealItem !== null && mealItem.localID !== null){
                                MealRepository.deleteMeal(mealItem.id, mealItem.localID)
                                servings.forEach {serving ->
                                    ServingRepository.deleteServing(serving)
                                }
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

        val saveMealButton = findViewById<Button>(R.id.meal_btn_save_meal)
        var isMealSaved: Boolean = false
        lifecycleScope.launch {
            isMealSaved = MealRepository.getSavedMeal(mealItem.id, mealItem.localID) !== null
            if(isMealSaved){
                saveMealButton.text = "♥"
            }

            saveMealButton.setOnClickListener {
                if(isMealSaved){
                    val confirmSavedMealDeleteDialogView =
                        LayoutInflater.from(this@MealActivity)
                            .inflate(R.layout.dialog_empty, null)
                    val confirmSavedMealDeleteDialog =
                        AlertDialog.Builder(this@MealActivity)
                            .setTitle("Вы точно хотите удалить из сохранённого приём пищи?")
                            .setView(confirmSavedMealDeleteDialogView)
                            .setPositiveButton("Да", null)
                            .setNegativeButton("Нет", null)
                            .create()

                    confirmSavedMealDeleteDialog.setOnShowListener {
                        val positiveButton =
                            confirmSavedMealDeleteDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        positiveButton.setOnClickListener {
                            lifecycleScope.launch {
                                MealRepository.deleteSavedMeal(
                                    mealItem.id,
                                    mealItem.localID
                                )
                                confirmSavedMealDeleteDialog.dismiss()
                                saveMealButton.text = "♡"
                            }
                        }

                        val negativeButton =
                            confirmSavedMealDeleteDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        negativeButton.setOnClickListener {
                            confirmSavedMealDeleteDialog.dismiss()
                        }
                    }

                    confirmSavedMealDeleteDialog.show()
                }
                else{
                    val saveMealDialogView = LayoutInflater.from(this@MealActivity).inflate(R.layout.dialog_save_meal, null)
                    val saveMealDialog = AlertDialog.Builder(this@MealActivity)
                        .setTitle("Что вы желаете?")
                        .setView(saveMealDialogView)
                        .setPositiveButton("Сохранить", null)
                        .setNegativeButton("Отменить", null)
                        .create()

                    saveMealDialog.setOnShowListener {
                        val saveButton = saveMealDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        saveButton.setOnClickListener {
                            lifecycleScope.launch {
                                try {
                                    val titleField = saveMealDialog.findViewById<EditText>(R.id.dialog_save_meal_et_title)
                                    val title = titleField?.text.toString()
                                    if (title.length > 3 && title.length < 256) {
                                        val savedMeal = MealRepository.addSavedMeal(
                                            title,
                                            mealItem.id,
                                            mealItem.localID
                                        )
                                        if (savedMeal !== null) {
                                            saveMealButton.text = "♥"
                                            saveMealDialog.dismiss()
                                        }
                                    } else if (title.length < 3) {
                                        titleField?.error =
                                            "Название должно состоять хотя бы из 3 символов!"
                                    } else if (title.length > 255) {
                                        titleField?.error =
                                            "Название должно состоять не более чем из 255 символов!"
                                    }

                                }
                                catch (e: Exception){
                                    Log.e("APP_DEBUG", "ERRORRRR!!!!!!!! ${e.toString()}")

                                }
                            }
                        }

                        val backButton = saveMealDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        backButton.setOnClickListener {
                            saveMealDialog.dismiss()
                        }
                    }

                    saveMealDialog.show()
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
                            if(checkForInternetConnection() == false && mealItem.localID !== null){
                                val updatedMeal = UserRepository.localDb.mealDao().getLocalMeal(mealItem.localID!!)
                                if(updatedMeal !== null){
                                    updatedMeal.wasUpdated = 1;
                                    UserRepository.localDb.mealDao().update(updatedMeal)
                                }
                            }


                            addedServings.forEach { serving ->
                                ServingRepository.addServing(MealRepository.getMeal(mealItem.id, mealItem.localID!!)!!, serving).onSuccess {serverServing ->
                                    ServingRepository.addRecentServing(serverServing.serverID, serverServing.localID)
                                }
                            }

                            deletedServings.forEach {serving ->
                                ServingRepository.deleteServing(serving)
                            }

                            if(ServingRepository.getMealServings(mealItem.id, mealItem.localID).count() == 0){
                                MealRepository.deleteMeal(mealItem.id, mealItem.localID)
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
                Log.d("APP_DEBUG!", "${deletedServings}")
                servings = ServingRepository.getMealServings(mealItem.id, mealItem.localID).filter{ serving ->
                    Log.d("APP_DEBUG!", "${serving}")
                    deletedServings.map{ it->it.localID}.indexOf(serving.localID) == -1
                }.toMutableList()
                servings.addAll(addedServings)
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
            showServingActionsDialog(serving)
        }, context = this)
        adapter.items = emptyList()
        productsList.adapter = adapter
    }

    private fun showServingActionsDialog(serving: Serving): Unit{

        val servingActionsDialogView = LayoutInflater.from(this@MealActivity).inflate(R.layout.dialog_empty, null)
        val servingActionsDialog = AlertDialog.Builder(this@MealActivity)
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

        val changeServingDialogView = LayoutInflater.from(this@MealActivity).inflate(R.layout.dialog_product_amount, null)
        val changeServingDialog = AlertDialog.Builder(this@MealActivity)
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

                            lifecycleScope.launch {
                                //If serving was there before
                                if(addedServings.map{it->it.localID}.indexOf(serving.localID) == -1){
                                    serving.productAmount = intAmount
                                    ServingRepository.updateServing(serving)
                                    loadData()
                                }
                                //If serving was added now
                                else{
                                    val index = addedServings.indexOf(serving)
                                    addedServings[index].productAmount = intAmount
                                    loadData()
                                }
                            }

                            Toast.makeText(this@MealActivity, "Порция была изменена успешно!", Toast.LENGTH_LONG).show()
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

        val dialogView = LayoutInflater.from(this@MealActivity).inflate(R.layout.dialog_empty, null)
        val dialog = AlertDialog.Builder(this@MealActivity)
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

                        //If serving was there before
                        if(addedServings.map{it->it.localID}.indexOf(serving.localID) == -1){
                            addedServings.remove(serving)
                            deletedServings.add(serving)
                            loadData()
                        }
                        Toast.makeText(this@MealActivity, "Порция успешно удалена!", Toast.LENGTH_LONG).show()

                        dialog.dismiss()
                        servingActionsDialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }
}