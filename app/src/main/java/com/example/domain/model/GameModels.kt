package com.example.domain.model

import androidx.annotation.DrawableRes
import com.example.R

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    HINDI("hi")
}

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

enum class PlayPreference {
    DAILY,
    DUEL,
    BOTH
}

enum class SkillType {
    LOGIC,
    SPEED,
    MEMORY,
    PATTERN,
    WORD
}

enum class PuzzleType(val skill: SkillType) {
    NUMBER_CHAIN(SkillType.LOGIC),
    CODE_BREAKER(SkillType.LOGIC),
    ODD_SHAPE(SkillType.PATTERN),
    QUICK_CALC(SkillType.SPEED),
    WORD_TWIST(SkillType.WORD),
    MEMORY_GRID(SkillType.MEMORY)
}

enum class GameMode {
    ONBOARDING,
    CAMPAIGN_LEVEL,
    DAILY,
    DUEL,
    FRIEND_CHALLENGE,
    PRACTICE
}

const val TOTAL_CAMPAIGN_LEVELS = 20

data class CampaignLevelMeta(
    val levelNumber: Int,
    val chapterIndex: Int,
    val roundCount: Int,
    val difficulty: Int
) {
    companion object {
        fun forLevel(levelNumber: Int): CampaignLevelMeta {
            val clamped = levelNumber.coerceIn(1, TOTAL_CAMPAIGN_LEVELS)
            val chapter = (clamped - 1) / 5
            val rounds = when {
                clamped <= 5 -> 3
                clamped <= 10 -> 4
                else -> 5
            }
            val diff = when {
                clamped <= 6 -> 1
                clamped <= 14 -> 2
                else -> 3
            }
            return CampaignLevelMeta(
                levelNumber = clamped,
                chapterIndex = chapter,
                roundCount = rounds,
                difficulty = diff
            )
        }
    }
}

enum class RankTier(
    val minXp: Int,
    val levelIndex: Int
) {
    ROOKIE(0, 0),
    THINKER(500, 1),
    STRATEGIST(1500, 2),
    MASTERMIND(4000, 3),
    LEGEND(10000, 4);

    fun nextTier(): RankTier? = entries.getOrNull(ordinal + 1)

    companion object {
        fun fromXp(xp: Int): RankTier {
            return entries.lastOrNull { xp >= it.minXp } ?: ROOKIE
        }
    }
}

enum class RivalId(
    val unlockRank: RankTier,
    @DrawableRes val avatarRes: Int,
    val baseSpeedFactor: Float, // Lower = faster answers (0.35 = very fast, 0.72 = deliberate)
    val baseAccuracy: Float,    // 0.0..1.0
    val strongSkill: SkillType?,
    val weakSkill: SkillType?
) {
    QUICK_QASIM(
        unlockRank = RankTier.ROOKIE,
        avatarRes = R.drawable.img_rival_qasim,
        baseSpeedFactor = 0.38f,
        baseAccuracy = 0.66f,
        strongSkill = SkillType.SPEED,
        weakSkill = SkillType.LOGIC
    ),
    PATTERN_PRANAV(
        unlockRank = RankTier.ROOKIE,
        avatarRes = R.drawable.img_rival_pranav,
        baseSpeedFactor = 0.54f,
        baseAccuracy = 0.76f,
        strongSkill = SkillType.PATTERN,
        weakSkill = SkillType.WORD
    ),
    CALM_KAVYA(
        unlockRank = RankTier.THINKER,
        avatarRes = R.drawable.img_rival_kavya,
        baseSpeedFactor = 0.66f,
        baseAccuracy = 0.86f,
        strongSkill = SkillType.LOGIC,
        weakSkill = SkillType.SPEED
    ),
    BOSS_BHAVNA(
        unlockRank = RankTier.MASTERMIND,
        avatarRes = R.drawable.img_rival_bhavna,
        baseSpeedFactor = 0.48f,
        baseAccuracy = 0.90f,
        strongSkill = null,
        weakSkill = null
    )
}

data class BrainSkills(
    val logic: Int = 50,
    val speed: Int = 50,
    val memory: Int = 50,
    val pattern: Int = 50,
    val word: Int = 50
) {
    fun scoreFor(skill: SkillType): Int = when (skill) {
        SkillType.LOGIC -> logic
        SkillType.SPEED -> speed
        SkillType.MEMORY -> memory
        SkillType.PATTERN -> pattern
        SkillType.WORD -> word
    }

    fun average(): Int = (logic + speed + memory + pattern + word) / 5

    fun updated(skill: SkillType, delta: Int): BrainSkills {
        fun clamp(v: Int) = v.coerceIn(10, 99)
        return when (skill) {
            SkillType.LOGIC -> copy(logic = clamp(logic + delta))
            SkillType.SPEED -> copy(speed = clamp(speed + delta))
            SkillType.MEMORY -> copy(memory = clamp(memory + delta))
            SkillType.PATTERN -> copy(pattern = clamp(pattern + delta))
            SkillType.WORD -> copy(word = clamp(word + delta))
        }
    }
}

data class GameSettings(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val reminderEnabled: Boolean = false,
    val hardModeEnabled: Boolean = false
)

data class UserProfile(
    val nickname: String = "Player",
    val onboardingCompleted: Boolean = false,
    val playPreference: PlayPreference = PlayPreference.BOTH,
    val xp: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val streakFreezes: Int = 2,
    val lastPlayedEpochDay: Long = -1L,
    val maxSeenEpochDay: Long = -1L,
    val lastDailyCompletedEpochDay: Long = -1L,
    val lastDailyScore: Int = 0,
    val hintsRemainingToday: Int = 3,
    val lastHintResetEpochDay: Long = -1L,
    val totalDuelsPlayed: Int = 0,
    val totalWins: Int = 0,
    val consecutiveLosses: Int = 0,
    val duelsPlayedToday: Int = 0,
    val lastDuelCountEpochDay: Long = -1L,
    val distinctDaysPlayed: Int = 0,
    val day3GiftClaimed: Boolean = false,
    val brainSkills: BrainSkills = BrainSkills(),
    val baselineSkillAverage: Int = 50,
    val recentScoreAverage: Int = 0,
    val highestUnlockedLevel: Int = 1,
    val levelStars: Map<Int, Int> = emptyMap(),
    val settings: GameSettings = GameSettings()
) {
    val rank: RankTier get() = RankTier.fromXp(xp)

    fun totalCampaignStars(): Int = levelStars.values.sum()

    fun starsForLevel(level: Int): Int = levelStars[level] ?: 0

    fun isLevelUnlocked(level: Int): Boolean = level in 1..highestUnlockedLevel

    fun isDailyCompleted(currentEffectiveDay: Long): Boolean =
        lastDailyCompletedEpochDay >= currentEffectiveDay && currentEffectiveDay > 0L

    fun isRivalUnlocked(rival: RivalId): Boolean =
        rank.ordinal >= rival.unlockRank.ordinal

    fun unlockedBadgesCount(): Int {
        var count = 0
        if (totalDuelsPlayed >= 3) count++
        if (totalWins >= 1) count++
        if (bestStreak >= 7) count++
        if (bestStreak >= 30) count++
        if (rank.ordinal >= RankTier.THINKER.ordinal) count++
        if (rank.ordinal >= RankTier.MASTERMIND.ordinal) count++
        if (rank == RankTier.LEGEND) count++
        return count
    }
}

data class FriendChallengeData(
    val code: String,
    val seed: Long,
    val challengerName: String,
    val targetScore: Int?
)

data class RoundOutcome(
    val roundIndex: Int,
    val puzzleType: PuzzleType,
    val playerCorrect: Boolean,
    val playerPoints: Int,
    val playerTimeMs: Long,
    val rivalCorrect: Boolean,
    val rivalPoints: Int,
    val rivalTimeMs: Long
)

enum class MatchVerdict {
    WIN,
    DRAW,
    LOSS,
    COMPLETED
}

data class MatchResult(
    val mode: GameMode,
    val verdict: MatchVerdict,
    val playerScore: Int,
    val rivalScore: Int,
    val rivalId: RivalId?,
    val friendChallenge: FriendChallengeData?,
    val dailyEpochDay: Long?,
    val rounds: List<RoundOutcome>,
    val xpEarned: Int,
    val previousRank: RankTier,
    val newRank: RankTier,
    val streakAfter: Int,
    val firstSessionBadgeUnlocked: Boolean,
    val day3FreezeGiftUnlocked: Boolean,
    val levelNumber: Int? = null,
    val starsEarned: Int = 0,
    val unlockedNextLevel: Boolean = false
) {
    val accuracyPercent: Int
        get() = if (rounds.isEmpty()) 0 else (rounds.count { it.playerCorrect } * 100) / rounds.size

    val averageTimeSec: Float
        get() = if (rounds.isEmpty()) 0f else (rounds.sumOf { it.playerTimeMs } / rounds.size) / 1000f
}
