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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.KeepScreenOnEffect
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.core.formatDurationMillis
import com.example.data.model.ToolCatalog
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class LapEntry(
    val number: Int,
    val splitMs: Long,
    val lapMs: Long,
)

@Composable
fun StopwatchScreen(
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

    var running by rememberSaveable { mutableStateOf(false) }
    var accumulatedMs by rememberSaveable { mutableLongStateOf(0L) }
    var startedAtElapsed by rememberSaveable { mutableLongStateOf(0L) }
    var displayedMs by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<LapEntry>() }

    LaunchedEffect(running, accumulatedMs, startedAtElapsed) {
        while (running) {
            displayedMs = accumulatedMs + (SystemClock.elapsedRealtime() - startedAtElapsed)
            delay(30L)
        }
        displayedMs = accumulatedMs
    }

    fun toggle() {
        if (running) {
            accumulatedMs += SystemClock.elapsedRealtime() - startedAtElapsed
            running = false
        } else {
            startedAtElapsed = SystemClock.elapsedRealtime()
            running = true
        }
        feedback.play(SoundCue.CLICK)
    }

    fun lap() {
        val currentTotal = if (running) {
            accumulatedMs + (SystemClock.elapsedRealtime() - startedAtElapsed)
        } else {
            accumulatedMs
        }
        val prevSplit = laps.firstOrNull()?.splitMs ?: 0L
        val lapDelta = (currentTotal - prevSplit).coerceAtLeast(0L)
        laps.add(0, LapEntry(number = laps.size + 1, splitMs = currentTotal, lapMs = lapDelta))
        feedback.play(SoundCue.TICK)
    }

    fun reset() {
        running = false
        accumulatedMs = 0L
        startedAtElapsed = 0L
        displayedMs = 0L
        laps.clear()
        feedback.play(SoundCue.CLICK)
    }

    fun saveSession() {
        val total = if (running) accumulatedMs + (SystemClock.elapsedRealtime() - startedAtElapsed) else accumulatedMs
        if (total <= 0L) return
        scope.launch {
            repo.recordHistory(
                toolId = ToolCatalog.TOOL_STOPWATCH,
                category = "Timers",
                title = "Stopwatch Session",
                summary = formatDurationMillis(total),
                detail = if (laps.isEmpty()) "Single run" else "${laps.size} laps recorded",
                metricMs = total,
            )
            feedback.play(SoundCue.COMPLETE)
            Toast.makeText(context, "Stopwatch record saved", Toast.LENGTH_SHORT).show()
        }
    }

    MonoToolScaffold(
        title = "Stopwatch",
        subtitle = "Monotonic Lap Timer",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(
                onClick = { saveSession() },
                enabled = displayedMs > 0L,
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Icon(Icons.Rounded.Save, contentDescription = "Save session", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        Column(
            modifier = contentModifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MonoCard(
                cornerRadius = 26.dp,
                contentPadding = PaddingValues(vertical = 38.dp, horizontal = 20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = formatDurationMillis(displayedMs),
                        style = MonoTabularStyle.copy(fontSize = 54.sp, fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                        modifier = Modifier.testTag("stopwatch_display"),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (running) "RUNNING" else if (displayedMs > 0L) "PAUSED" else "READY",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textMuted,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MonoActionButton(
                    text = if (running) "Lap" else "Reset",
                    icon = if (running) Icons.Rounded.Flag else Icons.Rounded.Refresh,
                    primary = false,
                    enabled = running || displayedMs > 0L,
                    onClick = { if (running) lap() else reset() },
                    modifier = Modifier.weight(0.42f),
                )
                MonoActionButton(
                    text = if (running) "Pause" else "Start",
                    icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    onClick = { toggle() },
                    testTag = "stopwatch_start_pause",
                    modifier = Modifier.weight(0.58f),
                )
            }

            if (laps.isNotEmpty()) {
                Text("Laps (${laps.size})", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(laps) { _, entry ->
                        MonoCard(
                            cornerRadius = 16.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Lap ${entry.number}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.textSecondary,
                                )
                                Text(
                                    text = "+${formatDurationMillis(entry.lapMs)}",
                                    style = MonoTabularStyle.copy(fontSize = 15.sp),
                                    color = palette.textSecondary,
                                )
                                Text(
                                    text = formatDurationMillis(entry.splitMs),
                                    style = MonoTabularStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                    color = palette.textPrimary,
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
