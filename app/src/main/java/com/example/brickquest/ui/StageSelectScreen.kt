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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brickquest.data.BrickQuestProgress

private data class StageChapter(
    val title: String,
    val subtitle: String,
    val range: IntRange,
    val accent: Color
)

private val QUEST_CHAPTERS = listOf(
    StageChapter(
        title = "ZONE 1 • NEON ARCADE",
        subtitle = "Master crisp bounces & +1 Ball combos",
        range = 1..10,
        accent = Color(0xFF38BDF8)
    ),
    StageChapter(
        title = "ZONE 2 • LASER CORE",
        subtitle = "Trigger horizontal & vertical laser chains",
        range = 11..20,
        accent = Color(0xFF4ADE80)
    ),
    StageChapter(
        title = "ZONE 3 • COSMIC CITADEL",
        subtitle = "Heavy fortresses & 3×3 bomb vaults",
        range = 21..30,
        accent = Color(0xFFE879F9)
    )
)

@Composable
fun StageSelectScreen(
    progress: BrickQuestProgress,
    onSelectStage: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

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
            // Top Bar
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
                            text = "SELECT STAGE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = BrickQuestColors.TextPrimary
                        )
                        Text(
                            text = "Choose a neon brick quest",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrickQuestColors.TextSecondary
                        )
                    }
                }

                // Total Stars Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = BrickQuestColors.CardSurfaceElevated,
                    border = BorderStroke(1.3.dp, BrickQuestColors.NeonGold.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Stars Collected",
                            tint = BrickQuestColors.NeonGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${progress.totalStars}/${progress.maxStars}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = BrickQuestColors.TextPrimary
                        )
                    }
                }
            }

            // Quick Play Highest Unlocked Stage Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(BrickQuestColors.PlayButtonBrush)
                    .border(1.4.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .clickable { onSelectStage(progress.highestUnlockedStage) }
                    .testTag("stage_select_quick_play_button")
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CONTINUE QUEST",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White.copy(alpha = 0.88f)
                        )
                        Text(
                            text = "PLAY STAGE ${progress.highestUnlockedStage}",
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
                            contentDescription = "Play Stage ${progress.highestUnlockedStage}",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 30-Stage Chapter List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                QUEST_CHAPTERS.forEach { chapter ->
                    item(key = chapter.title) {
                        ChapterStageBlock(
                            chapter = chapter,
                            progress = progress,
                            onSelectStage = onSelectStage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterStageBlock(
    chapter: StageChapter,
    progress: BrickQuestProgress,
    onSelectStage: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = BrickQuestColors.CardSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.3.dp, chapter.accent.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = chapter.accent
                    )
                    Text(
                        text = chapter.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickQuestColors.TextSecondary
                    )
                }

                val chapterStars = chapter.range.sumOf { progress.starsFor(it) }
                val chapterMax = chapter.range.count() * 3
                Text(
                    text = "★ $chapterStars/$chapterMax",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrickQuestColors.NeonGold
                )
            }

            // 2 rows of 5 stage tiles
            val stages = chapter.range.toList()
            stages.chunked(5).forEach { rowStages ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowStages.forEach { stageNum ->
                        val unlocked = progress.isStageUnlocked(stageNum)
                        val isCurrent = stageNum == progress.highestUnlockedStage
                        val stars = progress.starsFor(stageNum)
                        StageTile(
                            stageNumber = stageNum,
                            unlocked = unlocked,
                            isCurrent = isCurrent,
                            stars = stars,
                            accent = chapter.accent,
                            onClick = { onSelectStage(stageNum) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private val CURRENT_STAGE_BRUSH = Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFFDB2777)))
private val UNLOCKED_STAGE_BRUSH = Brush.verticalGradient(listOf(BrickQuestColors.CardSurfaceElevated, BrickQuestColors.CardSurface))
private val LOCKED_STAGE_BRUSH = Brush.verticalGradient(listOf(Color(0xFF0A1020), Color(0xFF070B16)))
private val STAGE_TILE_SHAPE = RoundedCornerShape(14.dp)

@Composable
private fun StageTile(
    stageNumber: Int,
    unlocked: Boolean,
    isCurrent: Boolean,
    stars: Int,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgBrush = when {
        isCurrent -> CURRENT_STAGE_BRUSH
        unlocked && stars > 0 -> Brush.verticalGradient(
            listOf(accent.copy(alpha = 0.28f), BrickQuestColors.CardSurfaceElevated)
        )
        unlocked -> UNLOCKED_STAGE_BRUSH
        else -> LOCKED_STAGE_BRUSH
    }

    val borderColor = when {
        isCurrent -> BrickQuestColors.NeonGold
        unlocked && stars == 3 -> BrickQuestColors.NeonGold.copy(alpha = 0.7f)
        unlocked -> accent.copy(alpha = 0.65f)
        else -> BrickQuestColors.CardBorder.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .aspectRatio(0.92f)
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .border(if (isCurrent) 2.dp else 1.2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = unlocked, onClick = onClick)
            .testTag("stage_tile_$stageNumber")
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!unlocked) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Stage $stageNumber Locked",
                    tint = BrickQuestColors.TextMuted.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$stageNumber",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = BrickQuestColors.TextMuted.copy(alpha = 0.7f)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(Modifier.height(1.dp))
                Text(
                    text = "$stageNumber",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (s in 1..3) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (s <= stars) BrickQuestColors.NeonGold else Color.White.copy(alpha = 0.22f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
