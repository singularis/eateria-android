package com.singularis.eateria.services

import android.content.Context
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Local activity history (iOS UserDefaults parity). ActivityStatisticsView reads the
 * per-day summaries; ActivitiesView, the chess history and the orb cards read chess scores.
 */
object ActivityLocalStore {
    private const val SUMMARY_PREFS = "app_settings"
    private const val CHESS_PREFS = "eateria_prefs"
    private const val KEY_TOTAL = "activity_summary_total_"
    private const val KEY_TYPES = "activity_summary_types_"
    private const val KEY_CHESS_WINS = "chessTotalWins"
    private const val KEY_CHESS_OPPONENTS = "chessOpponents"

    /** Empty dateISO means today, matching the activities endpoints. */
    fun dayKey(dateISO: String): String =
        dateISO.ifBlank {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())
        }

    fun saveSummary(context: Context, dateISO: String, totalCalories: Int, types: List<String>) {
        val day = dayKey(dateISO)
        context.getSharedPreferences(SUMMARY_PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_TOTAL + day, totalCalories)
            .putString(KEY_TYPES + day, types.distinct().joinToString(","))
            .apply()
    }

    fun chessTotalWins(context: Context): Int =
        context.getSharedPreferences(CHESS_PREFS, Context.MODE_PRIVATE).getInt(KEY_CHESS_WINS, 0)

    /** Applies one game ("me", "opponent" or "draw") and returns the new (my wins, their wins). */
    fun recordChessResult(context: Context, opponentEmail: String, winner: String): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(CHESS_PREFS, Context.MODE_PRIVATE)
        val opponents = runCatching { JSONObject(prefs.getString(KEY_CHESS_OPPONENTS, "{}") ?: "{}") }.getOrDefault(JSONObject())
        val parts = opponents.optString(opponentEmail, "0:0").split(":")
        var mine = parts.getOrNull(0)?.toIntOrNull() ?: 0
        var theirs = parts.getOrNull(1)?.toIntOrNull() ?: 0
        var totalWins = prefs.getInt(KEY_CHESS_WINS, 0)
        when (winner) {
            "me" -> {
                mine++
                totalWins++
            }
            "opponent" -> theirs++
        }
        opponents.put(opponentEmail, "$mine:$theirs")
        prefs.edit()
            .putString(KEY_CHESS_OPPONENTS, opponents.toString())
            .putInt(KEY_CHESS_WINS, totalWins)
            .apply()
        return mine to theirs
    }

    /** Server score "wins:losses" replaces the local guess once the backend confirms the game. */
    fun setOpponentScore(context: Context, opponentEmail: String, score: String) {
        if (!score.matches(Regex("\\d+:\\d+"))) return
        val prefs = context.getSharedPreferences(CHESS_PREFS, Context.MODE_PRIVATE)
        val opponents = runCatching { JSONObject(prefs.getString(KEY_CHESS_OPPONENTS, "{}") ?: "{}") }.getOrDefault(JSONObject())
        opponents.put(opponentEmail, score)
        prefs.edit().putString(KEY_CHESS_OPPONENTS, opponents.toString()).apply()
    }
}
