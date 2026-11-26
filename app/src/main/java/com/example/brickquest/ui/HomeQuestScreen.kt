package com.example.brickquest.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.brickquest.data.BallSkin
import com.example.brickquest.data.BrickQuestProgress
import com.example.brickquest.data.TOTAL_QUEST_STAGES
import com.example.brickquest.engine.STAGES_PER_WORLD
import com.example.brickquest.engine.StageWorlds
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

// ------------------------------------------------------------------ palette

private val PanelColor = Color(0xEB080D2B)
private val PanelBorder = Color(0x7338BDF8)
private val TileColor = Color(0xB3111D52)
private val Cyan = Color(0xFF38BDF8)
private val CyanSoft = Color(0xFFBAE6FD)
private val Gold = Color(0xFFFACC15)
private val Muted = Color(0xFF94A3B8)
private val BossRed = Color(0xFFF43F5E)
private val DrawerColor = Color(0xFF0B1226)

private val HomeScrim = Brush.verticalGradient(
    0.0f to Color(0xD105081E),
    0.45f to Color(0xB305081E),
    0.72f to Color(0xEB05081E),
    1.0f to Color(0xF805081E)
)
private val PlayBrush = Brush.verticalGradient(listOf(Color(0xFF4F8BFF), Color(0xFF3B5BFF), Color(0xFF5B3BFF)))
private val EndlessBrush = Brush.linearGradient(listOf(Color(0xD9F97316), Color(0xD9EC4899)))
private val CurrentNodeBrush = Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFFDB2777)))
private val BossBrush = Brush.linearGradient(listOf(Color(0xFF7F1D1D), Color(0xFF3B0764)))

// Quest path slots (bottom -> top), as fractions of the path area. Slot 6 is the boss.
private val SLOT_X = floatArrayOf(0.27f, 0.55f, 0.84f, 0.58f, 0.27f, 0.53f, 0.81f)
private val SLOT_Y = floatArrayOf(0.89f, 0.78f, 0.64f, 0.50f, 0.39f, 0.25f, 0.12f)

/**
 * Home screen, design B "Quest Path".
 *
 * Every control works: PLAY, tapping cleared/current stages on the path, World Map,
 * Endless, Skins (bottom sheet), the menu (side drawer with navigation, sound and
 * vibration switches and How to Play).
 *
 * Performance: one static background image, no recomposition loops; the only animation
 * (the pulse on the current stage) is read inside graphicsLayer, so it never recomposes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrickQuestHomeScreen(
    progress: BrickQuestProgress,
    onPlayStage: (Int) -> Unit,
    onOpenWorldMap: () -> Unit,
    onStartEndless: () -> Unit,
    onSelectSkin: (BallSkin) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showSkins by rememberSaveable { mutableStateOf(false) }
    var showHelp by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val closeDrawerThen: (() -> Unit) -> Unit = { action ->
        scope.launch {
            drawerState.close()
            action()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            HomeDrawer(
                progress = progress,
                onPlay = { closeDrawerThen { onPlayStage(progress.highestUnlockedStage) } },
                onWorldMap = { closeDrawerThen(onOpenWorldMap) },
                onEndless = { closeDrawerThen(onStartEndless) },
                onSkins = { closeDrawerThen { showSkins = true } },
                onHelp = { closeDrawerThen { showHelp = true } },
                onToggleSound = onToggleSound,
                onToggleHaptics = onToggleHaptics
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_home_arcade_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeScrim)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeHeader(
                    progress = progress,
                    onMenu = { scope.launch { drawerState.open() } },
                    modifier = Modifier.widthIn(max = 520.dp)
                )

                QuestPath(
                    progress = progress,
                    onPlayStage = onPlayStage,
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                )

                HomeBottomPanel(
                    progress = progress,
                    onPlay = { onPlayStage(progress.highestUnlockedStage) },
                    onWorldMap = onOpenWorldMap,
                    onEndless = onStartEndless,
                    onSkins = { showSkins = true },
                    modifier = Modifier.widthIn(max = 520.dp)
                )
            }
        }
    }

    if (showSkins) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSkins = false },
            sheetState = sheetState,
            containerColor = DrawerColor
        ) {
            SkinsSheetContent(progress = progress, onSelectSkin = onSelectSkin)
        }
    }

    if (showHelp) {
        HowToPlayDialog(onDismiss = { showHelp = false })
    }
}

// ------------------------------------------------------------------ header

@Composable
private fun HomeHeader(
    progress: BrickQuestProgress,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val world = StageWorlds.worldIndex(progress.highestUnlockedStage)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFFFF4D2D))) { append("B") }
                    withStyle(SpanStyle(color = Color(0xFFFFC928))) { append("R") }
                    withStyle(SpanStyle(color = Color(0xFF5CE637))) { append("I") }
                    withStyle(SpanStyle(color = Color(0xFFE040FB))) { append("C") }
                    withStyle(SpanStyle(color = Color(0xFF29D8FF))) { append("K ") }
                    withStyle(SpanStyle(color = Color(0xFF5CEBFF), fontStyle = FontStyle.Italic)) { append("BREAKER") }
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                maxLines = 1
            )
            Text(
                text = "WORLD ${world + 1} · ${StageWorlds.name(world).uppercase()}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xD9090F2D))
                .border(1.4.dp, Gold.copy(alpha = 0.7f), RoundedCornerShape(50))
                .padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(Icons.Filled.Star, contentDescription = "Stars", tint = Gold, modifier = Modifier.size(16.dp))
            Text(
                text = "${progress.totalStars}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xD9111D52))
                .border(1.5.dp, Cyan.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onMenu, modifier = Modifier.testTag("home_menu_button")) {
                Icon(Icons.Filled.Menu, contentDescription = "Open menu", tint = Color(0xFF7DD3FC))
            }
        }
    }
}

// ------------------------------------------------------------------ quest path

private enum class NodeState { CLEARED, CURRENT, LOCKED }

@Composable
private fun QuestPath(
    progress: BrickQuestProgress,
    onPlayStage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val current = progress.highestUnlockedStage
    // The path shows the current block of 10: up to 6 normal stages around the player + the boss.
    val stages = remember(current) {
        val blockStart = ((current - 1) / 10) * 10 + 1
        val boss = (blockStart + 9).coerceAtMost(TOTAL_QUEST_STAGES)
        val windowStart = (current - 1).coerceIn(blockStart, (boss - 6).coerceAtLeast(blockStart))
        List(6) { windowStart + it }.filter { it < boss } + boss
    }
    val currentIndex = stages.indexOf(current).coerceAtLeast(0)

    val pulse = rememberInfiniteTransition(label = "current_pulse")
    val pulseScale = pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val wPx = constraints.maxWidth.toFloat()
        val hPx = constraints.maxHeight.toFloat()
        // Slot index for each shown stage; the boss always takes the top slot.
        val slots = stages.indices.map { i -> if (i == stages.size - 1) 6 else i }
        fun cx(slot: Int) = SLOT_X[slot] * wPx
        fun cy(slot: Int) = SLOT_Y[slot] * hPx

        Canvas(modifier = Modifier.fillMaxSize()) {
            val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 22f), 0f)
            for (i in 0 until slots.size - 1) {
                val done = i < currentIndex
                drawLine(
                    color = if (done) Cyan else Color(0x5A7DD3FC),
                    start = Offset(cx(slots[i]), cy(slots[i])),
                    end = Offset(cx(slots[i + 1]), cy(slots[i + 1])),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = dash
                )
            }
        }

        stages.forEachIndexed { i, stage ->
            val slot = slots[i]
            val isBoss = stage % 10 == 0
            val state = when {
                stage < current -> NodeState.CLEARED
                stage == current -> NodeState.CURRENT
                else -> NodeState.LOCKED
            }
            val sizeDp = when {
                isBoss -> 72.dp
                state == NodeState.CURRENT -> 66.dp
                else -> 52.dp
            }
            val half = with(density) { sizeDp.toPx() } / 2f
            Box(
                modifier = Modifier.offset {
                    IntOffset((cx(slot) - half).roundToInt(), (cy(slot) - half).roundToInt())
                }
            ) {
                StageNode(
                    stage = stage,
                    isBoss = isBoss,
                    state = state,
                    stars = progress.starsFor(stage),
                    sizeDp = sizeDp,
                    pulseScale = { if (state == NodeState.CURRENT) pulseScale.value else 1f },
                    onClick = { onPlayStage(stage) }
                )
            }
        }

        // "YOU ARE HERE" tag beside the current node (on whichever side has room)
        val curSlot = slots[currentIndex]
        val tagWidth = 112.dp
        val tagWidthPx = with(density) { tagWidth.toPx() }
        val gapPx = with(density) { 44.dp.toPx() }
        val onRight = SLOT_X[curSlot] < 0.5f
        val tagX = if (onRight) cx(curSlot) + gapPx else cx(curSlot) - gapPx - tagWidthPx
        val tagY = cy(curSlot) - with(density) { 13.dp.toPx() }
        if (stages[currentIndex] == current) {
            Text(
                text = if (current % 10 == 0) "BOSS FIGHT!" else "YOU ARE HERE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp),
                color = Color(0xFFFDE68A),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset { IntOffset(tagX.roundToInt(), tagY.roundToInt()) }
                    .width(tagWidth)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x26FACC15))
                    .border(1.dp, Color(0x99FACC15), RoundedCornerShape(10.dp))
                    .padding(vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun StageNode(
    stage: Int,
    isBoss: Boolean,
    state: NodeState,
    stars: Int,
    sizeDp: androidx.compose.ui.unit.Dp,
    pulseScale: () -> Float,
    onClick: () -> Unit
) {
    val playable = state != NodeState.LOCKED
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val shape = if (isBoss) RoundedCornerShape(22.dp) else CircleShape
        val base = Modifier
            .size(sizeDp)
            .graphicsLayer {
                val s = pulseScale()
                scaleX = s
                scaleY = s
            }
            .clip(shape)
        val styled = when {
            isBoss -> base
                .background(BossBrush)
                .border(2.5.dp, if (playable) BossRed else BossRed.copy(alpha = 0.55f), shape)
            state == NodeState.CURRENT -> base
                .background(CurrentNodeBrush)
                .border(3.dp, Gold, shape)
            state == NodeState.CLEARED -> base
                .background(Color(0xFF15223D))
                .border(2.dp, Cyan, shape)
            else -> base
                .background(Color(0xD90A1020))
                .border(1.5.dp, Color(0x7364748B), shape)
        }
        Box(
            modifier = styled
                .clickable(enabled = playable, role = Role.Button, onClick = onClick)
                .testTag("path_stage_$stage"),
            contentAlignment = Alignment.Center
        ) {
            when {
                isBoss -> Icon(
                    imageVector = Icons.Filled.Whatshot,
                    contentDescription = "Boss stage $stage",
                    tint = Color(0xFFFDA4AF),
                    modifier = Modifier.size(32.dp)
                )
                state == NodeState.LOCKED -> Text(
                    text = "$stage",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF64748B)
                )
                else -> Text(
                    text = "$stage",
                    style = (if (state == NodeState.CURRENT) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium)
                        .copy(fontWeight = FontWeight.Black, fontSize = if (stage >= 1000) 15.sp else if (state == NodeState.CURRENT) 22.sp else 17.sp),
                    color = Color.White
                )
            }
        }
        if (isBoss) {
            Text(
                text = "BOSS $stage",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = Color(0xFFFDA4AF),
                modifier = Modifier.padding(top = 3.dp)
            )
        } else if (state == NodeState.CLEARED) {
            Row(modifier = Modifier.padding(top = 2.dp)) {
                for (s in 1..3) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (s <= stars) Gold else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------ bottom panel

@Composable
private fun HomeBottomPanel(
    progress: BrickQuestProgress,
    onPlay: () -> Unit,
    onWorldMap: () -> Unit,
    onEndless: () -> Unit,
    onSkins: () -> Unit,
    modifier: Modifier = Modifier
) {
    val current = progress.highestUnlockedStage
    val inWorld = (current - 1) % STAGES_PER_WORLD + 1
    val isBoss = current % 10 == 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PanelColor)
            .border(1.3.dp, PanelBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0A1628))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(inWorld / STAGES_PER_WORLD.toFloat())
                        .fillMaxHeight()
                        .background(Cyan)
                )
            }
            Text(
                text = "  $inWorld / $STAGES_PER_WORLD",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Muted
            )
            Spacer(Modifier.width(14.dp))
            Icon(Icons.Filled.Bolt, contentDescription = "Lightning", tint = Color(0xFFFB7185), modifier = Modifier.size(16.dp))
            Text(
                text = "${progress.lightningCount}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
            Spacer(Modifier.width(10.dp))
            BallsGlyph(modifier = Modifier.size(15.dp))
            Text(
                text = " ${progress.extraBallsCount}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PlayBrush)
                .border(2.dp, Color(0xBFBAE6FD), RoundedCornerShape(20.dp))
                .clickable(role = Role.Button, onClick = onPlay)
                .testTag("home_play_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isBoss) "FIGHT BOSS $current" else "PLAY STAGE $current",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
                    color = Color.White
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeTile(
                label = "World Map",
                sub = "$TOTAL_QUEST_STAGES stages",
                background = Brush.linearGradient(listOf(TileColor, TileColor)),
                borderColor = Cyan.copy(alpha = 0.45f),
                tag = "home_world_map_button",
                onClick = onWorldMap,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Map, contentDescription = null, tint = Cyan, modifier = Modifier.size(22.dp))
            }
            HomeTile(
                label = "Endless",
                sub = "Best ${progress.endlessBestScore}",
                background = EndlessBrush,
                borderColor = Color.White.copy(alpha = 0.3f),
                tag = "home_endless_button",
                onClick = onEndless,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.AllInclusive, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            HomeTile(
                label = "Skins",
                sub = progress.selectedSkin.displayName,
                background = Brush.linearGradient(listOf(TileColor, TileColor)),
                borderColor = Color(0x73E879F9),
                tag = "home_skins_button",
                onClick = onSkins,
                modifier = Modifier.weight(1f)
            ) {
                val skin = progress.selectedSkin
                Canvas(modifier = Modifier.size(22.dp)) {
                    drawBallSkinGraphic(skin = skin, center = center, radius = size.minDimension * 0.24f)
                }
            }
        }
    }
}

@Composable
private fun HomeTile(
    label: String,
    sub: String,
    background: Brush,
    borderColor: Color,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        icon()
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White,
            maxLines = 1
        )
        Text(
            text = sub,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = CyanSoft.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BallsGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension * 0.2f
        val c = Color(0xFFFB7185)
        drawCircle(c, r, Offset(size.width * 0.5f, size.height * 0.28f))
        drawCircle(c, r, Offset(size.width * 0.26f, size.height * 0.72f))
        drawCircle(c, r, Offset(size.width * 0.74f, size.height * 0.72f))
    }
}

// ------------------------------------------------------------------ drawer

@Composable
private fun HomeDrawer(
    progress: BrickQuestProgress,
    onPlay: () -> Unit,
    onWorldMap: () -> Unit,
    onEndless: () -> Unit,
    onSkins: () -> Unit,
    onHelp: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    val current = progress.highestUnlockedStage
    val world = StageWorlds.worldIndex(current)
    val unlockedSkins = BallSkin.entries.count { progress.isSkinUnlocked(it) }
    val itemColors = NavigationDrawerItemDefaults.colors(
        unselectedContainerColor = Color.Transparent,
        unselectedTextColor = Color.White,
        unselectedIconColor = Color(0xFF7DD3FC),
        unselectedBadgeColor = CyanSoft
    )

    ModalDrawerSheet(
        drawerContainerColor = DrawerColor,
        drawerContentColor = Color.White,
        modifier = Modifier.widthIn(max = 320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = "BRICK BREAKER QUEST",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = Color(0xFF5CEBFF)
                )
                Text(
                    text = "Stage $current · World ${world + 1}: ${StageWorlds.name(world)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                    Text(
                        text = "${progress.totalStars} stars collected",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0x3338BDF8))

            NavigationDrawerItem(
                label = { Text(if (current % 10 == 0) "Fight Boss $current" else "Play Stage $current") },
                icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                selected = false,
                onClick = onPlay,
                colors = itemColors,
                modifier = Modifier.testTag("drawer_play")
            )
            NavigationDrawerItem(
                label = { Text("World Map") },
                icon = { Icon(Icons.Filled.Map, contentDescription = null) },
                badge = { Text("$TOTAL_QUEST_STAGES") },
                selected = false,
                onClick = onWorldMap,
                colors = itemColors,
                modifier = Modifier.testTag("drawer_world_map")
            )
            NavigationDrawerItem(
                label = { Text("Endless Mode") },
                icon = { Icon(Icons.Filled.AllInclusive, contentDescription = null) },
                badge = { Text("Best ${progress.endlessBestScore}") },
                selected = false,
                onClick = onEndless,
                colors = itemColors,
                modifier = Modifier.testTag("drawer_endless")
            )
            NavigationDrawerItem(
                label = { Text("Ball Skins") },
                icon = { Icon(Icons.Filled.Palette, contentDescription = null) },
                badge = { Text("$unlockedSkins/${BallSkin.entries.size}") },
                selected = false,
                onClick = onSkins,
                colors = itemColors,
                modifier = Modifier.testTag("drawer_skins")
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0x3338BDF8))

            NavigationDrawerItem(
                label = { Text("Sound") },
                icon = { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null) },
                badge = {
                    Switch(
                        checked = progress.soundEnabled,
                        onCheckedChange = { onToggleSound() },
                        colors = SwitchDefaults.colors(checkedTrackColor = Cyan)
                    )
                },
                selected = false,
                onClick = onToggleSound,
                colors = itemColors,
                modifier = Modifier.testTag("drawer_sound")
            )
            NavigationDrawerItem(
                label = { Text("Vibration") },
                icon = { Icon(Icons.Filled.Vibration, contentDescription = null) },
                badge = {
                    Switch(
                        checked = progress.hapticsEnabled,
                        onCheckedChange = { onToggleHaptics() },
                        colors = SwitchDefaults.colors(checkedTrackColor = Cyan)
                    )
                },
                selected = false,
                onClick = onToggleHaptics,
                colors = itemColors,
                modifier = Modifier.testTag("drawer_haptics")
            )
            NavigationDrawerItem(
                label = { Text("How to Play") },
                icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                selected = false,
                onClick = onHelp,
                colors = itemColors,
                modifier = Modifier.testTag("drawer_help")
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color(0xFFFB7185), modifier = Modifier.size(16.dp))
                    Text(" ${progress.lightningCount} Lightning", style = MaterialTheme.typography.labelMedium, color = CyanSoft)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BallsGlyph(modifier = Modifier.size(14.dp))
                    Text("  ${progress.extraBallsCount} Ball Storm", style = MaterialTheme.typography.labelMedium, color = CyanSoft)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ skins sheet

@Composable
private fun SkinsSheetContent(
    progress: BrickQuestProgress,
    onSelectSkin: (BallSkin) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Ball Skins",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White
        )
        Text(
            text = "You have ${progress.totalStars} stars. Clear stages with more stars to unlock skins.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
        ) {
            items(BallSkin.entries) { skin ->
                SkinCard(
                    skin = skin,
                    unlocked = progress.isSkinUnlocked(skin),
                    selected = progress.selectedSkin == skin,
                    starsNeeded = (skin.unlockStars - progress.totalStars).coerceAtLeast(0),
                    onClick = { onSelectSkin(skin) }
                )
            }
        }
    }
}

@Composable
private fun SkinCard(
    skin: BallSkin,
    unlocked: Boolean,
    selected: Boolean,
    starsNeeded: Int,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Color(0xB31D2B6E) else Color(0x800C1338))
            .border(if (selected) 2.dp else 1.dp, if (selected) Cyan else Color(0x55818CF8), shape)
            .clickable(enabled = unlocked, role = Role.Button, onClick = onClick)
            .testTag("skin_card_${skin.name.lowercase()}")
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBallSkinGraphic(skin = skin, center = center, radius = size.minDimension * 0.24f)
            }
            if (selected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Equipped",
                    tint = Cyan,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                )
            } else if (!unlocked) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Muted,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
        Text(
            text = skin.displayName,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (unlocked) Color.White else Muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = when {
                selected -> "EQUIPPED"
                unlocked -> "TAP TO USE"
                else -> "★ $starsNeeded more"
            },
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
            color = when {
                selected -> Color(0xFF67E8F9)
                unlocked -> Color(0xFFCBD5E1)
                else -> Gold
            }
        )
    }
}

// ------------------------------------------------------------------ how to play

@Composable
private fun HowToPlayDialog(onDismiss: () -> Unit) {
    val rules = listOf(
        "Drag on the arena to aim, release to shoot all your balls.",
        "Every hit lowers a brick's number. At 0 it breaks.",
        "After each turn the bricks move down one row. If a brick reaches the red line, you lose.",
        "Pink +1 orbs give you an extra ball for the next turns.",
        "Green and blue orbs fire lasers through a whole row or column.",
        "Orange bricks are bombs: breaking one damages all 8 bricks around it.",
        "Coloured power bricks drop a capsule. Catch it with the paddle for Multi-Ball, Laser or a Wide Paddle.",
        "Lightning clears the bottom row. Ball Storm adds 25 balls with double damage for one turn.",
        "Taking too long? Tap RECALL, and after a few seconds the game speeds up on its own.",
        "Every 10th stage is a boss. Clear stages with a high score to earn up to 3 stars."
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DrawerColor,
        titleContentColor = Color.White,
        textContentColor = CyanSoft,
        title = { Text("How to Play", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rules.forEachIndexed { i, rule ->
                    Row {
                        Text("${i + 1}.  ", fontWeight = FontWeight.Bold, color = Cyan)
                        Text(rule, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("GOT IT", color = Cyan, fontWeight = FontWeight.Bold) }
        }
    )
}
