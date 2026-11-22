package com.example.feature.boardgames

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.data.model.ToolCatalog
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.launch

private data class DominoPlayer(
    val name: String,
    val score: Int,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DominoesScreen(
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

    val players = remember { mutableStateListOf(DominoPlayer("Player 1", 0), DominoPlayer("Player 2", 0)) }
    var targetScore by rememberSaveable { mutableIntStateOf(100) }
    val roundHistory = remember { mutableStateListOf<String>() }
    var addPointsIdx by remember { mutableStateOf<Int?>(null) }

    addPointsIdx?.let { idx ->
        val p = players.getOrNull(idx)
        if (p != null) {
            var ptsStr by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { addPointsIdx = null },
                containerColor = palette.cardElevated,
                title = { Text("Add Round Points (${p.name})", color = palette.textPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = ptsStr,
                            onValueChange = { ptsStr = it.filter(Char::isDigit) },
                            label = { Text("Pip Total") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(5, 10, 15, 20, 25, 30).forEach { q ->
                                MonoPillTag(label = "+$q", onClick = { ptsStr = q.toString() })
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val pts = ptsStr.toIntOrNull() ?: 0
                            if (pts > 0) {
                                val nextScore = p.score + pts
                                players[idx] = p.copy(score = nextScore)
                                roundHistory.add(0, "Round ${roundHistory.size + 1}: ${p.name} +$pts (Total $nextScore)")
                                feedback.play(if (nextScore >= targetScore) SoundCue.COMPLETE else SoundCue.CLICK)
                            }
                            addPointsIdx = null
                        },
                    ) { Text("Add", color = palette.textPrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { addPointsIdx = null }) { Text("Cancel", color = palette.textSecondary) }
                },
            )
        }
    }

    MonoToolScaffold(
        title = "Dominoes Scoreboard",
        subtitle = "Pip Calculator & Round Log",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(
                onClick = {
                    if (players.size < 4) players.add(DominoPlayer("Player ${players.size + 1}", 0))
                },
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add player", tint = palette.textPrimary)
            }
            IconButton(
                onClick = {
                    scope.launch {
                        repo.recordHistory(
                            toolId = ToolCatalog.TOOL_DOMINOES,
                            category = "Board Games",
                            title = "Dominoes Match",
                            summary = players.joinToString(" • ") { "${it.name}: ${it.score}" },
                            detail = "${roundHistory.size} rounds • Target $targetScore",
                        )
                        feedback.play(SoundCue.COMPLETE)
                        Toast.makeText(context, "Dominoes match saved", Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Icon(Icons.Rounded.Save, contentDescription = "Save match", tint = palette.textPrimary)
            }
            IconButton(
                onClick = {
                    for (i in players.indices) players[i] = players[i].copy(score = 0)
                    roundHistory.clear()
                },
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset scores", tint = palette.textSecondary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Target:", style = MaterialTheme.typography.titleSmall, color = palette.textSecondary)
                    listOf(100, 150, 200, 250).forEach { target ->
                        MonoPillTag(label = "$target", selected = targetScore == target, onClick = { targetScore = target })
                    }
                }
            }

            itemsIndexed(players) { idx, player ->
                val won = player.score >= targetScore
                MonoCard(
                    onClick = { addPointsIdx = idx },
                    cornerRadius = 20.dp,
                    borderColor = if (won) StatusSuccess else palette.border,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = if (won) "${player.name} • WINNER" else player.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (won) StatusSuccess else palette.textPrimary,
                            )
                            Text("Tap to add round pips", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                        }
                        Text(
                            text = "${player.score}",
                            style = MonoTabularStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 15, 20).forEach { quick ->
                            MonoPillTag(
                                label = "+$quick",
                                onClick = {
                                    val next = player.score + quick
                                    players[idx] = player.copy(score = next)
                                    roundHistory.add(0, "Round ${roundHistory.size + 1}: ${player.name} +$quick (Total $next)")
                                    feedback.play(if (next >= targetScore) SoundCue.COMPLETE else SoundCue.CLICK)
                                },
                            )
                        }
                    }
                }
            }

            if (roundHistory.isNotEmpty()) {
                item {
                    Text("Round History", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
                items(roundHistory) { item ->
                    MonoCard(cornerRadius = 14.dp, contentPadding = PaddingValues(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(item, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                    }
                }
            }
        }
    }
}
