package com.example.feature.timers

import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.KeepScreenOnEffect
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.core.formatClockMillis
import com.example.data.model.ToolCatalog
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CountdownTimerScreen(
    isFavorite: Boolean,
    keepScreenOn: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val container = LocalAppContainer.current
    val feedback = container.feedback
    val repo = container.repository
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    KeepScreenOnEffect(keepScreenOn)

    val allPresets by repo.timerPresets.collectAsStateWithLifecycle(initialValue = emptyList())
    val countdownPresets = remember(allPresets) { allPresets.filter { it.timerType == "COUNTDOWN" } }

    var configuredSeconds by rememberSaveable { mutableIntStateOf(180) }
    var running by rememberSaveable { mutableStateOf(false) }
    var accumulatedElapsedMs by rememberSaveable { mutableLongStateOf(0L) }
    var startedAtElapsed by rememberSaveable { mutableLongStateOf(0L) }
    var remainingMs by remember { mutableLongStateOf(180_000L) }

    var showCustomDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    fun setDuration(seconds: Int) {
        configuredSeconds = seconds.coerceAtLeast(1)
        running = false
        accumulatedElapsedMs = 0L
        startedAtElapsed = 0L
        remainingMs = configuredSeconds * 1000L
        feedback.play(SoundCue.CLICK)
    }

    LaunchedEffect(running, configuredSeconds, accumulatedElapsedMs, startedAtElapsed) {
        val totalMs = configuredSeconds * 1000L
        while (running) {
            val elapsed = accumulatedElapsedMs + (SystemClock.elapsedRealtime() - startedAtElapsed)
            val rem = (totalMs - elapsed).coerceAtLeast(0L)
            remainingMs = rem
            if (rem == 0L) {
                running = false
                accumulatedElapsedMs = totalMs
                feedback.play(SoundCue.COMPLETE)
                scope.launch {
                    repo.recordHistory(
                        toolId = ToolCatalog.TOOL_COUNTDOWN,
                        category = "Timers",
                        title = "Countdown Completed",
                        summary = formatClockMillis(totalMs),
                        detail = "Finished countdown",
                        metricMs = totalMs,
                    )
                }
            }
            delay(80L)
        }
        remainingMs = (totalMs - accumulatedElapsedMs).coerceAtLeast(0L)
    }

    if (showCustomDialog) {
        var mins by remember { mutableStateOf((configuredSeconds / 60).toString()) }
        var secs by remember { mutableStateOf((configuredSeconds % 60).toString()) }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Custom Duration", color = palette.textPrimary) },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = mins,
                        onValueChange = { mins = it.filter(Char::isDigit) },
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = secs,
                        onValueChange = { secs = it.filter(Char::isDigit) },
                        label = { Text("Seconds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val total = ((mins.toIntOrNull() ?: 0) * 60) + (secs.toIntOrNull() ?: 0)
                        if (total > 0) setDuration(total)
                        showCustomDialog = false
                    },
                ) { Text("Set", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    if (showSaveDialog) {
        var name by remember { mutableStateOf("Turn Timer (${configuredSeconds}s)") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Save Countdown Preset", color = palette.textPrimary) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset Name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repo.saveTimerPreset(
                                name = name,
                                timerType = "COUNTDOWN",
                                workSeconds = configuredSeconds,
                            )
                            Toast.makeText(context, "Preset saved", Toast.LENGTH_SHORT).show()
                        }
                        showSaveDialog = false
                    },
                ) { Text("Save", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    val totalMs = (configuredSeconds * 1000L).coerceAtLeast(1L)
    val progress = (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    MonoToolScaffold(
        title = "Countdown Timer",
        subtitle = "Game Turns & Custom Presets",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(onClick = { showSaveDialog = true }) {
                Icon(Icons.Rounded.BookmarkAdd, contentDescription = "Save preset", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                MonoCard(
                    cornerRadius = 26.dp,
                    contentPadding = PaddingValues(vertical = 34.dp, horizontal = 22.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = formatClockMillis(remainingMs),
                            style = MonoTabularStyle.copy(fontSize = 60.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = palette.accentPrimary,
                            trackColor = palette.surfaceSecondary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MonoActionButton(
                        text = "Reset",
                        icon = Icons.Rounded.Refresh,
                        primary = false,
                        onClick = { setDuration(configuredSeconds) },
                        modifier = Modifier.weight(0.4f),
                    )
                    MonoActionButton(
                        text = if (running) "Pause" else "Start",
                        icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        onClick = {
                            if (running) {
                                accumulatedElapsedMs += SystemClock.elapsedRealtime() - startedAtElapsed
                                running = false
                            } else {
                                if (remainingMs == 0L) accumulatedElapsedMs = 0L
                                startedAtElapsed = SystemClock.elapsedRealtime()
                                running = true
                            }
                            feedback.play(SoundCue.CLICK)
                        },
                        modifier = Modifier.weight(0.6f),
                    )
                }
            }

            item {
                Text("Quick Durations", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(30 to "30s", 60 to "1m", 120 to "2m", 180 to "3m", 300 to "5m", 600 to "10m", 900 to "15m").forEach { (sec, lbl) ->
                        MonoPillTag(
                            label = lbl,
                            selected = configuredSeconds == sec,
                            onClick = { setDuration(sec) },
                        )
                    }
                    MonoPillTag(
                        label = "Custom...",
                        onClick = { showCustomDialog = true },
                    )
                }
            }

            if (countdownPresets.isNotEmpty()) {
                item {
                    Text("Saved Presets", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
                items(countdownPresets, key = { it.id }) { preset ->
                    MonoCard(
                        onClick = { setDuration(preset.workSeconds) },
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(preset.name, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                                Text(formatClockMillis(preset.workSeconds * 1000L), style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                            }
                            IconButton(onClick = { scope.launch { repo.deleteTimerPreset(preset.id) } }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete preset", tint = palette.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
