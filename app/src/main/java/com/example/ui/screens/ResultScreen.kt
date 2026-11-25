package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.GameMode
import com.example.domain.model.MatchResult
import com.example.domain.model.MatchVerdict
import com.example.domain.model.TOTAL_CAMPAIGN_LEVELS
import com.example.domain.model.UserProfile
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.MindfeudCard
import com.example.ui.components.MindfeudGradientCard
import com.example.ui.components.MindfeudPrimaryButton
import com.example.ui.components.MindfeudSecondaryButton
import com.example.ui.components.RivalAvatar
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.ArcadeCoral
import com.example.ui.theme.ArcadeCoralDeep
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldEnd
import com.example.ui.theme.EmeraldLip
import com.example.ui.theme.EmeraldStart
import com.example.ui.theme.LocalMindfeudColors
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.RoyalVioletBright
import com.example.ui.theme.RoyalVioletLip
import com.example.ui.theme.SkyCyanEnd
import com.example.ui.theme.SunsetOrangeStart
import java.util.Locale

@Composable
fun ResultScreen(
    result: MatchResult,
    profile: UserProfile,
    onRematch: () -> Unit,
    onShareCard: () -> Unit,
    onGoHome: () -> Unit,
    onOpenLevels: () -> Unit = onGoHome
) {
    BackHandler(onBack = if (result.mode == GameMode.CAMPAIGN_LEVEL) onOpenLevels else onGoHome)
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val isHindi = profile.settings.language == AppLanguage.HINDI
    val isCampaign = result.mode == GameMode.CAMPAIGN_LEVEL
    val levelNum = result.levelNumber ?: 1
    val canAdvanceNextLevel = isCampaign && result.starsEarned >= 1 && levelNum < TOTAL_CAMPAIGN_LEVELS

    val verdictTitle = when {
        isCampaign && result.starsEarned >= 1 -> "${strings.levelWord} $levelNum • ${strings.levelCompleteTitle}"
        isCampaign -> "${strings.levelWord} $levelNum • ${strings.levelRetryTitle}"
        result.mode == GameMode.DAILY -> strings.verdictDailyDone
        result.mode == GameMode.PRACTICE -> strings.verdictPracticeDone
        result.verdict == MatchVerdict.WIN -> strings.verdictWin
        result.verdict == MatchVerdict.DRAW -> strings.verdictDraw
        result.verdict == MatchVerdict.LOSS -> strings.verdictLoss
        else -> strings.verdictDailyDone
    }

    val (heroGradient, heroLip) = when (result.verdict) {
        MatchVerdict.WIN, MatchVerdict.COMPLETED -> Pair(
            listOf(EmeraldStart, EmeraldEnd, SkyCyanEnd),
            EmeraldLip
        )
        MatchVerdict.DRAW -> Pair(
            listOf(RoyalViolet, RoyalVioletBright, ElectricBlue),
            RoyalVioletLip
        )
        MatchVerdict.LOSS -> Pair(
            listOf(ArcadeCoral, SunsetOrangeStart),
            ArcadeCoralDeep
        )
    }

    val showConfetti = result.verdict == MatchVerdict.WIN || result.verdict == MatchVerdict.COMPLETED

    val nextTier = result.newRank.nextTier()
    val xpProgress = if (nextTier == null) {
        1f
    } else {
        val span = (nextTier.minXp - result.newRank.minXp).coerceAtLeast(1)
        ((profile.xp - result.newRank.minXp).toFloat() / span.toFloat()).coerceIn(0f, 1f)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Vibrant Full-Color Verdict & Scoreboard Hero Card
            MindfeudGradientCard(
                gradientColors = heroGradient,
                lipColor = heroLip,
                contentPadding = PaddingValues(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier.size(62.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = verdictTitle,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("result_verdict_text")
                    )

                    if (isCampaign) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("result_stars_row")
                        ) {
                            for (s in 1..3) {
                                val earned = s <= result.starsEarned
                                Icon(
                                    imageVector = if (earned) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = if (earned) Color(0xFFFDE047) else Color.White.copy(alpha = 0.45f),
                                    modifier = Modifier.size(if (s == 2) 48.dp else 40.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    if (result.mode == GameMode.DUEL && result.rivalId != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = profile.nickname,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "${result.playerScore}",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "VS",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFDE047),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    RivalAvatar(
                                        rival = result.rivalId,
                                        contentDescription = strings.rivalName(result.rivalId, isHindi),
                                        size = 28.dp,
                                        borderColor = Color.White
                                    )
                                    Text(
                                        text = strings.rivalName(result.rivalId, isHindi),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                Text(
                                    text = "${result.rivalScore}",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    } else if (result.mode == GameMode.FRIEND_CHALLENGE && result.friendChallenge != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = result.friendChallenge.code,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFDE047),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "${result.playerScore}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 50.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = Color.White
                            )
                            if (result.rivalScore > 0 && result.verdict != MatchVerdict.COMPLETED) {
                                Text(
                                    text = "${strings.friendTargetScore}: ${result.rivalScore}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.92f)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "${result.playerScore}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Per-round glass pills (color + icon for color-blind accessibility)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        result.rounds.forEach { round ->
                            val ok = round.playerCorrect
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = if (ok) 0.24f else 0.14f),
                                border = BorderStroke(1.2.dp, Color.White.copy(alpha = if (ok) 0.65f else 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (ok) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                        contentDescription = null,
                                        tint = if (ok) Color(0xFFBBF7D0) else Color(0xFFFECDD3),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${round.playerPoints}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Accuracy + Avg Time + XP inside translucent strip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.16f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatMiniOnHero(strings.accuracyLabel, "${result.accuracyPercent}%", Color(0xFFBBF7D0))
                            StatMiniOnHero(
                                strings.avgSpeedLabel,
                                String.format(Locale.US, "%.1fs", result.averageTimeSec),
                                Color(0xFFA5F3FC)
                            )
                            StatMiniOnHero(strings.xpEarnedLabel, "+${result.xpEarned}", Color(0xFFFDE047))
                        }
                    }
                }
            }

            // Milestone Banners (Next Level Unlocked, Rank Up, First Badge, Day 3 Gift)
            if (result.unlockedNextLevel) {
                HighlightBanner(
                    text = "${strings.nextLevelUnlockedBanner} (${strings.levelWord} ${levelNum + 1})",
                    bg = palette.mintContainer,
                    border = EmeraldStart,
                    fg = palette.mintText
                )
            }
            if (result.newRank.ordinal > result.previousRank.ordinal) {
                HighlightBanner(
                    text = "${strings.rankUpBanner} ${strings.rankName(result.newRank, isHindi)}",
                    bg = palette.goldContainer,
                    border = palette.gold,
                    fg = palette.goldText
                )
            }
            if (result.firstSessionBadgeUnlocked) {
                HighlightBanner(
                    text = strings.firstBadgeBanner,
                    bg = palette.violetContainer,
                    border = palette.violet,
                    fg = palette.violetText
                )
            }
            if (result.day3FreezeGiftUnlocked) {
                HighlightBanner(
                    text = strings.day3GiftBanner,
                    bg = palette.cyanContainer,
                    border = palette.cyan,
                    fg = palette.cyanText
                )
            }

            // Rank Progress Card
            if (result.mode != GameMode.PRACTICE) {
                MindfeudCard(
                    borderColor = palette.violet.copy(alpha = 0.55f),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = palette.violet
                            )
                            Text(
                                text = strings.rankName(result.newRank, isHindi),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (nextTier != null) "${profile.xp} / ${nextTier.minXp} XP" else "${profile.xp} XP",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = palette.violetText
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = palette.violet,
                        trackColor = palette.elevatedSurface
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            // Primary CTA: Next Level / Retry Level (Campaign) OR Rematch
            val primaryCtaText = when {
                canAdvanceNextLevel -> "${strings.nextLevelCta} (${strings.levelWord} ${levelNum + 1})"
                isCampaign -> strings.retryLevelCta
                result.mode == GameMode.DAILY -> strings.duelPrimaryCta
                else -> strings.rematchCta
            }
            val primaryCtaIcon = if (canAdvanceNextLevel) Icons.Filled.PlayArrow else Icons.Filled.Replay

            MindfeudPrimaryButton(
                text = primaryCtaText,
                icon = primaryCtaIcon,
                onClick = onRematch,
                testTagName = "result_rematch_button"
            )

            // Secondary Row: Levels Map (for Campaign) / Share Card / Home
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isCampaign) {
                    MindfeudSecondaryButton(
                        text = strings.levelsMapCta,
                        icon = Icons.Filled.GridView,
                        accentColor = palette.violetText,
                        onClick = onOpenLevels,
                        modifier = Modifier.weight(1.1f),
                        testTagName = "result_levels_button"
                    )
                    MindfeudSecondaryButton(
                        text = strings.homeCta,
                        icon = Icons.Filled.Home,
                        accentColor = palette.cyanText,
                        onClick = onGoHome,
                        modifier = Modifier.weight(1f),
                        testTagName = "result_home_button"
                    )
                } else {
                    MindfeudSecondaryButton(
                        text = strings.shareCardCta,
                        icon = Icons.Filled.Share,
                        accentColor = palette.cyanText,
                        onClick = onShareCard,
                        modifier = Modifier.weight(1.2f),
                        testTagName = "result_share_button"
                    )
                    MindfeudSecondaryButton(
                        text = strings.homeCta,
                        icon = Icons.Filled.Home,
                        accentColor = palette.violetText,
                        onClick = onGoHome,
                        modifier = Modifier.weight(1f),
                        testTagName = "result_home_button"
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        ConfettiOverlay(
            trigger = showConfetti,
            reduceMotion = profile.settings.reduceMotion
        )
    }
}

@Composable
private fun HighlightBanner(
    text: String,
    bg: Color,
    border: Color,
    fg: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.8.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = fg,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun StatMiniOnHero(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.88f)
        )
    }
}
