package com.example.muscletruth.ui.Meals

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muscletruth.R
import com.example.muscletruth.data.models.SavedMeal
import com.example.muscletruth.utils.Utils
import com.example.muscletruth.utils.Utils.NetworkUtils.checkForInternetConnection
import java.util.Locale

class SavedMealAdapter(val context: Context, val onItemClick: (item: SavedMeal) -> Unit, val deleteSavedMeal: (item: SavedMeal) -> Unit): RecyclerView.Adapter<SavedMealAdapter.ViewHolder>() {
    var items = mutableListOf<SavedMeal>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView? = view.findViewById(R.id.item_saved_meal_tv_title)
        val tvProteins: TextView? = view.findViewById(R.id.item_saved_meal_tv_proteins_val)
        val tvFats: TextView? = view.findViewById(R.id.item_saved_meal_tv_fats_val)
        val tvCarbs: TextView? = view.findViewById(R.id.item_saved_meal_tv_carbs_val)
        val tvCalories: TextView? = view.findViewById(R.id.item_saved_meal_tv_calories_val)
        val picture: ImageView? = view.findViewById(R.id.item_saved_meal_iv)
        val deleteButton: Button? = view.findViewById(R.id.item_saved_meal_btn_delete)


        init {
            itemView.setOnClickListener {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_meal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle?.text = item.title
        holder.tvProteins?.text = "%.2f".format(Locale.US, item.totalProteins)
        holder.tvFats?.text = "%.2f".format(Locale.US, item.totalFats)
        holder.tvCarbs?.text = "%.2f".format(Locale.US, item.totalCarbs)
        holder.tvCalories?.text = "%.2f".format(Locale.US, item.totalCalories)

        Log.d("APP_DEBUG!", "$item")
        if(holder.picture !== null && item.localImage !== null){
            val path = item.localImage!!
            Glide.with(context)
                .load(path)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.picture)
        }
        else if(holder.picture !== null && checkForInternetConnection() && item.serverImage !== null){
            val path = item.serverImage!!
            Glide.with(context)
                .load(Utils.ImageUtils.getImagePath(path))
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.picture)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.deleteButton?.setOnClickListener {
            deleteSavedMeal(item)
        }
    }

    override fun getItemCount(): Int {
        return items.count()
    }
}