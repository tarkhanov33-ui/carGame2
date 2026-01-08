package com.example.cargame

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class UpdateScore(
    private val context: Context,
    private val gameType: Int
) {
    fun tryFetchAndUpdateLocation(savedEntry: ScoreEntry) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return

        val fused = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        @SuppressLint("MissingPermission")
        fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    HighScoreStore.updateLocation(
                        context = context,
                        gameType = gameType,
                        ts = savedEntry.ts,
                        lat = loc.latitude,
                        lng = loc.longitude
                    )
                }
            }
    }
}
