package com.example.muscletruth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.data.api.models.Weighting
import com.example.muscletruth.utils.Utils.DateUtils

class WeightingAdapter(private val onItemClick: (Weighting.WeightingBase) -> Unit) : RecyclerView.Adapter<WeightingAdapter.ViewHolder>() {
    var items = emptyList<Weighting.WeightingBase>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.item_weighting_tv_date)
        val tvWeight: TextView = view.findViewById(R.id.item_weighting_tv_weight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weighting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvDate.text = DateUtils.convertTimestamp(item.creationDate)
        holder.tvWeight.text = "${item.result} кг"

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size
}