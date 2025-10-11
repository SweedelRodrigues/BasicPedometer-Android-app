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
import android.widget.TextView
import com.google.android.material.button.MaterialButton
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
    private val threshold = 6.0  // Adjust this threshold for accelerometer-based step detection

    private lateinit var tvSteps: TextView
    private lateinit var btnReset: MaterialButton
    private lateinit var btnStart: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        tvSteps = findViewById(R.id.tv_steps)
        btnReset = findViewById(R.id.btn_reset)
        btnStart = findViewById(R.id.btn_start)

        // Initialize SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Request permission for Android 10+ (Q)
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

        // Get Step Counter sensor if available
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        hasStepCounter = stepCounterSensor != null

        if (!hasStepCounter) {
            // Fallback to accelerometer
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }

        // Start button listener (optional: reset counters)
        btnStart.setOnClickListener {
            previousSteps = totalSteps
            accelStepCount = 0
            tvSteps.text = "0"
        }

        // Reset button listener
        btnReset.setOnClickListener {
            previousSteps = totalSteps
            accelStepCount = 0
            tvSteps.text = "0"
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
                val currentSteps = (totalSteps - previousSteps).toInt()
                tvSteps.text = "$currentSteps"
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
                    tvSteps.text = "$accelStepCount"
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
