package com.example.muscletruth

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.data.api.models.Product
import com.example.muscletruth.data.api.models.Serving
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServingAdapter(private val onItemClick: (Serving.ServingItem) -> Unit, private val lifecycleScope: LifecycleCoroutineScope) : RecyclerView.Adapter<ServingAdapter.ViewHolder>(){
    var items = emptyList<Serving.ServingItem>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.item_serving_tv_title)
        val tvProteins: TextView = view.findViewById(R.id.item_serving_tv_proteins_val)
        val tvFats: TextView = view.findViewById(R.id.item_serving_tv_fats_val)
        val tvCarbs: TextView = view.findViewById(R.id.item_serving_tv_carbs_val)
        val tvCalories: TextView = view.findViewById(R.id.item_serving_tv_calories)
        val tvAmount: TextView = view.findViewById(R.id.item_serving_tv_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_serving, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        lifecycleScope.launch {
            val product = with(Dispatchers.IO){
                val userRepository = UserRepository()
                userRepository.getProduct(item.productID)!!
            }
            with(Dispatchers.Main){
                Log.d("servings", "product: ${item.product}")
                val totalCalories = (product.proteins * 4 + product.fats * 9 + product.carbs * 4) / 100.00 * item.productAmount
                holder.tvTitle.text = product.title
                holder.tvProteins.text = "${product.proteins / 100.00 * item.productAmount}"
                holder.tvFats.text = "${product.fats / 100.00 * item.productAmount}"
                holder.tvCarbs.text = "${product.carbs / 100.00 * item.productAmount}"
                holder.tvCalories.text = "${totalCalories} ккал"
                holder.tvAmount.text = "${item.productAmount}"

                holder.itemView.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    override fun getItemCount() = items.size
}