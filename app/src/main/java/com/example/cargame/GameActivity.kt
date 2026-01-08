package com.example.cargame
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cargame.LoginActivity.Companion.GAME_TYPE_KEY
import com.example.cargame.LoginActivity.Companion.PLAYER_NAME_KEY

class GameActivity : AppCompatActivity() {

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
    private lateinit var loop: GameLoop
    private lateinit var render: GameRender

    companion object {
        const val GAME_SCORE_KEY = "GAME_SCORE_KEY"
    }

    private val tickDelay = 350L

    private var playerName: String = ""
    private var gameType: Int = LoginActivity.GAME_TYPE_BUTTONS

    private var accApi: AccSensorApi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent.extras?.let { b ->
            playerName = b.getString(PLAYER_NAME_KEY).orEmpty()
            gameType = b.getInt(GAME_TYPE_KEY, LoginActivity.GAME_TYPE_BUTTONS)
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
                val id = resources.getIdentifier("rock_${r}_${c}", "id", packageName)
                findViewById(id)
            }
        }

        coins = Array(engine.rows) { r ->
            Array(engine.cols) { c ->
                val id = resources.getIdentifier("coin_${r}_${c}", "id", packageName)
                findViewById(id)
            }
        }

        render = GameRender(
            scoreTv = scoreTv,
            life1 = life1,
            life2 = life2,
            life3 = life3,
            cars = cars,
            rocks = rocks,
            coins = coins
        )

        if (gameType == LoginActivity.GAME_TYPE_SENSORS) {
            btnLeft.visibility = View.GONE
            btnRight.visibility = View.GONE

            accApi = AccSensorApi(this, object : LaneCallback {
                override fun onLane(lane: Int) {
                    if (lane != engine.lane) {
                        engine.setLane(lane)
                        render.renderAll(engine)
                        handleStepResult(engine.checkNow())
                    }
                }
            })
        } else {
            btnLeft.visibility = View.VISIBLE
            btnRight.visibility = View.VISIBLE

            btnLeft.setOnClickListener {
                engine.moveLeft()
                render.renderAll(engine)
                handleStepResult(engine.checkNow())
            }

            btnRight.setOnClickListener {
                engine.moveRight()
                render.renderAll(engine)
                handleStepResult(engine.checkNow())
            }
        }

        engine.reset()
        render.renderAll(engine)

        loop = GameLoop(tickDelay) {
            val res = engine.step()
            render.renderAll(engine)
            handleStepResult(res)
        }
        loop.start()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        if (gameType == LoginActivity.GAME_TYPE_SENSORS) accApi?.start()
    }

    override fun onPause() {
        super.onPause()
        if (gameType == LoginActivity.GAME_TYPE_SENSORS) accApi?.stop()
    }

    override fun onStop() {
        super.onStop()
        if (::loop.isInitialized) loop.stop()
    }

    private fun handleStepResult(res: StepResult) {
        if (res.hitRock) Signals.vibrate()
        if (res.gameOver) gameOver()
    }

    private fun gameOver() {
        if (::loop.isInitialized) loop.stop()
        Signals.toast("Game Over${if (playerName.isNotBlank()) ", $playerName" else ""}")

        val intent = Intent(this, ScoreActivity::class.java).apply {
            putExtra(GAME_SCORE_KEY, engine.score)
            putExtra(PLAYER_NAME_KEY, playerName)
            putExtra(GAME_TYPE_KEY, gameType)
        }
        startActivity(intent)
        finish()
    }
}
