package com.example.muscletruth.ui.Products

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muscletruth.R
import com.example.muscletruth.data.models.Product
import com.example.muscletruth.data.repositories.ProductRepository
import com.example.muscletruth.utils.Utils
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import kotlinx.coroutines.launch
import java.util.Locale

class ProductAdapter(private val onItemClick: (Product) -> Unit, val context: Context? = null, val lifecycle: LifecycleCoroutineScope, val notifyItemChanged: () -> Unit) : RecyclerView.Adapter<ProductAdapter.ViewHolder>(), Filterable{
    var items = mutableListOf<Product>()
    val filteredItems = mutableListOf<Product>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.item_product_tv_title)
        val tvProteins: TextView = view.findViewById(R.id.item_product_tv_proteins_val)
        val tvFats: TextView = view.findViewById(R.id.item_product_tv_fats_val)
        val tvCarbs: TextView = view.findViewById(R.id.item_product_tv_carbs_val)
        val tvCalories: TextView = view.findViewById(R.id.item_product_tv_calories_val)
        val picture: ImageView = view.findViewById(R.id.item_product_iv)
        val favouriteButton: Button = view.findViewById(R.id.item_product_btn_favourite)
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
        if(checkForInternetConnection() === true && item.serverPicture !== null && context != null){
            val path = item.serverPicture
            Glide.with(context)
                .load(Utils.ImageUtils.getImagePath(path!!))
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.picture)
        }
        else if(item.localPicture !== null && context !== null){
            val path = item.localPicture
            Glide.with(context)
                .load(path)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.picture)
        }
        else if(context !== null){
            Glide.with(context)
                .load(R.drawable.ic_launcher_foreground)
                .into(holder.picture)
        }

        lifecycle.launch {
            Log.d("APP_DEBUG!", "${ProductRepository.getFavouriteProducts().find{product -> product.productLocalID == item.localID}}")
            if(ProductRepository.getFavouriteProduct(item.serverID, item.localID) !== null){
                holder.favouriteButton.text = "♥"
            }
            else{
                holder.favouriteButton.text = "♡"
            }
        }

        holder.favouriteButton.setOnClickListener {
            lifecycle.launch {
                //If product not favourite
                if(ProductRepository.getFavouriteProduct(item.serverID, item.localID) !== null){
                    holder.favouriteButton.text = "♡"
                    ProductRepository.deleteFavouriteProduct(item.serverID, item.localID)
                }
                //If product already favourite
                else{
                    holder.favouriteButton.text = "♥"
                    ProductRepository.addFavouriteProduct(item.serverID, item.localID)
                }
                notifyItemChanged()
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