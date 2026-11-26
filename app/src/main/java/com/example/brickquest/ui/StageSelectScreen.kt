package com.example.brickquest.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brickquest.data.BrickQuestProgress
import com.example.brickquest.data.TOTAL_QUEST_STAGES
import com.example.brickquest.engine.STAGES_PER_WORLD
import com.example.brickquest.engine.StageWorlds
import kotlinx.coroutines.launch

private const val TILES_PER_ROW = 5
private const val ROWS_PER_WORLD = STAGES_PER_WORLD / TILES_PER_ROW

/**
 * Stage map for 1500 stages.
 * Worlds up to the player's current world are expanded (rows of 5 tiles, lazily composed),
 * later worlds show as a single locked teaser card. Opens scrolled to the current stage.
 */
@Composable
fun StageSelectScreen(
    progress: BrickQuestProgress,
    onSelectStage: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val current = progress.highestUnlockedStage
    val currentWorld = StageWorlds.worldIndex(current)
    val currentRowInWorld = ((current - 1) % STAGES_PER_WORLD) / TILES_PER_ROW
    // List layout: for every expanded world -> 1 header + ROWS_PER_WORLD rows; locked worlds -> 1 item.
    val currentRowIndex = currentWorld * (1 + ROWS_PER_WORLD) + 1 + currentRowInWorld
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (currentRowIndex - 1).coerceAtLeast(0))
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrickQuestColors.CosmicBackdropBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BrickQuestColors.CardSurfaceElevated,
                        border = BorderStroke(1.3.dp, BrickQuestColors.NeonCyan.copy(alpha = 0.65f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("stage_select_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = BrickQuestColors.TextPrimary
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "WORLD MAP",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = BrickQuestColors.TextPrimary
                        )
                        Text(
                            text = "$TOTAL_QUEST_STAGES stages • ${StageWorlds.count} worlds",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrickQuestColors.TextSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StarCountPill(total = progress.totalStars)
                    Surface(
                        shape = CircleShape,
                        color = BrickQuestColors.CardSurfaceElevated,
                        border = BorderStroke(1.3.dp, BrickQuestColors.NeonCyan.copy(alpha = 0.65f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch { listState.animateScrollToItem((currentRowIndex - 1).coerceAtLeast(0)) }
                            },
                            modifier = Modifier.testTag("stage_select_locate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = "Scroll to current stage",
                                tint = BrickQuestColors.NeonCyanBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Continue banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(BrickQuestColors.PlayButtonBrush)
                    .border(1.4.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .clickable { onSelectStage(current) }
                    .testTag("stage_select_quick_play_button")
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WORLD ${currentWorld + 1} • ${StageWorlds.name(currentWorld).uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (current % 10 == 0) "PLAY BOSS STAGE $current" else "PLAY STAGE $current",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play Stage $current",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (w in 0 until StageWorlds.count) {
                    val accent = Color(StageWorlds.accentArgb(w))
                    val first = StageWorlds.firstStage(w)
                    val last = StageWorlds.lastStage(w)
                    if (w <= currentWorld) {
                        item(key = "world_$w") {
                            WorldHeader(
                                worldIndex = w,
                                accent = accent,
                                first = first,
                                last = last,
                                progress = progress
                            )
                        }
                        for (r in 0 until ROWS_PER_WORLD) {
                            val rowStart = first + r * TILES_PER_ROW
                            if (rowStart > last) break
                            item(key = "row_${w}_$r") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (i in 0 until TILES_PER_ROW) {
                                        val stage = rowStart + i
                                        if (stage <= last) {
                                            StageTile(
                                                stageNumber = stage,
                                                unlocked = progress.isStageUnlocked(stage),
                                                isCurrent = stage == current,
                                                isBoss = stage % 10 == 0,
                                                stars = progress.starsFor(stage),
                                                accent = accent,
                                                onClick = { onSelectStage(stage) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item(key = "locked_world_$w") {
                            LockedWorldCard(worldIndex = w, accent = accent, first = first, last = last)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StarCountPill(total: Int) {
    Surface(
        shape = RoundedCornerShape(50),
        color = BrickQuestColors.CardSurfaceElevated,
        border = BorderStroke(1.3.dp, BrickQuestColors.NeonGold.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Stars Collected",
                tint = BrickQuestColors.NeonGold,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$total",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = BrickQuestColors.TextPrimary
            )
        }
    }
}

@Composable
private fun WorldHeader(
    worldIndex: Int,
    accent: Color,
    first: Int,
    last: Int,
    progress: BrickQuestProgress
) {
    val stars = remember(progress.stageStars, first, last) { (first..last).sumOf { progress.starsFor(it) } }
    val maxStars = (last - first + 1) * 3
    val cleared = (progress.highestUnlockedStage - first).coerceIn(0, last - first + 1)
    val ratio = cleared / (last - first + 1).toFloat()

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BrickQuestColors.CardSurface.copy(alpha = 0.94f),
        border = BorderStroke(1.3.dp, accent.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (worldIndex == 0) 0.dp else 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WORLD ${worldIndex + 1} • ${StageWorlds.name(worldIndex).uppercase()}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Stages $first-$last • boss every 10th stage",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickQuestColors.TextSecondary
                    )
                }
                Text(
                    text = "★ $stars/$maxStars",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrickQuestColors.NeonGold
                )
            }
            // World completion bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0A1628))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(ratio.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                )
            }
        }
    }
}

@Composable
private fun LockedWorldCard(worldIndex: Int, accent: Color, first: Int, last: Int) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF080D1A),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = accent.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WORLD ${worldIndex + 1} • ${StageWorlds.name(worldIndex).uppercase()}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = BrickQuestColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Clear stage ${first - 1} to unlock • stages $first-$last",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrickQuestColors.TextMuted.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private val CURRENT_STAGE_BRUSH = Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFFDB2777)))
private val BOSS_STAGE_BRUSH = Brush.verticalGradient(listOf(Color(0xFF7F1D1D), Color(0xFF3B0764)))
private val UNLOCKED_STAGE_BRUSH = Brush.verticalGradient(listOf(BrickQuestColors.CardSurfaceElevated, BrickQuestColors.CardSurface))
private val LOCKED_STAGE_BRUSH = Brush.verticalGradient(listOf(Color(0xFF0A1020), Color(0xFF070B16)))
private val STAGE_TILE_SHAPE = RoundedCornerShape(14.dp)

@Composable
private fun StageTile(
    stageNumber: Int,
    unlocked: Boolean,
    isCurrent: Boolean,
    isBoss: Boolean,
    stars: Int,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgBrush = when {
        isCurrent -> CURRENT_STAGE_BRUSH
        isBoss && unlocked -> BOSS_STAGE_BRUSH
        unlocked && stars > 0 -> Brush.verticalGradient(listOf(accent.copy(alpha = 0.28f), BrickQuestColors.CardSurfaceElevated))
        unlocked -> UNLOCKED_STAGE_BRUSH
        else -> LOCKED_STAGE_BRUSH
    }
    val borderColor = when {
        isCurrent -> BrickQuestColors.NeonGold
        isBoss -> Color(0xFFF43F5E).copy(alpha = if (unlocked) 0.9f else 0.35f)
        unlocked && stars == 3 -> BrickQuestColors.NeonGold.copy(alpha = 0.7f)
        unlocked -> accent.copy(alpha = 0.65f)
        else -> BrickQuestColors.CardBorder.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .aspectRatio(0.92f)
            .clip(STAGE_TILE_SHAPE)
            .background(bgBrush)
            .border(if (isCurrent || isBoss) 2.dp else 1.2.dp, borderColor, STAGE_TILE_SHAPE)
            .clickable(enabled = unlocked, onClick = onClick)
            .testTag("stage_tile_$stageNumber")
            .padding(vertical = 6.dp, horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isBoss) {
                Icon(
                    imageVector = Icons.Filled.Whatshot,
                    contentDescription = "Boss stage",
                    tint = if (unlocked) Color(0xFFFB7185) else BrickQuestColors.TextMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            } else if (!unlocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = BrickQuestColors.TextMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = "$stageNumber",
                style = (if (stageNumber >= 1000) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium)
                    .copy(fontWeight = FontWeight.Black),
                color = if (unlocked) Color.White else BrickQuestColors.TextMuted.copy(alpha = 0.7f),
                maxLines = 1
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (s in 1..3) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (unlocked && s <= stars) BrickQuestColors.NeonGold else Color.White.copy(alpha = if (unlocked) 0.22f else 0.08f),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}
