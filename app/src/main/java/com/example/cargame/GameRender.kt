package com.example.cargame

import android.view.View
import android.widget.ImageView
import android.widget.TextView

class GameRender(
    private val scoreTv: TextView,
    private val life1: ImageView,
    private val life2: ImageView,
    private val life3: ImageView,
    private val cars: Array<ImageView>,
    private val rocks: Array<Array<ImageView>>,
    private val coins: Array<Array<ImageView>>,
) {
     fun renderAll(engine: GameEngine) {
        renderLives(engine.lives)
        renderScore(engine.score)
        renderCar(engine.lane)
        renderMatrix(rocks, engine.rockMatrix)
        renderMatrix(coins, engine.coinMatrix)
    }
     fun renderScore(score: Int)  {
        scoreTv.text = "Score:$score"
    }
     fun renderCar(lane: Int) {
        for (i in cars.indices) {
            cars[i].visibility = if (i == lane) View.VISIBLE else View.INVISIBLE
        }
    }
     fun renderMatrix(viewGrid: Array<Array<ImageView>>, matrix: Array<IntArray>) {
        for (r in matrix.indices) {
            for (c in matrix[r].indices) {
                viewGrid[r][c].visibility = if (matrix[r][c] == 1) View.VISIBLE else View.INVISIBLE
            }
        }
    }
     fun renderLives(lives: Int) {
        life1.visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        life2.visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        life3.visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE
    }

}