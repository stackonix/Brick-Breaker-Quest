package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.domain.engine.PuzzleEngine
import com.example.domain.model.AppLanguage
import com.example.domain.model.PlayPreference
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.MindfeudCard
import com.example.ui.components.MindfeudPrimaryButton
import com.example.ui.components.NumberChainView
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LocalMindfeudColors
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.RoyalVioletBright

/**
 * Section 4: 10-second onboarding — jump straight into a guided first puzzle with a hand indicator,
 * zero login/forms, celebrate win with confetti, then 1-tap play preference to Home.
 */
@Composable
fun OnboardingScreen(
    solved: Boolean,
    language: AppLanguage,
    reduceMotion: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onPuzzleSolved: () -> Unit,
    onComplete: (PlayPreference) -> Unit
) {
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val puzzle = remember { PuzzleEngine.generateOnboardingPuzzle() }
    val correctIndex = remember(puzzle) { puzzle.options.indexOf(puzzle.correctAnswer) }
    var wrongTapFeedback by remember { mutableStateOf(false) }
    var selectedPref by remember { mutableStateOf(PlayPreference.BOTH) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vibrant top pill header with language quick toggle (English / हिन्दी)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(RoyalViolet, RoyalVioletBright, ElectricBlue)))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.appName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.22f),
                        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.45f)),
                        onClick = {
                            val next = if (language == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
                            onLanguageChange(next)
                        },
                        modifier = Modifier.testTag("onboarding_lang_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Language,
                                contentDescription = strings.languageLabel,
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (language == AppLanguage.ENGLISH) "हिन्दी" else "English",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(0.5f))

                if (!solved) {
                    MindfeudCard(
                        borderColor = palette.cyan.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = palette.goldContainer
                            ) {
                                Text(
                                    text = strings.onboardingHintStep,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.goldText,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            NumberChainView(
                                puzzle = puzzle,
                                eliminatedIndices = emptySet(),
                                highlightAnswerIndex = correctIndex,
                                reduceMotion = reduceMotion,
                                onSelectOption = { chosen ->
                                    if (chosen == puzzle.correctAnswer) {
                                        wrongTapFeedback = false
                                        onPuzzleSolved()
                                    } else {
                                        wrongTapFeedback = true
                                    }
                                }
                            )

                            if (wrongTapFeedback) {
                                Text(
                                    text = strings.onboardingHintStep,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.coralText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    MindfeudCard(
                        borderColor = palette.mint.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = palette.mint,
                                modifier = Modifier.size(58.dp)
                            )
                            Text(
                                text = strings.onboardingWinTitle,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = strings.onboardingWinSubtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.mintText,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = strings.onboardingPrefQuestion,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    PlayPreference.DAILY to strings.prefDaily,
                                    PlayPreference.DUEL to strings.prefDuel,
                                    PlayPreference.BOTH to strings.prefBoth
                                ).forEach { (pref, label) ->
                                    FilterChip(
                                        selected = selectedPref == pref,
                                        onClick = { selectedPref = pref },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = palette.violetContainer,
                                            selectedLabelColor = palette.violetText
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("pref_chip_${pref.name.lowercase()}")
                                    )
                                }
                            }

                            Spacer(Modifier.height(6.dp))

                            MindfeudPrimaryButton(
                                text = strings.continueCta,
                                icon = Icons.Filled.Bolt,
                                onClick = { onComplete(selectedPref) },
                                testTagName = "onboarding_continue_button"
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }

        ConfettiOverlay(trigger = solved, reduceMotion = reduceMotion)
    }
}
