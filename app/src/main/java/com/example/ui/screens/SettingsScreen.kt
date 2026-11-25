package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.domain.model.RankTier
import com.example.domain.model.ThemeMode
import com.example.domain.model.UserProfile
import com.example.ui.components.AmbientDarkBackdrop
import com.example.ui.components.MindfeudCard
import com.example.ui.components.ScreenTopBar
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalMindfeudColors

@Composable
fun SettingsScreen(
    profile: UserProfile,
    onUpdateNickname: (String) -> Unit,
    onUpdateLanguage: (AppLanguage) -> Unit,
    onUpdateTheme: (ThemeMode) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleReduceMotion: (Boolean) -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onToggleHardMode: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current
    val palette = LocalMindfeudColors.current
    val settings = profile.settings

    var nicknameDraft by remember(profile.nickname) { mutableStateOf(profile.nickname) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val hardModeUnlocked = profile.rank.ordinal >= RankTier.MASTERMIND.ordinal

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
                    // Optional Nickname Card (No email/phone/login — Section 9)
                    MindfeudCard(contentPadding = PaddingValues(16.dp)) {
                        Text(
                            text = strings.nicknameLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = nicknameDraft,
                                onValueChange = { if (it.length <= 16) nicknameDraft = it },
                                placeholder = { Text(strings.nicknameHint) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_nickname_input")
                            )
                            Button(
                                onClick = { onUpdateNickname(nicknameDraft) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.primaryActionBg,
                                    contentColor = palette.primaryActionFg
                                ),
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("settings_nickname_save")
                            ) {
                                Text(strings.saveCta)
                            }
                        }
                    }

                    // Language & Theme Card
                    MindfeudCard(contentPadding = PaddingValues(16.dp)) {
                        Text(
                            text = strings.languageLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = settings.language == AppLanguage.ENGLISH,
                                onClick = { onUpdateLanguage(AppLanguage.ENGLISH) },
                                label = { Text("English") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = palette.cyanContainer,
                                    selectedLabelColor = palette.cyanText
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("lang_english_chip")
                            )
                            FilterChip(
                                selected = settings.language == AppLanguage.HINDI,
                                onClick = { onUpdateLanguage(AppLanguage.HINDI) },
                                label = { Text("हिन्दी") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = palette.cyanContainer,
                                    selectedLabelColor = palette.cyanText
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("lang_hindi_chip")
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = palette.borderSubtle)
                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = strings.themeLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                ThemeMode.LIGHT to strings.themeLight,
                                ThemeMode.SYSTEM to strings.themeSystem,
                                ThemeMode.DARK to strings.themeDark
                            ).forEach { (mode, label) ->
                                FilterChip(
                                    selected = settings.themeMode == mode,
                                    onClick = { onUpdateTheme(mode) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.violetContainer,
                                        selectedLabelColor = palette.violetText
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("theme_${mode.name.lowercase()}_chip")
                                )
                            }
                        }
                    }

                    // Audio, Haptics, Accessibility, Notifications & Hard Mode Card
                    MindfeudCard(contentPadding = PaddingValues(16.dp)) {
                        SettingSwitchRow(
                            title = strings.soundLabel,
                            checked = settings.soundEnabled,
                            onCheckedChange = onToggleSound,
                            tag = "switch_sound"
                        )
                        HorizontalDivider(color = palette.borderSubtle)
                        SettingSwitchRow(
                            title = strings.hapticsLabel,
                            checked = settings.hapticsEnabled,
                            onCheckedChange = onToggleHaptics,
                            tag = "switch_haptics"
                        )
                        HorizontalDivider(color = palette.borderSubtle)
                        SettingSwitchRow(
                            title = strings.reduceMotionLabel,
                            checked = settings.reduceMotion,
                            onCheckedChange = onToggleReduceMotion,
                            tag = "switch_reduce_motion"
                        )
                        HorizontalDivider(color = palette.borderSubtle)
                        SettingSwitchRow(
                            title = strings.dailyReminderLabel,
                            subtitle = strings.dailyReminderSub,
                            checked = settings.reminderEnabled,
                            onCheckedChange = onToggleReminder,
                            tag = "switch_daily_reminder"
                        )
                        HorizontalDivider(color = palette.borderSubtle)
                        SettingSwitchRow(
                            title = strings.hardModeLabel,
                            subtitle = if (hardModeUnlocked) strings.hardModeUnlockedSub else strings.hardModeLockedSub,
                            checked = settings.hardModeEnabled,
                            enabled = hardModeUnlocked,
                            onCheckedChange = onToggleHardMode,
                            tag = "switch_hard_mode"
                        )
                    }

                    // Privacy & 15+ Safety Policy button (Section 6 & 9)
                    OutlinedButton(
                        onClick = { showPrivacyDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("settings_privacy_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = palette.mint
                        )
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text(strings.privacyPolicyTitle, color = MaterialTheme.colorScheme.onSurface)
                    }

            Spacer(Modifier.height(16.dp))
        }
    }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = palette.cardSurface,
            title = {
                Text(
                    text = strings.privacyPolicyTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = strings.privacyPolicyBody,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primaryActionBg,
                        contentColor = palette.primaryActionFg
                    )
                ) {
                    Text(strings.closeCta)
                }
            }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag)
        )
    }
}
