package com.example.basicpedometer

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recycler_history)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load history data
        val sharedPref = getSharedPreferences("StepData", Context.MODE_PRIVATE)
        val historyString = sharedPref.getString("history", "") ?: ""

        val historyList = mutableListOf<Pair<String, Int>>()

        if (historyString.isNotEmpty()) {
            val entries = historyString.trim().split("\n")
            for (entry in entries) {
                val parts = entry.split(" - ")
                if (parts.size == 2) {
                    val date = parts[0]
                    val steps = parts[1].replace("steps", "").trim().toIntOrNull() ?: 0
                    historyList.add(Pair(date, steps))
                }
            }
        }

        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter
    }
}



