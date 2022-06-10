package com.rudy.finalproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rudy.finalproject.R
import com.rudy.finalproject.database.StepsData

class StepsAdapter(
    private val context: Context,
    private val dataset: List<StepsData>
    ): RecyclerView.Adapter<StepsAdapter.StepsViewHolder>() {

    class StepsViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val steps: TextView = view.findViewById(R.id.data)
        val time: TextView = view.findViewById(R.id.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepsAdapter.StepsViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return StepsAdapter.StepsViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: StepsAdapter.StepsViewHolder, position: Int) {
        val item = dataset[position]

        holder.steps.text = item.steps.toString()
        holder.time.text = java.util.Date(item.time).toString()
    }

    override fun getItemCount() = dataset.size
}