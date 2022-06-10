package com.rudy.finalproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rudy.finalproject.R
import com.rudy.finalproject.database.HeartRateData

class HeartRateAdapter(
    private val context: Context,
    private val dataset: List<HeartRateData>
): RecyclerView.Adapter<HeartRateAdapter.HeartRateViewHolder>() {

    class HeartRateViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val heartRate: TextView = view.findViewById(R.id.data)
        val time: TextView = view.findViewById(R.id.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeartRateAdapter.HeartRateViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return HeartRateAdapter.HeartRateViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: HeartRateAdapter.HeartRateViewHolder, position: Int) {
        val item = dataset[position]

        holder.heartRate.text = item.heartRate.toString()
        holder.time.text = java.util.Date(item.time).toString()
    }

    override fun getItemCount() = dataset.size
}