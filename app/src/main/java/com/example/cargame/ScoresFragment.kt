package com.example.cargame

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class ScoresFragment : Fragment(R.layout.table_layout) {

    companion object {
        const val RESULT_KEY = "score_loc"
        const val KEY_NAME = "name"
        const val KEY_SCORE = "score"
        const val KEY_LAT = "lat"
        const val KEY_LON = "lng"

        private const val ARG_GAME_TYPE = "ARG_GAME_TYPE"

        fun newInstance(gameType: Int?): ScoresFragment {
            return ScoresFragment().apply {
                arguments = Bundle().apply {
                    if (gameType != null) putInt(ARG_GAME_TYPE, gameType)
                }
            }
        }
    }

    private var gameType: Int? = null
    private var rows: Array<RowViews>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameType = arguments?.takeIf { it.containsKey(ARG_GAME_TYPE) }?.getInt(ARG_GAME_TYPE)

        rows = Array(10) { i -> RowViews(view, i + 1) }

        rows?.forEach { row ->
            row.rootRow.setOnClickListener {
                val e = row.current ?: return@setOnClickListener
                if (e.name == "---") return@setOnClickListener

                parentFragmentManager.setFragmentResult(
                    RESULT_KEY,
                    Bundle().apply {
                        putString(KEY_NAME, e.name)
                        putInt(KEY_SCORE, e.score)
                        putDouble(KEY_LAT, e.lat ?: Double.NaN)
                        putDouble(KEY_LON, e.lng ?: Double.NaN)
                    }
                )
            }
        }

        render()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val ctx = context ?: return
        val rowsLocal = rows ?: return

        val list = HighScoreStore.load(ctx, gameType)

        for (i in 0 until 10) {
            val entry = list.getOrNull(i)
            rowsLocal[i].bind(entry)
        }
    }

    private class RowViews(rootView: View, idx: Int) {
        val rootRow: LinearLayout = rootView.findViewById(
            rootView.resources.getIdentifier("row$idx", "id", rootView.context.packageName)
        )

        private val nickTv: TextView = rootView.findViewById(
            rootView.resources.getIdentifier("nick$idx", "id", rootView.context.packageName)
        )

        private val scoreTv: TextView = rootView.findViewById(
            rootView.resources.getIdentifier("score$idx", "id", rootView.context.packageName)
        )

        var current: ScoreEntry? = null
            private set

        fun bind(entry: ScoreEntry?) {
            current = entry
            nickTv.text = entry?.name ?: "---"
            scoreTv.text = (entry?.score ?: 0).toString()
        }
    }
}
