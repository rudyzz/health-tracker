package com.rudy.finalproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rudy.finalproject.R
import com.rudy.finalproject.database.HeightData

class HeightAdapter(
    private val context: Context,
    private val dataset: List<HeightData>
    ): RecyclerView.Adapter<HeightAdapter.HeightViewHolder>() {

    class HeightViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val height: TextView = view.findViewById(R.id.data)
        val time: TextView = view.findViewById(R.id.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeightViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return HeightViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: HeightViewHolder, position: Int) {
        val item = dataset[position]

        holder.height.text = item.height.toString()
        holder.time.text = java.util.Date(item.time).toString()
    }

    override fun getItemCount() = dataset.size
}