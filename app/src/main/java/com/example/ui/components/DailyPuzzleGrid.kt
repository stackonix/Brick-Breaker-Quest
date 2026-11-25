package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.ArcadeCoral
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldEnd
import com.example.ui.theme.EmeraldStart
import com.example.ui.theme.LocalMindfeudColors
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.RoyalVioletBright
import com.example.ui.theme.SunsetOrangeStart
import com.example.ui.theme.TileAmberBg
import com.example.ui.theme.TileAmberBorder
import com.example.ui.theme.TileAmberLip
import com.example.ui.theme.TileBlueBg
import com.example.ui.theme.TileBlueBorder
import com.example.ui.theme.TileBlueLip
import com.example.ui.theme.TileMintBg
import com.example.ui.theme.TileMintBorder
import com.example.ui.theme.TileMintLip
import com.example.ui.theme.TilePurpleBg
import com.example.ui.theme.TilePurpleBorder
import com.example.ui.theme.TilePurpleLip
import kotlin.random.Random

enum class GridDifficulty(val preFilledCells: Int) {
    EASY(11),
    MEDIUM(8),
    HARD(5)
}

enum class DailyGridStatus {
    IN_PROGRESS,
    HAS_CONFLICT,
    SOLVED
}

/**
 * Immutable UI state for tracking difficulty, user inputs, selected cell, conflicts, and solve status
 * inside the 4x4 Daily Logic Grid puzzle.
 */
@Immutable
data class DailyPuzzleGridState(
    val epochDay: Long,
    val difficulty: GridDifficulty = GridDifficulty.MEDIUM,
    val gridSize: Int = 4,
    val solution: List<Int>,
    val givenIndices: Set<Int>,
    val userInputs: Map<Int, Int> = emptyMap(),
    val selectedCellIndex: Int? = null,
    val movesCount: Int = 0
) {
    val totalEditableCount: Int
        get() = (gridSize * gridSize) - givenIndices.size

    val filledEditableCount: Int
        get() = userInputs.count { (idx, _) -> idx !in givenIndices }

    fun valueAt(index: Int): Int? =
        if (index in givenIndices) solution[index] else userInputs[index]

    fun isGiven(index: Int): Boolean = index in givenIndices

    /**
     * Returns all grid cell indices that violate 4x4 Latin-square / 2x2-box uniqueness rules.
     */
    fun conflictingIndices(): Set<Int> {
        val conflicts = mutableSetOf<Int>()
        val values = List(gridSize * gridSize) { valueAt(it) }

        // Check rows
        for (r in 0 until gridSize) {
            val seen = mutableMapOf<Int, MutableList<Int>>()
            for (c in 0 until gridSize) {
                val idx = r * gridSize + c
                val v = values[idx] ?: continue
                seen.getOrPut(v) { mutableListOf() }.add(idx)
            }
            seen.values.filter { it.size > 1 }.forEach { conflicts.addAll(it) }
        }

        // Check columns
        for (c in 0 until gridSize) {
            val seen = mutableMapOf<Int, MutableList<Int>>()
            for (r in 0 until gridSize) {
                val idx = r * gridSize + c
                val v = values[idx] ?: continue
                seen.getOrPut(v) { mutableListOf() }.add(idx)
            }
            seen.values.filter { it.size > 1 }.forEach { conflicts.addAll(it) }
        }

        // Check 2x2 boxes
        for (boxRow in 0 until 2) {
            for (boxCol in 0 until 2) {
                val seen = mutableMapOf<Int, MutableList<Int>>()
                for (dr in 0 until 2) {
                    for (dc in 0 until 2) {
                        val idx = (boxRow * 2 + dr) * gridSize + (boxCol * 2 + dc)
                        val v = values[idx] ?: continue
                        seen.getOrPut(v) { mutableListOf() }.add(idx)
                    }
                }
                seen.values.filter { it.size > 1 }.forEach { conflicts.addAll(it) }
            }
        }

        return conflicts
    }

    val status: DailyGridStatus
        get() {
            val conflicts = conflictingIndices()
            if (conflicts.isNotEmpty()) return DailyGridStatus.HAS_CONFLICT
            val allFilled = (0 until gridSize * gridSize).all { valueAt(it) != null }
            return if (allFilled) DailyGridStatus.SOLVED else DailyGridStatus.IN_PROGRESS
        }

    fun selectCell(index: Int): DailyPuzzleGridState {
        if (index in givenIndices || index !in 0 until (gridSize * gridSize)) return this
        return copy(selectedCellIndex = index)
    }

    fun enterDigit(digit: Int): DailyPuzzleGridState {
        val target = selectedCellIndex?.takeIf { it !in givenIndices } ?: firstOpenEditableIndex() ?: return this
        val nextInputs = userInputs.toMutableMap().apply { put(target, digit) }
        val updated = copy(
            userInputs = nextInputs,
            movesCount = movesCount + 1
        )
        val nextSelected = updated.nextOpenEditableIndex(after = target) ?: target
        return updated.copy(selectedCellIndex = nextSelected)
    }

    fun clearSelected(): DailyPuzzleGridState {
        val target = selectedCellIndex ?: return this
        if (target in givenIndices || target !in userInputs) return this
        val nextInputs = userInputs.toMutableMap().apply { remove(target) }
        return copy(userInputs = nextInputs)
    }

    fun applyHint(): DailyPuzzleGridState {
        val target = selectedCellIndex
            ?.takeIf { it !in givenIndices && userInputs[it] != solution[it] }
            ?: (0 until gridSize * gridSize).firstOrNull { it !in givenIndices && userInputs[it] != solution[it] }
            ?: return this
        val nextInputs = userInputs.toMutableMap().apply { put(target, solution[target]) }
        val updated = copy(userInputs = nextInputs, movesCount = movesCount + 1)
        val nextSelected = updated.nextOpenEditableIndex(after = target) ?: target
        return updated.copy(selectedCellIndex = nextSelected)
    }

    fun reset(): DailyPuzzleGridState {
        val firstEditable = (0 until gridSize * gridSize).firstOrNull { it !in givenIndices }
        return copy(
            userInputs = emptyMap(),
            selectedCellIndex = firstEditable,
            movesCount = 0
        )
    }

    private fun firstOpenEditableIndex(): Int? =
        (0 until gridSize * gridSize).firstOrNull { it !in givenIndices && it !in userInputs }

    private fun nextOpenEditableIndex(after: Int): Int? {
        val total = gridSize * gridSize
        for (offset in 1..total) {
            val idx = (after + offset) % total
            if (idx !in givenIndices && idx !in userInputs) return idx
        }
        return null
    }
}

/**
 * Deterministic daily 4x4 logic grid generator supporting Easy (11 pre-filled),
 * Medium (8 pre-filled), and Hard (5 pre-filled) clue layouts — all mathematically
 * guaranteed to have a unique pure-logic solution via isomorphic band/stack permutations.
 */
object DailyLogicGridGenerator {
    private val baseGrid = listOf(
        1, 2, 3, 4,
        3, 4, 1, 2,
        2, 1, 4, 3,
        4, 3, 2, 1
    )

    // Proven uniquely solvable base clue sets (mapped through the exact same row/col permutation!)
    private val easyGivenBaseIndices = setOf(0, 1, 2, 3, 5, 6, 7, 9, 11, 12, 14)   // 11 pre-filled clues (5 blanks)
    private val mediumGivenBaseIndices = setOf(0, 1, 6, 7, 9, 11, 12, 14)         // 8 pre-filled clues (8 blanks)
    private val hardGivenBaseIndices = setOf(1, 4, 6, 11, 14)                      // 5 pre-filled clues (11 blanks)

    fun createForDay(
        epochDay: Long,
        difficulty: GridDifficulty = GridDifficulty.MEDIUM
    ): DailyPuzzleGridState {
        val rng = Random(epochDay * 31_337L + difficulty.ordinal * 997L + 17L)

        // Permute digits 1..4
        val digitMap = listOf(1, 2, 3, 4).shuffled(rng)

        // Valid 2x2-box-preserving row & column permutations
        val rowBands = listOf(listOf(0, 1).shuffled(rng), listOf(2, 3).shuffled(rng)).shuffled(rng).flatten()
        val colStacks = listOf(listOf(0, 1).shuffled(rng), listOf(2, 3).shuffled(rng)).shuffled(rng).flatten()

        val baseGivens = when (difficulty) {
            GridDifficulty.EASY -> easyGivenBaseIndices
            GridDifficulty.MEDIUM -> mediumGivenBaseIndices
            GridDifficulty.HARD -> hardGivenBaseIndices
        }

        val solution = MutableList(16) { 1 }
        val mappedGivens = mutableSetOf<Int>()

        for (r in 0 until 4) {
            for (c in 0 until 4) {
                val srcR = rowBands[r]
                val srcC = colStacks[c]
                val srcIdx = srcR * 4 + srcC
                val dstIdx = r * 4 + c
                val baseVal = baseGrid[srcIdx]
                solution[dstIdx] = digitMap[baseVal - 1]
                if (srcIdx in baseGivens) {
                    mappedGivens.add(dstIdx)
                }
            }
        }

        val firstEditable = (0 until 16).firstOrNull { it !in mappedGivens }

        return DailyPuzzleGridState(
            epochDay = epochDay,
            difficulty = difficulty,
            gridSize = 4,
            solution = solution,
            givenIndices = mappedGivens,
            userInputs = emptyMap(),
            selectedCellIndex = firstEditable,
            movesCount = 0
        )
    }
}

/**
 * Interactive Jetpack Compose Daily Puzzle component displaying a colorful 4x4 logic grid
 * with a dynamic Easy / Medium / Hard difficulty selector that adjusts the number of
 * pre-filled cells (11 / 8 / 5), plus full user input state tracking and conflict detection.
 */
@Composable
fun DailyPuzzleGrid(
    epochDay: Long,
    modifier: Modifier = Modifier,
    initialDifficulty: GridDifficulty = GridDifficulty.MEDIUM,
    onSolved: ((moves: Int) -> Unit)? = null
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current

    var selectedDifficulty by remember(epochDay) { mutableStateOf(initialDifficulty) }
    var gridState by remember(epochDay, selectedDifficulty) {
        mutableStateOf(DailyLogicGridGenerator.createForDay(epochDay, selectedDifficulty))
    }
    var reportedSolved by remember(epochDay, selectedDifficulty) { mutableStateOf(false) }

    val conflicts = remember(gridState.userInputs, gridState.givenIndices) {
        gridState.conflictingIndices()
    }
    val status = gridState.status

    val digitPalettes = listOf(
        Triple(TileBlueBg, TileBlueBorder, TileBlueLip),
        Triple(TilePurpleBg, TilePurpleBorder, TilePurpleLip),
        Triple(TileMintBg, TileMintBorder, TileMintLip),
        Triple(TileAmberBg, TileAmberBorder, TileAmberLip)
    )

    MindfeudCard(
        borderColor = when (status) {
            DailyGridStatus.SOLVED -> EmeraldStart
            DailyGridStatus.HAS_CONFLICT -> palette.coral
            DailyGridStatus.IN_PROGRESS -> RoyalVioletBright.copy(alpha = 0.65f)
        },
        containerColor = palette.cardSurface,
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_puzzle_grid_component")
    ) {
        // Colorful Header Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        if (status == DailyGridStatus.SOLVED) {
                            listOf(EmeraldStart, EmeraldEnd)
                        } else {
                            listOf(RoyalViolet, ElectricBlue)
                        }
                    )
                )
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (status == DailyGridStatus.SOLVED) Icons.Filled.CheckCircle else Icons.Filled.GridOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = strings.dailyGridTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${gridState.givenIndices.size} ${strings.cluesLabel} • ${strings.dailyGridSubtitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.24f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Text(
                    text = if (status == DailyGridStatus.SOLVED) {
                        strings.dailyGridSolvedBadge
                    } else {
                        "${gridState.filledEditableCount}/${gridState.totalEditableCount}"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("daily_grid_progress_badge")
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Dynamic Difficulty Selector (Easy = 11 clues, Medium = 8 clues, Hard = 5 clues)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_grid_difficulty_selector"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val options = listOf(
                Triple(GridDifficulty.EASY, strings.difficultyEasy, EmeraldStart),
                Triple(GridDifficulty.MEDIUM, strings.difficultyMedium, ElectricBlue),
                Triple(GridDifficulty.HARD, strings.difficultyHard, SunsetOrangeStart)
            )
            options.forEach { (diff, label, activeColor) ->
                val isSelected = selectedDifficulty == diff
                val chipShape = RoundedCornerShape(14.dp)
                Surface(
                    shape = chipShape,
                    color = if (isSelected) {
                        activeColor
                    } else {
                        palette.elevatedSurface
                    },
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.2.dp,
                        color = if (isSelected) activeColor else palette.borderSubtle
                    ),
                    onClick = {
                        if (selectedDifficulty != diff) {
                            selectedDifficulty = diff
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("daily_grid_difficulty_${diff.name.lowercase()}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${diff.preFilledCells} ${strings.cluesLabel}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 4x4 Grid Layout (with subtle extra spacing between 2x2 sub-boxes)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_logic_grid"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0 until gridState.gridSize) {
                if (row == 2) {
                    Spacer(Modifier.height(2.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until gridState.gridSize) {
                        if (col == 2) {
                            Spacer(Modifier.width(2.dp))
                        }
                        val idx = row * gridState.gridSize + col
                        val value = gridState.valueAt(idx)
                        val isGiven = gridState.isGiven(idx)
                        val isSelected = gridState.selectedCellIndex == idx && !isGiven
                        val hasConflict = idx in conflicts
                        val tileShape = RoundedCornerShape(14.dp)

                        val digitStyle = value?.let { digitPalettes[(it - 1).coerceIn(0, 3)] }
                        val cellBg = when {
                            hasConflict -> palette.coralContainer
                            isGiven -> if (palette.isDark) palette.elevatedSurface else Color(0xFFEDE9FE)
                            value != null && !palette.isDark -> digitStyle!!.first
                            value != null -> palette.violetContainer
                            isSelected -> if (palette.isDark) palette.cyanContainer else TileBlueBg
                            else -> if (palette.isDark) palette.cardSurface else Color(0xFFF8FAFF)
                        }
                        val cellBorder = when {
                            hasConflict -> ArcadeCoral
                            isSelected -> ElectricBlue
                            isGiven -> RoyalVioletBright.copy(alpha = 0.45f)
                            value != null -> digitStyle!!.second
                            else -> palette.borderSubtle
                        }
                        val cellLip = when {
                            hasConflict -> ArcadeCoral
                            isSelected -> ElectricBlue
                            isGiven -> RoyalViolet.copy(alpha = 0.35f)
                            value != null -> digitStyle!!.third
                            else -> palette.cardLip
                        }
                        val cellTextColor = when {
                            hasConflict -> palette.coralText
                            isGiven -> palette.violetText
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        val cellDesc = if (value != null) {
                            "Row ${row + 1} Column ${col + 1}, value $value${if (isGiven) ", locked clue" else ""}"
                        } else {
                            "Row ${row + 1} Column ${col + 1}, empty"
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(tileShape)
                                .background(cellLip)
                                .padding(bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(tileShape)
                                    .background(cellBg)
                                    .border(
                                        width = if (isSelected || hasConflict) 2.5.dp else 1.6.dp,
                                        color = cellBorder,
                                        shape = tileShape
                                    )
                                    .clickable(enabled = !isGiven && status != DailyGridStatus.SOLVED) {
                                        gridState = gridState.selectCell(idx)
                                    }
                                    .minimumInteractiveComponentSize()
                                    .semantics { contentDescription = cellDesc }
                                    .testTag("daily_grid_cell_$idx"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = value?.toString() ?: "",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 24.sp,
                                        fontWeight = if (isGiven) FontWeight.ExtraBold else FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = cellTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Live Status Feedback Banner (Conflict or Solved)
        if (status == DailyGridStatus.HAS_CONFLICT) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.coralContainer,
                border = BorderStroke(1.2.dp, palette.coral),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = palette.coral,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.dailyGridConflictHint,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.coralText
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        } else if (status == DailyGridStatus.SOLVED) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = palette.mintContainer,
                border = BorderStroke(1.5.dp, EmeraldStart),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_grid_solved_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldStart,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "${strings.dailyGridSolvedBanner} (${gridState.movesCount})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.mintText
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            reportedSolved = false
                            gridState = gridState.reset()
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("daily_grid_replay_button")
                    ) {
                        Text(strings.dailyGridReset, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Interactive 1-4 Colorful Arcade Number Keypad
        if (status != DailyGridStatus.SOLVED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (digit in 1..4) {
                    val (bg, border, lip) = digitPalettes[digit - 1]
                    val keyShape = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(keyShape)
                            .background(lip)
                            .padding(bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(keyShape)
                                .background(if (palette.isDark) palette.elevatedSurface else bg)
                                .border(1.8.dp, border, keyShape)
                                .clickable {
                                    val next = gridState.enterDigit(digit)
                                    gridState = next
                                    if (next.status == DailyGridStatus.SOLVED && !reportedSolved) {
                                        reportedSolved = true
                                        onSolved?.invoke(next.movesCount)
                                    }
                                }
                                .minimumInteractiveComponentSize()
                                .testTag("daily_grid_key_$digit"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$digit",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Utility Controls: Clear, Hint, Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { gridState = gridState.clearSelected() },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("daily_grid_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(strings.dailyGridClear, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }

                OutlinedButton(
                    onClick = {
                        val next = gridState.applyHint()
                        gridState = next
                        if (next.status == DailyGridStatus.SOLVED && !reportedSolved) {
                            reportedSolved = true
                            onSolved?.invoke(next.movesCount)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.4.dp, palette.gold),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("daily_grid_hint_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = palette.gold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = strings.hintBtn,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.goldText,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = {
                        reportedSolved = false
                        gridState = gridState.reset()
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("daily_grid_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(strings.dailyGridReset, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
            }
        }
    }
}
