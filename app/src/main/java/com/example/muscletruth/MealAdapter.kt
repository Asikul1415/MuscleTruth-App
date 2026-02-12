package com.example.muscletruth

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muscletruth.data.api.models.Meal
import com.example.muscletruth.data.api.models.MealType
import com.example.muscletruth.data.api.models.Serving
import com.example.muscletruth.data.repository.UserRepository
import com.example.muscletruth.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MealAdapter(private val lifecycleScope: LifecycleCoroutineScope, private val context: Context? = null, private val onServingClick: (Serving.ServingItem) -> Unit) : RecyclerView.Adapter<MealAdapter.ViewHolder>() {
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
            is Meal.MealItem -> TYPE_MEAL
            is Serving.ServingItem -> TYPE_SERVING
            is MealType.MealTypeBase -> TYPE_MEAL_TYPE
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
            is Meal.MealItem -> {
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
            is Serving.ServingItem -> {
                lifecycleScope.launch {
                    val product = with(Dispatchers.IO){
                        val userRepository = UserRepository()
                        userRepository.getProduct(item.productID)!!
                    }
                    with(Dispatchers.Main){
                        val totalCalories = (product.proteins * 4 + product.fats * 9 + product.carbs * 4) / 100.00 * item.productAmount

                        holder.servingTvTitle?.text = product.title
                        holder.servingTvProteins?.text = "${product.proteins / 100.00 * item.productAmount}"
                        holder.servingTvFats?.text = "${product.fats / 100.00 * item.productAmount}"
                        holder.servingTvCarbs?.text = "${product.carbs / 100.00 * item.productAmount}"
                        holder.servingTvCalories?.text = "${"%.2f".format(totalCalories)} ккал"
                        holder.servingTvAmount?.text = "${item.productAmount} г"
                        if(product.picture != null && holder.servingPicture != null && context != null){
                            Glide.with(context)
                                .load(Utils.ImageUtils.getImagePath(product.picture))
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(holder.servingPicture)
                        }

                        holder.itemView.setOnClickListener {
                            onServingClick(item)
                        }
                    }
                }
            }
            is MealType.MealTypeBase -> {
                holder.mealTypeTvTitle?.text = item.title
                lifecycleScope.launch {
                    val response = withContext(Dispatchers.IO){
                        val userRepository = UserRepository()
                        userRepository.getMealTypeTotal(item.id)
                    }
                    withContext(Dispatchers.Main){
                        holder.mealTypeTvProteins?.text = "Б: ${response?.proteins}"
                        holder.mealTypeTvFats?.text = "Ж: ${response?.fats}"
                        holder.mealTypeTvCarbs?.text = "У: ${response?.carbs}"
                        holder.mealTypeTvCalories?.text = "${response?.totalCalories} ккал"
                    }
                }
            }
        }

    }

    override fun getItemCount() = items.size
}