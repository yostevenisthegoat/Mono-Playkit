package com.example.feature.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Scoreboard
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.LocalAppContainer
import com.example.data.preferences.UserPreferences
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoGroupedContainer
import com.example.ui.components.MonoGroupedRow
import com.example.ui.components.MonoLogoMark
import com.example.ui.components.MonoPillTag
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val container = LocalAppContainer.current
    val repo = container.repository
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showInspectionDialog by remember { mutableStateOf(false) }
    var showScoreboardPresetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showCommunityDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = repo.exportBackupJson(context, uri)
                val msg = res.fold(
                    onSuccess = { count -> "Exported $count items to Mono PlayKit backup" },
                    onFailure = { err -> "Export failed: ${err.message ?: "Unknown error"}" },
                )
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = repo.importBackupJson(context, uri)
                val msg = res.fold(
                    onSuccess = { count -> "Restored $count records from backup" },
                    onFailure = { err -> "Import failed: ${err.message ?: "Invalid backup"}" },
                )
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Appearance", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        ThemeMode.DARK to "Mono Dark (Official Default)",
                        ThemeMode.LIGHT to "Mono Light",
                        ThemeMode.SYSTEM to "Follow System",
                    ).forEach { (mode, label) ->
                        MonoPillTag(
                            label = label,
                            selected = preferences.themeMode == mode,
                            onClick = {
                                scope.launch { repo.setThemeMode(mode) }
                                showThemeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close", color = palette.textSecondary)
                }
            },
        )
    }

    if (showInspectionDialog) {
        AlertDialog(
            onDismissRequest = { showInspectionDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Cube Inspection Countdown", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        0 to "Disabled (Instant start)",
                        8 to "8 seconds (Short)",
                        15 to "15 seconds (WCA Standard)",
                    ).forEach { (sec, label) ->
                        MonoPillTag(
                            label = label,
                            selected = preferences.cubeInspectionSeconds == sec,
                            onClick = {
                                scope.launch { repo.setCubeInspectionSeconds(sec) }
                                showInspectionDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInspectionDialog = false }) {
                    Text("Close", color = palette.textSecondary)
                }
            },
        )
    }

    if (showScoreboardPresetDialog) {
        AlertDialog(
            onDismissRequest = { showScoreboardPresetDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Default Scoreboard Mode", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "STANDARD" to "Standard Two-Team",
                        "BASKETBALL" to "Basketball",
                        "VOLLEYBALL" to "Volleyball",
                        "FOOTBALL" to "Football",
                        "DARTS_501" to "Darts 501",
                    ).forEach { (id, label) ->
                        MonoPillTag(
                            label = label,
                            selected = preferences.defaultScoreboardPreset == id,
                            onClick = {
                                scope.launch { repo.setDefaultScoreboardPreset(id) }
                                showScoreboardPresetDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showScoreboardPresetDialog = false }) {
                    Text("Close", color = palette.textSecondary)
                }
            },
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Clear all history?", color = palette.textPrimary) },
            text = { Text("Deletes all saved solves, match logs, dice history, and timer records.", color = palette.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearHistoryDialog = false
                        scope.launch {
                            repo.clearAllHistory()
                            Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) { Text("Clear", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = palette.textSecondary)
                }
            },
        )
    }

    if (showResetDataDialog) {
        AlertDialog(
            onDismissRequest = { showResetDataDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Reset all application data?", color = palette.textPrimary) },
            text = { Text("Resets all settings, pinned favorites, history, toy checklists, and saved counters to factory defaults.", color = palette.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDataDialog = false
                        scope.launch {
                            repo.resetAllApplicationData()
                            Toast.makeText(context, "All data reset", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) { Text("Reset Everything", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDataDialog = false }) {
                    Text("Cancel", color = palette.textSecondary)
                }
            },
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("About Mono PlayKit", color = palette.textPrimary) },
            text = {
                Text(
                    "Mono PlayKit is an offline-first companion toolkit for physical games, tabletop sessions, puzzles, and recreational toys.\n\n" +
                        "Version: 1.0.0\n" +
                        "Philosophy: 100% Free, Open Source, Zero Ads, Zero Accounts.\n" +
                        "Built with Kotlin, Jetpack Compose, Room, and DataStore.",
                    color = palette.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Done", color = palette.textPrimary)
                }
            },
        )
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Open Source Licenses", color = palette.textPrimary) },
            text = {
                Text(
                    "• Jetpack Compose & Material 3 (Apache 2.0)\n" +
                        "• AndroidX Room & DataStore (Apache 2.0)\n" +
                        "• Navigation Compose (Apache 2.0)\n" +
                        "• Space Grotesk, Plus Jakarta Sans & JetBrains Mono (SIL OFL 1.1)\n" +
                        "• Mono PlayKit Core & Solvers (MIT / Open Source)",
                    color = palette.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close", color = palette.textPrimary)
                }
            },
        )
    }

    if (showCommunityDialog) {
        AlertDialog(
            onDismissRequest = { showCommunityDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Mono Community & Support", color = palette.textPrimary) },
            text = {
                Text(
                    "Mono PlayKit operates 100% offline with zero telemetry. You can copy diagnostic info below to include when sharing feedback or filing a bug report in the Mono community repository.",
                    color = palette.textSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val info = "Mono PlayKit v1.0.0 | Android SDK ${android.os.Build.VERSION.SDK_INT} | Theme=${preferences.themeMode}"
                        cm?.setPrimaryClip(ClipData.newPlainText("Mono PlayKit Diagnostics", info))
                        Toast.makeText(context, "Diagnostics copied to clipboard", Toast.LENGTH_SHORT).show()
                        showCommunityDialog = false
                    },
                ) { Text("Copy Diagnostics", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showCommunityDialog = false }) {
                    Text("Close", color = palette.textSecondary)
                }
            },
        )
    }

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = palette.bgMain,
        checkedTrackColor = palette.accentPrimary,
        uncheckedThumbColor = palette.textSecondary,
        uncheckedTrackColor = palette.surfaceSecondary,
        uncheckedBorderColor = palette.border,
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displayMedium,
                        color = palette.textPrimary,
                    )
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = palette.textPrimary,
                        )
                    }
                }
            }

            // Brand / Profile Card matching reference Settings top card
            item {
                MonoCard(
                    onClick = { showAboutDialog = true },
                    cornerRadius = 22.dp,
                    contentPadding = PaddingValues(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(palette.surfaceSecondary)
                                .border(1.dp, palette.border, RoundedCornerShape(15.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            MonoLogoMark(size = 32.dp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mono PlayKit",
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.textPrimary,
                            )
                            Text(
                                text = "Offline Toolkit • 100% Free",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary,
                            )
                        }
                    }
                }
            }

            // Section 1: Appearance & Feedback
            item {
                MonoGroupedContainer {
                    MonoGroupedRow(
                        title = "Appearance",
                        subtitle = when (preferences.themeMode) {
                            ThemeMode.DARK -> "Mono Dark"
                            ThemeMode.LIGHT -> "Mono Light"
                            ThemeMode.SYSTEM -> "System theme"
                        },
                        icon = if (palette.isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        onClick = { showThemeDialog = true },
                        testTag = "settings_appearance_row",
                    )
                    MonoGroupedRow(
                        title = "Sound Effects",
                        subtitle = "Timer chimes, metronome & dice cues",
                        icon = Icons.Outlined.VolumeUp,
                        showChevron = false,
                        trailingContent = {
                            Switch(
                                checked = preferences.soundEnabled,
                                onCheckedChange = { scope.launch { repo.setSoundEnabled(it) } },
                                colors = switchColors,
                            )
                        },
                    )
                    MonoGroupedRow(
                        title = "Haptic Feedback",
                        subtitle = "Tactile pulse on clock taps & rolls",
                        icon = Icons.Outlined.Vibration,
                        showChevron = false,
                        trailingContent = {
                            Switch(
                                checked = preferences.hapticEnabled,
                                onCheckedChange = { scope.launch { repo.setHapticEnabled(it) } },
                                colors = switchColors,
                            )
                        },
                    )
                    MonoGroupedRow(
                        title = "Keep Screen On",
                        subtitle = "Prevent sleep during timers & matches",
                        icon = Icons.Outlined.Timer,
                        showChevron = false,
                        showDivider = false,
                        trailingContent = {
                            Switch(
                                checked = preferences.keepScreenOn,
                                onCheckedChange = { scope.launch { repo.setKeepScreenOn(it) } },
                                colors = switchColors,
                            )
                        },
                    )
                }
            }

            // Section 2: Utility Preferences
            item {
                MonoGroupedContainer {
                    MonoGroupedRow(
                        title = "Cube Inspection Timer",
                        subtitle = if (preferences.cubeInspectionSeconds == 0) "Disabled" else "${preferences.cubeInspectionSeconds} seconds before solve",
                        icon = Icons.Outlined.Timer,
                        onClick = { showInspectionDialog = true },
                    )
                    MonoGroupedRow(
                        title = "Default Scoreboard",
                        subtitle = preferences.defaultScoreboardPreset.replace("_", " "),
                        icon = Icons.Outlined.Scoreboard,
                        onClick = { showScoreboardPresetDialog = true },
                    )
                    MonoGroupedRow(
                        title = "Save Dice Roll History",
                        subtitle = "Log dice totals to History tab",
                        icon = Icons.Outlined.Info,
                        showChevron = false,
                        showDivider = false,
                        trailingContent = {
                            Switch(
                                checked = preferences.saveDiceHistory,
                                onCheckedChange = { scope.launch { repo.setSaveDiceHistory(it) } },
                                colors = switchColors,
                            )
                        },
                    )
                }
            }

            // Section 3: Data Management
            item {
                MonoGroupedContainer {
                    MonoGroupedRow(
                        title = "Export Backup",
                        subtitle = "Save settings, history & checklists as JSON",
                        icon = Icons.Outlined.FileUpload,
                        onClick = {
                            exportLauncher.launch("mono_playkit_backup_${System.currentTimeMillis() / 1000}.json")
                        },
                    )
                    MonoGroupedRow(
                        title = "Import Backup",
                        subtitle = "Restore data from a Mono PlayKit JSON file",
                        icon = Icons.Outlined.FileDownload,
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    )
                    MonoGroupedRow(
                        title = "Clear History",
                        subtitle = "Delete saved session logs",
                        icon = Icons.Outlined.DeleteSweep,
                        onClick = { showClearHistoryDialog = true },
                    )
                    MonoGroupedRow(
                        title = "Reset Application Data",
                        subtitle = "Restore factory defaults",
                        icon = Icons.Outlined.Restore,
                        showDivider = false,
                        onClick = { showResetDataDialog = true },
                    )
                }
            }

            // Section 4: About (matching reference Settings bottom cards)
            item {
                MonoGroupedContainer {
                    MonoGroupedRow(
                        title = "About Mono PlayKit",
                        subtitle = "Version 1.0.0 • Open Source",
                        icon = Icons.Outlined.Info,
                        onClick = { showAboutDialog = true },
                    )
                    MonoGroupedRow(
                        title = "Open Source Licenses",
                        subtitle = "Libraries & bundled typography",
                        icon = Icons.Outlined.Code,
                        onClick = { showLicensesDialog = true },
                    )
                    MonoGroupedRow(
                        title = "Report a Problem",
                        subtitle = "Copy offline diagnostic summary",
                        icon = Icons.Outlined.BugReport,
                        onClick = { showCommunityDialog = true },
                    )
                    MonoGroupedRow(
                        title = "Join the Mono Community",
                        subtitle = "Built for physical play, 100% free",
                        icon = Icons.Outlined.Groups,
                        showDivider = false,
                        onClick = { showAboutDialog = true },
                    )
                }
            }
        }
    }
}
