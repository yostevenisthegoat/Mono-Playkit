package com.example.feature.puzzles

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.ui.theme.StatusSuccess

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PyraminxSolverScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback

    var state by remember { mutableStateOf(PyraminxState()) }
    var lastScramble by remember { mutableStateOf<List<String>>(emptyList()) }
    var solution by remember { mutableStateOf<List<String>?>(emptyList()) }
    var stepIdx by remember { mutableIntStateOf(0) }

    val tipNames = remember { listOf("u", "l", "r", "b") }
    val centerNames = remember { listOf("U", "L", "R", "B") }

    MonoToolScaffold(
        title = "Pyraminx Solver",
        subtitle = "Optimal Bidirectional BFS Solver",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(
                onClick = {
                    state = PyraminxState()
                    lastScramble = emptyList()
                    solution = emptyList()
                    stepIdx = 0
                },
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                MonoCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Pyraminx State", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                        Text(
                            text = if (state.isSolved()) "SOLVED" else "SCRAMBLED",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.isSolved()) StatusSuccess else palette.textSecondary,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                    ) {
                        val cx = size.width / 2f
                        val h = size.height * 0.85f
                        val halfBase = h * 0.58f
                        val topY = size.height * 0.08f
                        val p = Path().apply {
                            moveTo(cx, topY)
                            lineTo(cx - halfBase, topY + h)
                            lineTo(cx + halfBase, topY + h)
                            close()
                        }
                        drawPath(p, color = palette.surfaceSecondary)
                    }
                    if (lastScramble.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Applied moves: ${lastScramble.joinToString(" ")}",
                            style = MonoTabularStyle.copy(fontSize = 13.sp),
                            color = palette.textSecondary,
                        )
                    }
                }
            }

            // Interactive Move Buttons to configure your physical Pyraminx state
            item {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Text("Configure via Moves (or Scramble)", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        (centerNames.flatMap { listOf(it, "$it'") } + tipNames.flatMap { listOf(it, "$it'") }).forEach { m ->
                            MonoPillTag(
                                label = m,
                                onClick = {
                                    state = state.copy().applyMove(m)
                                    lastScramble = lastScramble + m
                                    solution = null
                                    stepIdx = 0
                                    feedback.play(SoundCue.CLICK)
                                },
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MonoActionButton(
                        text = "Scramble",
                        icon = Icons.Rounded.Casino,
                        primary = false,
                        onClick = {
                            val scr = PyraminxState.randomScramble()
                            val next = PyraminxState()
                            scr.forEach { next.applyMove(it) }
                            state = next
                            lastScramble = scr
                            solution = null
                            stepIdx = 0
                            feedback.play(SoundCue.CLICK)
                        },
                        modifier = Modifier.weight(0.42f),
                    )
                    MonoActionButton(
                        text = "Solve Optimal",
                        icon = Icons.Rounded.AutoFixHigh,
                        onClick = {
                            solution = PyraminxState.solve(state)
                            stepIdx = 0
                            feedback.play(SoundCue.COMPLETE)
                        },
                        modifier = Modifier.weight(0.58f),
                    )
                }
            }

            solution?.let { sol ->
                item {
                    MonoCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (sol.isEmpty()) "Pyraminx is already solved!" else "Optimal Solution (${sol.size} moves)",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                        )
                        if (sol.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                sol.forEachIndexed { idx, move ->
                                    val done = idx < stepIdx
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (done) palette.accentPrimary else palette.surfaceSecondary)
                                            .clickable { stepIdx = idx + 1 }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                    ) {
                                        Text(
                                            text = move,
                                            style = MonoTabularStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                            color = if (done) {
                                                if (palette.isDark) Color(0xFF0A0A0A) else Color.White
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkewbSolverScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback

    var state by remember { mutableStateOf(SkewbState()) }
    var lastScramble by remember { mutableStateOf<List<String>>(emptyList()) }
    var solution by remember { mutableStateOf<List<String>?>(emptyList()) }
    var stepIdx by remember { mutableIntStateOf(0) }

    MonoToolScaffold(
        title = "Skewb Solver",
        subtitle = "Optimal Bidirectional BFS Solver",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(
                onClick = {
                    state = SkewbState()
                    lastScramble = emptyList()
                    solution = emptyList()
                    stepIdx = 0
                },
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                MonoCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Skewb State", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                        Text(
                            text = if (state.isSolved()) "SOLVED" else "SCRAMBLED",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.isSolved()) StatusSuccess else palette.textSecondary,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (lastScramble.isEmpty()) "Apply Skewb moves below or scramble to compute an optimal solution." else "Moves applied: ${lastScramble.joinToString(" ")}",
                        style = MonoTabularStyle.copy(fontSize = 13.sp),
                        color = palette.textSecondary,
                    )
                }
            }

            item {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Text("WCA Skewb Moves (U, R, L, B)", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf("R", "R'", "L", "L'", "U", "U'", "B", "B'").forEach { m ->
                            MonoPillTag(
                                label = m,
                                onClick = {
                                    state = state.copy().applyMove(m)
                                    lastScramble = lastScramble + m
                                    solution = null
                                    stepIdx = 0
                                    feedback.play(SoundCue.CLICK)
                                },
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MonoActionButton(
                        text = "Scramble",
                        icon = Icons.Rounded.Casino,
                        primary = false,
                        onClick = {
                            val scr = SkewbState.randomScramble(10)
                            val next = SkewbState()
                            scr.forEach { next.applyMove(it) }
                            state = next
                            lastScramble = scr
                            solution = null
                            stepIdx = 0
                            feedback.play(SoundCue.CLICK)
                        },
                        modifier = Modifier.weight(0.42f),
                    )
                    MonoActionButton(
                        text = "Solve Optimal",
                        icon = Icons.Rounded.AutoFixHigh,
                        onClick = {
                            solution = SkewbState.solve(state)
                            stepIdx = 0
                            feedback.play(SoundCue.COMPLETE)
                        },
                        modifier = Modifier.weight(0.58f),
                    )
                }
            }

            solution?.let { sol ->
                item {
                    MonoCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (sol.isEmpty()) "Skewb is already solved!" else "Optimal Solution (${sol.size} moves)",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                        )
                        if (sol.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                sol.forEachIndexed { idx, move ->
                                    val done = idx < stepIdx
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (done) palette.accentPrimary else palette.surfaceSecondary)
                                            .clickable { stepIdx = idx + 1 }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                    ) {
                                        Text(
                                            text = move,
                                            style = MonoTabularStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                            color = if (done) {
                                                if (palette.isDark) Color(0xFF0A0A0A) else Color.White
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
    }
}
