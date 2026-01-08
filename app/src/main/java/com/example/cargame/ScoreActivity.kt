package com.example.cargame

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ScoreActivity : AppCompatActivity() {
    private lateinit var tvScoreValue: TextView
    private lateinit var btnScore: ImageButton
    private var finalScore: Int = 0
    private var playerName: String = ""
    private var gameType: Int = LoginActivity.GAME_TYPE_BUTTONS
    private var savedEntry: ScoreEntry? = null
    private lateinit var updater: UpdateScore

    private val requestLocPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            savedEntry?.let { updater.tryFetchAndUpdateLocation(it) }
        }
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
        updater = UpdateScore(this, gameType)

        if (playerName.isNotBlank()) {
            val entry = ScoreEntry(
                name = playerName,
                score = finalScore,
                gameType = gameType
            )
            savedEntry = entry

            HighScoreStore.addScore(this, entry, sync = true)

            val lat = intent.getDoubleExtra("lat", Double.NaN)
            val lng = intent.getDoubleExtra("lng", Double.NaN)

            if (!lat.isNaN() && !lng.isNaN()) {
                HighScoreStore.updateLocation(
                    context = this,
                    gameType = gameType,
                    ts = entry.ts,
                    lat = lat,
                    lng = lng,
                    sync = true
                )
            } else {
                val hasFine = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val hasCoarse = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasFine || hasCoarse) {
                    updater.tryFetchAndUpdateLocation(entry)
                } else {
                    requestLocPerms.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
            }
        }

        btnScore.setOnClickListener {
            startActivity(Intent(this, TableActivity::class.java))
            finish()
        }
    }
}
