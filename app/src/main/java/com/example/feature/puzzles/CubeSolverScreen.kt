package com.example.feature.puzzles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess

private val cubeFaceNames = listOf("Yellow (U)", "Red (R)", "Green (F)", "White (D)", "Orange (L)", "Blue (B)")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CubeSolverScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback

    var stickers by remember { mutableStateOf(CubeState.solvedArray()) }
    var selectedColor by remember { mutableIntStateOf(0) }
    var solveResult by remember { mutableStateOf<CubeSolveResult?>(null) }
    var playbackStep by remember { mutableIntStateOf(0) }

    val validationError = remember(stickers) { CubeSolverEngine.validate(stickers) }

    val playbackState = remember(stickers, solveResult, playbackStep) {
        val res = solveResult
        if (res != null && res.isValid && res.allMoves.isNotEmpty()) {
            val s = CubeState(stickers.copyOf())
            val steps = res.allMoves.take(playbackStep.coerceIn(0, res.allMoves.size))
            s.applyMoves(steps)
            s.stickers
        } else {
            stickers
        }
    }

    fun applyScramble() {
        val scr = CubeState.generateWcaScramble(18)
        stickers = CubeState().applyMoves(scr).stickers
        solveResult = null
        playbackStep = 0
        feedback.play(SoundCue.CLICK)
    }

    MonoToolScaffold(
        title = "Rubik's Cube Solver",
        subtitle = "Interactive Input & Step-by-Step Solver",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(
                onClick = {
                    stickers = CubeState.solvedArray()
                    solveResult = null
                    playbackStep = 0
                },
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset solved cube", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Color Palette Selector
            item {
                MonoCard(cornerRadius = 18.dp, contentPadding = PaddingValues(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1. Select Color • Tap Stickers Below",
                        style = MaterialTheme.typography.titleSmall,
                        color = palette.textPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        wcaCubeColors.forEachIndexed { idx, col ->
                            val isSelected = selectedColor == idx
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) palette.textPrimary else palette.border,
                                        shape = CircleShape,
                                    )
                                    .clickable { selectedColor = idx },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Active brush: ${cubeFaceNames[selectedColor]} (center stickers define face)",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                    )
                }
            }

            // Interactive 6-Face Unfolded Cube Net
            item {
                MonoCard(cornerRadius = 22.dp, contentPadding = PaddingValues(16.dp), modifier = Modifier.fillMaxWidth()) {
                    InteractiveCubeNet(
                        stickers = if (solveResult?.isValid == true && playbackStep > 0) playbackState else stickers,
                        interactive = solveResult == null || playbackStep == 0,
                        onStickerTap = { idx ->
                            val updated = stickers.copyOf()
                            updated[idx] = selectedColor
                            stickers = updated
                            solveResult = null
                            playbackStep = 0
                            feedback.vibrateLight(10L)
                        },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = validationError ?: "✓ Valid cube configuration ready to solve",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (validationError == null) StatusSuccess else StatusError,
                    )
                }
            }

            // Actions: Scramble + Solve
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MonoActionButton(
                        text = "Scramble",
                        icon = Icons.Rounded.Casino,
                        primary = false,
                        onClick = { applyScramble() },
                        modifier = Modifier.weight(0.42f),
                    )
                    MonoActionButton(
                        text = "Solve Cube",
                        icon = Icons.Rounded.AutoFixHigh,
                        enabled = validationError == null,
                        onClick = {
                            solveResult = CubeSolverEngine.solve(stickers)
                            playbackStep = 0
                            feedback.play(SoundCue.COMPLETE)
                        },
                        testTag = "solve_cube_button",
                        modifier = Modifier.weight(0.58f),
                    )
                }
            }

            // Solution Playback & Phases
            solveResult?.let { res ->
                if (!res.isValid) {
                    item {
                        MonoCard(cornerRadius = 18.dp, borderColor = StatusError, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = res.errorMessage ?: "Invalid cube state",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StatusError,
                            )
                        }
                    }
                } else if (res.allMoves.isEmpty()) {
                    item {
                        MonoCard(cornerRadius = 18.dp, borderColor = StatusSuccess, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = StatusSuccess)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cube is already solved!", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            }
                        }
                    }
                } else {
                    item {
                        MonoCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text("STEP PLAYBACK", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                                    Text(
                                        text = "Move $playbackStep / ${res.allMoves.size}",
                                        style = MonoTabularStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                        color = palette.textPrimary,
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = { if (playbackStep > 0) playbackStep-- },
                                        enabled = playbackStep > 0,
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = "Previous move", tint = palette.textPrimary)
                                    }
                                    IconButton(
                                        onClick = { if (playbackStep < res.allMoves.size) playbackStep++ },
                                        enabled = playbackStep < res.allMoves.size,
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.NavigateNext, contentDescription = "Next move", tint = palette.textPrimary)
                                    }
                                }
                            }
                            if (playbackStep > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Applied: ${res.allMoves[playbackStep - 1]}",
                                    style = MonoTabularStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = StatusSuccess,
                                )
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Next: ${res.allMoves.first()} (Hold Green front, Yellow top)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary,
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                res.allMoves.forEachIndexed { idx, move ->
                                    val done = idx < playbackStep
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (done) palette.accentPrimary else palette.surfaceSecondary)
                                            .clickable { playbackStep = idx + 1 }
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                    ) {
                                        Text(
                                            text = move,
                                            style = MonoTabularStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
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

                    items(res.phases) { phase ->
                        MonoCard(cornerRadius = 16.dp, contentPadding = PaddingValues(14.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(phase.title, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = phase.moves.joinToString(" "),
                                style = MonoTabularStyle.copy(fontSize = 14.sp),
                                color = palette.textSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveCubeNet(
    stickers: IntArray,
    interactive: Boolean,
    onStickerTap: (Int) -> Unit,
) {
    // Unfolded 4 cols x 3 rows grid of faces
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Row 0: [empty, U(0), empty, empty]
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            FaceGrid(0, "U", stickers, interactive, onStickerTap, Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
        }
        // Row 1: [L(4), F(2), R(1), B(5)]
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            FaceGrid(4, "L", stickers, interactive, onStickerTap, Modifier.weight(1f))
            FaceGrid(2, "F", stickers, interactive, onStickerTap, Modifier.weight(1f))
            FaceGrid(1, "R", stickers, interactive, onStickerTap, Modifier.weight(1f))
            FaceGrid(5, "B", stickers, interactive, onStickerTap, Modifier.weight(1f))
        }
        // Row 2: [empty, D(3), empty, empty]
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            FaceGrid(3, "D", stickers, interactive, onStickerTap, Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FaceGrid(
    faceIdx: Int,
    faceLabel: String,
    stickers: IntArray,
    interactive: Boolean,
    onStickerTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        for (r in 0..2) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                for (c in 0..2) {
                    val idx = faceIdx * 9 + r * 3 + c
                    val color = wcaCubeColors.getOrElse(stickers[idx]) { Color.Gray }
                    val isCenter = r == 1 && c == 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                            .border(1.dp, palette.bgMain, RoundedCornerShape(4.dp))
                            .let {
                                if (interactive) it.clickable { onStickerTap(idx) } else it
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isCenter) {
                            Text(
                                text = faceLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black.copy(alpha = 0.65f),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
