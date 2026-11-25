package com.example

import com.example.domain.engine.ChallengeCodec
import com.example.domain.engine.CodeBreakerPuzzle
import com.example.domain.engine.DeterministicRandom
import com.example.domain.engine.MemoryGridPuzzle
import com.example.domain.engine.NumberChainPuzzle
import com.example.domain.engine.OddShapePuzzle
import com.example.domain.engine.PuzzleEngine
import com.example.domain.engine.QuickCalcPuzzle
import com.example.domain.engine.WordTwistPuzzle
import com.example.domain.model.PuzzleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleEngineTest {

    @Test
    fun dailySetIsDeterministicAcrossDevices() {
        val setA = PuzzleEngine.generateDailySet(epochDay = 20417L, hardMode = false)
        val setB = PuzzleEngine.generateDailySet(epochDay = 20417L, hardMode = false)
        assertEquals(5, setA.size)
        assertEquals(setA.map { it.type }, setB.map { it.type })

        val numA = setA[0] as NumberChainPuzzle
        val numB = setB[0] as NumberChainPuzzle
        assertEquals(numA.sequence, numB.sequence)
        assertEquals(numA.correctAnswer, numB.correctAnswer)
        assertTrue(numA.correctAnswer in numA.options)
    }

    @Test
    fun allSixPuzzleGeneratorsProduceValidSolvablePuzzles() {
        val rng = DeterministicRandom(123456L)
        for (type in PuzzleType.entries) {
            val puzzle = PuzzleEngine.generatePuzzle(type, rng, difficulty = 2)
            when (puzzle) {
                is NumberChainPuzzle -> {
                    assertEquals(4, puzzle.sequence.size)
                    assertEquals(4, puzzle.options.size)
                    assertTrue(puzzle.correctAnswer in puzzle.options)
                }
                is CodeBreakerPuzzle -> {
                    assertEquals(4, puzzle.secretCode.size)
                    val feedback = PuzzleEngine.evaluateCodeGuess(puzzle.secretCode, puzzle.secretCode)
                    assertEquals(4, feedback.exactMatches)
                    assertEquals(0, feedback.colorOnlyMatches)
                }
                is OddShapePuzzle -> {
                    assertEquals(6, puzzle.tiles.size)
                    assertTrue(puzzle.oddIndex in 0..5)
                }
                is QuickCalcPuzzle -> {
                    assertEquals(5, puzzle.cards.size)
                    assertTrue(PuzzleEngine.isQuickCalcCorrect(puzzle, puzzle.solutionCardIds))
                }
                is WordTwistPuzzle -> {
                    assertEquals(6, puzzle.letters.size)
                    assertTrue(PuzzleEngine.isValidWordTwistSubmission(puzzle, puzzle.anchorWord))
                    assertFalse(PuzzleEngine.isValidWordTwistSubmission(puzzle, "ZZZZZZ"))
                }
                is MemoryGridPuzzle -> {
                    assertTrue(puzzle.targetCells.isNotEmpty())
                    assertTrue(puzzle.targetCells.all { it in 0 until (puzzle.gridSize * puzzle.gridSize) })
                }
            }
        }
    }

    @Test
    fun accuracyOutweighsSpeedInScoring() {
        val puzzle = PuzzleEngine.generateOnboardingPuzzle()
        val slowCorrect = PuzzleEngine.calculatePoints(puzzle, isCorrect = true, elapsedMs = 28000L)
        val fastWrong = PuzzleEngine.calculatePoints(puzzle, isCorrect = false, elapsedMs = 500L)
        val fastCorrect = PuzzleEngine.calculatePoints(puzzle, isCorrect = true, elapsedMs = 2000L)
        assertTrue(slowCorrect > fastWrong)
        assertTrue(fastCorrect > slowCorrect)
    }

    @Test
    fun friendChallengeCodecRoundTripsAndSanitizesNickname() {
        val code = ChallengeCodec.generateCode(999999L)
        assertTrue(ChallengeCodec.isValidCode(code))
        val link = ChallengeCodec.buildDeepLink(code, 540, "Aarav 9876543210")
        val parsed = ChallengeCodec.parseInput(link)
        assertNotNull(parsed)
        assertEquals(code, parsed!!.code)
        assertEquals(540, parsed.targetScore)
        assertEquals("Aarav", parsed.challengerName)
        assertEquals("Player", ChallengeCodec.sanitizeNickname("fuck123"))
    }
}
