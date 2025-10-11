package com.example.basicpedometer

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private val threshold = 6.0  // Adjust based on testing

    private lateinit var tvSteps: TextView
    private lateinit var btnReset: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSteps = findViewById(R.id.tv_steps)
        btnReset = findViewById(R.id.btn_reset)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Permission for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 100
                )
            }
        }

        // Try to get Step Counter Sensor
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounterSensor != null) {
            hasStepCounter = true
        } else {
            // Fall back to Accelerometer
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            hasStepCounter = false
        }

        btnReset.setOnClickListener {
            previousSteps = totalSteps
            accelStepCount = 0
            tvSteps.text = "Steps: 0"
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasStepCounter) {
            stepCounterSensor?.also {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        } else {
            accelerometerSensor?.also {
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
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
