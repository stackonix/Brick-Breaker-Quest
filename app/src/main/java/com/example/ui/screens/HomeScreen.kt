package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AppLanguage
import com.example.domain.model.TOTAL_CAMPAIGN_LEVELS
import com.example.domain.model.UserProfile
import com.example.ui.components.MindfeudCard
import com.example.ui.components.MindfeudGradientCard
import com.example.ui.components.MindfeudPrimaryButton
import com.example.ui.components.RankChip
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.AmberWarmEnd
import com.example.ui.theme.AmberWarmLip
import com.example.ui.theme.AmberWarmStart
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldEnd
import com.example.ui.theme.EmeraldLip
import com.example.ui.theme.EmeraldStart
import com.example.ui.theme.LocalMindfeudColors
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.RoyalVioletLip
import com.example.ui.theme.SkyCyanLip
import com.example.ui.theme.SkyCyanStart

/**
 * Proper Real-Game Home Page:
 * Clean Game HUD + Big Vibrant Adventure Hero Banner with prominent START GAME button (-> Levels 1..20 -> Gameplay!)
 * followed by compact colorful cards for Daily Challenge, 60s Duel, Friend Challenge, and Practice.
 */
@Composable
fun HomeScreen(
    profile: UserProfile,
    effectiveEpochDay: Long,
    showDay3Prompt: Boolean,
    onStartGame: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenDuelLobby: () -> Unit,
    onOpenFriendChallenge: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenBrainProfile: () -> Unit,
    onEnableReminderFromPrompt: () -> Unit,
    onDismissDay3Prompt: () -> Unit
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val isHindi = profile.settings.language == AppLanguage.HINDI
    val dailyDone = profile.isDailyCompleted(effectiveEpochDay)
    val totalStars = profile.totalCampaignStars()
    val maxStars = TOTAL_CAMPAIGN_LEVELS * 3

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Arcade HUD Strip: Rank Chip + Stars Counter + Daily Hints
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RankChip(
                    rank = profile.rank,
                    rankLabel = strings.rankName(profile.rank, isHindi),
                    xp = profile.xp,
                    onClick = onOpenBrainProfile
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.goldContainer,
                        border = BorderStroke(1.5.dp, palette.gold.copy(alpha = 0.8f)),
                        onClick = onStartGame
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = strings.starsLabel,
                                tint = palette.gold,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "$totalStars/$maxStars",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = palette.goldText
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.cyanContainer,
                        border = BorderStroke(1.5.dp, palette.cyan.copy(alpha = 0.75f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = strings.hintsTodayLabel,
                                tint = palette.cyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${profile.hintsRemainingToday}/3",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = palette.cyanText
                            )
                        }
                    }
                }
            }

            // 2. MAIN REAL-GAME HERO CARD: Custom 3D Artwork + Level Badge + BIG "START GAME" Button!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(RoyalVioletLip)
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.verticalGradient(listOf(RoyalViolet, ElectricBlue)))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Illustrated 3D Game Hero Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(195.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_home_hero_banner),
                            contentDescription = strings.homeHeroHeadline,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Subtle bottom gradient scrim so white text pops crisply over art
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.15f),
                                            RoyalViolet.copy(alpha = 0.92f)
                                        )
                                    )
                                )
                        )

                        // Top-left floating Current Level Pill
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.45f),
                            border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${strings.levelWord} ${profile.highestUnlockedLevel} / $TOTAL_CAMPAIGN_LEVELS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        // Bottom Adventure Title over banner
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = strings.homeHeroHeadline,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 26.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = strings.homeHeroSub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }

                    // Giant Primary START GAME CTA (-> Opens Level Map Screen!)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        MindfeudPrimaryButton(
                            text = strings.startGameCta,
                            icon = Icons.Filled.PlayArrow,
                            onClick = onStartGame,
                            testTagName = "home_start_game_button"
                        )
                    }
                }
            }

            // Healthy play banner (if 20+ duels today)
            if (profile.duelsPlayedToday >= 20) {
                MindfeudCard(
                    borderColor = palette.mint.copy(alpha = 0.7f),
                    containerColor = palette.mintContainer,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SelfImprovement,
                            contentDescription = null,
                            tint = palette.mint,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = strings.healthyBreakBannerTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.healthyBreakBannerBody,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Day 3 gentle notification prompt
            if (showDay3Prompt) {
                MindfeudCard(
                    borderColor = palette.gold.copy(alpha = 0.8f),
                    containerColor = palette.goldContainer,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = palette.gold
                            )
                            Text(
                                text = strings.day3PromptTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.goldText
                            )
                        }
                        Text(
                            text = strings.day3PromptBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onEnableReminderFromPrompt,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.gold,
                                    contentColor = Color(0xFF0F1226)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(strings.enableReminderCta, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = onDismissDay3Prompt) {
                                Text(strings.maybeLaterCta, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 3. MORE GAME MODES SECTION (Clean 2x2 Vibrant Arcade Grid)
            Text(
                text = strings.moreModesLabel,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorfulModeTile(
                    title = strings.dailyPuzzleTitle,
                    subtitle = if (dailyDone) strings.dailyCompletedBadge else strings.dailyPlayNow,
                    icon = Icons.Filled.CalendarToday,
                    gradientColors = if (dailyDone) {
                        listOf(EmeraldStart, EmeraldEnd)
                    } else {
                        listOf(Color(0xFF6D28D9), Color(0xFFC026D3))
                    },
                    lipColor = if (dailyDone) EmeraldLip else RoyalVioletLip,
                    onClick = onOpenDaily,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_daily_mode_card")
                )
                ColorfulModeTile(
                    title = strings.duelPrimaryCta,
                    subtitle = strings.duelLobbyTitle,
                    icon = Icons.Filled.Bolt,
                    gradientColors = listOf(SkyCyanStart, ElectricBlue),
                    lipColor = SkyCyanLip,
                    onClick = onOpenDuelLobby,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_start_duel_button")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorfulModeTile(
                    title = strings.friendChallengeTitle,
                    subtitle = strings.friendChallengeSub,
                    icon = Icons.Filled.GroupAdd,
                    gradientColors = listOf(AmberWarmStart, AmberWarmEnd),
                    lipColor = AmberWarmLip,
                    onClick = onOpenFriendChallenge,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_friend_challenge_card")
                )
                ColorfulModeTile(
                    title = strings.practiceTitle,
                    subtitle = strings.practiceSub,
                    icon = Icons.Filled.School,
                    gradientColors = listOf(EmeraldStart, EmeraldEnd),
                    lipColor = EmeraldLip,
                    onClick = onOpenPractice,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_practice_card")
                )
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ColorfulModeTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    lipColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MindfeudGradientCard(
        gradientColors = gradientColors,
        lipColor = lipColor,
        onClick = onClick,
        contentPadding = PaddingValues(15.dp),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.24f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.90f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
