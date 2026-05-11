package com.example.muscletruth.ui.Meals

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muscletruth.R
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.data.serviceClasses.MealType
import com.example.muscletruth.data.serviceClasses.MealItem
import com.example.muscletruth.data.repository.MealRepository
import com.example.muscletruth.data.repository.ProductRepository
import com.example.muscletruth.data.repository.ServingRepository
import com.example.muscletruth.utils.Utils
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class MealAdapter(private val lifecycleScope: LifecycleCoroutineScope, private val context: Context? = null, private val onItemsUpdate: () -> Unit) : RecyclerView.Adapter<MealAdapter.ViewHolder>() {
    var items = mutableListOf<Any>()

    private val TYPE_MEAL = 0;
    private val TYPE_SERVING = 1;
    private val TYPE_MEAL_TYPE = 2;

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mealTvDate: TextView? = view.findViewById(R.id.item_meal_tv_date)

        val servingTvTitle: TextView? = view.findViewById(R.id.item_serving_tv_title)
        val servingTvProteins: TextView? = view.findViewById(R.id.item_serving_tv_proteins_val)
        val servingTvFats: TextView? = view.findViewById(R.id.item_serving_tv_fats_val)
        val servingTvCarbs: TextView? = view.findViewById(R.id.item_serving_tv_carbs_val)
        val servingTvCalories: TextView? = view.findViewById(R.id.item_serving_tv_calories)
        val servingTvAmount: TextView? = view.findViewById(R.id.item_serving_tv_amount)
        val servingPicture: ImageView? = view.findViewById(R.id.item_serving_iv)

        val mealTypeTvTitle: TextView? = view.findViewById(R.id.item_meal_type_tv_title)
        val mealTypeTvProteins: TextView? = view.findViewById(R.id.item_meal_type_tv_proteins)
        val mealTypeTvFats: TextView? = view.findViewById(R.id.item_meal_type_tv_fats)
        val mealTypeTvCarbs: TextView? = view.findViewById(R.id.item_meal_type_tv_carbs)
        val mealTypeTvCalories: TextView? = view.findViewById(R.id.item_meal_type_tv_calories)

        init {
            itemView.setOnClickListener {
                //WIP
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MealItem -> TYPE_MEAL
            is Serving -> TYPE_SERVING
            is MealType -> TYPE_MEAL_TYPE
            else -> throw IllegalArgumentException()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            TYPE_MEAL -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_meal, parent, false)
                ViewHolder(view)
            }
            TYPE_SERVING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_serving, parent, false)
                ViewHolder(view)
            }
            TYPE_MEAL_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_meal_type, parent, false)
                ViewHolder(view)
            }

            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when(item){
            is MealItem -> {
                val russianLocale = Locale("ru", "RU")
                holder.mealTvDate?.text = ZonedDateTime.parse(item.creationDate).format(
                    DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", russianLocale)
                )
                holder.itemView.setOnClickListener {
                    if(context != null){
                        val intent = Intent(context, MealActivity::class.java)
                        intent.putExtra("meal", item)
                        context.startActivity(intent)
                    }
                }
            }
            is Serving -> {
                lifecycleScope.launch {
                    val product = with(Dispatchers.IO){
                        ProductRepository.getProduct(item.productID)!!
                    }
                    with(Dispatchers.Main){
                        val totalCalories = (product.proteins * 4 + product.fats * 9 + product.carbs * 4) / 100.00 * item.productAmount

                        holder.servingTvTitle?.text = product.title
                        holder.servingTvProteins?.text = "${"%.2f".format(product.proteins / 100.00 * item.productAmount)}"
                        holder.servingTvFats?.text = "${"%.2f".format(product.fats / 100.00 * item.productAmount)}"
                        holder.servingTvCarbs?.text = "${"%.2f".format(product.carbs / 100.00 * item.productAmount)}"
                        holder.servingTvCalories?.text = "${"%.2f".format(totalCalories)}"
                        holder.servingTvAmount?.text = "${item.productAmount}"

                        if(checkForInternetConnection() && product.serverPicture != null && holder.servingPicture != null && context != null){
                            val path = product.serverPicture
                            Glide.with(context)
                                .load(Utils.ImageUtils.getImagePath(path!!))
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(holder.servingPicture)
                        }
                        else if(product.localPicture !== null && holder.servingPicture !== null && context !== null){
                            val path = product.localPicture
                            Glide.with(context)
                                .load(path)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(holder.servingPicture)
                        }
                        holder.itemView.setOnClickListener {
                            showServingActionsDialog(item)
                        }
                    }
                }
            }
            is MealType -> {
                holder.mealTypeTvTitle?.text = item.title
                lifecycleScope.launch {
                    val response = withContext(Dispatchers.IO){
                        MealRepository.getMealTypeTotal(item.id)
                    }
                    Log.d("APP_DEBUG", "MEAL_TYPE_BASE: ${response}")
                    withContext(Dispatchers.Main){
                        holder.mealTypeTvProteins?.text = "Б: ${"%.2f".format(response?.proteins)}"
                        holder.mealTypeTvFats?.text = "Ж: ${"%.2f".format(response?.fats)}"
                        holder.mealTypeTvCarbs?.text = "У: ${"%.2f".format(response?.carbs)}"
                        holder.mealTypeTvCalories?.text = "${"%.2f".format(response?.totalCalories)} ккал"
                    }
                }
            }
        }

    }

    override fun getItemCount() = items.size

    private fun showServingActionsDialog(serving: Serving): Unit{
        if(context === null) return

        val servingActionsDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_empty, null)
        val servingActionsDialog = AlertDialog.Builder(context)
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
        if(context === null) return

        val displayMetrics = context.resources.displayMetrics
        val width = (displayMetrics.widthPixels * 1).toInt()
        val height = (displayMetrics.heightPixels * 0.55).toInt()

        val changeServingDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_product_amount, null)
        val changeServingDialog = AlertDialog.Builder(context)
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
                                serving.productAmount = intAmount
                                ServingRepository.updateServing(serving)
                                onItemsUpdate()
                            }

                            Toast.makeText(context, "Порция была изменена успешно!", Toast.LENGTH_LONG).show()
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
        if(context === null) return

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_empty, null)
        val dialog = AlertDialog.Builder(context)
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
                        val mealID = serving.mealID ?: -1
                        val mealLocalID = serving.localMealID

                        Log.d("APP_DEBUG", "MealAdapter: ${ServingRepository.getServings(mealID, mealLocalID)}")
                        if(ServingRepository.getServings(mealID, mealLocalID).size == 1){
                            MealRepository.deleteMeal(mealServerID = mealID, mealLocalID)
                        }
                        else{
                            ServingRepository.deleteServing(serving)
                        }
                        Toast.makeText(context, "Порция успешно удалена!", Toast.LENGTH_LONG).show()

                        dialog.dismiss()
                        servingActionsDialog.dismiss()

                        items.clear()
                        onItemsUpdate()
                    }
                }
            }
        }

        dialog.show()
    }
}