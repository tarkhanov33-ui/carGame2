package com.example.cargame

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ScoreEntry(
    val name: String,
    val score: Int,
    val ts: Long = System.currentTimeMillis(),
    val gameType: Int = LoginActivity.GAME_TYPE_BUTTONS,
    val lat: Double? = null,
    val lng: Double? = null
)

object HighScoreStore {
    private const val PREF = "high_scores_pref"
    private const val KEY_ALL = "scores_all"
    private const val KEY_BUTTONS = "scores_buttons"
    private const val KEY_SENSORS = "scores_sensors"

    private fun keyFor(gameType: Int?): String = when (gameType) {
        LoginActivity.GAME_TYPE_SENSORS -> KEY_SENSORS
        LoginActivity.GAME_TYPE_BUTTONS -> KEY_BUTTONS
        else -> KEY_ALL
    }

    fun addScore(context: Context, entry: ScoreEntry, keepTop: Int = 10) {
        val list = load(context, entry.gameType).toMutableList()
        list.add(entry)
        val sorted = list
            .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenByDescending { it.ts })
            .take(keepTop)

        save(context, entry.gameType, sorted)
        val all = load(context, null).toMutableList()
        all.add(entry.copy(gameType = -1))
        val sortedAll = all
            .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenByDescending { it.ts })
            .take(keepTop)
        save(context, null, sortedAll)
    }

    fun load(context: Context, gameType: Int?): List<ScoreEntry> {

        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val raw = sp.getString(keyFor(gameType), "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = ArrayList<ScoreEntry>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val lat = if (o.has("lat") && !o.isNull("lat")) o.optDouble("lat") else null
            val lng = if (o.has("lng") && !o.isNull("lng")) o.optDouble("lng") else null
            out.add(
                ScoreEntry(
                    name = o.optString("name", "---"),
                    score = o.optInt("score", 0),
                    ts = o.optLong("ts", 0L),
                    gameType = o.optInt("gameType", gameType ?: -1),
                    lat = lat,
                    lng = lng
                )
            )
        }
        return out
    }

    private fun save(context: Context, gameType: Int?, list: List<ScoreEntry>) {
        val arr = JSONArray()
        for (e in list) {
            arr.put(
                JSONObject()
                    .put("name", e.name)
                    .put("score", e.score)
                    .put("ts", e.ts)
                    .put("gameType", e.gameType)
                    .put("lat", e.lat)
                    .put("lng", e.lng)
            )
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(keyFor(gameType), arr.toString())
            .apply()
    }
    fun updateLocation(context: Context, gameType: Int, ts: Long, lat: Double, lng: Double) {
        run {
            val list = load(context, gameType).toMutableList()
            val idx = list.indexOfFirst { it.ts == ts }
            if (idx >= 0) {
                list[idx] = list[idx].copy(lat = lat, lng = lng)
                save(context, gameType, list)
            }
        }

        run {
            val all = load(context, null).toMutableList()
            val idxAll = all.indexOfFirst { it.ts == ts }
            if (idxAll >= 0) {
                all[idxAll] = all[idxAll].copy(lat = lat, lng = lng)
                save(context, null, all)
            }
        }
    }
}
