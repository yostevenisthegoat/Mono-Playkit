package com.example.feature.toys

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.KeepScreenOnEffect
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.core.formatClockMillis
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun MetronomeScreen(
    isFavorite: Boolean,
    keepScreenOn: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback
    KeepScreenOnEffect(keepScreenOn)

    var bpm by rememberSaveable { mutableIntStateOf(100) }
    var beatsPerMeasure by rememberSaveable { mutableIntStateOf(4) }
    var running by rememberSaveable { mutableStateOf(false) }
    var currentBeat by remember { mutableIntStateOf(0) }
    var practiceElapsedMs by remember { mutableLongStateOf(0L) }
    var practiceStartElapsed by remember { mutableLongStateOf(0L) }
    val tapTimes = remember { mutableStateListOf<Long>() }

    // Monotonic beat scheduler
    LaunchedEffect(running, bpm, beatsPerMeasure) {
        if (!running) {
            currentBeat = 0
            return@LaunchedEffect
        }
        practiceStartElapsed = SystemClock.elapsedRealtime()
        var beatIdx = 0
        var nextBeatAt = SystemClock.elapsedRealtime()
        while (running) {
            val now = SystemClock.elapsedRealtime()
            practiceElapsedMs = now - practiceStartElapsed
            if (now >= nextBeatAt) {
                currentBeat = (beatIdx % beatsPerMeasure) + 1
                feedback.play(if (currentBeat == 1) SoundCue.ACCENT_TICK else SoundCue.TICK)
                beatIdx++
                val intervalMs = (60_000.0 / bpm).roundToInt().toLong().coerceAtLeast(150L)
                nextBeatAt += intervalMs
                if (now - nextBeatAt > intervalMs) nextBeatAt = now + intervalMs
            }
            delay(12L)
        }
    }

    fun handleTapTempo() {
        val now = SystemClock.elapsedRealtime()
        if (tapTimes.isNotEmpty() && now - tapTimes.last() > 2500L) {
            tapTimes.clear()
        }
        tapTimes.add(now)
        if (tapTimes.size > 5) tapTimes.removeAt(0)
        if (tapTimes.size >= 2) {
            val avg = (tapTimes.last() - tapTimes.first()).toDouble() / (tapTimes.size - 1)
            bpm = (60_000.0 / avg).roundToInt().coerceIn(30, 240)
        }
        feedback.vibrateLight(12L)
    }

    MonoToolScaffold(
        title = "Metronome & Practice",
        subtitle = "BPM Controller & Practice Timer",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                MonoCard(cornerRadius = 26.dp, contentPadding = PaddingValues(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (b in 1..beatsPerMeasure) {
                                val active = running && currentBeat == b
                                Box(
                                    modifier = Modifier
                                        .size(if (b == 1) 22.dp else 18.dp)
                                        .clip(CircleShape)
                                        .background(if (active) palette.accentPrimary else palette.surfaceSecondary)
                                        .border(1.dp, palette.border, CircleShape),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "$bpm",
                            style = MonoTabularStyle.copy(fontSize = 68.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                        Text("BPM • $beatsPerMeasure/4", style = MaterialTheme.typography.titleMedium, color = palette.textSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Practice: ${formatClockMillis(practiceElapsedMs)}",
                            style = MonoTabularStyle.copy(fontSize = 14.sp),
                            color = palette.textMuted,
                        )
                    }
                }
            }

            item {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { if (bpm > 30) bpm-- }) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Decrease BPM", tint = palette.textPrimary)
                        }
                        Slider(
                            value = bpm.toFloat(),
                            onValueChange = { bpm = it.roundToInt().coerceIn(30, 240) },
                            valueRange = 30f..240f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.accentPrimary,
                                activeTrackColor = palette.accentPrimary,
                                inactiveTrackColor = palette.surfaceSecondary,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { if (bpm < 240) bpm++ }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Increase BPM", tint = palette.textPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 3, 4, 6).forEach { sig ->
                            MonoPillTag(
                                label = "$sig/4",
                                selected = beatsPerMeasure == sig,
                                onClick = { beatsPerMeasure = sig },
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MonoActionButton(
                        text = "Tap Tempo",
                        icon = Icons.Rounded.TouchApp,
                        primary = false,
                        onClick = { handleTapTempo() },
                        modifier = Modifier.weight(0.45f),
                    )
                    MonoActionButton(
                        text = if (running) "Stop" else "Start",
                        icon = if (running) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        onClick = {
                            running = !running
                            if (!running) practiceElapsedMs = 0L
                        },
                        modifier = Modifier.weight(0.55f),
                    )
                }
            }
        }
    }
}
