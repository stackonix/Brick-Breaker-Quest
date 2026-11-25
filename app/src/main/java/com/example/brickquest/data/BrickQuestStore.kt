package com.example.brickquest.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.brickQuestDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "brick_breaker_quest_store"
)

const val TOTAL_QUEST_STAGES = 30

enum class BallSkin(
    val displayName: String,
    val unlockStars: Int
) {
    NEON_PEARL("Neon Pearl", 0),
    STARLIGHT("Starlight", 0),
    SOLAR_FIRE("Solar Fire", 6),
    CYBER_MINT("Cyber Mint", 15)
}

data class BrickQuestProgress(
    val highestUnlockedStage: Int = 1,
    val stageStars: Map<Int, Int> = emptyMap(),
    val endlessBestScore: Int = 0,
    val selectedSkin: BallSkin = BallSkin.NEON_PEARL,
    val lightningCount: Int = 5,
    val extraBallsCount: Int = 5,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
) {
    val totalStars: Int get() = stageStars.values.sum()
    val maxStars: Int get() = TOTAL_QUEST_STAGES * 3

    fun starsFor(stage: Int): Int = stageStars[stage] ?: 0
    fun isStageUnlocked(stage: Int): Boolean = stage in 1..highestUnlockedStage
    fun isSkinUnlocked(skin: BallSkin): Boolean = totalStars >= skin.unlockStars
}

class BrickQuestStore(private val context: Context) {

    private object Keys {
        val HIGHEST_STAGE = intPreferencesKey("bq_highest_stage")
        val STAGE_STARS = stringPreferencesKey("bq_stage_stars")
        val ENDLESS_BEST = intPreferencesKey("bq_endless_best")
        val BALL_SKIN = stringPreferencesKey("bq_ball_skin")
        val LIGHTNING_COUNT = intPreferencesKey("bq_lightning_count")
        val EXTRA_BALLS_COUNT = intPreferencesKey("bq_extra_balls_count")
        val SOUND_ENABLED = booleanPreferencesKey("bq_sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("bq_haptics_enabled")
    }

    val progressFlow: Flow<BrickQuestProgress> = context.brickQuestDataStore.data.map { prefs ->
        BrickQuestProgress(
            highestUnlockedStage = (prefs[Keys.HIGHEST_STAGE] ?: 1).coerceIn(1, TOTAL_QUEST_STAGES),
            stageStars = decodeStars(prefs[Keys.STAGE_STARS]),
            endlessBestScore = prefs[Keys.ENDLESS_BEST] ?: 0,
            selectedSkin = prefs[Keys.BALL_SKIN]?.let {
                runCatching { BallSkin.valueOf(it) }.getOrNull()
            } ?: BallSkin.NEON_PEARL,
            lightningCount = (prefs[Keys.LIGHTNING_COUNT] ?: 5).coerceAtLeast(0),
            extraBallsCount = (prefs[Keys.EXTRA_BALLS_COUNT] ?: 5).coerceAtLeast(0),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true
        )
    }

    suspend fun recordStageClear(stage: Int, stars: Int) {
        context.brickQuestDataStore.edit { prefs ->
            val currentStars = decodeStars(prefs[Keys.STAGE_STARS]).toMutableMap()
            val prev = currentStars[stage] ?: 0
            val clampedStars = stars.coerceIn(1, 3)
            if (clampedStars > prev) {
                currentStars[stage] = clampedStars
                prefs[Keys.STAGE_STARS] = encodeStars(currentStars)
            }
            val currentHighest = (prefs[Keys.HIGHEST_STAGE] ?: 1).coerceIn(1, TOTAL_QUEST_STAGES)
            val nextStage = (stage + 1).coerceAtMost(TOTAL_QUEST_STAGES)
            if (nextStage > currentHighest) {
                prefs[Keys.HIGHEST_STAGE] = nextStage
                // Reward player with bonus power-ups on first clear of a new stage
                prefs[Keys.LIGHTNING_COUNT] = ((prefs[Keys.LIGHTNING_COUNT] ?: 5) + 1).coerceAtMost(99)
                prefs[Keys.EXTRA_BALLS_COUNT] = ((prefs[Keys.EXTRA_BALLS_COUNT] ?: 5) + 1).coerceAtMost(99)
            }
        }
    }

    suspend fun recordEndlessScore(score: Int) {
        context.brickQuestDataStore.edit { prefs ->
            val prev = prefs[Keys.ENDLESS_BEST] ?: 0
            if (score > prev) {
                prefs[Keys.ENDLESS_BEST] = score
            }
        }
    }

    suspend fun consumeLightning(): Boolean {
        var used = false
        context.brickQuestDataStore.edit { prefs ->
            val cur = prefs[Keys.LIGHTNING_COUNT] ?: 5
            if (cur > 0) {
                prefs[Keys.LIGHTNING_COUNT] = cur - 1
                used = true
            }
        }
        return used
    }

    suspend fun consumeExtraBalls(): Boolean {
        var used = false
        context.brickQuestDataStore.edit { prefs ->
            val cur = prefs[Keys.EXTRA_BALLS_COUNT] ?: 5
            if (cur > 0) {
                prefs[Keys.EXTRA_BALLS_COUNT] = cur - 1
                used = true
            }
        }
        return used
    }

    suspend fun setBallSkin(skin: BallSkin) {
        context.brickQuestDataStore.edit { it[Keys.BALL_SKIN] = skin.name }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.brickQuestDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.brickQuestDataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    private fun decodeStars(raw: String?): Map<Int, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        val out = mutableMapOf<Int, Int>()
        raw.split(",").forEach { part ->
            val kv = part.split(":")
            if (kv.size == 2) {
                val k = kv[0].toIntOrNull()
                val v = kv[1].toIntOrNull()
                if (k != null && v != null && k in 1..TOTAL_QUEST_STAGES) {
                    out[k] = v.coerceIn(0, 3)
                }
            }
        }
        return out
    }

    private fun encodeStars(map: Map<Int, Int>): String =
        map.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }
}
