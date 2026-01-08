package com.example.cargame

import android.os.Handler
import android.os.Looper

class GameLoop(
    private val tickDelay: Long,
    private val onTick: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val runnable = object : Runnable {
        override fun run() {
            if (!running) return
            onTick()
            handler.postDelayed(this, tickDelay)
        }
    }

    fun start() {
        if (running) return
        running = true
        handler.postDelayed(runnable, tickDelay)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(runnable)
    }

    fun isRunning(): Boolean = running
}
