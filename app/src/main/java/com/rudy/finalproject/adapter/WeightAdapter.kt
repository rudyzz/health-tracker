package com.rudy.finalproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rudy.finalproject.R
import com.rudy.finalproject.database.HeightData
import com.rudy.finalproject.database.WeightData

class WeightAdapter(
    private val context: Context,
    private val dataset: List<WeightData>
    ): RecyclerView.Adapter<WeightAdapter.WeightViewHolder>() {

    class WeightViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val weight: TextView = view.findViewById(R.id.data)
        val time: TextView = view.findViewById(R.id.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightAdapter.WeightViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return WeightAdapter.WeightViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: WeightAdapter.WeightViewHolder, position: Int) {
        val item = dataset[position]

        holder.weight.text = item.weight.toString()
        holder.time.text = java.util.Date(item.time).toString()
    }

    override fun getItemCount() = dataset.size
}