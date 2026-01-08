package com.example.cargame

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast

object Signals {

    @Volatile
    private var appCtx: Context? = null

    fun init(context: Context) {
        appCtx = context.applicationContext
    }

    private fun ctx(): Context =
        appCtx ?: throw IllegalStateException("Signals.init(context) must be called in Application")

    fun toast(msg: String) {
        Toast.makeText(ctx(), msg, Toast.LENGTH_SHORT).show()
    }

    fun vibrate() {
        val context = ctx()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager ?: return
            vm.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION") v.vibrate(200)
            }
        }
    }
}
