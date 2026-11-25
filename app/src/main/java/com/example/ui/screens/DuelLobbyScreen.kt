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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.domain.model.PuzzleType
import com.example.domain.model.RivalId
import com.example.domain.model.UserProfile
import com.example.ui.components.MindfeudCard
import com.example.ui.components.MindfeudPrimaryButton
import com.example.ui.components.RivalAvatar
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.AmberWarmStart
import com.example.ui.theme.ArcadeCoral
import com.example.ui.theme.ArcadeCyan
import com.example.ui.theme.BerryPinkEnd
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldStart
import com.example.ui.theme.LightCoralContainer
import com.example.ui.theme.LightCyanContainer
import com.example.ui.theme.LightGoldContainer
import com.example.ui.theme.LightMintContainer
import com.example.ui.theme.LightVioletContainer
import com.example.ui.theme.LocalMindfeudColors
import com.example.ui.theme.RoyalVioletBright
import com.example.ui.theme.SkyCyanStart
import com.example.ui.theme.TileAmberBg
import com.example.ui.theme.TileBlueBg
import com.example.ui.theme.TileMintBg
import com.example.ui.theme.TilePurpleBg

@Composable
fun DuelLobbyScreen(
    profile: UserProfile,
    selectedRival: RivalId,
    onSelectRival: (RivalId) -> Unit,
    onPickRandomRival: () -> Unit,
    onStartDuel: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val isHindi = profile.settings.language == AppLanguage.HINDI

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
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.chooseRival,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (profile.consecutiveLosses >= 3) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.mintContainer,
                        border = BorderStroke(1.5.dp, palette.mint.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = strings.mercyActiveNote,
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.mintText,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                RivalId.entries.forEach { rival ->
                    val unlocked = profile.isRivalUnlocked(rival)
                    val isSelected = selectedRival == rival && unlocked
                    val speedTag = when {
                        rival.baseSpeedFactor <= 0.42f -> strings.rivalSpeedFast
                        rival.baseSpeedFactor >= 0.60f -> strings.rivalSpeedCalm
                        else -> strings.rivalSpeedBalanced
                    }
                    val strongLabel = rival.strongSkill?.let {
                        "${strings.rivalStrongIn} ${strings.skillName(it, isHindi)}"
                    } ?: strings.rivalSpeedBalanced

                    val rivalAccent = when (rival) {
                        RivalId.QUICK_QASIM -> SkyCyanStart
                        RivalId.PATTERN_PRANAV -> RoyalVioletBright
                        RivalId.CALM_KAVYA -> EmeraldStart
                        RivalId.BOSS_BHAVNA -> ArcadeCoral
                    }
                    val rivalTintBg = if (palette.isDark) {
                        if (isSelected) rivalAccent.copy(alpha = 0.24f) else palette.cardSurface
                    } else {
                        when (rival) {
                            RivalId.QUICK_QASIM -> if (isSelected) TileBlueBg else Color(0xFFF5FAFF)
                            RivalId.PATTERN_PRANAV -> if (isSelected) TilePurpleBg else Color(0xFFFAF5FF)
                            RivalId.CALM_KAVYA -> if (isSelected) TileMintBg else Color(0xFFF2FDF8)
                            RivalId.BOSS_BHAVNA -> if (isSelected) LightCoralContainer else Color(0xFFFFF5F7)
                        }
                    }

                    MindfeudCard(
                        onClick = if (unlocked) ({ onSelectRival(rival) }) else null,
                        borderColor = when {
                            isSelected -> rivalAccent
                            unlocked -> rivalAccent.copy(alpha = 0.45f)
                            else -> palette.borderSubtle.copy(alpha = 0.5f)
                        },
                        containerColor = if (unlocked) rivalTintBg else palette.cardSurface,
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (unlocked) 1f else 0.58f)
                            .testTag("rival_card_${rival.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            RivalAvatar(
                                rival = rival,
                                contentDescription = strings.rivalName(rival, isHindi),
                                size = 64.dp,
                                borderColor = rivalAccent
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = strings.rivalName(rival, isHindi),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (!unlocked) {
                                            Icon(
                                                imageVector = Icons.Filled.Lock,
                                                contentDescription = null,
                                                tint = palette.gold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = rivalAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = strings.rivalBio(rival, isHindi),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(7.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (unlocked) {
                                        SmallTag(
                                            text = speedTag,
                                            bg = rivalAccent.copy(alpha = if (palette.isDark) 0.25f else 0.16f),
                                            fg = if (palette.isDark) Color.White else rivalAccent
                                        )
                                        SmallTag(text = strongLabel, bg = palette.goldContainer, fg = palette.goldText)
                                    } else {
                                        SmallTag(
                                            text = "${strings.rivalLockedAt} ${strings.rankName(rival.unlockRank, isHindi)} (${rival.unlockRank.minXp} XP)",
                                            bg = palette.elevatedSurface,
                                            fg = palette.goldText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                MindfeudPrimaryButton(
                    text = strings.startDuelCta,
                    icon = Icons.Filled.Bolt,
                    onClick = onStartDuel,
                    testTagName = "lobby_start_duel_button"
                )
            }
        }
    }
}

@Composable
fun PracticePickerScreen(
    language: AppLanguage,
    onStartPractice: (PuzzleType?) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val isHindi = language == AppLanguage.HINDI

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
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.practiceScreenSub,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                MindfeudPrimaryButton(
                    text = strings.practiceMixAll,
                    icon = Icons.Filled.Extension,
                    onClick = { onStartPractice(null) },
                    testTagName = "practice_mix_button"
                )

                Spacer(Modifier.height(4.dp))

                PuzzleType.entries.forEach { type ->
                    val (icon, badgeColor, cardTint) = when (type) {
                        PuzzleType.NUMBER_CHAIN -> Triple(Icons.Filled.Tag, ElectricBlue, if (palette.isDark) palette.cardSurface else TileBlueBg.copy(alpha = 0.55f))
                        PuzzleType.CODE_BREAKER -> Triple(Icons.Filled.Pin, RoyalVioletBright, if (palette.isDark) palette.cardSurface else LightVioletContainer.copy(alpha = 0.65f))
                        PuzzleType.ODD_SHAPE -> Triple(Icons.Filled.Pattern, BerryPinkEnd, if (palette.isDark) palette.cardSurface else LightCoralContainer.copy(alpha = 0.65f))
                        PuzzleType.QUICK_CALC -> Triple(Icons.Filled.Calculate, AmberWarmStart, if (palette.isDark) palette.cardSurface else TileAmberBg.copy(alpha = 0.55f))
                        PuzzleType.WORD_TWIST -> Triple(Icons.Filled.Spellcheck, EmeraldStart, if (palette.isDark) palette.cardSurface else LightMintContainer.copy(alpha = 0.65f))
                        PuzzleType.MEMORY_GRID -> Triple(Icons.Filled.GridView, ArcadeCyan, if (palette.isDark) palette.cardSurface else LightCyanContainer.copy(alpha = 0.65f))
                    }
                    MindfeudCard(
                        onClick = { onStartPractice(type) },
                        borderColor = badgeColor.copy(alpha = 0.6f),
                        containerColor = cardTint,
                        contentPadding = PaddingValues(15.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("practice_type_${type.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = badgeColor,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, contentDescription = null, tint = Color.White)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.puzzleName(type, isHindi),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${strings.skillName(type.skill, isHindi)} • ${strings.untimedBadge}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SmallTag(text: String, bg: Color, fg: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
