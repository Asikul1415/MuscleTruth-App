package com.example.muscletruth.ui.Products

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muscletruth.R
import com.example.muscletruth.data.models.Product
import com.example.muscletruth.utils.Utils
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import java.util.Locale

class ProductAdapter(private val onItemClick: (Product) -> Unit, val context: Context? = null) : RecyclerView.Adapter<ProductAdapter.ViewHolder>(), Filterable{
    var items = mutableListOf<Product>()
    val filteredItems = mutableListOf<Product>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.item_product_tv_title)
        val tvProteins: TextView = view.findViewById(R.id.item_product_tv_proteins_val)
        val tvFats: TextView = view.findViewById(R.id.item_product_tv_fats_val)
        val tvCarbs: TextView = view.findViewById(R.id.item_product_tv_carbs_val)
        val tvCalories: TextView = view.findViewById(R.id.item_product_tv_calories_val)
        val picture: ImageView = view.findViewById(R.id.item_product_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredItems[position]
        val totalCalories = item.proteins * 4 + item.fats * 9 + item.carbs * 4
        holder.tvTitle.text = item.title
        holder.tvProteins.text = item.proteins.toString()
        holder.tvFats.text = item.fats.toString()
        holder.tvCarbs.text = item.carbs.toString()
        holder.tvCalories.text = "${totalCalories}"
        Log.d("APP_DEBUG", "PRODUCT: $item")
        if(checkForInternetConnection() === true){
            if(item.serverPicture != null && context != null){
                val path = item.serverPicture
                Glide.with(context)
                    .load(Utils.ImageUtils.getImagePath(path!!))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.picture)
            }
        }
        else{
            if(item.localPicture !== null && context !== null){
                val path = item.localPicture
                Glide.with(context)
                    .load(path)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.picture)
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtered = mutableListOf<Product>()

                if (constraint.isNullOrEmpty()) {
                    filtered.addAll(items)
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault())

                    items.forEach { product ->
                        if (product.title.lowercase(Locale.getDefault()).contains(filterPattern)) {
                            filtered.add(product)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filtered
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems.clear()
                filteredItems.addAll(results?.values as List<Product>)
                notifyDataSetChanged()
            }
        }
    }

    fun submitList(list: List<Product>) {
        items.clear()
        items.addAll(list)
        filteredItems.clear()
        filteredItems.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount() = filteredItems.size
}