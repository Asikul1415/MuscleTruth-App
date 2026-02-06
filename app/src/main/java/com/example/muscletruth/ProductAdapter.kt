package com.example.muscletruth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.data.api.models.Product

class ProductAdapter(private val onItemClick: (Product.ProductBase) -> Unit) : RecyclerView.Adapter<ProductAdapter.ViewHolder>(){
    var items = emptyList<Product.ProductBase>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.item_product_tv_title)
        val tvProteins: TextView = view.findViewById(R.id.item_product_tv_proteins_val)
        val tvFats: TextView = view.findViewById(R.id.item_product_tv_fats_val)
        val tvCarbs: TextView = view.findViewById(R.id.item_product_tv_carbs_val)
        val tvCalories: TextView = view.findViewById(R.id.item_product_tv_calories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val totalCalories = item.proteins * 4 + item.fats * 9 + item.carbs * 4
        holder.tvTitle.text = item.title
        holder.tvProteins.text = item.proteins.toString()
        holder.tvFats.text = item.fats.toString()
        holder.tvCarbs.text = item.carbs.toString()
        holder.tvCalories.text = "${totalCalories} ккал"

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size
}