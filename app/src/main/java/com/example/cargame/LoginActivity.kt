package com.example.cargame

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var btnStart: ImageButton
    private lateinit var btn2Buttons: ImageButton
    private lateinit var btn1Sensor: ImageButton
    private lateinit var etName: EditText

    private var selectedGameType: Int? = null

    companion object {
        const val GAME_TYPE_KEY = "GAME_TYPE_KEY"
        const val GAME_TYPE_SENSORS = 0
        const val GAME_TYPE_BUTTONS = 1
        const val PLAYER_NAME_KEY = "PLAYER_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_layout)
        btnStart = findViewById(R.id.btnStartButton)
        btn2Buttons = findViewById(R.id.btn2Button)
        btn1Sensor = findViewById(R.id.btn1Sensor)
        etName = findViewById(R.id.etName)

        btn2Buttons.setOnClickListener {
            selectedGameType = GAME_TYPE_BUTTONS
            Signals.toast("2 Button Mode selected")
        }

        btn1Sensor.setOnClickListener {
            selectedGameType = GAME_TYPE_SENSORS
            Signals.toast("1 Sensor Mode selected")
        }

        btnStart.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Signals.toast("Enter your name")
                return@setOnClickListener
            }

            val gameType = selectedGameType
            if (gameType == null) {
                Signals.toast("Choose a mode first")
                return@setOnClickListener
            }
            val b = Bundle().apply {
                putString(PLAYER_NAME_KEY, name)
                putInt(GAME_TYPE_KEY, gameType)
            }

            startActivity(Intent(this, GameActivity::class.java).apply {
                putExtras(b)
            }
            )
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
