package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.engine.ChallengeCodec
import com.example.domain.model.FriendChallengeData
import com.example.domain.model.GameMode
import com.example.domain.model.MatchResult
import com.example.ui.components.AmbientDarkBackdrop
import com.example.ui.components.MindfeudCard
import com.example.ui.components.MindfeudPrimaryButton
import com.example.ui.components.MindfeudSecondaryButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalMindfeudColors

@Composable
fun FriendChallengeScreen(
    pendingDeepLink: FriendChallengeData?,
    lastResult: MatchResult?,
    onConsumePendingDeepLink: () -> Unit,
    onCreateChallenge: () -> Unit,
    onAcceptChallenge: (FriendChallengeData) -> Unit,
    onShareChallengeInvite: (FriendChallengeData, Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current

    var selectedTab by remember { mutableIntStateOf(if (pendingDeepLink != null) 1 else 0) }
    var codeInput by remember { mutableStateOf(pendingDeepLink?.code ?: "") }
    var parsedPreview by remember { mutableStateOf(pendingDeepLink) }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(pendingDeepLink) {
        if (pendingDeepLink != null) {
            selectedTab = 1
            codeInput = pendingDeepLink.code
            parsedPreview = pendingDeepLink
            onConsumePendingDeepLink()
        }
    }

    val lastCreatedChallenge = lastResult?.takeIf { it.mode == GameMode.FRIEND_CHALLENGE }?.friendChallenge

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
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tab switch: Create vs Join
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text(strings.createChallengeTab, style = MaterialTheme.typography.labelLarge) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.cyanContainer,
                                selectedLabelColor = palette.cyanText
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("friend_tab_create")
                        )
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text(strings.joinChallengeTab, style = MaterialTheme.typography.labelLarge) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.violetContainer,
                                selectedLabelColor = palette.violetText
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("friend_tab_join")
                        )
                    }

                    if (selectedTab == 0) {
                        MindfeudCard(
                            borderColor = palette.cyan.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GroupAdd,
                                contentDescription = null,
                                tint = palette.cyan,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = strings.createChallengeDesc,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(18.dp))
                            MindfeudPrimaryButton(
                                text = strings.playAndCreateCta,
                                icon = Icons.Filled.Bolt,
                                onClick = onCreateChallenge,
                                testTagName = "friend_play_create_button"
                            )
                        }

                        if (lastCreatedChallenge != null && lastResult != null) {
                            MindfeudCard(
                                borderColor = palette.gold.copy(alpha = 0.65f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = lastCreatedChallenge.code,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = palette.goldText
                                )
                                Text(
                                    text = "${strings.youLabel}: ${lastResult.playerScore} pts",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))
                                MindfeudSecondaryButton(
                                    text = strings.shareInviteCta,
                                    icon = Icons.Filled.Share,
                                    accentColor = palette.goldText,
                                    onClick = { onShareChallengeInvite(lastCreatedChallenge, lastResult.playerScore) },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTagName = "friend_share_invite_button"
                                )
                            }
                        }
                    } else {
                        MindfeudCard(
                            borderColor = palette.violet.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Link,
                                contentDescription = null,
                                tint = palette.violet,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = codeInput,
                                onValueChange = {
                                    codeInput = it
                                    showError = false
                                    parsedPreview = ChallengeCodec.parseInput(it)
                                },
                                label = { Text(strings.enterCodeLabel) },
                                placeholder = { Text(strings.enterCodeHint) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("friend_code_input")
                            )

                            if (showError) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = strings.invalidCodeError,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.coralText
                                )
                            }

                            if (parsedPreview != null) {
                                Spacer(Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = palette.goldContainer,
                                    border = BorderStroke(1.dp, palette.gold.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "${parsedPreview!!.code} • ${parsedPreview!!.challengerName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = palette.goldText
                                        )
                                        if (parsedPreview!!.targetScore != null) {
                                            Text(
                                                text = "${strings.friendTargetScore}: ${parsedPreview!!.targetScore}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            MindfeudPrimaryButton(
                                text = strings.startChallengeCta,
                                icon = Icons.Filled.Bolt,
                                onClick = {
                                    val parsed = ChallengeCodec.parseInput(codeInput)
                                    if (parsed != null) {
                                        onAcceptChallenge(parsed)
                                    } else {
                                        showError = true
                                    }
                                },
                                testTagName = "friend_accept_start_button"
                            )
                        }
                    }

                    // Privacy & offline guarantee callout (Section 7 & 9)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.elevatedSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = palette.mint,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = strings.noServerNote,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
        }
    }
}
