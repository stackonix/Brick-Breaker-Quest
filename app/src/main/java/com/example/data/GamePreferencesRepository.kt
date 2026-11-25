package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.engine.ChallengeCodec
import com.example.domain.model.AppLanguage
import com.example.domain.model.BrainSkills
import com.example.domain.model.GameMode
import com.example.domain.model.GameSettings
import com.example.domain.model.MatchVerdict
import com.example.domain.model.PlayPreference
import com.example.domain.model.RoundOutcome
import com.example.domain.model.TOTAL_CAMPAIGN_LEVELS
import com.example.domain.model.ThemeMode
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max

private val Context.mindfeudDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mindfeud_local_store"
)

/**
 * 100% offline local DataStore persistence (Section 9 & 10).
 * Includes anti-clock-rollback guard (`maxSeenEpochDay`), streak & streak-freeze logic,
 * daily hint reset (3 free/day), and Day 3 streak-freeze gift.
 */
class GamePreferencesRepository(private val context: Context) {

    private object Keys {
        val NICKNAME = stringPreferencesKey("nickname")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val PLAY_PREF = stringPreferencesKey("play_pref")
        val XP = intPreferencesKey("xp")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val BEST_STREAK = intPreferencesKey("best_streak")
        val STREAK_FREEZES = intPreferencesKey("streak_freezes")
        val LAST_PLAYED_DAY = longPreferencesKey("last_played_day")
        val MAX_SEEN_DAY = longPreferencesKey("max_seen_day")
        val LAST_DAILY_DAY = longPreferencesKey("last_daily_day")
        val LAST_DAILY_SCORE = intPreferencesKey("last_daily_score")
        val HINTS_REMAINING = intPreferencesKey("hints_remaining")
        val LAST_HINT_RESET_DAY = longPreferencesKey("last_hint_reset_day")
        val TOTAL_DUELS = intPreferencesKey("total_duels")
        val TOTAL_WINS = intPreferencesKey("total_wins")
        val CONSECUTIVE_LOSSES = intPreferencesKey("consecutive_losses")
        val DUELS_TODAY = intPreferencesKey("duels_today")
        val LAST_DUEL_COUNT_DAY = longPreferencesKey("last_duel_count_day")
        val DISTINCT_DAYS = intPreferencesKey("distinct_days")
        val DAY3_GIFT_CLAIMED = booleanPreferencesKey("day3_gift_claimed")

        val SKILL_LOGIC = intPreferencesKey("skill_logic")
        val SKILL_SPEED = intPreferencesKey("skill_speed")
        val SKILL_MEMORY = intPreferencesKey("skill_memory")
        val SKILL_PATTERN = intPreferencesKey("skill_pattern")
        val SKILL_WORD = intPreferencesKey("skill_word")
        val BASELINE_SKILL_AVG = intPreferencesKey("baseline_skill_avg")
        val RECENT_SCORE_AVG = intPreferencesKey("recent_score_avg")
        val HIGHEST_UNLOCKED_LEVEL = intPreferencesKey("highest_unlocked_level")
        val LEVEL_STARS_MAP = stringPreferencesKey("level_stars_map")

        val LANGUAGE = stringPreferencesKey("lang")
        val THEME_MODE = stringPreferencesKey("theme_mode_v2")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val HARD_MODE = booleanPreferencesKey("hard_mode")
    }

    val userProfileFlow: Flow<UserProfile> = context.mindfeudDataStore.data.map { prefs ->
        UserProfile(
            nickname = prefs[Keys.NICKNAME] ?: "Player",
            onboardingCompleted = prefs[Keys.ONBOARDING_DONE] ?: false,
            playPreference = prefs[Keys.PLAY_PREF]?.let { runCatching { PlayPreference.valueOf(it) }.getOrNull() }
                ?: PlayPreference.BOTH,
            xp = prefs[Keys.XP] ?: 0,
            currentStreak = prefs[Keys.CURRENT_STREAK] ?: 0,
            bestStreak = prefs[Keys.BEST_STREAK] ?: 0,
            streakFreezes = prefs[Keys.STREAK_FREEZES] ?: 2,
            lastPlayedEpochDay = prefs[Keys.LAST_PLAYED_DAY] ?: -1L,
            maxSeenEpochDay = prefs[Keys.MAX_SEEN_DAY] ?: -1L,
            lastDailyCompletedEpochDay = prefs[Keys.LAST_DAILY_DAY] ?: -1L,
            lastDailyScore = prefs[Keys.LAST_DAILY_SCORE] ?: 0,
            hintsRemainingToday = prefs[Keys.HINTS_REMAINING] ?: 3,
            lastHintResetEpochDay = prefs[Keys.LAST_HINT_RESET_DAY] ?: -1L,
            totalDuelsPlayed = prefs[Keys.TOTAL_DUELS] ?: 0,
            totalWins = prefs[Keys.TOTAL_WINS] ?: 0,
            consecutiveLosses = prefs[Keys.CONSECUTIVE_LOSSES] ?: 0,
            duelsPlayedToday = prefs[Keys.DUELS_TODAY] ?: 0,
            lastDuelCountEpochDay = prefs[Keys.LAST_DUEL_COUNT_DAY] ?: -1L,
            distinctDaysPlayed = prefs[Keys.DISTINCT_DAYS] ?: 0,
            day3GiftClaimed = prefs[Keys.DAY3_GIFT_CLAIMED] ?: false,
            brainSkills = BrainSkills(
                logic = prefs[Keys.SKILL_LOGIC] ?: 50,
                speed = prefs[Keys.SKILL_SPEED] ?: 50,
                memory = prefs[Keys.SKILL_MEMORY] ?: 50,
                pattern = prefs[Keys.SKILL_PATTERN] ?: 50,
                word = prefs[Keys.SKILL_WORD] ?: 50
            ),
            baselineSkillAverage = prefs[Keys.BASELINE_SKILL_AVG] ?: 50,
            recentScoreAverage = prefs[Keys.RECENT_SCORE_AVG] ?: 0,
            highestUnlockedLevel = (prefs[Keys.HIGHEST_UNLOCKED_LEVEL] ?: 1).coerceIn(1, TOTAL_CAMPAIGN_LEVELS),
            levelStars = decodeLevelStars(prefs[Keys.LEVEL_STARS_MAP]),
            settings = GameSettings(
                language = prefs[Keys.LANGUAGE]?.let { code ->
                    AppLanguage.entries.firstOrNull { it.code == code }
                } ?: AppLanguage.ENGLISH,
                themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.LIGHT,
                soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
                hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
                reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
                reminderEnabled = prefs[Keys.REMINDER_ENABLED] ?: false,
                hardModeEnabled = prefs[Keys.HARD_MODE] ?: false
            )
        )
    }

    /**
     * Syncs effective day safely (anti-clock-rollback guard per Section 5),
     * resets daily 3 hints when a new forward day arrives, and auto-applies a Streak Freeze
     * if exactly 1 day was missed.
     */
    suspend fun syncCurrentDay(deviceEpochDay: Long) {
        context.mindfeudDataStore.edit { prefs ->
            val maxSeen = prefs[Keys.MAX_SEEN_DAY] ?: -1L
            val effectiveDay = max(deviceEpochDay, maxSeen)
            prefs[Keys.MAX_SEEN_DAY] = effectiveDay

            val lastHintDay = prefs[Keys.LAST_HINT_RESET_DAY] ?: -1L
            if (effectiveDay > lastHintDay) {
                prefs[Keys.HINTS_REMAINING] = 3
                prefs[Keys.LAST_HINT_RESET_DAY] = effectiveDay
            }

            val lastDuelDay = prefs[Keys.LAST_DUEL_COUNT_DAY] ?: -1L
            if (effectiveDay > lastDuelDay) {
                prefs[Keys.DUELS_TODAY] = 0
                prefs[Keys.LAST_DUEL_COUNT_DAY] = effectiveDay
            }

            // Evaluate streak continuity on forward days only
            val lastPlayed = prefs[Keys.LAST_PLAYED_DAY] ?: -1L
            val streak = prefs[Keys.CURRENT_STREAK] ?: 0
            val freezes = prefs[Keys.STREAK_FREEZES] ?: 2
            if (lastPlayed > 0L && streak > 0) {
                val gap = effectiveDay - lastPlayed
                if (gap == 2L && freezes > 0) {
                    // Missed 1 calendar day -> protect streak with 1 Streak Freeze!
                    prefs[Keys.STREAK_FREEZES] = freezes - 1
                    prefs[Keys.LAST_PLAYED_DAY] = effectiveDay - 1L
                } else if (gap > 2L || (gap == 2L && freezes <= 0)) {
                    prefs[Keys.CURRENT_STREAK] = 0
                }
            }
        }
    }

    suspend fun completeOnboarding(preference: PlayPreference, effectiveDay: Long) {
        context.mindfeudDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_DONE] = true
            prefs[Keys.PLAY_PREF] = preference.name
            val currentXp = prefs[Keys.XP] ?: 0
            prefs[Keys.XP] = currentXp + 50
            if ((prefs[Keys.MAX_SEEN_DAY] ?: -1L) < effectiveDay) {
                prefs[Keys.MAX_SEEN_DAY] = effectiveDay
            }
        }
    }

    suspend fun consumeHint(effectiveDay: Long): Boolean {
        var consumed = false
        context.mindfeudDataStore.edit { prefs ->
            val lastReset = prefs[Keys.LAST_HINT_RESET_DAY] ?: -1L
            val currentHints = if (effectiveDay > lastReset) 3 else (prefs[Keys.HINTS_REMAINING] ?: 3)
            if (currentHints > 0) {
                prefs[Keys.HINTS_REMAINING] = currentHints - 1
                prefs[Keys.LAST_HINT_RESET_DAY] = max(effectiveDay, lastReset)
                consumed = true
            }
        }
        return consumed
    }

    data class CommitMatchEffects(
        val newXp: Int,
        val newStreak: Int,
        val firstSessionBadgeUnlocked: Boolean,
        val day3FreezeGiftUnlocked: Boolean,
        val unlockedNextLevel: Boolean = false
    )

    suspend fun recordMatchResult(
        mode: GameMode,
        verdict: MatchVerdict,
        playerScore: Int,
        xpEarned: Int,
        rounds: List<RoundOutcome>,
        deviceEpochDay: Long,
        levelNumber: Int? = null,
        starsEarned: Int = 0
    ): CommitMatchEffects {
        var effects = CommitMatchEffects(0, 0, false, false, false)
        context.mindfeudDataStore.edit { prefs ->
            val maxSeen = prefs[Keys.MAX_SEEN_DAY] ?: -1L
            val effectiveDay = max(deviceEpochDay, maxSeen)
            prefs[Keys.MAX_SEEN_DAY] = effectiveDay

            val prevXp = prefs[Keys.XP] ?: 0
            val updatedXp = prevXp + xpEarned
            prefs[Keys.XP] = updatedXp

            val prevTotalDuels = prefs[Keys.TOTAL_DUELS] ?: 0
            val newTotalDuels = if (mode != GameMode.PRACTICE) prevTotalDuels + 1 else prevTotalDuels
            prefs[Keys.TOTAL_DUELS] = newTotalDuels

            val firstSessionBadge = prevTotalDuels < 3 && newTotalDuels >= 3

            if (verdict == MatchVerdict.WIN) {
                prefs[Keys.TOTAL_WINS] = (prefs[Keys.TOTAL_WINS] ?: 0) + 1
                prefs[Keys.CONSECUTIVE_LOSSES] = 0
            } else if (verdict == MatchVerdict.LOSS) {
                prefs[Keys.CONSECUTIVE_LOSSES] = (prefs[Keys.CONSECUTIVE_LOSSES] ?: 0) + 1
            }

            if (mode == GameMode.DUEL || mode == GameMode.FRIEND_CHALLENGE) {
                val lastDuelDay = prefs[Keys.LAST_DUEL_COUNT_DAY] ?: -1L
                val countToday = if (effectiveDay > lastDuelDay) 1 else (prefs[Keys.DUELS_TODAY] ?: 0) + 1
                prefs[Keys.DUELS_TODAY] = countToday
                prefs[Keys.LAST_DUEL_COUNT_DAY] = effectiveDay
            }

            if (mode == GameMode.DAILY && effectiveDay > (prefs[Keys.LAST_DAILY_DAY] ?: -1L)) {
                prefs[Keys.LAST_DAILY_DAY] = effectiveDay
                prefs[Keys.LAST_DAILY_SCORE] = playerScore
            }

            // Update streak & distinct days (only on forward days, never on clock rollback)
            var currentStreak = prefs[Keys.CURRENT_STREAK] ?: 0
            var bestStreak = prefs[Keys.BEST_STREAK] ?: 0
            val lastPlayed = prefs[Keys.LAST_PLAYED_DAY] ?: -1L
            var distinctDays = prefs[Keys.DISTINCT_DAYS] ?: 0
            var day3GiftUnlocked = false

            if (mode != GameMode.PRACTICE && effectiveDay > lastPlayed) {
                distinctDays += 1
                prefs[Keys.DISTINCT_DAYS] = distinctDays
                currentStreak = if (lastPlayed == -1L || effectiveDay - lastPlayed == 1L) {
                    currentStreak + 1
                } else {
                    1
                }
                if (currentStreak > bestStreak) bestStreak = currentStreak
                prefs[Keys.CURRENT_STREAK] = currentStreak
                prefs[Keys.BEST_STREAK] = bestStreak
                prefs[Keys.LAST_PLAYED_DAY] = effectiveDay

                val claimed = prefs[Keys.DAY3_GIFT_CLAIMED] ?: false
                if (distinctDays >= 3 && !claimed) {
                    prefs[Keys.DAY3_GIFT_CLAIMED] = true
                    prefs[Keys.STREAK_FREEZES] = (prefs[Keys.STREAK_FREEZES] ?: 2) + 1
                    day3GiftUnlocked = true
                }
            }

            // Update Brain Profile 5 skills
            var skills = BrainSkills(
                logic = prefs[Keys.SKILL_LOGIC] ?: 50,
                speed = prefs[Keys.SKILL_SPEED] ?: 50,
                memory = prefs[Keys.SKILL_MEMORY] ?: 50,
                pattern = prefs[Keys.SKILL_PATTERN] ?: 50,
                word = prefs[Keys.SKILL_WORD] ?: 50
            )
            for (r in rounds) {
                val delta = if (r.playerCorrect) {
                    if (r.playerTimeMs < 7500L) 3 else 2
                } else {
                    -1
                }
                skills = skills.updated(r.puzzleType.skill, delta)
            }
            prefs[Keys.SKILL_LOGIC] = skills.logic
            prefs[Keys.SKILL_SPEED] = skills.speed
            prefs[Keys.SKILL_MEMORY] = skills.memory
            prefs[Keys.SKILL_PATTERN] = skills.pattern
            prefs[Keys.SKILL_WORD] = skills.word

            val prevAvg = prefs[Keys.RECENT_SCORE_AVG] ?: playerScore
            prefs[Keys.RECENT_SCORE_AVG] = if (prevAvg == 0) playerScore else ((prevAvg * 3 + playerScore) / 4)

            // Update Campaign Level Stars & Unlock Next Level
            var unlockedNext = false
            if (mode == GameMode.CAMPAIGN_LEVEL && levelNumber != null && starsEarned >= 1) {
                val existingStars = decodeLevelStars(prefs[Keys.LEVEL_STARS_MAP]).toMutableMap()
                val prevStars = existingStars[levelNumber] ?: 0
                if (starsEarned > prevStars) {
                    existingStars[levelNumber] = starsEarned.coerceIn(1, 3)
                    prefs[Keys.LEVEL_STARS_MAP] = encodeLevelStars(existingStars)
                }
                val prevHighest = (prefs[Keys.HIGHEST_UNLOCKED_LEVEL] ?: 1).coerceIn(1, TOTAL_CAMPAIGN_LEVELS)
                val candidateNext = (levelNumber + 1).coerceAtMost(TOTAL_CAMPAIGN_LEVELS)
                if (candidateNext > prevHighest) {
                    prefs[Keys.HIGHEST_UNLOCKED_LEVEL] = candidateNext
                    unlockedNext = true
                }
            }

            effects = CommitMatchEffects(
                newXp = updatedXp,
                newStreak = currentStreak,
                firstSessionBadgeUnlocked = firstSessionBadge,
                day3FreezeGiftUnlocked = day3GiftUnlocked,
                unlockedNextLevel = unlockedNext
            )
        }
        return effects
    }

    private fun decodeLevelStars(raw: String?): Map<Int, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        val result = mutableMapOf<Int, Int>()
        raw.split(",").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val lvl = parts[0].toIntOrNull()
                val stars = parts[1].toIntOrNull()
                if (lvl != null && stars != null && lvl in 1..TOTAL_CAMPAIGN_LEVELS) {
                    result[lvl] = stars.coerceIn(0, 3)
                }
            }
        }
        return result
    }

    private fun encodeLevelStars(map: Map<Int, Int>): String =
        map.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }

    suspend fun updateNickname(rawNickname: String) {
        val clean = ChallengeCodec.sanitizeNickname(rawNickname)
        context.mindfeudDataStore.edit { it[Keys.NICKNAME] = clean }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        context.mindfeudDataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.mindfeudDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.mindfeudDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun updateHapticsEnabled(enabled: Boolean) {
        context.mindfeudDataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun updateReduceMotion(enabled: Boolean) {
        context.mindfeudDataStore.edit { it[Keys.REDUCE_MOTION] = enabled }
    }

    suspend fun updateReminderEnabled(enabled: Boolean) {
        context.mindfeudDataStore.edit { it[Keys.REMINDER_ENABLED] = enabled }
    }

    suspend fun updateHardMode(enabled: Boolean) {
        context.mindfeudDataStore.edit { it[Keys.HARD_MODE] = enabled }
    }
}
