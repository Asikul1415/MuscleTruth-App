package com.example.muscletruth

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.data.api.models.Meal
import com.example.muscletruth.data.api.models.Serving
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MealAdapter(private val lifecycleScope: LifecycleCoroutineScope) : RecyclerView.Adapter<MealAdapter.ViewHolder>() {
    var items = mutableListOf<Meal.MealItem>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.item_meal_tv_date)
        val ivExpand: ImageView = view.findViewById(R.id.item_meal_iv_expand)
        val childContainer: LinearLayout = view.findViewById(R.id.item_meal_ll_child)

        init {
            // Click on header to expand/collapse
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    items[position].isExpanded = !items[position].isExpanded
                    notifyItemChanged(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        Log.d("APP_DEBUG", "{${item.creationDate.toString()}}")
        val russianLocale = Locale("ru", "RU")
        holder.tvDate.text = ZonedDateTime.parse(item.creationDate).format(
            DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", russianLocale)
        )

        // Set expand/collapse icon
        val rotation = if (item.isExpanded) 180f else 0f
        holder.ivExpand.animate().rotation(rotation).setDuration(200).start()

        // Show/hide child items
        if (item.isExpanded) {
            holder.childContainer.visibility = View.VISIBLE
            showChildItems(holder.childContainer, item.servings)
        } else {
            holder.childContainer.visibility = View.GONE
        }
    }

    private fun showChildItems(container: LinearLayout, servings: List<Serving.ServingItem>) {
        container.removeAllViews()

        val inflater = LayoutInflater.from(container.context)

        servings.forEach { serving ->
            lifecycleScope.launch {
                val userRepository = UserRepository()
                val product = with(Dispatchers.IO){
                    userRepository.getProduct(serving.productID)!!
                }

                with(Dispatchers.Main){
                    if(product != null){
                        val childView = inflater.inflate(R.layout.item_product, container, false)
                        //            Log.d("APP_DEBUG","${product}")

                        val totalCalories = product.proteins * 4 + product.fats * 9 + product.carbs * 4
                        childView.findViewById<TextView>(R.id.item_product_tv_title).text = product.title
                        childView.findViewById<TextView>(R.id.item_product_tv_proteins_val).text = product.proteins.toString()
                        childView.findViewById<TextView>(R.id.item_product_tv_fats_val).text = product.fats.toString()
                        childView.findViewById<TextView>(R.id.item_product_tv_carbs_val).text = product.carbs.toString()
                        childView.findViewById<TextView>(R.id.item_product_tv_calories).text = "${totalCalories} ккал"

                        // Optional: Click listener for child items
                        childView.setOnClickListener {
                            //                Toast.makeText(container.context, "Selected: ${meal.name}", Toast.LENGTH_SHORT).show()
                        }

                        container.addView(childView)
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<Meal.MealItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}