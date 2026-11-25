package com.example.domain.engine

import com.example.domain.model.CampaignLevelMeta
import com.example.domain.model.PuzzleType
import com.example.domain.model.RankTier
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Pure Kotlin puzzle generator and validator with zero Android/UI dependencies.
 * Guarantees identical puzzle sequences for identical seeds (Daily Puzzle & Friend Challenge).
 */
object PuzzleEngine {

    /**
     * Generates the 10-second onboarding puzzle (guaranteed clear & welcoming).
     */
    fun generateOnboardingPuzzle(): NumberChainPuzzle {
        return NumberChainPuzzle(
            id = "onboarding_1",
            sequence = listOf(4, 8, 12, 16),
            options = listOf(18, 20, 22, 24),
            correctAnswer = 20,
            ruleTag = "+4",
            timeLimitSec = 30
        )
    }

    /**
     * Generates the fixed 5-puzzle Daily Gauntlet for a given epoch day.
     * Everyone playing on [epochDay] gets the exact same 5 puzzles.
     */
    fun generateDailySet(epochDay: Long, hardMode: Boolean = false): List<Puzzle> {
        val rng = DeterministicRandom(epochDay * 7919L + 104729L)
        val types = listOf(
            PuzzleType.NUMBER_CHAIN,
            PuzzleType.ODD_SHAPE,
            PuzzleType.QUICK_CALC,
            PuzzleType.WORD_TWIST,
            PuzzleType.MEMORY_GRID
        )
        val difficulty = if (hardMode) 3 else 2
        return types.mapIndexed { idx, type ->
            generatePuzzle(
                type = type,
                rng = DeterministicRandom(rng.nextLong() xor (idx + 1L)),
                difficulty = difficulty,
                id = "daily_${epochDay}_$idx"
            )
        }
    }

    /**
     * Generates a 5-puzzle Duel set tailored to the player's rank (and Code Breaker unlocks at Thinker+).
     */
    fun generateDuelSet(
        seed: Long,
        rank: RankTier,
        hardMode: Boolean = false,
        includeCodeBreaker: Boolean = rank.ordinal >= RankTier.THINKER.ordinal
    ): List<Puzzle> {
        val rng = DeterministicRandom(seed)
        val pool = mutableListOf(
            PuzzleType.NUMBER_CHAIN,
            PuzzleType.ODD_SHAPE,
            PuzzleType.QUICK_CALC,
            PuzzleType.WORD_TWIST,
            PuzzleType.MEMORY_GRID
        )
        if (includeCodeBreaker) {
            pool.add(PuzzleType.CODE_BREAKER)
        }
        val selectedTypes = rng.shuffle(pool).take(5)
        val difficulty = when {
            hardMode -> 3
            rank.ordinal >= RankTier.MASTERMIND.ordinal -> 3
            rank.ordinal >= RankTier.THINKER.ordinal -> 2
            else -> 1
        }
        return selectedTypes.mapIndexed { idx, type ->
            generatePuzzle(
                type = type,
                rng = DeterministicRandom(rng.nextLong() xor (idx * 31L + 7L)),
                difficulty = difficulty,
                id = "duel_${seed}_$idx"
            )
        }
    }

    /**
     * Generates a deterministic 5-puzzle Friend Challenge set from a shared challenge seed.
     */
    fun generateFriendChallengeSet(seed: Long): List<Puzzle> {
        val rng = DeterministicRandom(seed * 3571L + 17L)
        val types = listOf(
            PuzzleType.NUMBER_CHAIN,
            PuzzleType.ODD_SHAPE,
            PuzzleType.QUICK_CALC,
            PuzzleType.WORD_TWIST,
            PuzzleType.MEMORY_GRID
        )
        val ordered = rng.shuffle(types)
        return ordered.mapIndexed { idx, type ->
            generatePuzzle(
                type = type,
                rng = DeterministicRandom(rng.nextLong() xor (idx + 99L)),
                difficulty = 2,
                id = "friend_${seed}_$idx"
            )
        }
    }

    /**
     * Generates a deterministic, progressively challenging puzzle set for Campaign Levels 1..20.
     */
    fun generateCampaignLevelSet(levelNumber: Int): List<Puzzle> {
        val meta = CampaignLevelMeta.forLevel(levelNumber)
        val rng = DeterministicRandom(meta.levelNumber * 9973L + 4242L)
        val pool = when {
            meta.levelNumber <= 2 -> listOf(
                PuzzleType.NUMBER_CHAIN,
                PuzzleType.ODD_SHAPE,
                PuzzleType.QUICK_CALC
            )
            meta.levelNumber <= 5 -> listOf(
                PuzzleType.NUMBER_CHAIN,
                PuzzleType.ODD_SHAPE,
                PuzzleType.QUICK_CALC,
                PuzzleType.WORD_TWIST,
                PuzzleType.MEMORY_GRID
            )
            else -> listOf(
                PuzzleType.NUMBER_CHAIN,
                PuzzleType.ODD_SHAPE,
                PuzzleType.QUICK_CALC,
                PuzzleType.WORD_TWIST,
                PuzzleType.MEMORY_GRID,
                PuzzleType.CODE_BREAKER
            )
        }
        val selectedTypes = rng.shuffle(pool).take(meta.roundCount)
        return selectedTypes.mapIndexed { idx, type ->
            generatePuzzle(
                type = type,
                rng = DeterministicRandom(rng.nextLong() xor (idx * 73L + meta.levelNumber * 19L)),
                difficulty = meta.difficulty,
                id = "level_${meta.levelNumber}_round_$idx"
            )
        }
    }

    /**
     * Calculates 0..3 stars for a Campaign Level completion.
     * 1+ star clears the level and unlocks the next level.
     */
    fun calculateLevelStars(correctCount: Int, totalRounds: Int): Int {
        if (totalRounds <= 0 || correctCount <= 0) return 0
        val pct = (correctCount * 100) / totalRounds
        return when {
            correctCount >= totalRounds -> 3
            pct >= 60 -> 2
            else -> 1
        }
    }

    fun generatePuzzle(
        type: PuzzleType,
        rng: DeterministicRandom,
        difficulty: Int = 1,
        id: String = "${type.name}_${rng.nextInt(100000)}"
    ): Puzzle {
        return when (type) {
            PuzzleType.NUMBER_CHAIN -> generateNumberChain(id, rng, difficulty)
            PuzzleType.CODE_BREAKER -> generateCodeBreaker(id, rng, difficulty)
            PuzzleType.ODD_SHAPE -> generateOddShape(id, rng, difficulty)
            PuzzleType.QUICK_CALC -> generateQuickCalc(id, rng, difficulty)
            PuzzleType.WORD_TWIST -> generateWordTwist(id, rng, difficulty)
            PuzzleType.MEMORY_GRID -> generateMemoryGrid(id, rng, difficulty)
        }
    }

    // 1. NUMBER CHAIN (Logic)
    private fun generateNumberChain(id: String, rng: DeterministicRandom, difficulty: Int): NumberChainPuzzle {
        val patternKind = when (difficulty) {
            1 -> rng.nextInt(3) // 0=arithmetic, 1=increasing step, 2=doubling/x2+k
            2 -> rng.nextInt(5)
            else -> rng.nextInt(1, 6)
        }
        val seq: List<Int>
        val answer: Int
        val ruleTag: String

        when (patternKind) {
            0 -> {
                val start = rng.nextInt(2, 18)
                val step = rng.nextInt(3, 12)
                seq = List(4) { start + it * step }
                answer = start + 4 * step
                ruleTag = "+$step"
            }
            1 -> {
                // Growing difference: +d, +(d+inc), +(d+2*inc)...
                val start = rng.nextInt(2, 12)
                val baseStep = rng.nextInt(2, 6)
                val inc = rng.nextInt(2, 5)
                val nums = mutableListOf(start)
                var cur = start
                var step = baseStep
                repeat(4) {
                    cur += step
                    nums.add(cur)
                    step += inc
                }
                seq = nums.take(4)
                answer = nums[4]
                ruleTag = "+$baseStep, +${baseStep + inc}..."
            }
            2 -> {
                // Multiply by 2 + offset
                val start = rng.nextInt(2, 6)
                val offset = rng.nextInt(0, 4)
                val nums = mutableListOf(start)
                var cur = start
                repeat(4) {
                    cur = cur * 2 + offset
                    nums.add(cur)
                }
                seq = nums.take(4)
                answer = nums[4]
                ruleTag = if (offset == 0) "×2" else "×2 + $offset"
            }
            3 -> {
                // Alternating +a, -b
                val start = rng.nextInt(10, 25)
                val up = rng.nextInt(5, 11)
                val down = rng.nextInt(2, 5)
                val nums = mutableListOf(start)
                var cur = start
                for (i in 0 until 4) {
                    cur = if (i % 2 == 0) cur + up else cur - down
                    nums.add(cur)
                }
                seq = nums.take(4)
                answer = nums[4]
                ruleTag = "+$up, -$down"
            }
            4 -> {
                // Squares + offset
                val base = rng.nextInt(2, 6)
                val offset = rng.nextInt(0, 5)
                seq = List(4) { i -> (base + i) * (base + i) + offset }
                answer = (base + 4) * (base + 4) + offset
                ruleTag = "n²"
            }
            else -> {
                // Fibonacci-like
                val a = rng.nextInt(2, 8)
                val b = rng.nextInt(a + 1, a + 7)
                val c = a + b
                val d = b + c
                seq = listOf(a, b, c, d)
                answer = c + d
                ruleTag = "a+b"
            }
        }

        val distractors = mutableSetOf<Int>()
        val deltaCandidates = listOf(-6, -4, -3, -2, -1, 1, 2, 3, 4, 6, 8, 10)
        var attempts = 0
        while (distractors.size < 3 && attempts < 40) {
            attempts++
            val delta = rng.pick(deltaCandidates)
            val candidate = answer + delta
            if (candidate > 0 && candidate != answer) {
                distractors.add(candidate)
            }
        }
        while (distractors.size < 3) {
            distractors.add(answer + distractors.size + 5)
        }
        val options = rng.shuffle(listOf(answer) + distractors.take(3))
        return NumberChainPuzzle(
            id = id,
            sequence = seq,
            options = options,
            correctAnswer = answer,
            ruleTag = ruleTag,
            timeLimitSec = if (difficulty >= 3) 14 else 16
        )
    }

    // 2. CODE BREAKER (Mastermind Deduction)
    private fun generateCodeBreaker(id: String, rng: DeterministicRandom, difficulty: Int): CodeBreakerPuzzle {
        val paletteSize = if (difficulty >= 3) 6 else 5
        val allowDuplicates = difficulty >= 3
        val code = if (allowDuplicates) {
            List(4) { rng.nextInt(paletteSize) }
        } else {
            rng.shuffle((0 until paletteSize).toList()).take(4)
        }
        return CodeBreakerPuzzle(
            id = id,
            secretCode = code,
            paletteSize = paletteSize,
            maxAttempts = 5,
            timeLimitSec = 26
        )
    }

    fun evaluateCodeGuess(secret: List<Int>, guess: List<Int>): CodeGuessFeedback {
        require(secret.size == 4 && guess.size == 4)
        var exact = 0
        val unmatchedSecret = mutableListOf<Int>()
        val unmatchedGuess = mutableListOf<Int>()
        for (i in 0 until 4) {
            if (secret[i] == guess[i]) {
                exact++
            } else {
                unmatchedSecret.add(secret[i])
                unmatchedGuess.add(guess[i])
            }
        }
        var colorOnly = 0
        for (g in unmatchedGuess) {
            val idx = unmatchedSecret.indexOf(g)
            if (idx >= 0) {
                colorOnly++
                unmatchedSecret.removeAt(idx)
            }
        }
        return CodeGuessFeedback(
            guess = guess,
            exactMatches = exact,
            colorOnlyMatches = colorOnly
        )
    }

    // 3. ODD SHAPE (Pattern)
    private fun generateOddShape(id: String, rng: DeterministicRandom, difficulty: Int): OddShapePuzzle {
        val shapes = ShapeKind.entries
        val baseShape = rng.pick(shapes)
        val baseColor = rng.nextInt(5)
        val baseFilled = rng.nextInt(2) == 0
        val baseDots = rng.nextInt(0, 4)
        val baseRot = if (baseShape == ShapeKind.TRIANGLE) 0f else 0f

        val oddIndex = rng.nextInt(6)
        // Pick a clear distinguishing rule
        val anomalyMode = when (difficulty) {
            1 -> rng.nextInt(3) // 0=shape, 1=fill, 2=dots
            else -> rng.nextInt(4) // +3=rotation/color subtle combination
        }

        val otherShape = rng.pick(shapes.filter { it != baseShape })
        val otherColor = (baseColor + rng.nextInt(1, 5)) % 5
        val otherDots = (baseDots + rng.nextInt(1, 3)) % 4

        val tiles = List(6) { idx ->
            val isOdd = idx == oddIndex
            when (anomalyMode) {
                0 -> {
                    // 5 share shape (varied colors so player inspects shape), 1 has different shape
                    val tileColor = if (difficulty >= 2) idx % 5 else baseColor
                    ShapeTileSpec(
                        shape = if (isOdd) otherShape else baseShape,
                        colorIndex = tileColor,
                        isFilled = baseFilled,
                        innerDots = baseDots,
                        rotationDeg = baseRot
                    )
                }
                1 -> {
                    // 5 are filled/outlined, 1 is opposite
                    val tileShape = if (difficulty >= 2) shapes[idx % shapes.size] else baseShape
                    ShapeTileSpec(
                        shape = tileShape,
                        colorIndex = baseColor,
                        isFilled = if (isOdd) !baseFilled else baseFilled,
                        innerDots = if (baseDots == 0) 1 else baseDots,
                        rotationDeg = 0f
                    )
                }
                2 -> {
                    // 5 have exact same inner dot count, 1 has different count
                    ShapeTileSpec(
                        shape = baseShape,
                        colorIndex = if (difficulty >= 2) (baseColor + idx) % 5 else baseColor,
                        isFilled = false,
                        innerDots = if (isOdd) otherDots else baseDots,
                        rotationDeg = 0f
                    )
                }
                else -> {
                    // 5 triangles point up, 1 points down (or distinct color + shape)
                    ShapeTileSpec(
                        shape = ShapeKind.TRIANGLE,
                        colorIndex = if (isOdd) otherColor else baseColor,
                        isFilled = true,
                        innerDots = baseDots.coerceAtMost(2),
                        rotationDeg = if (isOdd) 180f else 0f
                    )
                }
            }
        }
        return OddShapePuzzle(
            id = id,
            tiles = tiles,
            oddIndex = oddIndex,
            timeLimitSec = if (difficulty >= 3) 12 else 14
        )
    }

    // 4. QUICK CALC (Speed): Start -> apply 3 cards in sequence to reach exact Target
    private fun generateQuickCalc(id: String, rng: DeterministicRandom, difficulty: Int): QuickCalcPuzzle {
        val start = rng.nextInt(2, 9)
        val solutionCards = mutableListOf<CalcCard>()
        var current = start

        val ops = listOf(CalcOp.ADD, CalcOp.SUB, CalcOp.MUL)
        for (step in 0 until 3) {
            val op = when {
                step == 1 && current in 2..12 -> CalcOp.MUL
                current > 24 -> CalcOp.SUB
                current < 5 -> CalcOp.ADD
                else -> rng.pick(ops)
            }
            val operand = when (op) {
                CalcOp.ADD -> rng.nextInt(2, if (difficulty >= 2) 12 else 8)
                CalcOp.SUB -> rng.nextInt(1, max(2, current - 1).coerceAtMost(9))
                CalcOp.MUL -> rng.nextInt(2, if (difficulty >= 2) 5 else 4)
            }
            val card = CalcCard(id = step, op = op, operand = operand)
            current = card.applyTo(current)
            solutionCards.add(card)
        }
        val target = current

        // Create 2 distractor cards
        val distractors = mutableListOf<CalcCard>()
        var nextId = 3
        while (distractors.size < 2) {
            val op = rng.pick(ops)
            val operand = when (op) {
                CalcOp.MUL -> rng.nextInt(2, 4)
                else -> rng.nextInt(2, 10)
            }
            val candidate = CalcCard(id = nextId, op = op, operand = operand)
            val alreadyExists = (solutionCards + distractors).any {
                it.op == candidate.op && it.operand == candidate.operand
            }
            if (!alreadyExists) {
                distractors.add(candidate)
                nextId++
            }
        }
        val allCards = rng.shuffle(solutionCards + distractors)
        return QuickCalcPuzzle(
            id = id,
            startValue = start,
            targetValue = target,
            cards = allCards,
            solutionCardIds = solutionCards.map { it.id },
            timeLimitSec = if (difficulty >= 3) 16 else 18
        )
    }

    fun evaluateQuickCalc(puzzle: QuickCalcPuzzle, selectedCardIds: List<Int>): Int {
        var value = puzzle.startValue
        for (cardId in selectedCardIds) {
            val card = puzzle.cards.firstOrNull { it.id == cardId } ?: continue
            value = card.applyTo(value)
        }
        return value
    }

    fun isQuickCalcCorrect(puzzle: QuickCalcPuzzle, selectedCardIds: List<Int>): Boolean {
        if (selectedCardIds.size != 3) return false
        if (selectedCardIds.distinct().size != 3) return false
        return evaluateQuickCalc(puzzle, selectedCardIds) == puzzle.targetValue
    }

    // 5. WORD TWIST (Word)
    private fun generateWordTwist(id: String, rng: DeterministicRandom, difficulty: Int): WordTwistPuzzle {
        val wordSet = rng.pick(WordBank.sets)
        var scrambled = rng.shuffle(wordSet.anchorWord.toList())
        if (scrambled.joinToString("") == wordSet.anchorWord) {
            scrambled = scrambled.reversed()
        }
        return WordTwistPuzzle(
            id = id,
            letters = scrambled,
            anchorWord = wordSet.anchorWord,
            validWords = wordSet.validWords,
            timeLimitSec = if (difficulty >= 3) 16 else 18
        )
    }

    fun isValidWordTwistSubmission(puzzle: WordTwistPuzzle, submitted: String): Boolean {
        val clean = submitted.trim().uppercase()
        if (clean.length < 3) return false
        if (clean !in puzzle.validWords) return false
        // Verify letters come from the rack
        val available = puzzle.letters.toMutableList()
        for (ch in clean) {
            val idx = available.indexOf(ch)
            if (idx < 0) return false
            available.removeAt(idx)
        }
        return true
    }

    // 6. MEMORY GRID (Memory)
    private fun generateMemoryGrid(id: String, rng: DeterministicRandom, difficulty: Int): MemoryGridPuzzle {
        val gridSize = if (difficulty >= 2) 4 else 3
        val totalCells = gridSize * gridSize
        val targetCount = when {
            gridSize == 3 -> 4
            difficulty >= 3 -> 6
            else -> 5
        }
        val cells = rng.shuffle((0 until totalCells).toList()).take(targetCount).toSet()
        return MemoryGridPuzzle(
            id = id,
            gridSize = gridSize,
            targetCells = cells,
            previewDurationMs = 3000L,
            timeLimitSec = 16
        )
    }

    /**
     * Fairness scoring rule (Section 5):
     * Correctness always outweighs speed.
     * Base correct = 100 pts (Word Twist scales 80..130 by word length).
     * Speed bonus = 0..45 pts based on remaining time ratio.
     */
    fun calculatePoints(
        puzzle: Puzzle,
        isCorrect: Boolean,
        elapsedMs: Long,
        wordLength: Int = 0
    ): Int {
        if (!isCorrect) return 0
        val limitMs = (puzzle.timeLimitSec * 1000L).coerceAtLeast(1000L)
        val remainingRatio = ((limitMs - elapsedMs).coerceIn(0L, limitMs)).toFloat() / limitMs.toFloat()
        val base = when (puzzle) {
            is WordTwistPuzzle -> when {
                wordLength >= 6 -> 130
                wordLength == 5 -> 115
                wordLength == 4 -> 100
                else -> 85
            }
            is CodeBreakerPuzzle -> 115
            else -> 100
        }
        val speedBonus = (remainingRatio * 45f).roundToInt()
        return base + speedBonus
    }

    /**
     * Computes XP earned from a finished match.
     */
    fun calculateXpEarned(
        correctCount: Int,
        totalRounds: Int,
        wonMatch: Boolean,
        isDaily: Boolean,
        isPractice: Boolean
    ): Int {
        if (isPractice) return 0
        val base = correctCount * 18
        val winBonus = if (wonMatch) 55 else 20
        val dailyBonus = if (isDaily) 45 else 0
        val perfectBonus = if (correctCount == totalRounds && totalRounds > 0) 30 else 0
        return base + winBonus + dailyBonus + perfectBonus
    }

    /**
     * Computes brain skill delta per round.
     */
    fun skillDeltaForRound(isCorrect: Boolean, elapsedMs: Long, limitSec: Int): Int {
        if (!isCorrect) return -1
        val fast = elapsedMs < (limitSec * 500L)
        return if (fast) 3 else 2
    }

    fun closeDifferenceCheck(a: Int, b: Int): Boolean = abs(a - b) <= 2
}
