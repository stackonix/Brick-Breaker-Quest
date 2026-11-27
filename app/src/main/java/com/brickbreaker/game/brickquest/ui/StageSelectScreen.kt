package com.brickbreaker.game.brickquest.ui

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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brickbreaker.game.brickquest.data.BrickQuestProgress
import com.brickbreaker.game.brickquest.data.TOTAL_QUEST_STAGES
import com.brickbreaker.game.brickquest.engine.STAGES_PER_WORLD
import com.brickbreaker.game.brickquest.engine.StageWorlds
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

private const val TILES_PER_ROW = 5
private const val ROWS_PER_WORLD = STAGES_PER_WORLD / TILES_PER_ROW

private val CARD_SHAPE_18 = RoundedCornerShape(18.dp)
private val STAGE_TILE_SHAPE = RoundedCornerShape(14.dp)
private val PILL_SHAPE = RoundedCornerShape(50)

private val CURRENT_STAGE_BRUSH = Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFFDB2777)))
private val BOSS_STAGE_BRUSH = Brush.verticalGradient(listOf(Color(0xFF7F1D1D), Color(0xFF3B0764)))
private val UNLOCKED_STAGE_BRUSH = Brush.verticalGradient(listOf(BrickQuestColors.CardSurfaceElevated, BrickQuestColors.CardSurface))
private val LOCKED_STAGE_BRUSH = Brush.verticalGradient(listOf(Color(0xFF0A1020), Color(0xFF070B16)))

private val LOCKED_CARD_BG = Color(0xFF080D1A)
private val HEADER_CARD_BG = BrickQuestColors.CardSurface.copy(alpha = 0.94f)
private val PROGRESS_TRACK_BG = Color(0xFF0A1628)
private val STAR_DIM_UNLOCKED = Color.White.copy(alpha = 0.22f)
private val STAR_DIM_LOCKED = Color.White.copy(alpha = 0.08f)
private val TEXT_LOCKED = BrickQuestColors.TextMuted.copy(alpha = 0.7f)
private val SUBTEXT_LOCKED = BrickQuestColors.TextMuted.copy(alpha = 0.8f)
private val BOSS_ICON_UNLOCKED = Color(0xFFFB7185)
private val ICON_MUTED = BrickQuestColors.TextMuted.copy(alpha = 0.6f)

private const val CONTENT_TYPE_HEADER = 0
private const val CONTENT_TYPE_ROW = 1
private const val CONTENT_TYPE_LOCKED = 2

@Immutable
private sealed interface MapListItem {
    val key: String
    val contentType: Int

    @Immutable
    data class WorldHeaderItem(
        override val key: String,
        val isFirstWorld: Boolean,
        val title: String,
        val subtitle: String,
        val starsLabel: String,
        val progressRatio: Float,
        val accent: Color,
        val borderColor: Color
    ) : MapListItem {
        override val contentType: Int get() = CONTENT_TYPE_HEADER
    }

    @Immutable
    data class StageTileData(
        val stageNumber: Int,
        val label: String,
        val unlocked: Boolean,
        val isCurrent: Boolean,
        val isBoss: Boolean,
        val stars: Int,
        val accent: Color
    )

    @Immutable
    data class StageRowItem(
        override val key: String,
        val tiles: List<StageTileData?>
    ) : MapListItem {
        override val contentType: Int get() = CONTENT_TYPE_ROW
    }

    @Immutable
    data class LockedWorldItem(
        override val key: String,
        val title: String,
        val subtitle: String,
        val badgeBg: Color,
        val iconTint: Color,
        val borderColor: Color
    ) : MapListItem {
        override val contentType: Int get() = CONTENT_TYPE_LOCKED
    }
}

@Immutable
private class MapTypographyCache(
    val headerTitle: TextStyle,
    val headerSubtitle: TextStyle,
    val headerStars: TextStyle,
    val lockedTitle: TextStyle,
    val lockedSubtitle: TextStyle,
    val tileNumberNormal: TextStyle,
    val tileNumberLarge: TextStyle
)

/**
 * Ultra-smooth 60/120 FPS Stage Map for 1,500 stages.
 * Uses precomputed immutable items, explicit LazyColumn contentTypes, hoisted VectorPainters &
 * TextStyles, and lightweight GPU-drawn tiles/cards for butter-smooth fling scrolling.
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
    val currentRowIndex = currentWorld * (1 + ROWS_PER_WORLD) + 1 + currentRowInWorld
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (currentRowIndex - 1).coerceAtLeast(0))
    val scope = rememberCoroutineScope()

    // Precompute the entire flat immutable list once whenever progress changes (ZERO work during scroll!)
    val mapItems = remember(current, progress.stageStars) {
        buildMapItems(progress = progress, current = current, currentWorld = currentWorld)
    }

    // Hoist all TextStyles & VectorPainters ONCE so scrolling allocates ZERO objects
    val typography = MaterialTheme.typography
    val styles = remember(typography) {
        MapTypographyCache(
            headerTitle = typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
            headerSubtitle = typography.labelSmall,
            headerStars = typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            lockedTitle = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            lockedSubtitle = typography.labelSmall,
            tileNumberNormal = typography.titleMedium.copy(fontWeight = FontWeight.Black),
            tileNumberLarge = typography.titleSmall.copy(fontWeight = FontWeight.Black)
        )
    }
    val lockPainter = rememberVectorPainter(Icons.Filled.Lock)
    val bossPainter = rememberVectorPainter(Icons.Filled.Whatshot)

    val currentWorldTitle = remember(currentWorld) {
        "WORLD ${currentWorld + 1} • ${StageWorlds.name(currentWorld).uppercase()}"
    }
    val continueTitle = remember(current) {
        if (current % 10 == 0) "PLAY BOSS STAGE $current" else "PLAY STAGE $current"
    }

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
                    .clip(CARD_SHAPE_18)
                    .background(BrickQuestColors.PlayButtonBrush)
                    .border(1.4.dp, Color.White.copy(alpha = 0.4f), CARD_SHAPE_18)
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
                            text = currentWorldTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = continueTitle,
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
                items(
                    items = mapItems,
                    key = { it.key },
                    contentType = { it.contentType }
                ) { item ->
                    when (item) {
                        is MapListItem.WorldHeaderItem -> WorldHeader(
                            item = item,
                            styles = styles
                        )

                        is MapListItem.StageRowItem -> StageRow(
                            tiles = item.tiles,
                            styles = styles,
                            lockPainter = lockPainter,
                            bossPainter = bossPainter,
                            onSelectStage = onSelectStage
                        )

                        is MapListItem.LockedWorldItem -> LockedWorldCard(
                            item = item,
                            styles = styles,
                            lockPainter = lockPainter
                        )
                    }
                }
            }
        }
    }
}

private fun buildMapItems(
    progress: BrickQuestProgress,
    current: Int,
    currentWorld: Int
): List<MapListItem> {
    val result = ArrayList<MapListItem>((currentWorld + 1) * (ROWS_PER_WORLD + 1) + StageWorlds.count)
    for (w in 0 until StageWorlds.count) {
        val accent = Color(StageWorlds.accentArgb(w))
        val first = StageWorlds.firstStage(w)
        val last = StageWorlds.lastStage(w)
        val worldTitle = "WORLD ${w + 1} • ${StageWorlds.name(w).uppercase()}"
        if (w <= currentWorld) {
            var stars = 0
            for (s in first..last) stars += progress.starsFor(s)
            val totalInWorld = last - first + 1
            val maxStars = totalInWorld * 3
            val cleared = (current - first).coerceIn(0, totalInWorld)
            result.add(
                MapListItem.WorldHeaderItem(
                    key = "world_$w",
                    isFirstWorld = w == 0,
                    title = worldTitle,
                    subtitle = "Stages $first-$last • boss every 10th stage",
                    starsLabel = "★ $stars/$maxStars",
                    progressRatio = (cleared / totalInWorld.toFloat()).coerceIn(0f, 1f),
                    accent = accent,
                    borderColor = accent.copy(alpha = 0.55f)
                )
            )
            for (r in 0 until ROWS_PER_WORLD) {
                val rowStart = first + r * TILES_PER_ROW
                if (rowStart > last) break
                val rowTiles = List<MapListItem.StageTileData?>(TILES_PER_ROW) { i ->
                    val stage = rowStart + i
                    if (stage <= last) {
                        MapListItem.StageTileData(
                            stageNumber = stage,
                            label = stage.toString(),
                            unlocked = stage <= current,
                            isCurrent = stage == current,
                            isBoss = stage % 10 == 0,
                            stars = progress.starsFor(stage),
                            accent = accent
                        )
                    } else {
                        null
                    }
                }
                result.add(MapListItem.StageRowItem(key = "row_${w}_$r", tiles = rowTiles))
            }
        } else {
            result.add(
                MapListItem.LockedWorldItem(
                    key = "locked_world_$w",
                    title = worldTitle,
                    subtitle = "Clear stage ${first - 1} to unlock • stages $first-$last",
                    badgeBg = accent.copy(alpha = 0.12f),
                    iconTint = accent.copy(alpha = 0.7f),
                    borderColor = accent.copy(alpha = 0.22f)
                )
            )
        }
    }
    return result
}

@Composable
private fun StarCountPill(total: Int) {
    Surface(
        shape = PILL_SHAPE,
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
    item: MapListItem.WorldHeaderItem,
    styles: MapTypographyCache
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (item.isFirstWorld) 0.dp else 10.dp)
            .background(HEADER_CARD_BG, CARD_SHAPE_18)
            .border(1.3.dp, item.borderColor, CARD_SHAPE_18)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = styles.headerTitle,
                    color = item.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = styles.headerSubtitle,
                    color = BrickQuestColors.TextSecondary,
                    maxLines = 1
                )
            }
            Text(
                text = item.starsLabel,
                style = styles.headerStars,
                color = BrickQuestColors.NeonGold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(PILL_SHAPE)
                .background(PROGRESS_TRACK_BG)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.progressRatio)
                    .fillMaxHeight()
                    .background(item.accent)
            )
        }
    }
}

@Composable
private fun LockedWorldCard(
    item: MapListItem.LockedWorldItem,
    styles: MapTypographyCache,
    lockPainter: VectorPainter
) {
    // Lightweight flat Row (no Material3 Surface wrapper or Shape/Color allocations!)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .background(LOCKED_CARD_BG, CARD_SHAPE_18)
            .border(1.dp, item.borderColor, CARD_SHAPE_18)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(item.badgeBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = lockPainter,
                contentDescription = null,
                tint = item.iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = styles.lockedTitle,
                color = BrickQuestColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.subtitle,
                style = styles.lockedSubtitle,
                color = SUBTEXT_LOCKED,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StageRow(
    tiles: List<MapListItem.StageTileData?>,
    styles: MapTypographyCache,
    lockPainter: VectorPainter,
    bossPainter: VectorPainter,
    onSelectStage: (Int) -> Unit
) {
    // Fixed height removes 5x aspectRatio double-measure passes per row during fast fling scrolling!
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until TILES_PER_ROW) {
            val tile = tiles[i]
            if (tile != null) {
                StageTile(
                    tile = tile,
                    styles = styles,
                    lockPainter = lockPainter,
                    bossPainter = bossPainter,
                    onSelectStage = onSelectStage,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StageTile(
    tile: MapListItem.StageTileData,
    styles: MapTypographyCache,
    lockPainter: VectorPainter,
    bossPainter: VectorPainter,
    onSelectStage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = tile.accent
    val unlocked = tile.unlocked
    val isCurrent = tile.isCurrent
    val isBoss = tile.isBoss
    val stars = tile.stars

    val clearedBrush = remember(accent) {
        Brush.verticalGradient(listOf(accent.copy(alpha = 0.28f), BrickQuestColors.CardSurfaceElevated))
    }
    val bgBrush = when {
        isCurrent -> CURRENT_STAGE_BRUSH
        isBoss && unlocked -> BOSS_STAGE_BRUSH
        unlocked && stars > 0 -> clearedBrush
        unlocked -> UNLOCKED_STAGE_BRUSH
        else -> LOCKED_STAGE_BRUSH
    }
    val borderColor = when {
        isCurrent -> BrickQuestColors.NeonGold
        isBoss -> if (unlocked) Color(0xE6F43F5E) else Color(0x59F43F5E)
        unlocked && stars == 3 -> Color(0xB3FACC15)
        unlocked -> accent.copy(alpha = 0.65f)
        else -> Color(0x5928426B)
    }
    val borderStrokeDp = if (isCurrent || isBoss) 2.dp else 1.2.dp

    // Single-pass GPU Canvas draws background, crisp neon border, AND all 3 geometric 5-point stars
    // (replaces 3 Icon vector nodes + Row per tile = 15 fewer VectorNodes per row!)
    Box(
        modifier = modifier
            .drawWithCache {
                val cornerPx = 14.dp.toPx()
                val corner = CornerRadius(cornerPx, cornerPx)
                val strokePx = borderStrokeDp.toPx()
                val borderStyle = Stroke(width = strokePx)
                val starOuterR = 4.8.dp.toPx()
                val starInnerR = starOuterR * 0.46f
                val starSpacing = 12.dp.toPx()
                val starY = size.height - 11.dp.toPx()
                val cx = size.width * 0.5f
                val starPaths = Array(3) { idx ->
                    buildStarPath(
                        cx = cx + (idx - 1) * starSpacing,
                        cy = starY,
                        outerR = starOuterR,
                        innerR = starInnerR
                    )
                }
                val dimStarColor = if (unlocked) STAR_DIM_UNLOCKED else STAR_DIM_LOCKED
                onDrawBehind {
                    drawRoundRect(brush = bgBrush, cornerRadius = corner)
                    drawRoundRect(color = borderColor, cornerRadius = corner, style = borderStyle)
                    drawStarPips(starPaths = starPaths, earned = if (unlocked) stars else 0, dimColor = dimStarColor)
                }
            }
            .clip(STAGE_TILE_SHAPE)
            .clickable(enabled = unlocked) { onSelectStage(tile.stageNumber) }
            .testTag("stage_tile_${tile.stageNumber}"),
        contentAlignment = Alignment.Center
    ) {
        if (isBoss) {
            Icon(
                painter = bossPainter,
                contentDescription = "Boss stage",
                tint = if (unlocked) BOSS_ICON_UNLOCKED else ICON_MUTED,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .size(13.dp)
            )
        } else if (!unlocked) {
            Icon(
                painter = lockPainter,
                contentDescription = null,
                tint = ICON_MUTED,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .size(12.dp)
            )
        }

        Text(
            text = tile.label,
            style = if (tile.stageNumber >= 1000) styles.tileNumberLarge else styles.tileNumberNormal,
            color = if (unlocked) Color.White else TEXT_LOCKED,
            maxLines = 1,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

private fun buildStarPath(cx: Float, cy: Float, outerR: Float, innerR: Float): Path {
    val path = Path()
    val step = (PI / 5.0).toFloat()
    val start = -(PI / 2.0).toFloat()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = start + i * step
        val x = cx + cos(angle) * r
        val y = cy + sin(angle) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun DrawScope.drawStarPips(starPaths: Array<Path>, earned: Int, dimColor: Color) {
    for (i in 0 until 3) {
        drawPath(
            path = starPaths[i],
            color = if (i < earned) BrickQuestColors.NeonGold else dimColor
        )
    }
}
