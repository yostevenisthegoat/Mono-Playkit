package com.example.feature.boardgames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BingoCallerScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback

    var maxBall by rememberSaveable { mutableIntStateOf(75) }
    var autoCall by rememberSaveable { mutableStateOf(false) }
    val pool = remember(maxBall) { mutableStateListOf<Int>().apply { addAll((1..maxBall).shuffled()) } }
    val called = remember(maxBall) { mutableStateListOf<Int>() }

    fun drawNext() {
        if (pool.isEmpty()) {
            autoCall = false
            feedback.play(SoundCue.COMPLETE)
            return
        }
        val next = pool.removeAt(0)
        called.add(0, next)
        feedback.play(SoundCue.ACCENT_TICK)
    }

    fun resetGame() {
        autoCall = false
        pool.clear()
        pool.addAll((1..maxBall).shuffled())
        called.clear()
        feedback.play(SoundCue.CLICK)
    }

    LaunchedEffect(autoCall, maxBall) {
        while (autoCall) {
            delay(4000L)
            if (pool.isEmpty()) {
                autoCall = false
            } else {
                drawNext()
            }
        }
    }

    val currentBall = called.firstOrNull()
    val bingoLetter = remember(currentBall, maxBall) {
        if (currentBall == null || maxBall != 75) "" else when (currentBall) {
            in 1..15 -> "B"
            in 16..30 -> "I"
            in 31..45 -> "N"
            in 46..60 -> "G"
            else -> "O"
        }
    }

    MonoToolScaffold(
        title = "Bingo Number Caller",
        subtitle = "Zero-Duplicate Random Caller",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(onClick = { resetGame() }) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset caller", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonoPillTag(label = "75-Ball (BINGO)", selected = maxBall == 75, onClick = { maxBall = 75; autoCall = false })
                    MonoPillTag(label = "90-Ball (UK)", selected = maxBall == 90, onClick = { maxBall = 90; autoCall = false })
                }
            }

            item {
                MonoCard(cornerRadius = 26.dp, contentPadding = PaddingValues(26.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(136.dp)
                                .clip(CircleShape)
                                .background(palette.surfaceSecondary)
                                .border(2.dp, palette.accentPrimary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (bingoLetter.isNotEmpty()) {
                                    Text(
                                        text = bingoLetter,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = palette.textSecondary,
                                    )
                                }
                                Text(
                                    text = currentBall?.toString() ?: "—",
                                    style = MonoTabularStyle.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold),
                                    color = palette.textPrimary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Called ${called.size} / $maxBall • Remaining ${pool.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
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
                        text = if (autoCall) "Stop Auto" else "Auto (4s)",
                        icon = if (autoCall) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        primary = false,
                        enabled = pool.isNotEmpty(),
                        onClick = { autoCall = !autoCall },
                        modifier = Modifier.weight(0.42f),
                    )
                    MonoActionButton(
                        text = if (pool.isEmpty()) "All Called" else "Call Next",
                        icon = Icons.Rounded.Casino,
                        enabled = pool.isNotEmpty(),
                        onClick = { drawNext() },
                        modifier = Modifier.weight(0.58f),
                    )
                }
            }

            if (called.isNotEmpty()) {
                item {
                    Text("Called History (${called.size})", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        called.forEachIndexed { idx, num ->
                            val prefix = if (maxBall == 75) {
                                when (num) {
                                    in 1..15 -> "B"
                                    in 16..30 -> "I"
                                    in 31..45 -> "N"
                                    in 46..60 -> "G"
                                    else -> "O"
                                }
                            } else ""
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (idx == 0) palette.accentPrimary else palette.cardElevated)
                                    .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = "$prefix$num",
                                    style = MonoTabularStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                    color = if (idx == 0) {
                                        if (palette.isDark) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color.White
                                    } else {
                                        palette.textPrimary
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
