package com.example.feature.toys

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import kotlin.math.roundToInt

@Composable
fun FidgetSpinnerScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val container = LocalAppContainer.current
    val feedback = container.feedback
    val repo = container.repository
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val savedSpins by repo.observeHistoryByTool(ToolCatalog.TOOL_FIDGET_SPINNER)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var spinning by remember { mutableStateOf(false) }
    var spinStartElapsed by remember { mutableLongStateOf(0L) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var estimatedRpm by remember { mutableIntStateOf(0) }
    val tapTimestamps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(spinning, spinStartElapsed) {
        while (spinning) {
            elapsedMs = SystemClock.elapsedRealtime() - spinStartElapsed
            delay(35L)
        }
    }

    fun recordRevolutionTap() {
        val now = SystemClock.elapsedRealtime()
        tapTimestamps.add(now)
        if (tapTimestamps.size > 6) tapTimestamps.removeAt(0)
        if (tapTimestamps.size >= 2) {
            val span = (tapTimestamps.last() - tapTimestamps.first()).coerceAtLeast(1L)
            val intervals = tapTimestamps.size - 1
            val avgPeriodMs = span.toDouble() / intervals.toDouble()
            estimatedRpm = (60_000.0 / avgPeriodMs).roundToInt().coerceIn(1, 9999)
        }
        feedback.vibrateLight(10L)
    }

    val bestSpinMs = remember(savedSpins) { savedSpins.mapNotNull { it.metricMs }.maxOrNull() }

    MonoToolScaffold(
        title = "Fidget Spinner Tracker",
        subtitle = "Spin Duration & Cadence RPM Estimator",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                MonoCard(cornerRadius = 26.dp, contentPadding = PaddingValues(28.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("SPIN DURATION", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatDurationMillis(elapsedMs),
                            style = MonoTabularStyle.copy(fontSize = 54.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("EST. RPM", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                                Text(
                                    text = if (estimatedRpm > 0) "$estimatedRpm" else "—",
                                    style = MonoTabularStyle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                                    color = palette.textPrimary,
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BEST SPIN", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                                Text(
                                    text = bestSpinMs?.let { formatDurationMillis(it) } ?: "—",
                                    style = MonoTabularStyle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                                    color = palette.textPrimary,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MonoActionButton(
                        text = if (spinning) "Stop & Save" else "Start Spin",
                        icon = if (spinning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        onClick = {
                            if (!spinning) {
                                spinStartElapsed = SystemClock.elapsedRealtime()
                                elapsedMs = 0L
                                estimatedRpm = 0
                                tapTimestamps.clear()
                                spinning = true
                                feedback.play(SoundCue.ACCENT_TICK)
                            } else {
                                val finalMs = SystemClock.elapsedRealtime() - spinStartElapsed
                                elapsedMs = finalMs
                                spinning = false
                                feedback.play(SoundCue.COMPLETE)
                                scope.launch {
                                    repo.recordHistory(
                                        toolId = ToolCatalog.TOOL_FIDGET_SPINNER,
                                        category = "Physical Toys",
                                        title = "Fidget Spinner Run",
                                        summary = formatDurationMillis(finalMs),
                                        detail = if (estimatedRpm > 0) "Peak measured cadence ~$estimatedRpm RPM" else "Duration recorded",
                                        metricMs = finalMs,
                                    )
                                    Toast.makeText(context, "Spin saved", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    MonoActionButton(
                        text = "Tap Rev",
                        icon = Icons.Rounded.Loop,
                        primary = false,
                        enabled = spinning,
                        onClick = { recordRevolutionTap() },
                        modifier = Modifier.weight(0.75f),
                    )
                }
            }

            if (savedSpins.isNotEmpty()) {
                item {
                    Text("Saved Spins", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
                items(savedSpins.take(15), key = { it.id }) { spin ->
                    MonoCard(cornerRadius = 16.dp, contentPadding = PaddingValues(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(spin.detail, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                            Text(
                                text = spin.summary,
                                style = MonoTabularStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}
