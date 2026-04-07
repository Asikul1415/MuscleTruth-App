package com.example.muscletruth.ui.Meals

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muscletruth.R
import com.example.muscletruth.data.serviceClasses.MealType
import com.example.muscletruth.data.serviceClasses.MealItem
import com.example.muscletruth.data.serviceClasses.ServingItem
import com.example.muscletruth.data.repository.MealRepository
import com.example.muscletruth.data.repository.ProductRepository
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class MealAdapter(private val lifecycleScope: LifecycleCoroutineScope, private val context: Context? = null, private val onServingClick: (ServingItem) -> Unit) : RecyclerView.Adapter<MealAdapter.ViewHolder>() {
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

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MealItem -> TYPE_MEAL
            is ServingItem -> TYPE_SERVING
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
                        intent.putExtra("mealID", item.id)
                        context.startActivity(intent)
                    }
                }
            }
            is ServingItem -> {
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
                        holder.servingTvCalories?.text = "${"%.2f".format(totalCalories)} ккал"
                        holder.servingTvAmount?.text = "${item.productAmount} г"
                        if(product.serverPicture != null && holder.servingPicture != null && context != null){
                            val path = product.serverPicture
                            Glide.with(context)
                                .load(Utils.ImageUtils.getImagePath(path!!))
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(holder.servingPicture)
                        }

                        holder.itemView.setOnClickListener {
                            onServingClick(item)
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
}