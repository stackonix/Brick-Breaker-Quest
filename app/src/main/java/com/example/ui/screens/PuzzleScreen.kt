package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.CodeBreakerPuzzle
import com.example.domain.engine.MemoryGridPuzzle
import com.example.domain.engine.NumberChainPuzzle
import com.example.domain.engine.OddShapePuzzle
import com.example.domain.engine.QuickCalcPuzzle
import com.example.domain.engine.WordTwistPuzzle
import com.example.domain.model.AppLanguage
import com.example.domain.model.GameMode
import com.example.ui.components.CodeBreakerView
import com.example.ui.components.LeaveMatchDialog
import com.example.ui.components.MemoryGridView
import com.example.ui.components.NumberChainView
import com.example.ui.components.OddShapeView
import com.example.ui.components.QuickCalcView
import com.example.ui.components.RivalAvatar
import com.example.ui.components.WordTwistView
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LocalMindfeudColors
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.RoyalVioletBright
import com.example.ui.theme.SkyCyanEnd
import com.example.ui.viewmodel.ActiveMatchState
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Section 6 rule: Puzzle play area occupies >65% of vertical space, top has tabular timer & score,
 * bottom has limited Hint button, Back button always prompts "Keep Playing" first.
 */
@Composable
fun PuzzleScreen(
    match: ActiveMatchState,
    hintsRemainingToday: Int,
    language: AppLanguage,
    reduceMotion: Boolean,
    onSubmitNumberChain: (Int) -> Unit,
    onSubmitOddShape: (Int) -> Unit,
    onSubmitQuickCalc: (List<Int>) -> Unit,
    onSubmitWordTwist: (String) -> Boolean,
    onSubmitCodeBreaker: (List<Int>) -> Unit,
    onSubmitMemoryGrid: (Set<Int>) -> Unit,
    onUseHint: () -> Unit,
    onLeaveMatch: () -> Unit
) {
    var showLeaveDialog by remember { mutableStateOf(false) }
    BackHandler { showLeaveDialog = true }

    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val isHindi = language == AppLanguage.HINDI
    val puzzle = match.currentPuzzle ?: return

    // Soft horizontal shake on wrong answer (Section 6)
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(match.roundFeedback) {
        val fb = match.roundFeedback
        if (fb != null && !fb.isCorrect && !reduceMotion) {
            for (target in listOf(-14f, 14f, -10f, 10f, 0f)) {
                shakeOffset.animateTo(target, tween(durationMillis = 45))
            }
        }
    }

    val secondsLeft = ceil(match.remainingRoundMs / 1000.0).toInt().coerceAtLeast(0)
    val timerColor = when {
        match.isUntimed -> palette.mintText
        secondsLeft <= 4 -> palette.coralText
        secondsLeft <= 8 -> palette.goldText
        else -> palette.cyanText
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP HUD (~16% height): Close + Round progress + Tabular Timer + Scores
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { showLeaveDialog = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("puzzle_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = strings.leaveMatchCta,
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Round pill + Puzzle name (with Level badge for Campaign Mode!)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val roundHeader = if (match.mode == GameMode.CAMPAIGN_LEVEL && match.levelNumber != null) {
                                "${strings.levelWord} ${match.levelNumber} • ${strings.roundOf} ${match.currentRoundIndex + 1}/${match.puzzles.size}"
                            } else {
                                "${strings.roundOf} ${match.currentRoundIndex + 1}/${match.puzzles.size}"
                            }
                            Text(
                                text = roundHeader,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = palette.violetText
                            )
                            Text(
                                text = strings.puzzleName(puzzle.type, isHindi),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Tabular Timer (>=28sp per Section 6)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = timerColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = if (match.isUntimed) "∞" else "${secondsLeft}s",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 28.sp,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = timerColor,
                                modifier = Modifier.testTag("puzzle_timer_text")
                            )
                        }
                    }

                    // Vibrant Gradient Scoreboard Strip (You vs Rival or Friend Target)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.horizontalGradient(listOf(RoyalViolet, RoyalVioletBright, ElectricBlue)))
                            .padding(horizontal = 16.dp, vertical = 11.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = strings.youLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "${match.playerScore}",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = Color.White
                                )
                            }

                            // Round pips
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (i in match.puzzles.indices) {
                                    val done = match.roundsCompleted.getOrNull(i)
                                    val color = when {
                                        done?.playerCorrect == true -> Color(0xFF4ADE80)
                                        done?.playerCorrect == false -> Color(0xFFFB7185)
                                        i == match.currentRoundIndex -> Color(0xFFFDE047)
                                        else -> Color.White.copy(alpha = 0.30f)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(width = if (i == match.currentRoundIndex) 20.dp else 10.dp, height = 10.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(color)
                                    )
                                }
                            }

                            if (match.mode == GameMode.DUEL && match.rivalId != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (match.rivalHasSolvedCurrentRound) strings.rivalSolved else strings.rivalThinking,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (match.rivalHasSolvedCurrentRound) Color(0xFFFDE047) else Color.White.copy(alpha = 0.85f)
                                        )
                                        Text(
                                            text = "${match.rivalScore}",
                                            style = MaterialTheme.typography.displayMedium.copy(
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFeatureSettings = "tnum"
                                            ),
                                            color = Color.White
                                        )
                                    }
                                    RivalAvatar(
                                        rival = match.rivalId,
                                        contentDescription = strings.rivalName(match.rivalId, isHindi),
                                        size = 38.dp,
                                        borderColor = Color.White
                                    )
                                }
                            } else if (match.mode == GameMode.FRIEND_CHALLENGE && match.friendChallenge?.targetScore != null) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = match.friendChallenge.challengerName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "${match.friendChallenge.targetScore}",
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        color = Color(0xFFFDE047)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.22f)
                                ) {
                                    Text(
                                        text = strings.skillName(puzzle.type.skill, isHindi),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFDE047),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Thin timer progress bar
                    if (!match.isUntimed) {
                        val totalMs = (puzzle.timeLimitSec * 1000f).coerceAtLeast(1000f)
                        LinearProgressIndicator(
                            progress = { (match.remainingRoundMs / totalMs).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = timerColor,
                            trackColor = palette.elevatedSurface
                        )
                    }
                }

                // MAIN PUZZLE AREA (>=65% vertical weight per Section 6!)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (puzzle) {
                        is NumberChainPuzzle -> NumberChainView(
                            puzzle = puzzle,
                            eliminatedIndices = match.eliminatedOptionIndices,
                            onSelectOption = onSubmitNumberChain
                        )
                        is CodeBreakerPuzzle -> CodeBreakerView(
                            puzzle = puzzle,
                            history = match.codeBreakerHistory,
                            revealedPegIndex = match.revealedCodePegIndex,
                            onSubmitGuess = onSubmitCodeBreaker
                        )
                        is OddShapePuzzle -> OddShapeView(
                            puzzle = puzzle,
                            eliminatedIndices = match.eliminatedOptionIndices,
                            onSelectTile = onSubmitOddShape
                        )
                        is QuickCalcPuzzle -> QuickCalcView(
                            puzzle = puzzle,
                            hintFirstCardId = match.revealedCodePegIndex,
                            onSubmitSequence = onSubmitQuickCalc
                        )
                        is WordTwistPuzzle -> WordTwistView(
                            puzzle = puzzle,
                            hintPrefix = match.revealedWordPrefix,
                            onSubmitWord = onSubmitWordTwist
                        )
                        is MemoryGridPuzzle -> MemoryGridView(
                            puzzle = puzzle,
                            previewActive = match.memoryPreviewActive,
                            previewRemainingMs = match.memoryPreviewRemainingMs,
                            onCompleteSelection = onSubmitMemoryGrid
                        )
                    }
                }

                // BOTTOM BAR: Round feedback toast OR Limited Hint button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val fb = match.roundFeedback
                    if (fb != null) {
                        val bannerBg = if (fb.isCorrect) palette.mintContainer else palette.coralContainer
                        val bannerBorder = if (fb.isCorrect) palette.mint else palette.coral
                        val bannerFg = if (fb.isCorrect) palette.mintText else palette.coralText
                        val msg = when {
                            fb.isCorrect -> "${strings.correctToast} +${fb.pointsAwarded}"
                            fb.isTimeUp -> strings.timeUpGentle
                            fb.messageIndex == 0 -> strings.wrongGentle1
                            fb.messageIndex == 1 -> strings.wrongGentle2
                            else -> strings.wrongGentle3
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = bannerBg,
                            border = BorderStroke(1.5.dp, bannerBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (fb.isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                    contentDescription = null,
                                    tint = bannerBorder,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = bannerFg,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        val canUseHint = !match.hintUsedThisRound &&
                            match.countdownValue == 0 &&
                            (match.mode == GameMode.PRACTICE || hintsRemainingToday > 0)
                        OutlinedButton(
                            onClick = onUseHint,
                            enabled = canUseHint,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("puzzle_hint_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (canUseHint) palette.goldContainer else palette.elevatedSurface
                            ),
                            border = BorderStroke(
                                1.8.dp,
                                if (canUseHint) palette.gold else palette.borderSubtle
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = if (canUseHint) palette.gold else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = when {
                                    match.mode == GameMode.PRACTICE -> "${strings.hintBtn} (∞)"
                                    hintsRemainingToday > 0 -> "${strings.hintBtn} ($hintsRemainingToday/3)"
                                    else -> strings.noHintsLeft
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (canUseHint) palette.goldText else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Vibrant 3-2-1 Countdown Overlay (Section 5)
            AnimatedVisibility(
                visible = match.countdownValue > 0,
                enter = fadeIn(tween(120)),
                exit = fadeOut(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    RoyalViolet.copy(alpha = 0.94f),
                                    ElectricBlue.copy(alpha = 0.94f),
                                    SkyCyanEnd.copy(alpha = 0.94f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (match.mode == GameMode.CAMPAIGN_LEVEL && match.levelNumber != null) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.22f),
                                border = BorderStroke(1.5.dp, Color(0xFFFDE047))
                            ) {
                                Text(
                                    text = "${strings.levelWord} ${match.levelNumber}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                        } else if (match.rivalId != null) {
                            RivalAvatar(
                                rival = match.rivalId,
                                contentDescription = strings.rivalName(match.rivalId, isHindi),
                                size = 92.dp,
                                borderColor = Color.White
                            )
                            Text(
                                text = "vs ${strings.rivalName(match.rivalId, isHindi)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Text(
                            text = strings.getReady,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.90f)
                        )
                        Text(
                            text = "${match.countdownValue}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 84.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = Color(0xFFFDE047)
                        )
                }
            }
        }
    }

    if (showLeaveDialog) {
        LeaveMatchDialog(
            onKeepPlaying = { showLeaveDialog = false },
            onLeave = {
                showLeaveDialog = false
                onLeaveMatch()
            }
        )
    }
}
