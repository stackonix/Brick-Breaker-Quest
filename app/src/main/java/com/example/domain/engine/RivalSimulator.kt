package com.example.domain.engine

import com.example.domain.model.RankTier
import com.example.domain.model.RivalId

data class RivalRoundPlan(
    val willBeCorrect: Boolean,
    val solveDelayMs: Long,
    val points: Int
)

/**
 * Offline AI Rival simulator (Section 5 & Section 10).
 * Models each rival's speed, accuracy, strong/weak skills, rank scaling, and 3-loss mercy easing.
 */
object RivalSimulator {

    fun planRound(
        rival: RivalId,
        puzzle: Puzzle,
        playerRank: RankTier,
        consecutiveLosses: Int,
        rng: DeterministicRandom
    ): RivalRoundPlan {
        val skill = puzzle.type.skill
        var accuracy = rival.baseAccuracy
        if (rival.strongSkill == skill) accuracy += 0.12f
        if (rival.weakSkill == skill) accuracy -= 0.16f

        // Match difficulty to player's rank slightly
        val rankAdjustment = (playerRank.levelIndex - rival.unlockRank.levelIndex) * 0.03f
        accuracy += rankAdjustment

        // Mercy rule (Section 5): after 3 consecutive losses, ease rival slightly to avoid frustration
        val mercyActive = consecutiveLosses >= 3
        if (mercyActive) {
            accuracy -= 0.14f
        }
        accuracy = accuracy.coerceIn(0.35f, 0.94f)

        val willBeCorrect = rng.nextFloat() <= accuracy

        val limitMs = puzzle.timeLimitSec * 1000L
        var speedFactor = rival.baseSpeedFactor
        if (rival.strongSkill == skill) speedFactor -= 0.10f
        if (rival.weakSkill == skill) speedFactor += 0.12f
        if (mercyActive) speedFactor += 0.12f

        val jitter = (rng.nextFloat() - 0.5f) * 0.18f
        val clampedRatio = (speedFactor + jitter).coerceIn(0.24f, 0.88f)
        val solveDelayMs = (limitMs * clampedRatio).toLong().coerceIn(2200L, limitMs - 800L)

        val points = if (willBeCorrect) {
            val wordLen = if (puzzle is WordTwistPuzzle) {
                if (rival.strongSkill == skill) 5 else 4
            } else 0
            PuzzleEngine.calculatePoints(puzzle, true, solveDelayMs, wordLen)
        } else {
            0
        }

        return RivalRoundPlan(
            willBeCorrect = willBeCorrect,
            solveDelayMs = solveDelayMs,
            points = points
        )
    }
}
