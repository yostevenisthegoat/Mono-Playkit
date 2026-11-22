package com.example.feature.timers

import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IntervalTimerScreen(
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
    val intervalPresets = remember(allPresets) { allPresets.filter { it.timerType == "INTERVAL" } }

    var workSec by rememberSaveable { mutableIntStateOf(45) }
    var restSec by rememberSaveable { mutableIntStateOf(15) }
    var totalRounds by rememberSaveable { mutableIntStateOf(5) }

    var currentRound by rememberSaveable { mutableIntStateOf(1) }
    var isWorkPhase by rememberSaveable { mutableStateOf(true) }
    var running by rememberSaveable { mutableStateOf(false) }
    var phaseElapsedAccumulated by rememberSaveable { mutableLongStateOf(0L) }
    var phaseStartedAt by rememberSaveable { mutableLongStateOf(0L) }
    var phaseRemainingMs by remember { mutableLongStateOf(45_000L) }
    var finished by rememberSaveable { mutableStateOf(false) }

    var showSaveDialog by remember { mutableStateOf(false) }

    fun reset() {
        running = false
        finished = false
        currentRound = 1
        isWorkPhase = true
        phaseElapsedAccumulated = 0L
        phaseStartedAt = 0L
        phaseRemainingMs = workSec * 1000L
    }

    LaunchedEffect(running, workSec, restSec, totalRounds, currentRound, isWorkPhase, phaseElapsedAccumulated, phaseStartedAt) {
        while (running && !finished) {
            val phaseTargetMs = (if (isWorkPhase) workSec else restSec) * 1000L
            val elapsed = phaseElapsedAccumulated + (SystemClock.elapsedRealtime() - phaseStartedAt)
            val rem = (phaseTargetMs - elapsed).coerceAtLeast(0L)
            phaseRemainingMs = rem
            if (rem == 0L) {
                if (isWorkPhase) {
                    if (currentRound >= totalRounds) {
                        running = false
                        finished = true
                        feedback.play(SoundCue.COMPLETE)
                        scope.launch {
                            repo.recordHistory(
                                toolId = ToolCatalog.TOOL_INTERVAL_TIMER,
                                category = "Timers",
                                title = "Interval Session Complete",
                                summary = "$totalRounds rounds (${workSec}s / ${restSec}s)",
                            )
                        }
                    } else if (restSec > 0) {
                        isWorkPhase = false
                        phaseElapsedAccumulated = 0L
                        phaseStartedAt = SystemClock.elapsedRealtime()
                        feedback.play(SoundCue.WARNING)
                    } else {
                        currentRound++
                        phaseElapsedAccumulated = 0L
                        phaseStartedAt = SystemClock.elapsedRealtime()
                        feedback.play(SoundCue.ACCENT_TICK)
                    }
                } else {
                    isWorkPhase = true
                    currentRound++
                    phaseElapsedAccumulated = 0L
                    phaseStartedAt = SystemClock.elapsedRealtime()
                    feedback.play(SoundCue.ACCENT_TICK)
                }
            }
            delay(80L)
        }
        val phaseTargetMs = (if (isWorkPhase) workSec else restSec) * 1000L
        phaseRemainingMs = (phaseTargetMs - phaseElapsedAccumulated).coerceAtLeast(0L)
    }

    if (showSaveDialog) {
        var name by remember { mutableStateOf("Rounds ${totalRounds}x (${workSec}s/${restSec}s)") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Save Interval Config", color = palette.textPrimary) },
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
                                timerType = "INTERVAL",
                                workSeconds = workSec,
                                restSeconds = restSec,
                                rounds = totalRounds,
                            )
                            Toast.makeText(context, "Interval preset saved", Toast.LENGTH_SHORT).show()
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

    MonoToolScaffold(
        title = "Interval & Round Timer",
        subtitle = "Work, Rest & Round Cycles",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(onClick = { showSaveDialog = true }) {
                Icon(Icons.Rounded.BookmarkAdd, contentDescription = "Save interval preset", tint = palette.textPrimary)
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
                    borderColor = when {
                        finished -> StatusSuccess
                        running && isWorkPhase -> palette.accentPrimary
                        running && !isWorkPhase -> StatusWarning
                        else -> palette.border
                    },
                    contentPadding = PaddingValues(vertical = 32.dp, horizontal = 20.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = when {
                                finished -> "SESSION COMPLETE"
                                isWorkPhase -> "ACTIVE • ROUND $currentRound / $totalRounds"
                                else -> "REST • ROUND $currentRound / $totalRounds"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (finished) StatusSuccess else if (isWorkPhase) palette.textPrimary else StatusWarning,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = formatClockMillis(phaseRemainingMs),
                            style = MonoTabularStyle.copy(fontSize = 62.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
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
                        onClick = { reset() },
                        modifier = Modifier.weight(0.4f),
                    )
                    MonoActionButton(
                        text = if (running) "Pause" else "Start",
                        icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        onClick = {
                            if (finished) reset()
                            if (running) {
                                phaseElapsedAccumulated += SystemClock.elapsedRealtime() - phaseStartedAt
                                running = false
                            } else {
                                phaseStartedAt = SystemClock.elapsedRealtime()
                                running = true
                            }
                            feedback.play(SoundCue.CLICK)
                        },
                        modifier = Modifier.weight(0.6f),
                    )
                }
            }

            item {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Text("Configuration", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = workSec.toString(),
                            onValueChange = {
                                workSec = (it.filter(Char::isDigit).toIntOrNull() ?: 1).coerceIn(1, 3600)
                                if (!running) reset()
                            },
                            label = { Text("Active (s)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = restSec.toString(),
                            onValueChange = {
                                restSec = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 3600)
                                if (!running) reset()
                            },
                            label = { Text("Rest (s)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = totalRounds.toString(),
                            onValueChange = {
                                totalRounds = (it.filter(Char::isDigit).toIntOrNull() ?: 1).coerceIn(1, 99)
                                if (!running) reset()
                            },
                            label = { Text("Rounds") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (intervalPresets.isNotEmpty()) {
                item {
                    Text("Saved Presets", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
                items(intervalPresets, key = { it.id }) { preset ->
                    MonoCard(
                        onClick = {
                            workSec = preset.workSeconds
                            restSec = preset.restSeconds
                            totalRounds = preset.rounds
                            reset()
                        },
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
                                Text("${preset.rounds} rounds • ${preset.workSeconds}s / ${preset.restSeconds}s", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
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
