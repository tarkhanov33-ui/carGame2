package com.example.cargame

import kotlin.random.Random

data class StepResult(
    val hitRock: Boolean = false,
    val gotCoin: Boolean = false,
    val gameOver: Boolean = false
)


class GameEngine(
    val rows: Int = 9,
    val cols: Int = 5,
    private val coinEveryTicks: Int = 3,
    private val coinBonus: Int = 50,
    private val survivalPoints: Int = 1
) {
    val rockMatrix: Array<IntArray> = Array(rows) { IntArray(cols) { 0 } }
    val coinMatrix: Array<IntArray> = Array(rows) { IntArray(cols) { 0 } }

    var lane: Int = cols / 2
        private set

    var lives: Int = 3
        private set

    var score: Int = 0
        private set

    private var tickCounter: Int = 0

    fun reset() {
        lives = 3
        score = 0
        lane = cols / 2
        tickCounter = 0
        clearMatrices()
    }

    fun moveLeft() {
        lane = (lane - 1).coerceAtLeast(0)
    }

    fun moveRight() {
        lane = (lane + 1).coerceAtMost(cols - 1)
    }

    fun setLane(newLane: Int) {
        lane = newLane.coerceIn(0, cols - 1)
    }


    fun step(): StepResult {
        shiftDown(rockMatrix)
        shiftDown(coinMatrix)

        spawnRock()

        if (coinEveryTicks > 0 && tickCounter % coinEveryTicks == 0) {
            spawnCoin()
        }
        tickCounter++
        score += survivalPoints
        return checkBottomRow()
    }


    fun checkNow(): StepResult = checkBottomRow()

    private fun checkBottomRow(): StepResult {
        val bottom = rows - 1
        var hit = false
        var coin = false

        if (rockMatrix[bottom][lane] == 1) {
            rockMatrix[bottom][lane] = 0
            lives--
            hit = true
        }

        if (coinMatrix[bottom][lane] == 1) {
            coinMatrix[bottom][lane] = 0
            score += coinBonus
            coin = true
        }

        return StepResult(
            hitRock = hit,
            gotCoin = coin,
            gameOver = (lives <= 0)
        )
    }

    private fun clearMatrices() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                rockMatrix[r][c] = 0
                coinMatrix[r][c] = 0
            }
        }
    }

    private fun shiftDown(m: Array<IntArray>) {
        for (r in rows - 1 downTo 1) {
            for (c in 0 until cols) {
                m[r][c] = m[r - 1][c]
            }
        }
        for (c in 0 until cols) {
            m[0][c] = 0
        }
    }

    private fun spawnRock() {
        val c = Random.nextInt(cols)
        rockMatrix[0][c] = 1
        coinMatrix[0][c] = 0
    }

    private fun spawnCoin() {
        val c = Random.nextInt(cols)
        if (rockMatrix[0][c] == 0) {
            coinMatrix[0][c] = 1
        }
    }
}
