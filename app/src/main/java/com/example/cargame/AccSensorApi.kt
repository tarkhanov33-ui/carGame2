package com.example.cargame

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock

interface LaneCallback {
    fun onLane(lane: Int)
}

class AccSensorApi(
    context: Context,
    private val callback: LaneCallback,
    private val cooldownMs: Long = 120L
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastMoveMs: Long = 0L

    private val listener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            val now = SystemClock.uptimeMillis()
            if (now - lastMoveMs < cooldownMs) return

            val x = event.values[0]
            val lane = laneFromAccel(x)
            callback.onLane(lane)
            lastMoveMs = now
        }
    }

    fun start() {
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        sensorManager.unregisterListener(listener, sensor)
    }

    private fun laneFromAccel(x: Float): Int {
        return when {
            x > 6f  -> 4
            x > 3f  -> 3
            x > 1f  -> 2
            x < -6f -> 0
            x < -3f -> 1
            else    -> 2
        }
    }
}
