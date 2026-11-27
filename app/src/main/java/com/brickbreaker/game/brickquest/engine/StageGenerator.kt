package com.brickbreaker.game.brickquest.engine

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Total number of quest stages. Every stage is generated deterministically from its number. */
const val MAX_STAGE = 1500

/** Stages per world (chapter). 1500 / 50 = 30 worlds. */
const val STAGES_PER_WORLD = 50

data class StageBrickSpec(
    val col: Int,
    val row: Int,
    val hp: Int,
    val special: BrickSpecial
)

data class StagePickupSpec(
    val col: Int,
    val row: Int,
    val kind: PickupKind
)

class StageBlueprint(
    val stage: Int,
    val bricks: List<StageBrickSpec>,
    val pickups: List<StagePickupSpec>,
    val balls: Int,
    val isBoss: Boolean,
    val patternName: String,
    val difficulty: Float
)

/**
 * World (chapter) metadata shown in the stage map and HUD.
 * Accent colors are plain ARGB ints so the engine stays free of UI dependencies.
 */
object StageWorlds {
    private val NAMES = arrayOf(
        "Neon Horizon", "Laser Core", "Cosmic Citadel", "Crystal Caves", "Solar Forge",
        "Frozen Grid", "Toxic Labs", "Thunder Peaks", "Ruby Ruins", "Void Station",
        "Pixel Jungle", "Plasma Sea", "Golden Vault", "Shadow Maze", "Nova Gardens",
        "Iron Fortress", "Aurora Fields", "Magma Depths", "Quantum Rift", "Starlight Tower",
        "Cyber City", "Obsidian Keep", "Prism Lake", "Storm Engine", "Echo Temple",
        "Galaxy Rail", "Phantom Docks", "Titan Core", "Infinity Gate", "Legend Throne"
    )

    private val ACCENTS = longArrayOf(
        0xFF38BDF8, 0xFF4ADE80, 0xFFE879F9, 0xFF22D3EE, 0xFFFB923C,
        0xFF93C5FD, 0xFFA3E635, 0xFFFACC15, 0xFFF43F5E, 0xFFA78BFA
    )

    val count: Int get() = (MAX_STAGE + STAGES_PER_WORLD - 1) / STAGES_PER_WORLD

    /** 0-based world index for a stage. */
    fun worldIndex(stage: Int): Int = (stage.coerceIn(1, MAX_STAGE) - 1) / STAGES_PER_WORLD

    fun name(worldIndex: Int): String = NAMES[worldIndex.coerceIn(0, NAMES.size - 1)]

    fun accentArgb(worldIndex: Int): Int = ACCENTS[worldIndex.mod(ACCENTS.size)].toInt()

    fun firstStage(worldIndex: Int): Int = worldIndex * STAGES_PER_WORLD + 1

    fun lastStage(worldIndex: Int): Int = min(MAX_STAGE, (worldIndex + 1) * STAGES_PER_WORLD)
}

/**
 * Deterministic procedural stage generator.
 *
 * Difficulty is controlled by K, with totalBrickHp = K * turnsAvailable * balls * [budgetScale].
 * K ramps from 1.0 at stage 1 towards ~3.4, with a 10-stage sawtooth so difficulty
 * comes in waves and every 10th stage is a tougher boss stage.
 */
object StageGenerator {

    /**
     * HP multiplier on top of K. Tuned by running a bot on the REAL engine (best of 8 random
     * aims per turn, no Lightning / +25 Balls): at 3.0 early stages are won in 3-5 turns,
     * stage ~300 bosses are won 2 out of 3 times and stage 1000 bosses 1 out of 3.
     * Raise for a harder game, lower for an easier one.
     */
    var budgetScale: Float = 3.0f

    private val PATTERN_NAMES = arrayOf(
        "Block", "Mirror", "Diamond", "Frame", "Checker", "Towers",
        "Pyramid", "Wave", "Cross", "Stripes", "Islands", "Zigzag"
    )

    // Position inside each block of 10 stages -> difficulty multiplier (index 9 = boss).
    private val SAWTOOTH = floatArrayOf(0.80f, 0.88f, 0.94f, 1.00f, 0.86f, 0.96f, 1.02f, 1.08f, 0.95f, 1.15f)

    fun isBoss(stage: Int): Boolean = stage % 10 == 0

    fun ballsFor(stage: Int): Int {
        val s = stage.coerceIn(1, MAX_STAGE)
        return min(50, 14 + (sqrt(s.toFloat()) * 2.2f).toInt())
    }

    fun difficultyFor(stage: Int): Float {
        val s = stage.coerceIn(1, MAX_STAGE)
        val base = 1.25f + 2.15f * (1f - exp(-(s - 1) / 220f))
        val local = SAWTOOTH[(s - 1) % 10]
        // Very first stages stay gentle so new players learn the controls.
        val intro = if (s <= 5) 0.75f + s * 0.05f else 1f
        return (base * local * intro).coerceIn(1.0f, 3.9f)
    }

    fun generate(stage: Int): StageBlueprint {
        val s = stage.coerceIn(1, MAX_STAGE)
        val rng = Random(s * 7919L + 104_729L)
        val boss = isBoss(s)
        val balls = ballsFor(s)
        val k = difficultyFor(s)

        // Deepest brick row. Deeper = fewer turns before the danger row.
        val maxRow = when {
            boss -> if (s >= 200) 7 else 6
            s <= 5 -> 4
            s <= 40 -> 4 + rng.nextInt(0, 2)
            s <= 300 -> 5 + rng.nextInt(0, 2)
            else -> 5 + rng.nextInt(0, 3)
        }.coerceAtMost(DANGER_ROW - 3)
        val turnsAvailable = DANGER_ROW - maxRow

        // ---- Layout mask: weights[r][c] > 0 means a brick (rows 1..maxRow) ----
        val patternIdx = if (boss) 0 else (rng.nextInt(PATTERN_NAMES.size) + s) % PATTERN_NAMES.size
        val weights = Array(maxRow) { FloatArray(ARENA_COLS) }
        buildPattern(patternIdx, weights, maxRow, rng, s)
        if (boss) applyBossCore(weights, maxRow)

        // Guarantee a minimum amount of bricks so a stage is never trivially empty.
        var count = weights.sumOf { row -> row.count { it > 0f } }
        val minBricks = if (s < 20) 12 else 16
        var guard = 0
        while (count < minBricks && guard < 200) {
            val r = rng.nextInt(maxRow)
            val c = rng.nextInt(ARENA_COLS)
            if (weights[r][c] <= 0f) {
                weights[r][c] = 1f
                count++
            }
            guard++
        }

        // Random variation + occasional armored bricks on later stages.
        for (r in 0 until maxRow) {
            for (c in 0 until ARENA_COLS) {
                if (weights[r][c] > 0f) {
                    var w = weights[r][c] * (0.8f + rng.nextFloat() * 0.45f)
                    if (s >= 40 && rng.nextFloat() < 0.08f) w *= 2.2f
                    weights[r][c] = w
                }
            }
        }

        // ---- Specials (on brick cells) ----
        val specials = Array(maxRow) { arrayOfNulls<BrickSpecial>(ARENA_COLS) }
        val brickCells = ArrayList<Int>()
        for (r in 0 until maxRow) for (c in 0 until ARENA_COLS) if (weights[r][c] > 0f) brickCells.add(r * ARENA_COLS + c)
        brickCells.shuffle(rng)
        var cursor = 0
        fun takeCell(): Int? = if (cursor < brickCells.size) brickCells[cursor++] else null

        val powerCount = 1 + (if (s % 3 == 0) 1 else 0) + (if (boss) 1 else 0)
        val powerTypes = arrayOf(BrickSpecial.POWER_MULTI_BALL, BrickSpecial.POWER_LASER_BEAM, BrickSpecial.POWER_PADDLE_EXPAND)
        repeat(powerCount) { i ->
            val cell = takeCell() ?: return@repeat
            specials[cell / ARENA_COLS][cell % ARENA_COLS] = powerTypes[(i + s) % powerTypes.size]
        }
        val bombCount = when {
            boss -> if (s >= 100) 2 else 1
            s >= 8 && rng.nextFloat() < 0.45f -> 1
            else -> 0
        }
        repeat(bombCount) {
            val cell = takeCell() ?: return@repeat
            val r = cell / ARENA_COLS
            val c = cell % ARENA_COLS
            specials[r][c] = BrickSpecial.BOMB_3X3
            weights[r][c] *= 1.3f
        }

        // ---- HP from the difficulty budget ----
        val budget = k * turnsAvailable * balls * budgetScale
        var weightSum = 0f
        for (row in weights) for (w in row) if (w > 0f) weightSum += w
        val bricks = ArrayList<StageBrickSpec>(count)
        for (r in 0 until maxRow) {
            for (c in 0 until ARENA_COLS) {
                val w = weights[r][c]
                if (w <= 0f) continue
                val hp = (budget * w / weightSum).roundToInt().coerceIn(1, 999)
                bricks.add(StageBrickSpec(col = c, row = r + 1, hp = hp, special = specials[r][c] ?: BrickSpecial.NONE))
            }
        }

        // ---- Pickups (on empty cells inside or just below the layout) ----
        val emptyCells = ArrayList<Int>()
        for (r in 0 until maxRow) for (c in 0 until ARENA_COLS) if (weights[r][c] <= 0f) emptyCells.add(r * ARENA_COLS + c)
        emptyCells.shuffle(rng)
        val belowRow = maxRow + 1
        val belowCols = (0 until ARENA_COLS).shuffled(rng).toMutableList()
        val pickups = ArrayList<StagePickupSpec>()
        fun placePickup(kind: PickupKind) {
            if (emptyCells.isNotEmpty()) {
                val cell = emptyCells.removeAt(emptyCells.size - 1)
                pickups.add(StagePickupSpec(cell % ARENA_COLS, cell / ARENA_COLS + 1, kind))
            } else if (belowCols.isNotEmpty()) {
                pickups.add(StagePickupSpec(belowCols.removeAt(belowCols.size - 1), belowRow, kind))
            }
        }
        val plusBalls = 1 + (if (rng.nextFloat() < 0.6f) 1 else 0) + (if (boss) 1 else 0)
        repeat(plusBalls) { placePickup(PickupKind.PLUS_BALL) }
        if (s >= 3) {
            val laserCount = rng.nextInt(0, 3) + (if (boss) 1 else 0)
            repeat(laserCount) {
                val kind = when {
                    s >= 20 && rng.nextFloat() < 0.25f -> PickupKind.LASER_CROSS
                    rng.nextBoolean() -> PickupKind.LASER_HORIZONTAL
                    else -> PickupKind.LASER_VERTICAL
                }
                placePickup(kind)
            }
        }

        return StageBlueprint(
            stage = s,
            bricks = bricks,
            pickups = pickups,
            balls = balls,
            isBoss = boss,
            patternName = if (boss) "Boss" else PATTERN_NAMES[patternIdx],
            difficulty = k
        )
    }

    private fun buildPattern(idx: Int, w: Array<FloatArray>, rows: Int, rng: Random, stage: Int) {
        val center = (ARENA_COLS - 1) / 2 // 4
        val midRow = (rows - 1) / 2f
        when (idx) {
            0 -> { // Block with holes
                val holes = 0.12f + rng.nextFloat() * 0.18f
                for (r in 0 until rows) for (c in 0 until ARENA_COLS) if (rng.nextFloat() >= holes) w[r][c] = 1f
            }
            1 -> { // Mirrored random
                val density = 0.55f + rng.nextFloat() * 0.25f
                for (r in 0 until rows) for (c in 0..center) {
                    if (rng.nextFloat() < density) {
                        val v = if (rng.nextFloat() < 0.25f) 1.5f else 1f
                        w[r][c] = v
                        w[r][ARENA_COLS - 1 - c] = v
                    }
                }
            }
            2 -> { // Diamond, heavier edge
                val rad = max(2, min(center, rows - 1))
                for (r in 0 until rows) for (c in 0 until ARENA_COLS) {
                    val d = abs(c - center) + abs(r - midRow).roundToInt()
                    if (d <= rad) w[r][c] = if (d == rad) 1.6f else 0.8f
                }
            }
            3 -> { // Frame: heavy ring, light sparse inside
                for (r in 0 until rows) for (c in 0 until ARENA_COLS) {
                    val edge = r == 0 || r == rows - 1 || c == 0 || c == ARENA_COLS - 1
                    if (edge) w[r][c] = 1.6f else if (rng.nextFloat() < 0.55f) w[r][c] = 0.6f
                }
                // A door in the bottom edge so balls can get inside.
                w[rows - 1][center] = 0f
            }
            4 -> { // Checker
                val offset = rng.nextInt(2)
                for (r in 0 until rows) for (c in 0 until ARENA_COLS) if ((r + c + offset) % 2 == 0) w[r][c] = 1.2f
            }
            5 -> { // Towers
                val towerCols = intArrayOf(0, 2, 4, 6, 8)
                val shift = rng.nextInt(2)
                for (c in towerCols) {
                    val col = (c + shift).coerceAtMost(ARENA_COLS - 1)
                    for (r in 0 until rows) w[r][col] = 1.4f
                }
                for (c in 0 until ARENA_COLS) if (w[0][c] <= 0f && rng.nextFloat() < 0.6f) w[0][c] = 0.8f
            }
            6 -> { // Pyramid (or inverted)
                val inverted = rng.nextBoolean()
                for (r in 0 until rows) {
                    val level = if (inverted) rows - 1 - r else r
                    val half = ((level + 1) * (center + 1) / rows.toFloat()).roundToInt().coerceAtLeast(1)
                    for (c in (center - half + 1).coerceAtLeast(0)..(center + half - 1).coerceAtMost(ARENA_COLS - 1)) {
                        w[r][c] = 1f + level * 0.1f
                    }
                }
            }
            7 -> { // Wave
                val phase = rng.nextFloat() * 6.28f
                val freq = 0.6f + rng.nextFloat() * 0.5f
                for (c in 0 until ARENA_COLS) {
                    val h = (midRow + sin(c * freq + phase) * (rows / 2f)).roundToInt().coerceIn(0, rows - 1)
                    for (r in 0..h) w[r][c] = if (r == h) 1.5f else 0.9f
                }
            }
            8 -> { // Cross + corners
                val mr = midRow.roundToInt()
                for (c in 0 until ARENA_COLS) w[mr][c] = 1.3f
                for (r in 0 until rows) {
                    w[r][center] = 1.3f
                    if (rows > 3) {
                        w[r][center - 1] = max(w[r][center - 1], 0.8f)
                        w[r][center + 1] = max(w[r][center + 1], 0.8f)
                    }
                }
                w[0][0] = 1.6f; w[0][ARENA_COLS - 1] = 1.6f
                w[rows - 1][0] = 1.6f; w[rows - 1][ARENA_COLS - 1] = 1.6f
            }
            9 -> { // Stripes
                val start = rng.nextInt(2)
                for (r in start until rows step 2) for (c in 0 until ARENA_COLS) w[r][c] = 1.2f
                for (r in 0 until rows) if (w[r][0] <= 0f && rng.nextFloat() < 0.4f) w[r][rng.nextInt(ARENA_COLS)] = 0.7f
            }
            10 -> { // Islands
                val islands = 2 + rng.nextInt(2)
                repeat(islands) {
                    val iw = 2 + rng.nextInt(2)
                    val ih = 2 + rng.nextInt(max(1, rows - 2))
                    val c0 = rng.nextInt(0, ARENA_COLS - iw + 1)
                    val r0 = rng.nextInt(0, max(1, rows - ih + 1))
                    for (r in r0 until min(rows, r0 + ih)) for (c in c0 until c0 + iw) w[r][c] = 1.3f
                }
            }
            else -> { // Zigzag diagonal bands
                val band = 3
                val shift = rng.nextInt(band)
                for (r in 0 until rows) for (c in 0 until ARENA_COLS) if ((r + c + shift) % band != 0) w[r][c] = 1f
            }
        }
        // Later stages: fill a few extra random cells so patterns get denser over time.
        if (stage > 150) {
            val extra = min(6, stage / 150)
            repeat(extra) {
                val r = rng.nextInt(rows)
                val c = rng.nextInt(ARENA_COLS)
                if (w[r][c] <= 0f) w[r][c] = 0.9f
            }
        }
    }

    /** Boss stages: a dense fortress with a heavy 3x3 core. */
    private fun applyBossCore(w: Array<FloatArray>, rows: Int) {
        val cr = rows / 2
        val cc = (ARENA_COLS - 1) / 2
        for (r in (cr - 1).coerceAtLeast(0)..(cr + 1).coerceAtMost(rows - 1)) {
            for (c in cc - 1..cc + 1) w[r][c] = 2.4f
        }
        // Two side gates so balls can reach the top.
        w[rows - 1][1] = 0f
        w[rows - 1][ARENA_COLS - 2] = 0f
    }
}
