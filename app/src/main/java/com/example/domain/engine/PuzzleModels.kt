package com.example.domain.engine

import com.example.domain.model.PuzzleType

sealed class Puzzle(
    val id: String,
    val type: PuzzleType,
    val timeLimitSec: Int
)

/**
 * 1. Number Chain (Logic): Find the next number in a deterministic mathematical pattern.
 */
class NumberChainPuzzle(
    id: String,
    val sequence: List<Int>,
    val options: List<Int>,
    val correctAnswer: Int,
    val ruleTag: String,
    timeLimitSec: Int = 15
) : Puzzle(id, PuzzleType.NUMBER_CHAIN, timeLimitSec)

/**
 * 2. Code Breaker (Deduction — Mastermind style): Crack the 4-peg secret code using color + symbol pegs.
 * Each peg index is 0..5 (representing 6 distinct color+symbol pairs for color-blind accessibility).
 */
class CodeBreakerPuzzle(
    id: String,
    val secretCode: List<Int>, // length 4, values 0..5
    val paletteSize: Int = 6,
    val maxAttempts: Int = 5,
    timeLimitSec: Int = 25
) : Puzzle(id, PuzzleType.CODE_BREAKER, timeLimitSec)

data class CodeGuessFeedback(
    val guess: List<Int>,
    val exactMatches: Int,     // Correct color + correct spot
    val colorOnlyMatches: Int  // Correct color + wrong spot
)

enum class ShapeKind {
    CIRCLE,
    TRIANGLE,
    SQUARE,
    DIAMOND,
    HEXAGON,
    STAR
}

data class ShapeTileSpec(
    val shape: ShapeKind,
    val colorIndex: Int,     // 0=Cyan, 1=Violet, 2=Mint, 3=Gold, 4=Coral
    val isFilled: Boolean,
    val innerDots: Int,      // 0, 1, 2, 3
    val rotationDeg: Float   // 0f, 45f, 90f, 180f
)

/**
 * 3. Odd Shape (Pattern): 6 geometric canvas cards where 5 follow one rule and 1 differs.
 */
class OddShapePuzzle(
    id: String,
    val tiles: List<ShapeTileSpec>,
    val oddIndex: Int,
    timeLimitSec: Int = 14
) : Puzzle(id, PuzzleType.ODD_SHAPE, timeLimitSec)

enum class CalcOp(val symbol: String) {
    ADD("+"),
    SUB("−"),
    MUL("×")
}

/**
 * 4. Quick Calc (Speed): Start from [startValue], choose 3 steps in sequence to reach [targetValue].
 * Each step has a pair of (op, operand) choices or 4 operation cards to pick 3 in order.
 */
data class CalcCard(
    val id: Int,
    val op: CalcOp,
    val operand: Int
) {
    fun applyTo(value: Int): Int = when (op) {
        CalcOp.ADD -> value + operand
        CalcOp.SUB -> value - operand
        CalcOp.MUL -> value * operand
    }

    fun label(): String = "${op.symbol}$operand"
}

class QuickCalcPuzzle(
    id: String,
    val startValue: Int,
    val targetValue: Int,
    val cards: List<CalcCard>,       // 5 cards total
    val solutionCardIds: List<Int>,  // Exact 3 card IDs in order that produce targetValue
    timeLimitSec: Int = 18
) : Puzzle(id, PuzzleType.QUICK_CALC, timeLimitSec)

/**
 * 5. Word Twist (Word): Form the longest valid word from 6 scrambled letter tiles.
 */
class WordTwistPuzzle(
    id: String,
    val letters: List<Char>, // 6 scrambled uppercase letters
    val anchorWord: String,  // 6-letter optimal word
    val validWords: Set<String>,
    timeLimitSec: Int = 18
) : Puzzle(id, PuzzleType.WORD_TWIST, timeLimitSec)

/**
 * 6. Memory Grid (Memory): Watch highlighted cells for 3 seconds, then tap the exact same cells.
 */
class MemoryGridPuzzle(
    id: String,
    val gridSize: Int,           // 3 (3x3) or 4 (4x4)
    val targetCells: Set<Int>,   // cell indices 0 until (gridSize * gridSize)
    val previewDurationMs: Long = 3000L,
    timeLimitSec: Int = 16
) : Puzzle(id, PuzzleType.MEMORY_GRID, timeLimitSec)
