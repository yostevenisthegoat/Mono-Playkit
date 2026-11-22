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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.core.formatClockMillis
import com.example.data.model.ToolCatalog
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class TrickEntry(
    val name: String,
    val level: String,
    val tip: String,
    val count: Int = 0,
    val mastered: Boolean = false,
)

private val defaultYoYoTricks = listOf(
    TrickEntry("Sleeper", "Beginner", "Throw down hard with palm up and hold steady spin."),
    TrickEntry("Walk the Dog", "Beginner", "Let a spinning sleeper gently touch a smooth floor."),
    TrickEntry("Forward Pass", "Beginner", "Swing out forward and tug cleanly back to palm."),
    TrickEntry("Trapeze", "Intermediate", "Breakaway throw landing the yo-yo onto the string."),
    TrickEntry("Brain Twister", "Intermediate", "Mount from front sleeper and flip twice over your finger."),
    TrickEntry("Bind Return", "Intermediate", "Pinch slack loop on unresponsive play to snap back."),
    TrickEntry("Double or Nothing", "Advanced", "Wrap string twice around both index fingers before landing."),
)

private val defaultKendamaTricks = listOf(
    TrickEntry("Big Cup", "Beginner", "Pull straight up with knees and cushion the landing."),
    TrickEntry("Small Cup", "Beginner", "Keep tama hole visible while catching on the small cup."),
    TrickEntry("Base Cup", "Beginner", "Flip ken grip and catch gently with soft knees."),
    TrickEntry("Spike", "Beginner", "Spin tama slightly or track bevel straight onto spike."),
    TrickEntry("Around Japan", "Intermediate", "Small cup → Big cup → Spike in one continuous sequence."),
    TrickEntry("Airplane", "Intermediate", "Hold the tama and swing the ken straight onto the spike."),
    TrickEntry("Lighthouse", "Advanced", "Balance ken vertically on top of tama for 3 seconds."),
    TrickEntry("Lunar", "Advanced", "Rest sarado horizontally balanced atop the tama."),
)

@Composable
fun YoYoTrackerScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    SkillToyTrackerContent(
        toolId = ToolCatalog.TOOL_YOYO_TRACKER,
        title = "Yo-Yo Practice Tracker",
        subtitle = "Trick Counter, Practice Timer & Records",
        initialTricks = defaultYoYoTricks,
        isFavorite = isFavorite,
        onBack = onBack,
        onToggleFavorite = onToggleFavorite,
    )
}

@Composable
fun KendamaTrackerScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    SkillToyTrackerContent(
        toolId = ToolCatalog.TOOL_KENDAMA_TRACKER,
        title = "Kendama Guide & Tracker",
        subtitle = "Trick Reference, Landings & Practice Timer",
        initialTricks = defaultKendamaTricks,
        isFavorite = isFavorite,
        onBack = onBack,
        onToggleFavorite = onToggleFavorite,
    )
}

@Composable
private fun SkillToyTrackerContent(
    toolId: String,
    title: String,
    subtitle: String,
    initialTricks: List<TrickEntry>,
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

    val records by repo.observeHistoryByTool(toolId).collectAsStateWithLifecycle(initialValue = emptyList())
    val tricks = remember { mutableStateListOf<TrickEntry>().apply { addAll(initialTricks) } }

    var practicing by remember { mutableStateOf(false) }
    var startElapsed by remember { mutableLongStateOf(0L) }
    var accumulatedMs by remember { mutableLongStateOf(0L) }
    var displayedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(practicing, startElapsed, accumulatedMs) {
        while (practicing) {
            displayedMs = accumulatedMs + (SystemClock.elapsedRealtime() - startElapsed)
            delay(100L)
        }
        displayedMs = accumulatedMs
    }

    val totalLanded = tricks.sumOf { it.count }
    val masteredCount = tricks.count { it.mastered }

    MonoToolScaffold(
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(
                onClick = {
                    val duration = if (practicing) accumulatedMs + (SystemClock.elapsedRealtime() - startElapsed) else accumulatedMs
                    scope.launch {
                        repo.recordHistory(
                            toolId = toolId,
                            category = "Physical Toys",
                            title = title,
                            summary = "$totalLanded tricks landed • ${formatClockMillis(duration)}",
                            detail = "Mastered: $masteredCount / ${tricks.size}",
                            metricMs = duration,
                        )
                        feedback.play(SoundCue.COMPLETE)
                        Toast.makeText(context, "Practice session saved", Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Icon(Icons.Rounded.Save, contentDescription = "Save practice", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonoCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("PRACTICE TIMER", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                            Text(
                                text = formatClockMillis(displayedMs),
                                style = MonoTabularStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                            Text(
                                text = "$totalLanded landed • $masteredCount/${tricks.size} mastered",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary,
                            )
                        }
                        MonoActionButton(
                            text = if (practicing) "Pause" else "Start",
                            icon = if (practicing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            onClick = {
                                if (practicing) {
                                    accumulatedMs += SystemClock.elapsedRealtime() - startElapsed
                                    practicing = false
                                } else {
                                    startElapsed = SystemClock.elapsedRealtime()
                                    practicing = true
                                }
                                feedback.play(SoundCue.CLICK)
                            },
                        )
                    }
                }
            }

            items(tricks.indices.toList()) { idx ->
                val trick = tricks[idx]
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                tricks[idx] = trick.copy(mastered = !trick.mastered)
                                feedback.play(SoundCue.TICK)
                            },
                        ) {
                            Icon(
                                imageVector = if (trick.mastered) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = "Toggle mastered",
                                tint = if (trick.mastered) StatusSuccess else palette.textMuted,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${trick.name} (${trick.level})", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                            Text(trick.tip, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${trick.count}",
                                style = MonoTabularStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                            IconButton(
                                onClick = {
                                    tricks[idx] = trick.copy(count = trick.count + 1)
                                    feedback.play(SoundCue.CLICK)
                                },
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add landing", tint = palette.textPrimary)
                            }
                        }
                    }
                }
            }

            if (records.isNotEmpty()) {
                item {
                    Text("Personal Records", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
                items(records.take(10), key = { it.id }) { rec ->
                    MonoCard(cornerRadius = 14.dp, contentPadding = PaddingValues(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(rec.summary, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Text(rec.detail, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                }
            }
        }
    }
}
