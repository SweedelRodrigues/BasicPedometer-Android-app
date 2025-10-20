package com.example.basicpedometer


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private var hasStepCounter = false
    private var totalSteps = 0f
    private var previousSteps = 0f
    private var accelStepCount = 0
    private var previousMagnitude = 0.0
    private val threshold = 6.0  // Adjust this threshold for accelerometer-based step detection

    private lateinit var tvSteps: TextView
    private lateinit var btnReset: Button
    private lateinit var btnHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        tvSteps = findViewById(R.id.tv_steps)
        btnReset = findViewById(R.id.btn_reset)
        btnHistory = findViewById(R.id.btn_history)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Load previous step count
        loadData()

        // Ask permission for physical activity (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    100
                )
            }
        }

        // Detect available sensor
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounterSensor != null) {
            hasStepCounter = true
        } else {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }

        // Reset button
        btnReset.setOnClickListener {
            saveHistory() // Save before resetting
            previousSteps = totalSteps
            accelStepCount = 0
            tvSteps.text = "Steps: 0"
            saveData(0)
        }

        // View History button
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasStepCounter) {
            stepCounterSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        } else {
            accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                if (previousSteps == 0f) previousSteps = event.values[0]
                totalSteps = event.values[0]
                val currentSteps = totalSteps - previousSteps
                tvSteps.text = "Steps: ${currentSteps.toInt()}"
                saveData(currentSteps.toInt())
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val magnitude = sqrt((x * x + y * y + z * z).toDouble())
                val delta = magnitude - previousMagnitude
                previousMagnitude = magnitude

                if (delta > threshold) {
                    accelStepCount++
                    tvSteps.text = "Steps: $accelStepCount"
                    saveData(accelStepCount)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun saveData(steps: Int) {
        val sharedPref = getSharedPreferences("StepData", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("steps", steps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPref = getSharedPreferences("StepData", Context.MODE_PRIVATE)
        val savedSteps = sharedPref.getInt("steps", 0)
        tvSteps.text = "Steps: $savedSteps"
    }

    private fun saveHistory() {
        val sharedPref = getSharedPreferences("StepData", Context.MODE_PRIVATE)
        val currentSteps = sharedPref.getInt("steps", 0)
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val history = sharedPref.getString("history", "") ?: ""
        val newHistory = "$history\n$date - $currentSteps steps"
        val editor = sharedPref.edit()
        editor.putString("history", newHistory.trim())
        editor.apply()
    }
}







