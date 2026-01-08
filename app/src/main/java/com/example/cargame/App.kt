package com.example.cargame

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Signals.init(this)
    }
}
