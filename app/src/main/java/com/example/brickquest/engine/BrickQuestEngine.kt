package com.example.brickquest.engine

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

const val ARENA_COLS = 9
const val ARENA_ROWS = 12
const val BALL_RADIUS = 0.165f
const val FLOOR_Y = ARENA_ROWS - 0.30f
const val DANGER_ROW = ARENA_ROWS - 2 // If any brick reaches row >= 10 -> Game Over!

enum class BrickQuestMode {
    STAGE,
    ENDLESS
}

enum class BrickSpecial {
    NONE,
    BOMB_3X3,
    POWER_MULTI_BALL,
    POWER_LASER_BEAM,
    POWER_PADDLE_EXPAND
}

enum class PowerUpType {
    MULTI_BALL,
    LASER_BEAM,
    PADDLE_EXPAND
}

enum class PickupKind {
    PLUS_BALL,
    LASER_HORIZONTAL,
    LASER_VERTICAL,
    LASER_CROSS
}

data class BrickEntity(
    val id: Int,
    val col: Int,
    var row: Int,
    var hp: Int,
    val maxHp: Int,
    val special: BrickSpecial = BrickSpecial.NONE,
    var hitFlash: Float = 0f,
    var renderRowOffset: Float = 0f, // Smooth drop animation offset
    var powerUpDropped: Boolean = false
) {
    val left: Float get() = col + 0.06f
    val right: Float get() = col + 0.94f
    val top: Float get() = (row + renderRowOffset) + 0.06f
    val bottom: Float get() = (row + renderRowOffset) + 0.94f
}

data class FallingPowerUpEntity(
    val id: Int,
    var x: Float,
    var y: Float,
    val vy: Float = 3.15f,
    val kind: PowerUpType,
    var pulsePhase: Float = 0f
)

data class PickupEntity(
    val id: Int,
    val col: Int,
    var row: Int,
    val kind: PickupKind,
    var triggeredThisTurn: Boolean = false,
    var collected: Boolean = false,
    var pulsePhase: Float = 0f
) {
    val cx: Float get() = col + 0.5f
    val cy: Float get() = row + 0.5f
}

data class BallEntity(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var active: Boolean = true,
    var returning: Boolean = false
)

data class SquareParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    val color: Color
)

data class LaserBeamFx(
    val isHorizontal: Boolean,
    val gridCoord: Float, // y if horizontal, x if vertical
    val color: Color,
    var life: Float = 1f
)

data class FloatingTextFx(
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var life: Float = 1f
)

enum class TurnPhase {
    AIMING,
    SHOOTING,
    DROPPING,
    STAGE_CLEAR,
    GAME_OVER
}

/**
 * Pure 60fps Bricks Breaker Quest physics & stage engine.
 * Coordinates are normalized in grid units: x in [0, ARENA_COLS], y in [0, ARENA_ROWS].
 */
class BrickQuestEngine(
    val mode: BrickQuestMode,
    val stageNumber: Int
) {
    val bricks = mutableListOf<BrickEntity>()
    val pickups = mutableListOf<PickupEntity>()
    val fallingPowerUps = mutableListOf<FallingPowerUpEntity>()
    val balls = mutableListOf<BallEntity>()
    val particles = mutableListOf<SquareParticle>()
    val laserBeams = mutableListOf<LaserBeamFx>()
    val floatingTexts = mutableListOf<FloatingTextFx>()

    var phase: TurnPhase = TurnPhase.AIMING
        private set

    var score: Int = 0
        private set
    var starTargetScore: Int = 1500
        private set
    var turnNumber: Int = 1
        private set

    var launchX: Float = ARENA_COLS / 2f
        private set
    var nextLaunchX: Float? = null
        private set

    // Interactive Neon Deflector & Catcher Paddle
    var paddleX: Float = ARENA_COLS / 2f
        private set
    var paddleWidth: Float = BASE_PADDLE_WIDTH
        private set
    val paddleY: Float = FLOOR_Y - 0.14f

    // Temporary timed power-up bonuses (seconds remaining)
    var multiBallTimer: Float = 0f
        private set
    var laserBeamTimer: Float = 0f
        private set
    var paddleExpandTimer: Float = 0f
        private set
    private var laserAutoFireCooldown: Float = 0f

    var totalBalls: Int = 16
        private set
    var pendingBonusBallsNextTurn: Int = 0
        private set
    var bonusBallsThisTurn: Int = 0
        private set
    var damagePerHit: Int = 1
        private set

    // Aiming state
    var isDraggingAim: Boolean = false
    var aimAngleRad: Float = (-PI / 2.0).toFloat()

    // Shot streaming state
    private var ballsRemainingToSpawn: Int = 0
    private var spawnCooldown: Float = 0f
    private var shotVx: Float = 0f
    private var shotVy: Float = 0f
    private var comboHitsThisTurn: Int = 0
    private var dropAnimRemaining: Float = 0f

    var turboSpeed: Boolean = false
    var hitSoundCounter: Int = 0
        private set
    var breakSoundCounter: Int = 0
        private set
    var laserSoundCounter: Int = 0
        private set

    // Zero-allocation aim preview buffers
    val aimPreviewX = FloatArray(48)
    val aimPreviewY = FloatArray(48)
    var aimPreviewCount: Int = 0
        private set
    private var aimPreviewDirty: Boolean = true

    private var nextEntityId = 1
    private val rng = Random(stageNumber * 7919L + if (mode == BrickQuestMode.ENDLESS) System.currentTimeMillis() else 1337L)

    init {
        if (mode == BrickQuestMode.STAGE) {
            loadStage(stageNumber)
        } else {
            loadEndlessStart()
        }
    }

    val currentStarCount: Int
        get() {
            val ratio = (score.toFloat() / starTargetScore.coerceAtLeast(1)).coerceIn(0f, 1.5f)
            return when {
                phase == TurnPhase.STAGE_CLEAR && ratio >= 0.85f -> 3
                ratio >= 0.90f -> 3
                ratio >= 0.55f -> 2
                ratio >= 0.22f || phase == TurnPhase.STAGE_CLEAR -> 1
                else -> 0
            }
        }

    val starProgressRatio: Float
        get() = (score.toFloat() / starTargetScore.coerceAtLeast(1)).coerceIn(0f, 1f)

    val displayedBallCount: Int
        get() {
            if (phase != TurnPhase.SHOOTING) return totalBalls + bonusBallsThisTurn
            var active = 0
            for (i in 0 until balls.size) {
                if (balls[i].active) active++
            }
            return ballsRemainingToSpawn + active
        }

    private fun loadEndlessStart() {
        totalBalls = 10
        starTargetScore = 3000
        spawnEndlessRow(row = 1, wave = 1)
        spawnEndlessRow(row = 2, wave = 2)
        spawnEndlessRow(row = 3, wave = 3)
    }

    private fun loadStage(stage: Int) {
        bricks.clear()
        pickups.clear()
        val s = stage.coerceIn(1, 30)
        totalBalls = (14 + (s - 1) * 2).coerceAtMost(48)

        // Hand-crafted recognizable patterns inspired directly by Bricks Breaker Quest!
        when (s % 6) {
            1 -> buildNeonGatewayStage(s)     // Matches Screenshot 1 (cyan core + ruby frame + magenta +1 orbs!)
            2 -> buildLaserCrossfireStage(s)  // Matches Screenshot 2 (horizontal neon green laser row + high-HP clusters!)
            3 -> buildDiamondVaultStage(s)
            4 -> buildTwinTowersStage(s)
            5 -> buildStarburstStage(s)
            else -> buildCitadelBossStage(s)
        }

        val totalHpSum = bricks.sumOf { it.hp }
        starTargetScore = max(600, totalHpSum * 11)
    }

    private fun buildNeonGatewayStage(s: Int) {
        val outerHp = 10 + s * 3
        val sideHp = 6 + s * 2
        // Top & bottom ruby-red frame with special power-up bricks!
        for (c in 1..7) {
            val topHp = if (c <= 2) max(6, outerHp - 4) else outerHp
            val topSpecial = when (c) {
                4 -> BrickSpecial.POWER_LASER_BEAM
                else -> BrickSpecial.NONE
            }
            val botSpecial = when (c) {
                2 -> BrickSpecial.POWER_MULTI_BALL
                6 -> BrickSpecial.POWER_PADDLE_EXPAND
                else -> BrickSpecial.NONE
            }
            addBrick(c, 1, topHp, topSpecial)
            addBrick(c, 4, outerHp, botSpecial)
        }
        // Electric cyan low-HP core (rows 2..3, cols 2..7)
        for (r in 2..3) {
            for (c in 2..7) {
                addBrick(c, r, max(1, s / 2))
            }
            addBrick(8, r, outerHp)
        }
        // Left emerald/gold/orange side pillar
        addBrick(0, 2, sideHp)
        addBrick(0, 3, sideHp + 5)
        addBrick(0, 4, max(4, sideHp - 2))

        // Magenta +1 Ball orbs on the right corners (exact Screenshot 1 feel!)
        addPickup(8, 1, PickupKind.PLUS_BALL)
        addPickup(8, 4, PickupKind.PLUS_BALL)
        addPickup(1, 2, PickupKind.LASER_HORIZONTAL)
    }

    private fun buildLaserCrossfireStage(s: Int) {
        val highHp = 16 + s * 4
        val lowHp = max(2, s)
        for (c in 0..8) {
            val hpTop = if (c in 1..2 || c in 6..7) lowHp else highHp
            val sp = if (c == 4) BrickSpecial.POWER_MULTI_BALL else BrickSpecial.NONE
            addBrick(c, 1, hpTop, sp)
        }
        for (c in 0..7) {
            if (c != 4) addBrick(c, 2, highHp - 2)
        }
        // Neon Green Horizontal Laser orb right in row 3!
        addPickup(4, 3, PickupKind.LASER_HORIZONTAL)
        addBrick(0, 3, highHp)
        addBrick(1, 3, lowHp)
        addBrick(2, 3, highHp - 4, BrickSpecial.POWER_LASER_BEAM)
        addBrick(6, 3, highHp - 4, BrickSpecial.POWER_PADDLE_EXPAND)
        addBrick(7, 3, lowHp)
        addBrick(8, 3, highHp)

        // Lower accent row
        addBrick(1, 4, highHp + 6, if (s >= 6) BrickSpecial.BOMB_3X3 else BrickSpecial.NONE)
        addBrick(2, 4, lowHp)
        addBrick(6, 4, lowHp)
        addBrick(7, 4, highHp + 6)
        addPickup(4, 4, PickupKind.PLUS_BALL)
        addPickup(0, 5, PickupKind.PLUS_BALL)
        addPickup(8, 5, PickupKind.LASER_VERTICAL)
    }

    private fun buildDiamondVaultStage(s: Int) {
        val baseHp = 8 + s * 3
        val center = 4
        for (r in 1..6) {
            val distY = abs(r - 3)
            for (c in 0..8) {
                val dist = abs(c - center) + distY
                when {
                    dist == 3 -> {
                        val sp = when {
                            r == 5 && c == 3 -> BrickSpecial.POWER_MULTI_BALL
                            r == 5 && c == 5 -> BrickSpecial.POWER_PADDLE_EXPAND
                            r == 3 && c == 1 -> BrickSpecial.POWER_LASER_BEAM
                            else -> BrickSpecial.NONE
                        }
                        addBrick(c, r, baseHp + 6, sp)
                    }
                    dist == 2 -> addBrick(c, r, max(2, baseHp / 2))
                    dist == 1 && (r == 3 && c == 4) -> Unit
                    dist == 0 -> addBrick(c, r, baseHp + 10, BrickSpecial.BOMB_3X3)
                }
            }
        }
        addPickup(4, 2, PickupKind.LASER_CROSS)
        addPickup(1, 5, PickupKind.PLUS_BALL)
        addPickup(7, 5, PickupKind.PLUS_BALL)
    }

    private fun buildTwinTowersStage(s: Int) {
        val towerHp = 12 + s * 3
        for (r in 1..5) {
            for (c in listOf(0, 1, 2, 6, 7, 8)) {
                val hp = if (c == 1 || c == 7) towerHp + 5 else max(3, towerHp - 4)
                val sp = when {
                    r == 5 && c == 2 -> BrickSpecial.POWER_MULTI_BALL
                    r == 5 && c == 6 -> BrickSpecial.POWER_PADDLE_EXPAND
                    r == 3 && c == 1 -> BrickSpecial.POWER_LASER_BEAM
                    else -> BrickSpecial.NONE
                }
                addBrick(c, r, hp, sp)
            }
        }
        // Center laser corridor
        addPickup(4, 2, PickupKind.LASER_HORIZONTAL)
        addPickup(4, 4, PickupKind.LASER_HORIZONTAL)
        addPickup(3, 3, PickupKind.PLUS_BALL)
        addPickup(5, 3, PickupKind.PLUS_BALL)
        addBrick(4, 1, towerHp + 8, BrickSpecial.BOMB_3X3)
    }

    private fun buildStarburstStage(s: Int) {
        val hp = 10 + s * 3
        for (r in 1..5) {
            for (c in 0..8) {
                if ((r + c) % 2 == 0) {
                    val isCore = r == 3 && c == 4
                    val sp = when {
                        isCore -> BrickSpecial.BOMB_3X3
                        r == 5 && c == 2 -> BrickSpecial.POWER_MULTI_BALL
                        r == 5 && c == 6 -> BrickSpecial.POWER_PADDLE_EXPAND
                        r == 3 && c == 0 -> BrickSpecial.POWER_LASER_BEAM
                        else -> BrickSpecial.NONE
                    }
                    addBrick(
                        col = c,
                        row = r,
                        hp = if (isCore) hp + 12 else hp,
                        special = sp
                    )
                } else if (r == 2 && (c == 2 || c == 6)) {
                    addPickup(c, r, PickupKind.LASER_VERTICAL)
                } else if (r == 4 && (c == 1 || c == 7)) {
                    addPickup(c, r, PickupKind.PLUS_BALL)
                }
            }
        }
        addPickup(4, 5, PickupKind.LASER_HORIZONTAL)
    }

    private fun buildCitadelBossStage(s: Int) {
        val hp = 16 + s * 4
        for (c in 0..8) {
            addBrick(c, 1, hp + 8)
            if (c != 4) {
                val sp = when (c) {
                    2 -> BrickSpecial.POWER_MULTI_BALL
                    6 -> BrickSpecial.POWER_PADDLE_EXPAND
                    else -> BrickSpecial.NONE
                }
                addBrick(c, 5, hp / 2, sp)
            }
        }
        for (r in 2..4) {
            addBrick(0, r, hp, if (r == 4) BrickSpecial.POWER_LASER_BEAM else BrickSpecial.NONE)
            addBrick(8, r, hp)
            for (c in 2..6) {
                if (r == 3 && c == 4) {
                    addBrick(c, r, hp + 18, BrickSpecial.BOMB_3X3)
                } else if ((r + c) % 2 == 0) {
                    addBrick(c, r, hp - 2)
                }
            }
        }
        addPickup(1, 3, PickupKind.LASER_HORIZONTAL)
        addPickup(7, 3, PickupKind.LASER_HORIZONTAL)
        addPickup(4, 5, PickupKind.PLUS_BALL)
        addPickup(4, 2, PickupKind.PLUS_BALL)
    }

    private fun spawnEndlessRow(row: Int, wave: Int) {
        val cols = (0 until ARENA_COLS).shuffled(rng)
        val brickCount = rng.nextInt(4, 7)
        val baseHp = max(2, wave * 2 + rng.nextInt(0, wave + 2))
        val chosenBricks = cols.take(brickCount)
        chosenBricks.forEachIndexed { idx, col ->
            val special = when {
                idx == 0 && wave % 4 == 0 -> BrickSpecial.BOMB_3X3
                idx == 1 && wave % 3 == 1 -> BrickSpecial.POWER_MULTI_BALL
                idx == 1 && wave % 3 == 2 -> BrickSpecial.POWER_LASER_BEAM
                idx == 1 && wave % 3 == 0 -> BrickSpecial.POWER_PADDLE_EXPAND
                else -> BrickSpecial.NONE
            }
            val hp = if (rng.nextFloat() < 0.25f) baseHp * 2 else baseHp
            addBrick(col, row, hp, special)
        }
        val freeCols = cols.drop(brickCount)
        if (freeCols.isNotEmpty()) {
            addPickup(freeCols[0], row, PickupKind.PLUS_BALL)
        }
        if (freeCols.size >= 2 && wave % 2 == 0) {
            val laserType = if (wave % 4 == 0) PickupKind.LASER_VERTICAL else PickupKind.LASER_HORIZONTAL
            addPickup(freeCols[1], row, laserType)
        }
    }

    private fun addBrick(col: Int, row: Int, hp: Int, special: BrickSpecial = BrickSpecial.NONE) {
        if (bricks.any { it.col == col && it.row == row }) return
        bricks.add(
            BrickEntity(
                id = nextEntityId++,
                col = col,
                row = row,
                hp = hp.coerceAtLeast(1),
                maxHp = hp.coerceAtLeast(1),
                special = special
            )
        )
    }

    private fun addPickup(col: Int, row: Int, kind: PickupKind) {
        if (bricks.any { it.col == col && it.row == row }) return
        if (pickups.any { it.col == col && it.row == row }) return
        pickups.add(
            PickupEntity(
                id = nextEntityId++,
                col = col,
                row = row,
                kind = kind,
                pulsePhase = rng.nextFloat() * 6.28f
            )
        )
    }

    /**
     * Updates aim angle based on pointer position in normalized arena coordinates.
     * Supports BOTH direct pointing (drag above floor) AND slingshot pullback (drag below floor).
     */
    fun updateAimFromPointer(normX: Float, normY: Float) {
        if (phase != TurnPhase.AIMING) return
        val dx: Float
        val dy: Float
        if (normY <= FLOOR_Y - 0.15f) {
            // Direct point-at-target aiming
            dx = normX - launchX
            dy = normY - FLOOR_Y
        } else {
            // Slingshot pullback below floor
            dx = launchX - normX
            dy = FLOOR_Y - normY
        }
        if (hypot(dx, dy) < 0.18f) return
        var angle = atan2(dy, dx)
        // Clamp angle strictly upward between -168 deg and -12 deg
        val minAngle = (-168.0 * PI / 180.0).toFloat()
        val maxAngle = (-12.0 * PI / 180.0).toFloat()
        if (angle > 0f) {
            angle = if (dx < 0f) minAngle else maxAngle
        }
        aimAngleRad = angle.coerceIn(minAngle, maxAngle)
        isDraggingAim = true
        aimPreviewDirty = true
    }

    fun cancelAim() {
        isDraggingAim = false
    }

    fun updatePaddlePosition(normX: Float) {
        val halfW = paddleWidth * 0.5f
        paddleX = normX.coerceIn(halfW, ARENA_COLS - halfW)
    }

    fun fireVolley() {
        if (phase != TurnPhase.AIMING || !isDraggingAim) return
        isDraggingAim = false
        phase = TurnPhase.SHOOTING
        balls.clear()
        nextLaunchX = null
        comboHitsThisTurn = 0
        turboSpeed = false
        paddleX = launchX.coerceIn(paddleWidth * 0.5f, ARENA_COLS - paddleWidth * 0.5f)

        val baseSpeed = 21.0f
        shotVx = cos(aimAngleRad) * baseSpeed
        shotVy = sin(aimAngleRad) * baseSpeed
        ballsRemainingToSpawn = totalBalls + bonusBallsThisTurn
        spawnCooldown = 0f

        // Reset per-turn laser triggers
        for (i in 0 until pickups.size) {
            pickups[i].triggeredThisTurn = false
        }
    }

    /**
     * Immediately recalls all flying balls smoothly to the launch floor so the player never waits!
     */
    fun recallBallsNow() {
        if (phase != TurnPhase.SHOOTING) return
        ballsRemainingToSpawn = 0
        val targetX = nextLaunchX ?: launchX
        balls.forEach { b ->
            b.returning = true
            val dx = targetX - b.x
            val dy = FLOOR_Y - b.y
            val dist = hypot(dx, dy).coerceAtLeast(0.01f)
            b.vx = (dx / dist) * 32f
            b.vy = (dy / dist) * 32f
        }
    }

    /**
     * Bottom-Left Power-Up (⚡ Lightning Strike): Blasts the bottom-most row of bricks!
     */
    fun triggerLightningRowBlast(): Boolean {
        if (phase != TurnPhase.AIMING && phase != TurnPhase.SHOOTING) return false
        if (bricks.isEmpty()) return false

        val lowestRow = bricks.maxOf { it.row }
        laserBeams.add(
            LaserBeamFx(
                isHorizontal = true,
                gridCoord = lowestRow + 0.5f,
                color = Color(0xFFFF3D5A),
                life = 1.2f
            )
        )
        laserBeams.add(
            LaserBeamFx(
                isHorizontal = true,
                gridCoord = lowestRow + 0.5f,
                color = Color(0xFFFDE047),
                life = 0.9f
            )
        )
        laserSoundCounter++

        val targets = bricks.filter { it.row == lowestRow }.toList()
        targets.forEach { b ->
            damageBrick(b, damage = b.hp, fromChain = true)
        }
        checkInstantStageClear()
        return true
    }

    /**
     * Bottom-Right Power-Up (🔺 Ball Storm): Adds +25 extra balls for this volley and boosts damage!
     */
    fun triggerExtraBallsStorm(): Boolean {
        if (phase != TurnPhase.AIMING && phase != TurnPhase.SHOOTING) return false
        if (phase == TurnPhase.AIMING) {
            bonusBallsThisTurn += 25
            damagePerHit = 2
        } else {
            ballsRemainingToSpawn += 25
            damagePerHit = 2
        }
        floatingTexts.add(
            FloatingTextFx(
                x = launchX,
                y = FLOOR_Y - 0.8f,
                text = "+25 BALLS! 2×",
                color = Color(0xFFFF4D6D),
                life = 1.3f
            )
        )
        laserSoundCounter++
        return true
    }

    /**
     * Recomputes the neon dotted aiming ray into pre-allocated [aimPreviewX]/[aimPreviewY] buffers
     * with ZERO heap allocations per frame.
     */
    fun refreshAimPreviewIfNeeded() {
        if (!aimPreviewDirty) return
        aimPreviewDirty = false
        var count = 0
        var cx = launchX
        var cy = FLOOR_Y
        var dx = cos(aimAngleRad)
        var dy = sin(aimAngleRad)
        val step = 0.30f
        var bounced = false
        var stepsAfterBounce = 0
        val maxPts = aimPreviewX.size

        for (i in 0 until 44) {
            if (count >= maxPts) break
            cx += dx * step
            cy += dy * step

            // Wall reflections
            if (cx <= BALL_RADIUS) {
                cx = BALL_RADIUS
                dx = abs(dx)
                if (bounced) break
                bounced = true
            } else if (cx >= ARENA_COLS - BALL_RADIUS) {
                cx = ARENA_COLS - BALL_RADIUS
                dx = -abs(dx)
                if (bounced) break
                bounced = true
            }
            if (cy <= BALL_RADIUS) {
                cy = BALL_RADIUS
                dy = abs(dy)
                if (bounced) break
                bounced = true
            }
            if (cy >= FLOOR_Y) break

            // Brick hit check (indexed loop, zero iterator allocation)
            var hitBrick: BrickEntity? = null
            for (bIdx in 0 until bricks.size) {
                val b = bricks[bIdx]
                if (cx + BALL_RADIUS >= b.left && cx - BALL_RADIUS <= b.right &&
                    cy + BALL_RADIUS >= b.top && cy - BALL_RADIUS <= b.bottom
                ) {
                    hitBrick = b
                    break
                }
            }
            if (hitBrick != null) {
                aimPreviewX[count] = cx
                aimPreviewY[count] = cy
                count++
                if (!bounced) {
                    val prevX = cx - dx * step
                    if (prevX < hitBrick.left || prevX > hitBrick.right) {
                        dx = -dx
                    } else {
                        dy = -dy
                    }
                    bounced = true
                } else {
                    break
                }
            } else {
                aimPreviewX[count] = cx
                aimPreviewY[count] = cy
                count++
            }

            if (bounced) {
                stepsAfterBounce++
                if (stepsAfterBounce >= 9) break
            }
        }
        aimPreviewCount = count
    }

    /**
     * Advances simulation by [rawDt] seconds (clamped for stability).
     */
    fun update(rawDt: Float) {
        val dt = rawDt.coerceIn(0.001f, 0.045f)

        // Update visual effects & falling collectable power-ups regardless of phase
        updateVisualFx(dt)
        updateActivePowerUps(dt)

        when (phase) {
            TurnPhase.AIMING, TurnPhase.STAGE_CLEAR, TurnPhase.GAME_OVER -> Unit

            TurnPhase.SHOOTING -> {
                val speedMult = if (turboSpeed) 2.35f else 1.0f
                val effectiveDt = dt * speedMult

                // Spawn ball chain tightly and smoothly
                if (ballsRemainingToSpawn > 0) {
                    spawnCooldown -= effectiveDt
                    while (spawnCooldown <= 0f && ballsRemainingToSpawn > 0) {
                        balls.add(
                            BallEntity(
                                x = launchX,
                                y = FLOOR_Y,
                                vx = shotVx,
                                vy = shotVy
                            )
                        )
                        ballsRemainingToSpawn--
                        spawnCooldown += 0.040f
                    }
                }

                // Sub-step ball motion (2 steps normal / 3 turbo: zero tunneling + ultra-low CPU)
                val subSteps = if (turboSpeed) 3 else 2
                val subDt = effectiveDt / subSteps
                for (step in 0 until subSteps) {
                    stepBalls(subDt)
                }

                // Check instant clear if lasers/multi-ball destroyed the final brick
                if (mode == BrickQuestMode.STAGE && bricks.isEmpty()) {
                    checkInstantStageClear()
                    return
                }

                // Check turn completion without iterator allocation
                if (ballsRemainingToSpawn == 0) {
                    var anyActive = false
                    for (i in 0 until balls.size) {
                        if (balls[i].active) {
                            anyActive = true
                            break
                        }
                    }
                    if (!anyActive) {
                        finishShootingTurn()
                    }
                }
            }

            TurnPhase.DROPPING -> {
                dropAnimRemaining = (dropAnimRemaining - dt * 7.5f).coerceAtLeast(0f)
                for (i in 0 until bricks.size) {
                    bricks[i].renderRowOffset = -dropAnimRemaining
                }
                if (dropAnimRemaining <= 0f) {
                    var dangerHit = false
                    for (i in 0 until bricks.size) {
                        val b = bricks[i]
                        b.renderRowOffset = 0f
                        if (b.row >= DANGER_ROW) dangerHit = true
                    }
                    aimPreviewDirty = true
                    phase = if (dangerHit) TurnPhase.GAME_OVER else TurnPhase.AIMING
                }
            }
        }
    }

    private fun updateVisualFx(dt: Float) {
        for (i in 0 until bricks.size) {
            val b = bricks[i]
            if (b.hitFlash > 0f) {
                b.hitFlash = (b.hitFlash - dt * 6.5f).coerceAtLeast(0f)
            }
        }
        for (i in 0 until pickups.size) {
            pickups[i].pulsePhase += dt * 4.5f
        }
        // Reverse-indexed removal avoids iterator allocations & element shifting overhead
        for (i in particles.size - 1 downTo 0) {
            val p = particles[i]
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 4.5f * dt
            p.alpha -= dt * 2.4f
            if (p.alpha <= 0f) {
                particles[i] = particles[particles.size - 1]
                particles.removeAt(particles.size - 1)
            }
        }
        for (i in laserBeams.size - 1 downTo 0) {
            val beam = laserBeams[i]
            beam.life -= dt * 4.2f
            if (beam.life <= 0f) {
                laserBeams.removeAt(i)
            }
        }
        for (i in floatingTexts.size - 1 downTo 0) {
            val ft = floatingTexts[i]
            ft.y -= dt * 1.5f
            ft.life -= dt * 1.5f
            if (ft.life <= 0f) {
                floatingTexts.removeAt(i)
            }
        }
    }

    private fun stepBalls(dt: Float) {
        val ballRadiusSq = BALL_RADIUS * BALL_RADIUS
        val pickupHitR = BALL_RADIUS + 0.30f
        val pickupHitRSq = pickupHitR * pickupHitR

        for (bIndex in 0 until balls.size) {
            val ball = balls[bIndex]
            if (!ball.active) continue

            ball.x += ball.vx * dt
            ball.y += ball.vy * dt

            if (ball.returning) {
                if (ball.y >= FLOOR_Y) {
                    ball.y = FLOOR_Y
                    ball.active = false
                }
                continue
            }

            // Left / Right walls
            if (ball.x - BALL_RADIUS < 0f) {
                ball.x = BALL_RADIUS
                ball.vx = abs(ball.vx)
            } else if (ball.x + BALL_RADIUS > ARENA_COLS) {
                ball.x = ARENA_COLS - BALL_RADIUS
                ball.vx = -abs(ball.vx)
            }

            // Ceiling wall
            if (ball.y - BALL_RADIUS < 0f) {
                ball.y = BALL_RADIUS
                ball.vy = abs(ball.vy)
            }

            // Expanded Paddle Deflection (active while PADDLE_EXPAND bonus timer > 0!)
            if (paddleExpandTimer > 0f && ball.vy > 0f &&
                ball.y + BALL_RADIUS >= paddleY && ball.y - BALL_RADIUS <= FLOOR_Y + 0.1f
            ) {
                val halfW = paddleWidth * 0.5f + BALL_RADIUS
                val dxPaddle = ball.x - paddleX
                if (abs(dxPaddle) <= halfW) {
                    val speed = kotlin.math.sqrt(ball.vx * ball.vx + ball.vy * ball.vy).coerceAtLeast(18f)
                    val ratio = (dxPaddle / halfW).coerceIn(-0.92f, 0.92f)
                    // Aimable rebound angle between -152 deg and -28 deg
                    val reboundAngle = (-PI / 2.0).toFloat() + ratio * 1.08f
                    ball.y = paddleY - BALL_RADIUS - 0.01f
                    ball.vx = cos(reboundAngle) * speed
                    ball.vy = sin(reboundAngle) * speed
                    hitSoundCounter++
                    spawnSquareBurst(ball.x, paddleY, Color(0xFF38BDF8), count = 3)
                    continue
                }
            }

            // Floor return
            if (ball.y >= FLOOR_Y && ball.vy > 0f) {
                ball.y = FLOOR_Y
                ball.active = false
                if (nextLaunchX == null) {
                    nextLaunchX = ball.x.coerceIn(0.65f, ARENA_COLS - 0.65f)
                }
                continue
            }

            // Check Falling Power-Up Capsules (balls can also snag them mid-air!)
            for (fpIdx in fallingPowerUps.size - 1 downTo 0) {
                val fp = fallingPowerUps[fpIdx]
                val fdx = ball.x - fp.x
                val fdy = ball.y - fp.y
                if (fdx * fdx + fdy * fdy <= 0.30f) {
                    fallingPowerUps.removeAt(fpIdx)
                    activatePowerUp(fp.kind, fp.x, fp.y)
                }
            }

            // Check Pickups using squared distance (avoids sqrt/hypot!)
            for (pIdx in 0 until pickups.size) {
                val pickup = pickups[pIdx]
                if (pickup.collected) continue
                val pdx = ball.x - pickup.cx
                val pdy = ball.y - pickup.cy
                if (pdx * pdx + pdy * pdy <= pickupHitRSq) {
                    handlePickupHit(pickup)
                }
            }

            // Check Bricks (Circle vs AABB with fast bounding rejection)
            var collidedBrick: BrickEntity? = null
            var bestOverlap = -1f
            var hitNormalX = 0f
            var hitNormalY = 0f

            for (idx in 0 until bricks.size) {
                val brick = bricks[idx]
                // Ultra-fast AABB rejection before clamp
                if (ball.x + BALL_RADIUS < brick.left || ball.x - BALL_RADIUS > brick.right) continue
                if (ball.y + BALL_RADIUS < brick.top || ball.y - BALL_RADIUS > brick.bottom) continue

                val closestX = ball.x.coerceIn(brick.left, brick.right)
                val closestY = ball.y.coerceIn(brick.top, brick.bottom)
                val dx = ball.x - closestX
                val dy = ball.y - closestY
                val distSq = dx * dx + dy * dy
                if (distSq < ballRadiusSq) {
                    val dist = kotlin.math.sqrt(distSq)
                    val overlap = BALL_RADIUS - dist
                    if (overlap > bestOverlap) {
                        bestOverlap = overlap
                        collidedBrick = brick
                        if (dist > 0.0001f) {
                            hitNormalX = dx / dist
                            hitNormalY = dy / dist
                        } else {
                            hitNormalX = 0f
                            hitNormalY = if (ball.vy > 0f) -1f else 1f
                        }
                    }
                }
            }

            if (collidedBrick != null) {
                // Push out cleanly
                ball.x += hitNormalX * (bestOverlap + 0.004f)
                ball.y += hitNormalY * (bestOverlap + 0.004f)

                // Reflect velocity cleanly
                if (abs(hitNormalX) > abs(hitNormalY)) {
                    ball.vx = if (hitNormalX > 0f) abs(ball.vx) else -abs(ball.vx)
                } else {
                    ball.vy = if (hitNormalY > 0f) abs(ball.vy) else -abs(ball.vy)
                }

                // Anti-horizontal-trap nudge so balls never bounce sideways infinitely
                val speed = kotlin.math.sqrt(ball.vx * ball.vx + ball.vy * ball.vy).coerceAtLeast(16f)
                if (abs(ball.vy) < speed * 0.14f) {
                    val signY = if (ball.vy >= 0f) 1f else -1f
                    ball.vy = signY * speed * 0.18f
                    val newVxMag = kotlin.math.sqrt((speed * speed - ball.vy * ball.vy).coerceAtLeast(1f))
                    ball.vx = if (ball.vx >= 0f) newVxMag else -newVxMag
                }

                val effectiveDmg = if (multiBallTimer > 0f) max(damagePerHit, 2) else damagePerHit
                damageBrick(collidedBrick, damage = effectiveDmg, fromChain = false)
            }
        }
    }

    private fun updateActivePowerUps(dt: Float) {
        // Smoothly animate paddle expansion width
        val targetWidth = if (paddleExpandTimer > 0f) EXPANDED_PADDLE_WIDTH else BASE_PADDLE_WIDTH
        paddleWidth += (targetWidth - paddleWidth) * (dt * 9.5f).coerceAtMost(1f)
        val halfW = paddleWidth * 0.5f
        paddleX = paddleX.coerceIn(halfW, ARENA_COLS - halfW)

        // Move falling collectable power-up capsules & check paddle catch
        val catchHalfW = halfW + 0.38f
        for (i in fallingPowerUps.size - 1 downTo 0) {
            val fp = fallingPowerUps[i]
            fp.y += fp.vy * dt
            fp.pulsePhase += dt * 6.0f

            // Paddle catch check
            if (fp.y >= paddleY - 0.34f && fp.y <= FLOOR_Y + 0.32f && abs(fp.x - paddleX) <= catchHalfW) {
                fallingPowerUps.removeAt(i)
                activatePowerUp(fp.kind, fp.x, paddleY)
                continue
            }
            if (fp.y > ARENA_ROWS + 0.45f) {
                fallingPowerUps.removeAt(i)
            }
        }

        if (phase == TurnPhase.STAGE_CLEAR || phase == TurnPhase.GAME_OVER) return

        // Countdown temporary bonus timers during active gameplay
        if (multiBallTimer > 0f) {
            multiBallTimer = (multiBallTimer - dt).coerceAtLeast(0f)
        }
        if (paddleExpandTimer > 0f) {
            paddleExpandTimer = (paddleExpandTimer - dt).coerceAtLeast(0f)
        }
        if (laserBeamTimer > 0f) {
            laserBeamTimer = (laserBeamTimer - dt).coerceAtLeast(0f)
            if (phase == TurnPhase.SHOOTING && bricks.isNotEmpty()) {
                laserAutoFireCooldown -= dt
                if (laserAutoFireCooldown <= 0f) {
                    laserAutoFireCooldown = 0.42f
                    firePaddleTwinLasers()
                }
            }
        }
    }

    private fun spawnFallingPowerUp(col: Int, row: Int, kind: PowerUpType) {
        if (fallingPowerUps.size >= 10) return
        fallingPowerUps.add(
            FallingPowerUpEntity(
                id = nextEntityId++,
                x = col + 0.5f,
                y = row + 0.5f,
                kind = kind,
                pulsePhase = rng.nextFloat() * 6.28f
            )
        )
    }

    private fun activatePowerUp(kind: PowerUpType, x: Float, y: Float) {
        laserSoundCounter++
        when (kind) {
            PowerUpType.MULTI_BALL -> {
                multiBallTimer = MULTI_BALL_DURATION_SEC
                spawnSquareBurst(x, y, Color(0xFFEC4899), count = 12)
                floatingTexts.add(FloatingTextFx(x, y - 0.3f, "MULTI-BALL!", Color(0xFFF472B6), 1.3f))
                // Split up to 6 active balls into angled twin neon balls (capped at MAX_ACTIVE_BALLS!)
                var spawned = 0
                val existingCount = balls.size
                for (i in 0 until existingCount) {
                    if (spawned >= 6 || balls.size >= MAX_ACTIVE_BALLS) break
                    val src = balls[i]
                    if (!src.active || src.returning) continue
                    val angle = atan2(src.vy, src.vx)
                    val speed = kotlin.math.sqrt(src.vx * src.vx + src.vy * src.vy).coerceAtLeast(18f)
                    val spread = if (spawned % 2 == 0) 0.24f else -0.24f
                    val newA = angle + spread
                    balls.add(
                        BallEntity(
                            x = src.x,
                            y = src.y,
                            vx = cos(newA) * speed,
                            vy = sin(newA) * speed
                        )
                    )
                    spawned++
                }
                // If collected when aiming or with few balls, queue bonus balls immediately!
                if (spawned < 4) {
                    if (phase == TurnPhase.SHOOTING) {
                        ballsRemainingToSpawn += (6 - spawned)
                    } else {
                        bonusBallsThisTurn += 6
                    }
                }
            }

            PowerUpType.LASER_BEAM -> {
                laserBeamTimer = LASER_BEAM_DURATION_SEC
                laserAutoFireCooldown = 0.05f
                spawnSquareBurst(x, y, Color(0xFF4ADE80), count = 12)
                floatingTexts.add(FloatingTextFx(x, y - 0.3f, "LASER BEAM!", Color(0xFF4ADE80), 1.3f))
                // Fire an immediate twin laser volley on pickup for instant gratification!
                firePaddleTwinLasers()
            }

            PowerUpType.PADDLE_EXPAND -> {
                paddleExpandTimer = PADDLE_EXPAND_DURATION_SEC
                spawnSquareBurst(x, y, Color(0xFF38BDF8), count = 12)
                floatingTexts.add(FloatingTextFx(x, y - 0.3f, "WIDE PADDLE!", Color(0xFF7DD3FC), 1.3f))
            }
        }
    }

    private fun firePaddleTwinLasers() {
        val halfW = paddleWidth * 0.5f
        val leftX = (paddleX - halfW * 0.72f).coerceIn(0.35f, ARENA_COLS - 0.35f)
        val rightX = (paddleX + halfW * 0.72f).coerceIn(0.35f, ARENA_COLS - 0.35f)
        laserSoundCounter++
        laserBeams.add(LaserBeamFx(isHorizontal = false, gridCoord = leftX, color = Color(0xFF4ADE80), life = 0.85f))
        laserBeams.add(LaserBeamFx(isHorizontal = false, gridCoord = rightX, color = Color(0xFF4ADE80), life = 0.85f))

        // Hit lowest brick intersecting each laser beam column
        hitLowestBrickAtX(leftX)
        hitLowestBrickAtX(rightX)
        checkInstantStageClear()
    }

    private fun hitLowestBrickAtX(beamX: Float) {
        var target: BrickEntity? = null
        var lowestRow = -1
        for (i in 0 until bricks.size) {
            val b = bricks[i]
            if (beamX >= b.left - 0.14f && beamX <= b.right + 0.14f && b.row > lowestRow) {
                lowestRow = b.row
                target = b
            }
        }
        if (target != null) {
            damageBrick(target, damage = max(3, damagePerHit + 2), fromChain = true)
        }
    }

    private fun handlePickupHit(pickup: PickupEntity) {
        when (pickup.kind) {
            PickupKind.PLUS_BALL -> {
                pickup.collected = true
                pendingBonusBallsNextTurn++
                laserSoundCounter++
                spawnSquareBurst(pickup.cx, pickup.cy, Color(0xFFE879F9), count = 8)
                floatingTexts.add(
                    FloatingTextFx(
                        x = pickup.cx,
                        y = pickup.cy,
                        text = "+1",
                        color = Color(0xFFF0ABFC)
                    )
                )
            }
            PickupKind.LASER_HORIZONTAL -> {
                if (!pickup.triggeredThisTurn) {
                    pickup.triggeredThisTurn = true
                    laserSoundCounter++
                    laserBeams.add(
                        LaserBeamFx(
                            isHorizontal = true,
                            gridCoord = pickup.cy,
                            color = Color(0xFF4ADE80),
                            life = 1.0f
                        )
                    )
                    val targets = bricks.filter { it.row == pickup.row }.toList()
                    targets.forEach { damageBrick(it, damage = max(2, damagePerHit + 1), fromChain = true) }
                }
            }
            PickupKind.LASER_VERTICAL -> {
                if (!pickup.triggeredThisTurn) {
                    pickup.triggeredThisTurn = true
                    laserSoundCounter++
                    laserBeams.add(
                        LaserBeamFx(
                            isHorizontal = false,
                            gridCoord = pickup.cx,
                            color = Color(0xFF38BDF8),
                            life = 1.0f
                        )
                    )
                    val targets = bricks.filter { it.col == pickup.col }.toList()
                    targets.forEach { damageBrick(it, damage = max(2, damagePerHit + 1), fromChain = true) }
                }
            }
            PickupKind.LASER_CROSS -> {
                if (!pickup.triggeredThisTurn) {
                    pickup.triggeredThisTurn = true
                    laserSoundCounter++
                    laserBeams.add(LaserBeamFx(true, pickup.cy, Color(0xFF4ADE80), 1.0f))
                    laserBeams.add(LaserBeamFx(false, pickup.cx, Color(0xFF38BDF8), 1.0f))
                    val targets = bricks.filter { it.row == pickup.row || it.col == pickup.col }.toList()
                    targets.forEach { damageBrick(it, damage = max(2, damagePerHit + 1), fromChain = true) }
                }
            }
        }
    }

    private fun damageBrick(brick: BrickEntity, damage: Int, fromChain: Boolean) {
        if (brick !in bricks) return
        val dealt = min(brick.hp, damage)
        brick.hp -= dealt
        brick.hitFlash = 1f
        comboHitsThisTurn++
        val comboMult = (1 + comboHitsThisTurn / 12).coerceAtMost(4)
        score += dealt * 10 * comboMult

        // Hitting a special power-up brick spawns a falling collectable capsule!
        if (!brick.powerUpDropped) {
            val dropType = when (brick.special) {
                BrickSpecial.POWER_MULTI_BALL -> PowerUpType.MULTI_BALL
                BrickSpecial.POWER_LASER_BEAM -> PowerUpType.LASER_BEAM
                BrickSpecial.POWER_PADDLE_EXPAND -> PowerUpType.PADDLE_EXPAND
                else -> null
            }
            if (dropType != null) {
                brick.powerUpDropped = true
                spawnFallingPowerUp(brick.col, brick.row, dropType)
            }
        }

        val color = brickColorForHp(brick.hp.coerceAtLeast(1), brick.special)
        if (brick.hp <= 0) {
            bricks.remove(brick)
            breakSoundCounter++
            spawnSquareBurst(brick.col + 0.5f, brick.row + 0.5f, color, count = 11)
            if (brick.special == BrickSpecial.BOMB_3X3) {
                detonateBomb(brick.col, brick.row)
            }
        } else {
            hitSoundCounter++
            if (!fromChain) {
                spawnSquareBurst(brick.col + 0.5f, brick.row + 0.5f, Color(0xFF67E8F9), count = 3)
            }
        }
    }

    private fun detonateBomb(centerCol: Int, centerRow: Int) {
        laserSoundCounter++
        spawnSquareBurst(centerCol + 0.5f, centerRow + 0.5f, Color(0xFFFDE047), count = 20)
        floatingTexts.add(
            FloatingTextFx(
                x = centerCol + 0.5f,
                y = centerRow + 0.5f,
                text = "BOOM!",
                color = Color(0xFFFDE047)
            )
        )
        val neighbors = bricks.filter { b ->
            abs(b.col - centerCol) <= 1 && abs(b.row - centerRow) <= 1
        }.toList()
        neighbors.forEach { n ->
            damageBrick(n, damage = max(12, n.hp / 2 + 6), fromChain = true)
        }
    }

    private fun spawnSquareBurst(cx: Float, cy: Float, color: Color, count: Int) {
        if (particles.size >= 48) return
        val allowed = min(count, 48 - particles.size)
        for (i in 0 until allowed) {
            val angle = rng.nextFloat() * 6.2831855f
            val speed = 1.5f + rng.nextFloat() * 4.8f
            particles.add(
                SquareParticle(
                    x = cx + (rng.nextFloat() - 0.5f) * 0.35f,
                    y = cy + (rng.nextFloat() - 0.5f) * 0.35f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 1.2f,
                    size = 0.14f + rng.nextFloat() * 0.15f,
                    alpha = 0.95f,
                    color = color
                )
            )
        }
    }

    private fun checkInstantStageClear() {
        if (mode == BrickQuestMode.STAGE && bricks.isEmpty()) {
            balls.clear()
            ballsRemainingToSpawn = 0
            phase = TurnPhase.STAGE_CLEAR
        }
    }

    private fun finishShootingTurn() {
        balls.clear()
        totalBalls += pendingBonusBallsNextTurn
        pendingBonusBallsNextTurn = 0
        bonusBallsThisTurn = 0
        damagePerHit = 1
        turboSpeed = false
        launchX = nextLaunchX ?: launchX
        nextLaunchX = null
        turnNumber++

        // Remove collected +1 ball pickups and triggered laser orbs
        pickups.removeAll { it.collected || it.triggeredThisTurn }

        if (mode == BrickQuestMode.STAGE && bricks.isEmpty()) {
            phase = TurnPhase.STAGE_CLEAR
            return
        }

        // Advance all surviving bricks & pickups down by 1 row
        bricks.forEach { b ->
            b.row += 1
            b.renderRowOffset = -1f
        }
        pickups.forEach { p ->
            p.row += 1
        }
        pickups.removeAll { it.row >= DANGER_ROW }

        if (mode == BrickQuestMode.ENDLESS) {
            spawnEndlessRow(row = 1, wave = turnNumber + 2)
        }

        dropAnimRemaining = 1f
        phase = TurnPhase.DROPPING
    }

    companion object {
        const val BASE_PADDLE_WIDTH = 2.15f
        const val EXPANDED_PADDLE_WIDTH = 3.90f
        const val MULTI_BALL_DURATION_SEC = 8.0f
        const val LASER_BEAM_DURATION_SEC = 7.0f
        const val PADDLE_EXPAND_DURATION_SEC = 10.0f
        private const val MAX_ACTIVE_BALLS = 80

        /**
         * Vibrant neon brick color palette matching the Bricks Breaker Quest screenshots,
         * plus distinct vivid colors for special power-up bricks.
         */
        fun brickColorForHp(hp: Int, special: BrickSpecial = BrickSpecial.NONE): Color {
            return when (special) {
                BrickSpecial.BOMB_3X3 -> Color(0xFFFF6B00)
                BrickSpecial.POWER_MULTI_BALL -> Color(0xFFEC4899)
                BrickSpecial.POWER_LASER_BEAM -> Color(0xFF10B981)
                BrickSpecial.POWER_PADDLE_EXPAND -> Color(0xFF06B6D4)
                BrickSpecial.NONE -> when {
                    hp <= 4 -> Color(0xFF22D3EE)
                    hp <= 9 -> Color(0xFF22C55E)
                    hp <= 14 -> Color(0xFFFACC15)
                    hp <= 24 -> Color(0xFFEF4444)
                    hp <= 38 -> Color(0xFFA855F7)
                    else -> Color(0xFFEC4899)
                }
            }
        }

        /**
         * Fast packed RGB color lookup for native Canvas batching (avoids Color -> Int conversion).
         */
        fun brickRgbForHp(hp: Int, special: BrickSpecial = BrickSpecial.NONE): Int {
            return when (special) {
                BrickSpecial.BOMB_3X3 -> 0x00FF6B00
                BrickSpecial.POWER_MULTI_BALL -> 0x00EC4899
                BrickSpecial.POWER_LASER_BEAM -> 0x0010B981
                BrickSpecial.POWER_PADDLE_EXPAND -> 0x0006B6D4
                BrickSpecial.NONE -> when {
                    hp <= 4 -> 0x0022D3EE
                    hp <= 9 -> 0x0022C55E
                    hp <= 14 -> 0x00FACC15
                    hp <= 24 -> 0x00EF4444
                    hp <= 38 -> 0x00A855F7
                    else -> 0x00EC4899
                }
            }
        }
    }
}
