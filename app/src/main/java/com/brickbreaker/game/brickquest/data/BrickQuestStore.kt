package com.brickbreaker.game.brickquest.data

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

/** All quest stages are generated procedurally (see engine/StageGenerator.kt). */
const val TOTAL_QUEST_STAGES = com.brickbreaker.game.brickquest.engine.MAX_STAGE

/**
 * Ball skins unlocked with total stars. The ARGB colors drive the generic glow renderer;
 * the first four skins keep their hand-made look.
 */
enum class BallSkin(
    val displayName: String,
    val unlockStars: Int,
    val glowArgb: Long,
    val ringArgb: Long,
    val coreArgb: Long
) {
    NEON_PEARL("Neon Pearl", 0, 0xFFE879F9, 0xFFF5D0FE, 0xFFFFFFFF),
    STARLIGHT("Starlight", 0, 0xFF38BDF8, 0xFF7DD3FC, 0xFFFFFFFF),
    SOLAR_FIRE("Solar Fire", 6, 0xFFFF7A1A, 0xFFFACC15, 0xFFFFFBEB),
    CYBER_MINT("Cyber Mint", 15, 0xFF34D399, 0xFF6EE7B7, 0xFFFFFFFF),
    ROYAL_GOLD("Royal Gold", 45, 0xFFFBBF24, 0xFFFDE68A, 0xFFFFFFFF),
    PLASMA_ROSE("Plasma Rose", 150, 0xFFF43F5E, 0xFFFDA4AF, 0xFFFFF1F2),
    AQUA_NOVA("Aqua Nova", 400, 0xFF06B6D4, 0xFFA5F3FC, 0xFFFFFFFF),
    VOID_VIOLET("Void Violet", 900, 0xFF8B5CF6, 0xFFC4B5FD, 0xFFF5F3FF),
    LEGEND_WHITE("Legend", 2000, 0xFFF8FAFC, 0xFFE2E8F0, 0xFFFFFFFF)
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
                // First-clear rewards: every 3rd stage +1 Lightning, every 5th +1 Ball Storm,
                // boss stages (every 10th) +2 of each. Keeps power-ups valuable over 1500 stages.
                val boss = stage % 10 == 0
                val lightningGain = (if (stage % 3 == 0) 1 else 0) + (if (boss) 2 else 0)
                val ballsGain = (if (stage % 5 == 0) 1 else 0) + (if (boss) 2 else 0)
                if (lightningGain > 0) {
                    prefs[Keys.LIGHTNING_COUNT] = ((prefs[Keys.LIGHTNING_COUNT] ?: 5) + lightningGain).coerceAtMost(99)
                }
                if (ballsGain > 0) {
                    prefs[Keys.EXTRA_BALLS_COUNT] = ((prefs[Keys.EXTRA_BALLS_COUNT] ?: 5) + ballsGain).coerceAtMost(99)
                }
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
