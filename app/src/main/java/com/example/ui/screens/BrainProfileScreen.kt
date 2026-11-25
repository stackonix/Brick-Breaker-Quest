package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.domain.model.RankTier
import com.example.domain.model.SkillType
import com.example.domain.model.UserProfile
import com.example.ui.components.AmbientDarkBackdrop
import com.example.ui.components.BrainRadarChart
import com.example.ui.components.MindfeudCard
import com.example.ui.components.MindfeudPrimaryButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalMindfeudColors

@Composable
fun BrainProfileScreen(
    profile: UserProfile,
    onShareProfile: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val isHindi = profile.settings.language == AppLanguage.HINDI
    val isLegend = profile.rank == RankTier.LEGEND

    val skillOrder = listOf(
        SkillType.LOGIC,
        SkillType.SPEED,
        SkillType.MEMORY,
        SkillType.PATTERN,
        SkillType.WORD
    )
    val skillLabels = skillOrder.map { strings.skillName(it, isHindi) }
    val avgDelta = profile.brainSkills.average() - profile.baselineSkillAverage

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Screenshot-friendly Brain Profile Scorecard
                    MindfeudCard(
                        borderColor = if (isLegend) palette.gold else palette.violet.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = profile.nickname,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.WorkspacePremium,
                                        contentDescription = null,
                                        tint = if (isLegend) palette.gold else palette.violet,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${strings.rankName(profile.rank, isHindi)} • ${profile.xp} XP",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                        color = if (isLegend) palette.goldText else palette.violetText
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = palette.cyanContainer,
                                border = BorderStroke(1.2.dp, palette.cyan)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${profile.brainSkills.average()}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
                                        color = palette.cyanText
                                    )
                                    Text(
                                        text = strings.avgSkillLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 5-Skill Pentagon Radar Chart
                        BrainRadarChart(
                            skills = profile.brainSkills,
                            labels = skillLabels,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )

                        Spacer(Modifier.height(14.dp))

                        // Self-comparison trend (Section 7: compare against yourself, not others)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (avgDelta > 0) palette.mintContainer else palette.elevatedSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (avgDelta > 0) {
                                    "+$avgDelta • ${strings.skillTrendUp}"
                                } else {
                                    strings.skillTrendSteady
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (avgDelta > 0) palette.mintText else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Skill bars
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            val barColors = listOf(
                                palette.cyan,
                                palette.gold,
                                palette.mint,
                                palette.coral,
                                palette.violet
                            )
                            skillOrder.forEachIndexed { idx, skill ->
                                val score = profile.brainSkills.scoreFor(skill)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = strings.skillName(skill, isHindi),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$score / 100",
                                            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { (score / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = barColors[idx],
                                        trackColor = palette.elevatedSurface
                                    )
                                }
                            }
                        }
                    }

                    // Key Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatBox(strings.duelsPlayedLabel, "${profile.totalDuelsPlayed}", Modifier.weight(1f))
                        ProfileStatBox(strings.winsLabel, "${profile.totalWins}", Modifier.weight(1f))
                        ProfileStatBox(strings.bestStreakLabel, "${profile.bestStreak}d", Modifier.weight(1f))
                        ProfileStatBox(strings.badgesLabel, "${profile.unlockedBadgesCount()}/7", Modifier.weight(1f))
                    }

                    // Rank Journey Card (Rookie -> Legend)
                    MindfeudCard(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.rankRoadTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            RankTier.entries.forEach { tier ->
                                val reached = profile.xp >= tier.minXp
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (reached) Icons.Filled.CheckCircle else Icons.Filled.WorkspacePremium,
                                            contentDescription = null,
                                            tint = if (reached) palette.mint else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = strings.rankName(tier, isHindi),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = strings.rankUnlockSummary(tier, isHindi),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${tier.minXp} XP",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                        color = if (reached) palette.mintText else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    MindfeudPrimaryButton(
                        text = strings.shareProfileCta,
                        icon = Icons.Filled.Share,
                        onClick = onShareProfile,
                        testTagName = "share_brain_profile_button"
                    )

                    Spacer(Modifier.height(16.dp))
                }
        }
    }
}

@Composable
private fun ProfileStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    val palette = LocalMindfeudColors.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = palette.cardSurface,
        border = BorderStroke(1.dp, palette.borderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                color = palette.cyanText
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
