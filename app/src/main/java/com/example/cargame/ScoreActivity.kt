package com.example.cargame

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class ScoreActivity : AppCompatActivity() {

    private lateinit var tvScoreValue: TextView
    private lateinit var btnScore: ImageButton

    private var finalScore: Int = 0
    private var playerName: String = ""
    private var gameType: Int = LoginActivity.GAME_TYPE_BUTTONS

    private lateinit var savedEntry: ScoreEntry

    private val requestLocPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) tryFetchAndUpdateLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        tvScoreValue = findViewById(R.id.tvScoreValue)
        btnScore = findViewById(R.id.btnScore)

        finalScore = intent.getIntExtra(GameActivity.GAME_SCORE_KEY, 0)
        playerName = intent.getStringExtra(LoginActivity.PLAYER_NAME_KEY).orEmpty()
        gameType = intent.getIntExtra(LoginActivity.GAME_TYPE_KEY, LoginActivity.GAME_TYPE_BUTTONS)

        tvScoreValue.text = finalScore.toString()

        if (playerName.isNotBlank()) {
            savedEntry = ScoreEntry(name = playerName, score = finalScore, gameType = gameType)
            HighScoreStore.addScore(this, savedEntry)
        }

        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            tryFetchAndUpdateLocation()
        } else {
            requestLocPerms.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

        btnScore.setOnClickListener {
            startActivity(Intent(this, TableActivity::class.java).apply {
                putExtra(LoginActivity.GAME_TYPE_KEY, gameType)
            })
            finish()
        }
    }

    private fun tryFetchAndUpdateLocation() {
        if (!::savedEntry.isInitialized) return

        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return

        val fused = LocationServices.getFusedLocationProviderClient(this)
        val cts = CancellationTokenSource()

        @SuppressLint("MissingPermission")
        fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    HighScoreStore.updateLocation(
                        context = this,
                        gameType = gameType,
                        ts = savedEntry.ts,
                        lat = loc.latitude,
                        lng = loc.longitude
                    )
                }
            }
    }
}
