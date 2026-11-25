package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.CodeBreakerPuzzle
import com.example.domain.engine.CodeGuessFeedback
import com.example.domain.engine.MemoryGridPuzzle
import com.example.domain.engine.NumberChainPuzzle
import com.example.domain.engine.OddShapePuzzle
import com.example.domain.engine.PuzzleEngine
import com.example.domain.engine.QuickCalcPuzzle
import com.example.domain.engine.ShapeKind
import com.example.domain.engine.ShapeTileSpec
import com.example.domain.engine.WordTwistPuzzle
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalMindfeudColors
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// 1. NUMBER CHAIN VIEW
@Composable
fun NumberChainView(
    puzzle: NumberChainPuzzle,
    eliminatedIndices: Set<Int>,
    onSelectOption: (Int) -> Unit,
    highlightAnswerIndex: Int? = null,
    reduceMotion: Boolean = false
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = strings.numChainPrompt,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Sequence strip (3D arcade tiles)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            puzzle.sequence.forEach { num ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.cardLip)
                        .padding(bottom = 3.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.cardSurface,
                        border = BorderStroke(1.5.dp, palette.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$num",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            // Mystery slot
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.violet.copy(alpha = 0.45f))
                    .padding(bottom = 3.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = palette.violetContainer,
                    border = BorderStroke(2.dp, palette.violet),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.violetText
                        )
                    }
                }
            }
        }

        // 2x2 large thumb-friendly 3D arcade answer tiles
        val bounceY = if (highlightAnswerIndex != null && !reduceMotion) {
            val inf = rememberInfiniteTransition(label = "handBounce")
            inf.animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
                label = "handY"
            ).value
        } else 0f

        val tilePalettes = listOf(
            Triple(TileBlueBg, TileBlueBorder, TileBlueLip),
            Triple(TilePurpleBg, TilePurpleBorder, TilePurpleLip),
            Triple(TileMintBg, TileMintBorder, TileMintLip),
            Triple(TileAmberBg, TileAmberBorder, TileAmberLip)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (row in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (col in 0..1) {
                        val idx = row * 2 + col
                        val value = puzzle.options[idx]
                        val eliminated = idx in eliminatedIndices
                        val isGuided = highlightAnswerIndex == idx
                        val tileShape = RoundedCornerShape(18.dp)
                        val (tileBg, tileBorder, tileLip) = tilePalettes[idx % tilePalettes.size]

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .alpha(if (eliminated) 0.25f else 1f)
                                .clip(tileShape)
                                .background(
                                    when {
                                        isGuided -> palette.violet
                                        palette.isDark -> tileLip.copy(alpha = 0.65f)
                                        else -> tileLip
                                    }
                                )
                                .padding(bottom = 5.dp)
                        ) {
                            Surface(
                                shape = tileShape,
                                color = when {
                                    isGuided -> palette.violetContainer
                                    palette.isDark -> palette.cardSurface
                                    else -> tileBg
                                },
                                border = BorderStroke(
                                    width = if (isGuided) 2.5.dp else 2.dp,
                                    color = if (isGuided) palette.violet else tileBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(74.dp)
                                    .clip(tileShape)
                                    .clickable(enabled = !eliminated) { onSelectOption(value) }
                                    .minimumInteractiveComponentSize()
                                    .testTag("number_option_$idx")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$value",
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isGuided) palette.violetText else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            if (isGuided) {
                                Icon(
                                    imageVector = Icons.Filled.TouchApp,
                                    contentDescription = null,
                                    tint = palette.gold,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 10.dp, bottom = 6.dp)
                                        .offset(y = bounceY.dp)
                                        .size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. CODE BREAKER VIEW (Mastermind with Color + Symbol pegs for color-blind accessibility)
@Composable
fun CodeBreakerView(
    puzzle: CodeBreakerPuzzle,
    history: List<CodeGuessFeedback>,
    revealedPegIndex: Int?,
    onSubmitGuess: (List<Int>) -> Unit
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val pegColors = listOf(palette.cyan, palette.violet, palette.mint, palette.gold, palette.coral, Color(0xFF3B82F6))
    val pegSymbols = listOf("●", "▲", "■", "★", "◆", "✚")

    val currentGuess = remember(puzzle.id) { mutableStateListOf<Int>() }

    LaunchedEffect(revealedPegIndex, puzzle.id) {
        if (revealedPegIndex != null && currentGuess.isEmpty()) {
            currentGuess.add(puzzle.secretCode[revealedPegIndex])
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = strings.codeBreakerPrompt,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (revealedPegIndex != null) {
            val revealedVal = puzzle.secretCode[revealedPegIndex]
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.goldContainer,
                border = BorderStroke(1.dp, palette.gold.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Hint: Slot #${revealedPegIndex + 1} = ${pegSymbols[revealedVal]}",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.goldText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Previous guesses
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (attemptIdx in 0 until puzzle.maxAttempts) {
                val entry = history.getOrNull(attemptIdx)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (entry != null) palette.cardSurface else palette.elevatedSurface.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, palette.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (slot in 0 until 4) {
                                val peg = entry?.guess?.getOrNull(slot)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (peg != null) pegColors[peg] else palette.borderSubtle.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (peg != null) {
                                        Text(
                                            text = pegSymbols[peg],
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (entry != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✓ ${entry.exactMatches}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = palette.mintText
                                )
                                Text(
                                    text = "◐ ${entry.colorOnlyMatches}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = palette.goldText
                                )
                            }
                        } else {
                            Text(
                                text = "#${attemptIdx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Current draft slots
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (slot in 0 until 4) {
                val peg = currentGuess.getOrNull(slot)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (peg != null) pegColors[peg] else palette.cardSurface)
                        .border(2.2.dp, if (peg != null) pegColors[peg] else palette.violet, CircleShape)
                        .clickable(enabled = peg != null) {
                            if (slot < currentGuess.size) currentGuess.removeAt(slot)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (peg != null) {
                        Text(
                            text = pegSymbols[peg],
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Color + Symbol picker row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            for (pegIdx in 0 until puzzle.paletteSize) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(pegColors[pegIdx])
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable(enabled = currentGuess.size < 4) {
                            if (currentGuess.size < 4) currentGuess.add(pegIdx)
                        }
                        .minimumInteractiveComponentSize()
                        .testTag("code_peg_$pegIdx"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pegSymbols[pegIdx],
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { currentGuess.clear() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(strings.codeBreakerClear)
            }
            Button(
                onClick = {
                    if (currentGuess.size == 4) {
                        val copy = currentGuess.toList()
                        currentGuess.clear()
                        onSubmitGuess(copy)
                    }
                },
                enabled = currentGuess.size == 4,
                modifier = Modifier
                    .weight(1.4f)
                    .height(50.dp)
                    .testTag("code_submit_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primaryActionBg,
                    contentColor = palette.primaryActionFg
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.codeBreakerSubmit, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 3. ODD SHAPE VIEW (6 custom 3D white game Canvas tiles)
@Composable
fun OddShapeView(
    puzzle: OddShapePuzzle,
    eliminatedIndices: Set<Int>,
    onSelectTile: (Int) -> Unit
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val shapeColors = listOf(palette.cyan, palette.violet, palette.mint, palette.gold, palette.coral)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.oddShapePrompt,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (row in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        val spec = puzzle.tiles[idx]
                        val eliminated = idx in eliminatedIndices
                        val tileShape = RoundedCornerShape(20.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .alpha(if (eliminated) 0.22f else 1f)
                                .clip(tileShape)
                                .background(palette.cardLip)
                                .padding(bottom = 4.dp)
                        ) {
                            Surface(
                                shape = tileShape,
                                color = palette.cardSurface,
                                border = BorderStroke(1.5.dp, palette.borderSubtle),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(tileShape)
                                    .clickable(enabled = !eliminated) { onSelectTile(idx) }
                                    .testTag("odd_shape_tile_$idx")
                            ) {
                                ShapeTileCanvas(
                                    spec = spec,
                                    color = shapeColors[spec.colorIndex % shapeColors.size],
                                    dotColor = if (spec.isFilled) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapeTileCanvas(
    spec: ShapeTileSpec,
    color: Color,
    dotColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.42f
        val stroke = Stroke(width = 7.5f)

        rotate(degrees = spec.rotationDeg, pivot = Offset(cx, cy)) {
            when (spec.shape) {
                ShapeKind.CIRCLE -> {
                    if (spec.isFilled) drawCircle(color = color, radius = r, center = Offset(cx, cy))
                    else drawCircle(color = color, radius = r, center = Offset(cx, cy), style = stroke)
                }
                ShapeKind.SQUARE -> {
                    val topLeft = Offset(cx - r * 0.85f, cy - r * 0.85f)
                    val s = Size(r * 1.7f, r * 1.7f)
                    if (spec.isFilled) drawRect(color = color, topLeft = topLeft, size = s)
                    else drawRect(color = color, topLeft = topLeft, size = s, style = stroke)
                }
                ShapeKind.TRIANGLE -> {
                    val p = polygonPath(cx, cy, r, 3, -90.0)
                    if (spec.isFilled) drawPath(p, color = color)
                    else drawPath(p, color = color, style = stroke)
                }
                ShapeKind.DIAMOND -> {
                    val p = polygonPath(cx, cy, r, 4, -90.0)
                    if (spec.isFilled) drawPath(p, color = color)
                    else drawPath(p, color = color, style = stroke)
                }
                ShapeKind.HEXAGON -> {
                    val p = polygonPath(cx, cy, r, 6, -30.0)
                    if (spec.isFilled) drawPath(p, color = color)
                    else drawPath(p, color = color, style = stroke)
                }
                ShapeKind.STAR -> {
                    val p = starPath(cx, cy, r, r * 0.48f, 5)
                    if (spec.isFilled) drawPath(p, color = color)
                    else drawPath(p, color = color, style = stroke)
                }
            }
        }

        // Inner dots (clear visual cue independent of color)
        val dotR = size.minDimension * 0.068f
        when (spec.innerDots) {
            1 -> drawCircle(dotColor, dotR, Offset(cx, cy))
            2 -> {
                drawCircle(dotColor, dotR, Offset(cx - dotR * 1.8f, cy))
                drawCircle(dotColor, dotR, Offset(cx + dotR * 1.8f, cy))
            }
            3 -> {
                drawCircle(dotColor, dotR, Offset(cx, cy - dotR * 1.6f))
                drawCircle(dotColor, dotR, Offset(cx - dotR * 1.8f, cy + dotR * 1.3f))
                drawCircle(dotColor, dotR, Offset(cx + dotR * 1.8f, cy + dotR * 1.3f))
            }
        }
    }
}

private fun polygonPath(cx: Float, cy: Float, r: Float, sides: Int, startAngleDeg: Double): Path {
    val path = Path()
    for (i in 0 until sides) {
        val rad = (startAngleDeg + i * (360.0 / sides)) * PI / 180.0
        val x = cx + (r * cos(rad)).toFloat()
        val y = cy + (r * sin(rad)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun starPath(cx: Float, cy: Float, outerR: Float, innerR: Float, points: Int): Path {
    val path = Path()
    for (i in 0 until points * 2) {
        val rad = (-90.0 + i * (180.0 / points)) * PI / 180.0
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + (r * cos(rad)).toFloat()
        val y = cy + (r * sin(rad)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

// 4. QUICK CALC VIEW (3 operation cards to hit exact Target)
@Composable
fun QuickCalcView(
    puzzle: QuickCalcPuzzle,
    hintFirstCardId: Int?,
    onSubmitSequence: (List<Int>) -> Unit
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val selectedIds = remember(puzzle.id) { mutableStateListOf<Int>() }

    LaunchedEffect(hintFirstCardId, puzzle.id) {
        if (hintFirstCardId != null && selectedIds.isEmpty()) {
            selectedIds.add(hintFirstCardId)
        }
    }

    val currentValue = PuzzleEngine.evaluateQuickCalc(puzzle, selectedIds)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.quickCalcPrompt,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Start -> Current -> Target HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                label = strings.quickCalcStart,
                value = "${puzzle.startValue}",
                accent = palette.violetText,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = strings.quickCalcCurrent,
                value = "$currentValue",
                accent = if (currentValue == puzzle.targetValue && selectedIds.size == 3) palette.mintText else palette.cyanText,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = strings.quickCalcTarget,
                value = "${puzzle.targetValue}",
                accent = palette.goldText,
                modifier = Modifier.weight(1f)
            )
        }

        // 3 Selected slots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            for (slot in 0 until 3) {
                val cardId = selectedIds.getOrNull(slot)
                val card = puzzle.cards.firstOrNull { it.id == cardId }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (card != null) palette.violetContainer else palette.elevatedSurface,
                    border = BorderStroke(
                        1.5.dp,
                        if (card != null) palette.violet else palette.borderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = card?.label() ?: "Step ${slot + 1}",
                            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                            color = if (card != null) palette.violetText else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 5 Tactile Colorful 3D Operation Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val firstRow = puzzle.cards.take(3)
            val secondRow = puzzle.cards.drop(3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                firstRow.forEachIndexed { idx, card ->
                    val used = card.id in selectedIds
                    CalcCardButton(
                        label = card.label(),
                        used = used,
                        colorSlot = idx,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!used && selectedIds.size < 3) {
                                selectedIds.add(card.id)
                                if (selectedIds.size == 3) {
                                    onSubmitSequence(selectedIds.toList())
                                }
                            }
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                secondRow.forEachIndexed { idx, card ->
                    val used = card.id in selectedIds
                    CalcCardButton(
                        label = card.label(),
                        used = used,
                        colorSlot = idx + 3,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!used && selectedIds.size < 3) {
                                selectedIds.add(card.id)
                                if (selectedIds.size == 3) {
                                    onSubmitSequence(selectedIds.toList())
                                }
                            }
                        }
                    )
                }
                OutlinedButton(
                    onClick = { if (selectedIds.isNotEmpty()) selectedIds.removeAt(selectedIds.lastIndex) },
                    enabled = selectedIds.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(66.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = strings.quickCalcUndo)
                    Spacer(Modifier.width(4.dp))
                    Text(strings.quickCalcUndo)
                }
            }
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    val palette = LocalMindfeudColors.current
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.28f))
            .padding(bottom = 3.dp)
    ) {
        Surface(
            shape = shape,
            color = palette.cardSurface,
            border = BorderStroke(1.5.dp, accent.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
                    color = accent
                )
            }
        }
    }
}

@Composable
private fun CalcCardButton(
    label: String,
    used: Boolean,
    colorSlot: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = LocalMindfeudColors.current
    val shape = RoundedCornerShape(16.dp)
    val tilePalettes = listOf(
        Triple(TileBlueBg, TileBlueBorder, TileBlueLip),
        Triple(TilePurpleBg, TilePurpleBorder, TilePurpleLip),
        Triple(TileMintBg, TileMintBorder, TileMintLip),
        Triple(TileAmberBg, TileAmberBorder, TileAmberLip)
    )
    val (bg, border, lip) = tilePalettes[colorSlot % tilePalettes.size]

    Box(
        modifier = modifier
            .height(66.dp)
            .alpha(if (used) 0.35f else 1f)
            .clip(shape)
            .background(if (used) palette.borderSubtle else lip)
            .padding(bottom = 4.dp)
    ) {
        Surface(
            shape = shape,
            color = when {
                used -> palette.elevatedSurface
                palette.isDark -> palette.cardSurface
                else -> bg
            },
            border = BorderStroke(2.dp, if (used) palette.borderSubtle else border),
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .clickable(enabled = !used, onClick = onClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// 5. WORD TWIST VIEW
@Composable
fun WordTwistView(
    puzzle: WordTwistPuzzle,
    hintPrefix: String?,
    onSubmitWord: (String) -> Boolean
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current

    var rack by remember(puzzle.id) { mutableStateOf(puzzle.letters) }
    val pickedIndices = remember(puzzle.id) { mutableStateListOf<Int>() }
    var invalidWarning by remember(puzzle.id) { mutableStateOf(false) }

    val builtWord = pickedIndices.map { rack[it] }.joinToString("")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.wordTwistPrompt,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (hintPrefix != null) {
            Surface(shape = RoundedCornerShape(12.dp), color = palette.goldContainer) {
                Text(
                    text = "Hint: Starts with \"$hintPrefix...\"",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.goldText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Word slots
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = palette.cardSurface,
            border = BorderStroke(
                2.dp,
                if (invalidWarning) palette.coral else palette.violet.copy(alpha = 0.7f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (builtWord.isEmpty()) "• • • • • •" else builtWord.toList().joinToString(" "),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 28.sp,
                        letterSpacing = 3.sp
                    ),
                    color = if (builtWord.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (invalidWarning) {
            Text(
                text = strings.wordTwistNotValid,
                style = MaterialTheme.typography.labelMedium,
                color = palette.coralText
            )
        }

        // 6 Tactile Colorful Golden-Amber 3D Letter Tiles
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (row in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        val used = idx in pickedIndices
                        val shape = RoundedCornerShape(16.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(66.dp)
                                .alpha(if (used) 0.3f else 1f)
                                .clip(shape)
                                .background(if (used) palette.borderSubtle else TileAmberLip)
                                .padding(bottom = 5.dp)
                        ) {
                            Surface(
                                shape = shape,
                                color = when {
                                    used -> palette.elevatedSurface
                                    palette.isDark -> palette.cardSurface
                                    else -> TileAmberBg
                                },
                                border = BorderStroke(2.dp, if (used) palette.borderSubtle else TileAmberBorder),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(shape)
                                    .clickable(enabled = !used) {
                                        invalidWarning = false
                                        pickedIndices.add(idx)
                                    }
                                    .testTag("word_letter_$idx")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${rack[idx]}",
                                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    pickedIndices.clear()
                    rack = rack.shuffled()
                    invalidWarning = false
                },
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Shuffle, contentDescription = strings.wordTwistShuffle)
            }
            OutlinedButton(
                onClick = {
                    if (pickedIndices.isNotEmpty()) {
                        pickedIndices.removeAt(pickedIndices.lastIndex)
                        invalidWarning = false
                    }
                },
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
            }
            Button(
                onClick = {
                    val ok = onSubmitWord(builtWord)
                    if (!ok) invalidWarning = true
                },
                enabled = builtWord.length >= 3,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("word_submit_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primaryActionBg,
                    contentColor = palette.primaryActionFg
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.wordTwistSubmit, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 6. MEMORY GRID VIEW
@Composable
fun MemoryGridView(
    puzzle: MemoryGridPuzzle,
    previewActive: Boolean,
    previewRemainingMs: Long,
    onCompleteSelection: (Set<Int>) -> Unit
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val selected = remember(puzzle.id) { mutableStateListOf<Int>() }
    val remainingToPick = (puzzle.targetCells.size - selected.size).coerceAtLeast(0)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (previewActive) {
                strings.memoryMemorize
            } else {
                "${strings.memoryRecall} ($remainingToPick ${strings.memoryRemaining})"
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (previewActive) palette.goldText else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (previewActive) {
            LinearProgressIndicator(
                progress = { (previewRemainingMs.toFloat() / puzzle.previewDurationMs.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = palette.gold,
                trackColor = palette.elevatedSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(if (puzzle.gridSize == 3) 0.82f else 0.92f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (row in 0 until puzzle.gridSize) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0 until puzzle.gridSize) {
                        val idx = row * puzzle.gridSize + col
                        val isTarget = idx in puzzle.targetCells
                        val isPicked = idx in selected
                        val lit = (previewActive && isTarget) || (!previewActive && isPicked)
                        val tileShape = RoundedCornerShape(16.dp)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(tileShape)
                                .background(
                                    when {
                                        previewActive && isTarget -> palette.cyanText
                                        !previewActive && isPicked -> palette.primaryActionLip
                                        else -> palette.cardLip
                                    }
                                )
                                .padding(bottom = 4.dp)
                        ) {
                            Surface(
                                shape = tileShape,
                                color = when {
                                    previewActive && isTarget -> palette.cyan
                                    !previewActive && isPicked -> palette.violet
                                    else -> palette.cardSurface
                                },
                                border = BorderStroke(
                                    1.8.dp,
                                    if (lit) Color.White.copy(alpha = 0.45f) else palette.borderSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(tileShape)
                                    .clickable(enabled = !previewActive) {
                                        if (isPicked) {
                                            selected.remove(idx)
                                        } else {
                                            selected.add(idx)
                                            if (selected.size == puzzle.targetCells.size) {
                                                onCompleteSelection(selected.toSet())
                                            }
                                        }
                                    }
                                    .testTag("memory_cell_$idx")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (lit) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!previewActive && selected.isNotEmpty()) {
            OutlinedButton(
                onClick = { selected.clear() },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.codeBreakerClear)
            }
        }
    }
}
