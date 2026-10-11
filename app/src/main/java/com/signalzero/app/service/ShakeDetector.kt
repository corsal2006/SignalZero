package com.signalzero.app.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

interface ShakeListener {
    fun onShakeTriggered()
}

class ShakeDetector(
    private val context: Context,
    var listener: ShakeListener? = null
) : SensorEventListener {

    private val tag = "SignalZero_Shake"
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var isListening = false
    var isTestMode = false

    private val shakeTimestamps = mutableListOf<Long>()
    private var lastTriggerTime = 0L

    private val accelerationThreshold = 14.5f
    private val windowDurationMs = 2500L
    private val cooldownDurationMs = 5000L
    private val requiredShakes = 3

    fun startListening(onShake: (() -> Unit)? = null) {
        if (onShake != null) {
            listener = object : ShakeListener {
                override fun onShakeTriggered() {
                    onShake()
                }
            }
        }
        start()
    }

    fun stopListening() {
        stop()
    }

    fun start() {
        if (isListening) return
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer != null) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            isListening = true
            Log.i(tag, "Shake detection registered.")
        } else {
            Log.w(tag, "Accelerometer sensor unavailable on this hardware.")
        }
    }

    fun stop() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        shakeTimestamps.clear()
        Log.i(tag, "Shake detection unregistered.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Compute magnitude minus gravity (approx 9.8)
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH

        if (magnitude > accelerationThreshold) {
            val now = System.currentTimeMillis()

            // Filter out events within 200ms of each other (rebound bouncing)
            if (shakeTimestamps.isNotEmpty() && now - shakeTimestamps.last() < 200L) {
                return
            }

            shakeTimestamps.add(now)

            // Prune timestamps outside window
            shakeTimestamps.removeAll { now - it > windowDurationMs }

            if (shakeTimestamps.size >= requiredShakes) {
                if (now - lastTriggerTime > cooldownDurationMs) {
                    lastTriggerTime = now
                    shakeTimestamps.clear()
                    Log.i(tag, "Deliberate 3-shake motion confirmed!")
                    listener?.onShakeTriggered()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
