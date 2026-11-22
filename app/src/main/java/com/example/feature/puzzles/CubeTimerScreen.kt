package com.example.feature.puzzles

import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.KeepScreenOnEffect
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.core.formatDurationMillis
import com.example.data.model.ToolCatalog
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class CubeTimerPhase {
    IDLE,
    INSPECTION,
    ARMED,
    SOLVING,
}

@Composable
fun CubeTimerScreen(
    inspectionSeconds: Int,
    isFavorite: Boolean,
    keepScreenOn: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val container = LocalAppContainer.current
    val feedback = container.feedback
    val repo = container.repository
    val scope = rememberCoroutineScope()

    KeepScreenOnEffect(keepScreenOn)

    val solveHistory by repo.observeHistoryByTool(ToolCatalog.TOOL_CUBE_TIMER)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var scramble by remember { mutableStateOf(CubeState.generateWcaScramble(20)) }
    val scrambledState = remember(scramble) { CubeState().applyMoves(scramble) }

    var phase by remember { mutableStateOf(CubeTimerPhase.IDLE) }
    var inspectionStartElapsed by remember { mutableLongStateOf(0L) }
    var inspectionRemainingSec by remember { mutableIntStateOf(inspectionSeconds) }
    var solveStartElapsed by remember { mutableLongStateOf(0L) }
    var solveElapsedMs by remember { mutableLongStateOf(0L) }
    var lastSolveId by remember { mutableStateOf<Long?>(null) }
    var lastSolvePlus2 by remember { mutableStateOf(false) }
    var lastSolveDnf by remember { mutableStateOf(false) }

    // Inspection countdown loop
    LaunchedEffect(phase, inspectionSeconds) {
        if (phase == CubeTimerPhase.INSPECTION) {
            var warned8 = false
            var warned12 = false
            while (phase == CubeTimerPhase.INSPECTION) {
                val elapsedSec = ((SystemClock.elapsedRealtime() - inspectionStartElapsed) / 1000L).toInt()
                val rem = inspectionSeconds - elapsedSec
                inspectionRemainingSec = rem
                if (elapsedSec >= 8 && !warned8 && inspectionSeconds >= 12) {
                    warned8 = true
                    feedback.play(SoundCue.WARNING)
                }
                if (elapsedSec >= 12 && !warned12 && inspectionSeconds >= 15) {
                    warned12 = true
                    feedback.play(SoundCue.WARNING)
                }
                if (rem <= -2) {
                    // Auto-DNF if inspection exceeds +2
                    phase = CubeTimerPhase.IDLE
                    feedback.play(SoundCue.COMPLETE)
                    break
                }
                delay(100L)
            }
        }
    }

    // Solve timer loop
    LaunchedEffect(phase) {
        while (phase == CubeTimerPhase.SOLVING) {
            solveElapsedMs = SystemClock.elapsedRealtime() - solveStartElapsed
            delay(25L)
        }
    }

    fun startNewScramble() {
        scramble = CubeState.generateWcaScramble(20)
    }

    fun finishSolve(finalMs: Long, penaltyPlus2: Boolean = false) {
        solveElapsedMs = finalMs
        phase = CubeTimerPhase.IDLE
        lastSolvePlus2 = penaltyPlus2
        lastSolveDnf = false
        val effectiveMs = finalMs + if (penaltyPlus2) 2000L else 0L
        val scrambleStr = scramble.joinToString(" ")
        feedback.play(SoundCue.COMPLETE)
        scope.launch {
            repo.recordHistory(
                toolId = ToolCatalog.TOOL_CUBE_TIMER,
                category = "Puzzles",
                title = "3x3 Cube Solve",
                summary = formatDurationMillis(effectiveMs) + if (penaltyPlus2) " (+2)" else "",
                detail = scrambleStr,
                metricMs = effectiveMs,
            )
        }
        startNewScramble()
    }

    // Compute WCA statistics (PB, Ao5, Ao12)
    val solveTimes = remember(solveHistory) { solveHistory.mapNotNull { it.metricMs } }
    val pbMs = remember(solveTimes) { solveTimes.minOrNull() }
    val ao5Ms = remember(solveTimes) { computeWcaAverage(solveTimes.take(5), 5) }
    val ao12Ms = remember(solveTimes) { computeWcaAverage(solveTimes.take(12), 12) }

    MonoToolScaffold(
        title = "Rubik's Cube Timer",
        subtitle = if (inspectionSeconds > 0) "WCA ${inspectionSeconds}s Inspection" else "Instant Start",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(
                onClick = { startNewScramble() },
                enabled = phase == CubeTimerPhase.IDLE,
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "New scramble", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Scramble + 2D Net
            item {
                MonoCard(cornerRadius = 20.dp, contentPadding = PaddingValues(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = scramble.joinToString("  "),
                        style = MonoTabularStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        color = palette.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CubeNetCanvas(
                            stickers = scrambledState.stickers,
                            modifier = Modifier
                                .fillMaxWidth(0.62f)
                                .aspectRatio(4f / 3f),
                        )
                    }
                }
            }

            // Main Timer Pad
            item {
                val padBorder = when (phase) {
                    CubeTimerPhase.ARMED -> StatusSuccess
                    CubeTimerPhase.INSPECTION -> if (inspectionRemainingSec <= 3) StatusError else StatusWarning
                    CubeTimerPhase.SOLVING -> palette.accentPrimary
                    CubeTimerPhase.IDLE -> palette.border
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(palette.cardPrimary)
                        .border(2.dp, padBorder, RoundedCornerShape(26.dp))
                        .pointerInput(phase, inspectionSeconds) {
                            detectTapGestures(
                                onTap = {
                                    when (phase) {
                                        CubeTimerPhase.IDLE -> {
                                            if (inspectionSeconds > 0) {
                                                inspectionStartElapsed = SystemClock.elapsedRealtime()
                                                inspectionRemainingSec = inspectionSeconds
                                                phase = CubeTimerPhase.INSPECTION
                                                feedback.play(SoundCue.TICK)
                                            } else {
                                                solveStartElapsed = SystemClock.elapsedRealtime()
                                                solveElapsedMs = 0L
                                                phase = CubeTimerPhase.SOLVING
                                                feedback.play(SoundCue.ACCENT_TICK)
                                            }
                                        }
                                        CubeTimerPhase.INSPECTION -> {
                                            val penalty = inspectionRemainingSec < 0
                                            solveStartElapsed = SystemClock.elapsedRealtime()
                                            solveElapsedMs = 0L
                                            lastSolvePlus2 = penalty
                                            phase = CubeTimerPhase.SOLVING
                                            feedback.play(SoundCue.ACCENT_TICK)
                                        }
                                        CubeTimerPhase.SOLVING -> {
                                            val finalMs = SystemClock.elapsedRealtime() - solveStartElapsed
                                            finishSolve(finalMs, lastSolvePlus2)
                                        }
                                        CubeTimerPhase.ARMED -> Unit
                                    }
                                },
                                onPress = {
                                    if (phase == CubeTimerPhase.INSPECTION || (phase == CubeTimerPhase.IDLE && inspectionSeconds == 0)) {
                                        val prev = phase
                                        phase = CubeTimerPhase.ARMED
                                        feedback.vibrateLight(14L)
                                        val released = tryAwaitRelease()
                                        if (released && phase == CubeTimerPhase.ARMED) {
                                            solveStartElapsed = SystemClock.elapsedRealtime()
                                            solveElapsedMs = 0L
                                            phase = CubeTimerPhase.SOLVING
                                            feedback.play(SoundCue.ACCENT_TICK)
                                        } else if (!released && phase == CubeTimerPhase.ARMED) {
                                            phase = prev
                                        }
                                    }
                                },
                            )
                        }
                        .testTag("cube_timer_pad"),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (phase) {
                                CubeTimerPhase.IDLE -> if (inspectionSeconds > 0) "TAP FOR ${inspectionSeconds}s INSPECTION" else "TAP OR HOLD TO START"
                                CubeTimerPhase.INSPECTION -> "INSPECTING • HOLD OR TAP TO SOLVE"
                                CubeTimerPhase.ARMED -> "READY — RELEASE TO START"
                                CubeTimerPhase.SOLVING -> "TAP ANYWHERE TO STOP"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = when (phase) {
                                CubeTimerPhase.ARMED -> StatusSuccess
                                CubeTimerPhase.INSPECTION -> StatusWarning
                                else -> palette.textMuted
                            },
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = when (phase) {
                                CubeTimerPhase.INSPECTION -> if (inspectionRemainingSec >= 0) "${inspectionRemainingSec}s" else "+2"
                                CubeTimerPhase.ARMED -> "0.00"
                                else -> formatDurationMillis(solveElapsedMs)
                            },
                            style = MonoTabularStyle.copy(fontSize = 58.sp, fontWeight = FontWeight.Bold),
                            color = when (phase) {
                                CubeTimerPhase.ARMED -> StatusSuccess
                                CubeTimerPhase.INSPECTION -> if (inspectionRemainingSec <= 3) StatusError else StatusWarning
                                else -> palette.textPrimary
                            },
                        )
                    }
                }
            }

            // Session Statistics (PB, Ao5, Ao12, Count)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatTile("PB", pbMs?.let { formatDurationMillis(it) } ?: "—", Modifier.weight(1f))
                    StatTile("Ao5", ao5Ms?.let { formatDurationMillis(it) } ?: "—", Modifier.weight(1f))
                    StatTile("Ao12", ao12Ms?.let { formatDurationMillis(it) } ?: "—", Modifier.weight(1f))
                    StatTile("Solves", "${solveTimes.size}", Modifier.weight(0.8f))
                }
            }

            if (solveHistory.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Solve History", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                        MonoPillTag(
                            label = "Clear",
                            onClick = { scope.launch { repo.deleteHistoryForTool(ToolCatalog.TOOL_CUBE_TIMER) } },
                        )
                    }
                }
                items(solveHistory.take(20), key = { it.id }) { item ->
                    MonoCard(cornerRadius = 16.dp, contentPadding = PaddingValues(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.summary,
                                    style = MonoTabularStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = palette.textPrimary,
                                )
                                Text(
                                    text = item.detail,
                                    style = MonoTabularStyle.copy(fontSize = 11.sp),
                                    color = palette.textMuted,
                                )
                            }
                            IconButton(onClick = { scope.launch { repo.deleteHistoryItem(item.id) } }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete solve", tint = palette.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val palette = MonoTheme.palette
    MonoCard(
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MonoTabularStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                color = palette.textPrimary,
            )
        }
    }
}

private fun computeWcaAverage(times: List<Long>, n: Int): Long? {
    if (times.size < n) return null
    val sorted = times.sorted()
    val trimmed = sorted.subList(1, sorted.size - 1)
    return trimmed.sum() / trimmed.size
}

val wcaCubeColors = listOf(
    Color(0xFFFACC15), // 0 = U (Yellow in solver / top)
    Color(0xFFEF4444), // 1 = R (Red)
    Color(0xFF22C55E), // 2 = F (Green)
    Color(0xFFF8FAFC), // 3 = D (White)
    Color(0xFFF97316), // 4 = L (Orange)
    Color(0xFF3B82F6), // 5 = B (Blue)
)

@Composable
fun CubeNetCanvas(
    stickers: IntArray,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    Canvas(modifier = modifier) {
        val faceSize = minOf(size.width / 4f, size.height / 3f)
        val gap = faceSize * 0.08f
        val stickerSize = (faceSize - gap * 4f) / 3f

        fun drawFace(faceIdx: Int, gridCol: Int, gridRow: Int) {
            val ox = gridCol * faceSize + gap
            val oy = gridRow * faceSize + gap
            for (r in 0..2) {
                for (c in 0..2) {
                    val colorIdx = stickers[faceIdx * 9 + r * 3 + c]
                    val col = wcaCubeColors.getOrElse(colorIdx) { Color.Gray }
                    drawRoundRect(
                        color = col,
                        topLeft = Offset(ox + c * (stickerSize + gap), oy + r * (stickerSize + gap)),
                        size = Size(stickerSize, stickerSize),
                        cornerRadius = CornerRadius(stickerSize * 0.16f, stickerSize * 0.16f),
                    )
                }
            }
        }

        // Standard unfolded cross: U(1,0), L(0,1), F(1,1), R(2,1), B(3,1), D(1,2)
        drawFace(0, 1, 0)
        drawFace(4, 0, 1)
        drawFace(2, 1, 1)
        drawFace(1, 2, 1)
        drawFace(5, 3, 1)
        drawFace(3, 1, 2)
    }
}
