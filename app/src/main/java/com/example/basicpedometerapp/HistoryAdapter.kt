package com.example.basicpedometer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val historyList: List<Triple<String, Int, Int>>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.tv_date)
        val stepsText: TextView = view.findViewById(R.id.tv_steps)
        val totalStepsText: TextView = view.findViewById(R.id.tv_total_steps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (date, steps, totalSteps) = historyList[position]
        holder.dateText.text = date
        holder.stepsText.text = "Steps: $steps"
        holder.totalStepsText.text = "Total steps that day: $totalSteps"
    }

    override fun getItemCount() = historyList.size
}
