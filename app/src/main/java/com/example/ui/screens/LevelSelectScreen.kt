package com.example.ui.screens

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.TOTAL_CAMPAIGN_LEVELS
import com.example.domain.model.UserProfile
import com.example.ui.components.MindfeudCard
import com.example.ui.components.MindfeudGradientCard
import com.example.ui.components.MindfeudPrimaryButton
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.AmberWarmEnd
import com.example.ui.theme.AmberWarmLip
import com.example.ui.theme.AmberWarmStart
import com.example.ui.theme.BerryPinkEnd
import com.example.ui.theme.BerryPinkLip
import com.example.ui.theme.BerryPinkStart
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldEnd
import com.example.ui.theme.EmeraldLip
import com.example.ui.theme.EmeraldStart
import com.example.ui.theme.LocalMindfeudColors
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.RoyalVioletBright
import com.example.ui.theme.RoyalVioletLip
import com.example.ui.theme.SkyCyanEnd
import com.example.ui.theme.SkyCyanLip
import com.example.ui.theme.SkyCyanStart
import com.example.ui.theme.SunsetOrangeEnd
import com.example.ui.theme.SunsetOrangeLip
import com.example.ui.theme.SunsetOrangeStart

@Composable
fun LevelSelectScreen(
    profile: UserProfile,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val isHindi = profile.settings.language == AppLanguage.HINDI
    val totalStars = profile.totalCampaignStars()
    val maxStars = TOTAL_CAMPAIGN_LEVELS * 3
    val nextLevelToPlay = profile.highestUnlockedLevel.coerceIn(1, TOTAL_CAMPAIGN_LEVELS)

    val chapterThemes = listOf(
        Triple(listOf(SkyCyanStart, SkyCyanEnd), SkyCyanLip, SkyCyanStart),
        Triple(listOf(RoyalViolet, RoyalVioletBright), RoyalVioletLip, RoyalVioletBright),
        Triple(listOf(AmberWarmStart, AmberWarmEnd), AmberWarmLip, AmberWarmStart),
        Triple(listOf(BerryPinkStart, BerryPinkEnd), BerryPinkLip, BerryPinkEnd)
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campaign Progress Hero Summary Card + Quick Play Current Level CTA
            MindfeudGradientCard(
                gradientColors = listOf(RoyalViolet, ElectricBlue, SkyCyanEnd),
                lipColor = RoyalVioletLip,
                contentPadding = PaddingValues(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.homeHeroHeadline,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFDE047),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$totalStars / $maxStars",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "${strings.levelsUnlockedLabel}: ${profile.highestUnlockedLevel}/$TOTAL_CAMPAIGN_LEVELS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                MindfeudPrimaryButton(
                    text = "${strings.playNowBadge} ${strings.levelWord} $nextLevelToPlay",
                    icon = Icons.Filled.PlayArrow,
                    onClick = { onSelectLevel(nextLevelToPlay) },
                    testTagName = "level_select_quick_play_button"
                )
            }

            // 4 Colorful Chapters (5 Levels each = 20 Levels total)
            for (chapterIdx in 0 until 4) {
                val (gradColors, lipColor, accent) = chapterThemes[chapterIdx]
                val startLevel = chapterIdx * 5 + 1
                val endLevel = startLevel + 4
                val chapterStars = (startLevel..endLevel).sumOf { profile.starsForLevel(it) }

                MindfeudCard(
                    borderColor = accent.copy(alpha = 0.55f),
                    containerColor = palette.cardSurface,
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Chapter Ribbon Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(gradColors))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.chapterTitle(chapterIdx, isHindi),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.24f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$chapterStars/15",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 5 Chunky 3D Arcade Level Nodes (3 on row 1, 2 centered on row 2 for big thumb-friendly tiles!)
                    val firstRowLevels = listOf(startLevel, startLevel + 1, startLevel + 2)
                    val secondRowLevels = listOf(startLevel + 3, startLevel + 4)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            firstRowLevels.forEach { lvl ->
                                LevelNodeTile(
                                    levelNumber = lvl,
                                    stars = profile.starsForLevel(lvl),
                                    unlocked = profile.isLevelUnlocked(lvl),
                                    isCurrentTarget = lvl == nextLevelToPlay,
                                    playBadgeText = strings.playNowBadge,
                                    chapterGradient = gradColors,
                                    chapterLip = lipColor,
                                    onClick = { onSelectLevel(lvl) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Spacer(Modifier.weight(0.5f))
                            secondRowLevels.forEach { lvl ->
                                LevelNodeTile(
                                    levelNumber = lvl,
                                    stars = profile.starsForLevel(lvl),
                                    unlocked = profile.isLevelUnlocked(lvl),
                                    isCurrentTarget = lvl == nextLevelToPlay,
                                    playBadgeText = strings.playNowBadge,
                                    chapterGradient = gradColors,
                                    chapterLip = lipColor,
                                    onClick = { onSelectLevel(lvl) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.weight(0.5f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LevelNodeTile(
    levelNumber: Int,
    stars: Int,
    unlocked: Boolean,
    isCurrentTarget: Boolean,
    playBadgeText: String,
    chapterGradient: List<Color>,
    chapterLip: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMindfeudColors.current
    val shape = RoundedCornerShape(18.dp)
    val completed = stars > 0

    val nodeGradient = when {
        isCurrentTarget -> listOf(SunsetOrangeStart, SunsetOrangeEnd)
        completed -> listOf(EmeraldStart, EmeraldEnd)
        unlocked -> chapterGradient
        else -> listOf(palette.elevatedSurface, palette.elevatedSurface)
    }
    val nodeLip = when {
        isCurrentTarget -> SunsetOrangeLip
        completed -> EmeraldLip
        unlocked -> chapterLip
        else -> palette.borderSubtle
    }

    Box(
        modifier = modifier
            .aspectRatio(0.95f)
            .alpha(if (unlocked) 1f else 0.62f)
            .clip(shape)
            .background(nodeLip)
            .padding(bottom = 5.dp)
            .clip(shape)
            .background(Brush.verticalGradient(nodeGradient))
            .border(
                width = if (isCurrentTarget) 2.5.dp else 1.5.dp,
                color = when {
                    isCurrentTarget -> Color(0xFFFDE047)
                    unlocked -> Color.White.copy(alpha = 0.4f)
                    else -> palette.borderSubtle
                },
                shape = shape
            )
            .clickable(enabled = unlocked, onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag("level_node_$levelNumber"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(6.dp)
        ) {
            if (!unlocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked Level $levelNumber",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$levelNumber",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (isCurrentTarget) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White
                    ) {
                        Text(
                            text = playBadgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp
                            ),
                            color = SunsetOrangeLip,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                }

                Text(
                    text = "$levelNumber",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = Color.White
                )

                Spacer(Modifier.height(4.dp))

                // 3-Star Indicator Row
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (s in 1..3) {
                        Icon(
                            imageVector = if (s <= stars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (s <= stars) Color(0xFFFDE047) else Color.White.copy(alpha = 0.55f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
