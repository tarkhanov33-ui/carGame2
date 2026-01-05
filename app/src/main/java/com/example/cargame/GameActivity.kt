package com.example.cargame

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cargame.LoginActivity.Companion.GAME_TYPE_KEY
import com.example.cargame.LoginActivity.Companion.PLAYER_NAME_KEY

class GameActivity : AppCompatActivity(), AccSensorCallBack {

    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton

    private lateinit var life1: ImageView
    private lateinit var life2: ImageView
    private lateinit var life3: ImageView

    private lateinit var scoreTv: TextView

    private lateinit var cars: Array<ImageView>
    private lateinit var rocks: Array<Array<ImageView>>
    private lateinit var coins: Array<Array<ImageView>>

    private lateinit var engine: GameEngine
    companion object {
        const val GAME_SCORE_KEY = "GAME_SCORE_KEY"}
    private val handler = Handler(Looper.getMainLooper())
    private val tickDelay = 350L
    private var timerOn = false

    private var playerName: String = ""
    private var gameType: Int = LoginActivity.GAME_TYPE_BUTTONS

    private var accApi: AccSensorApi? = null
    private var lastMoveMs: Long = 0L
    private val sensorCooldownMs = 120L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent.extras?.let { b ->
            playerName = b.getString(LoginActivity.PLAYER_NAME_KEY).orEmpty()
            gameType = b.getInt(LoginActivity.GAME_TYPE_KEY, LoginActivity.GAME_TYPE_BUTTONS)
        }

        setContentView(R.layout.activity_game)

        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)

        life1 = findViewById(R.id.life1)
        life2 = findViewById(R.id.life2)
        life3 = findViewById(R.id.life3)

        scoreTv = findViewById(R.id.score)

        cars = arrayOf(
            findViewById(R.id.car0),
            findViewById(R.id.car1),
            findViewById(R.id.car2),
            findViewById(R.id.car3),
            findViewById(R.id.car4),
        )

        engine = GameEngine(rows = 9, cols = 5, coinEveryTicks = 3)

        rocks = Array(engine.rows) { r ->
            Array(engine.cols) { c ->
                val idName = "rock_${r}_${c}"
                val id = resources.getIdentifier(idName, "id", packageName)
                findViewById(id)
            }
        }

        coins = Array(engine.rows) { r ->
            Array(engine.cols) { c ->
                val idName = "coin_${r}_${c}"
                val id = resources.getIdentifier(idName, "id", packageName)
                findViewById(id)
            }
        }

        if (gameType == LoginActivity.GAME_TYPE_SENSORS) {
            btnLeft.visibility = View.GONE
            btnRight.visibility = View.GONE
            accApi = AccSensorApi(this, this)
        } else {
            btnLeft.visibility = View.VISIBLE
            btnRight.visibility = View.VISIBLE

            btnLeft.setOnClickListener {
                engine.moveLeft()
                renderAll()
                val res = engine.checkNow()
                handleStepResult(res)
            }

            btnRight.setOnClickListener {
                engine.moveRight()
                renderAll()
                val res = engine.checkNow()
                handleStepResult(res)
            }
        }

        engine.reset()
        renderAll()
        startLoop()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        if (gameType == LoginActivity.GAME_TYPE_SENSORS) {
            accApi?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (gameType == LoginActivity.GAME_TYPE_SENSORS) {
            accApi?.stop()
        }
    }

    override fun onStop() {
        super.onStop()
        stopLoop()
    }


    private fun startLoop() {
        if (timerOn) return
        timerOn = true
        handler.postDelayed(loopRunnable, tickDelay)
    }

    private fun stopLoop() {
        timerOn = false
        handler.removeCallbacks(loopRunnable)
    }

    private val loopRunnable = object : Runnable {
        override fun run() {
            if (!timerOn) return
            val res = engine.step()
            renderAll()
            handleStepResult(res)
            handler.postDelayed(this, tickDelay)
        }
    }


    override fun data(x: Float, y: Float, z: Float) {
        if (gameType != LoginActivity.GAME_TYPE_SENSORS) return

        val now = SystemClock.uptimeMillis()
        if (now - lastMoveMs < sensorCooldownMs) return

        val newLane = laneFromAccel(x) // 0..4
        if (newLane != engine.lane) {
            engine.setLane(newLane)
            renderAll()
            val res = engine.checkNow()
            handleStepResult(res)
            lastMoveMs = now
        }
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


    private fun renderAll() {
        renderLives(engine.lives)
        renderScore(engine.score)
        renderCar(engine.lane)
        renderMatrix(rocks, engine.rockMatrix)
        renderMatrix(coins, engine.coinMatrix)
    }

    private fun renderLives(lives: Int) {
        life1.visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        life2.visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        life3.visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE
    }

    private fun renderScore(score: Int) {
        scoreTv.text = "Score:$score"
    }

    private fun renderCar(lane: Int) {
        for (i in cars.indices) {
            cars[i].visibility = if (i == lane) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun renderMatrix(viewGrid: Array<Array<ImageView>>, matrix: Array<IntArray>) {
        for (r in matrix.indices) {
            for (c in matrix[r].indices) {
                viewGrid[r][c].visibility = if (matrix[r][c] == 1) View.VISIBLE else View.INVISIBLE
            }
        }
    }


    private fun handleStepResult(res: StepResult) {
        if (res.hitRock) vibrate()
        if (res.gameOver) gameOver()
    }

    private fun gameOver() {
        stopLoop()
        Toast.makeText(
            this,
            "Game Over${if (playerName.isNotBlank()) ", $playerName" else ""}",
            Toast.LENGTH_SHORT
        ).show()


        val intent = Intent(this, ScoreActivity::class.java).apply {
            putExtra(GAME_SCORE_KEY, engine.score)
            putExtra(PLAYER_NAME_KEY, playerName)
            putExtra(GAME_TYPE_KEY, gameType)        }

        startActivity(intent)
        finish()

    }

    private fun vibrate() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (!v.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(150)
        }
    }
}
