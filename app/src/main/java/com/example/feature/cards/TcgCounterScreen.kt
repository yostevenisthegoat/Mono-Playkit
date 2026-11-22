package com.example.feature.cards

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme

private data class TcgPlayerState(
    val name: String,
    val life: Int,
    val damage: Int = 0,
    val energy: Int = 0,
    val statuses: Set<String> = emptySet(),
)

private val statusOptions = listOf("Poisoned", "Burned", "Stunned", "Shielded", "Tapped")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TcgCounterScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback

    val startingLife = rememberSaveable { mutableIntStateOf(20) }
    val turnNumber = rememberSaveable { mutableIntStateOf(1) }
    val players = androidx.compose.runtime.remember {
        mutableStateListOf(
            TcgPlayerState("Player 1", 20),
            TcgPlayerState("Player 2", 20),
        )
    }

    fun applyFormat(life: Int) {
        startingLife.intValue = life
        turnNumber.intValue = 1
        for (i in players.indices) {
            players[i] = players[i].copy(life = life, damage = 0, energy = 0, statuses = emptySet())
        }
        feedback.play(SoundCue.CLICK)
    }

    MonoToolScaffold(
        title = "TCG Life & Status",
        subtitle = "Life, Energy, Damage & Turn Tracker",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(
                onClick = {
                    if (players.size < 4) players.add(TcgPlayerState("Player ${players.size + 1}", startingLife.intValue))
                },
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add player", tint = palette.textPrimary)
            }
            IconButton(onClick = { applyFormat(startingLife.intValue) }) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset game", tint = palette.textSecondary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(20 to "20 HP (Standard)", 40 to "40 HP (Commander)", 8000 to "8000 LP").forEach { (hp, label) ->
                        MonoPillTag(label = label, selected = startingLife.intValue == hp, onClick = { applyFormat(hp) })
                    }
                }
            }

            item {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("TURN COUNTER", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                            Text(
                                text = "Turn ${turnNumber.intValue}",
                                style = MonoTabularStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (turnNumber.intValue > 1) turnNumber.intValue-- }) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Prev turn", tint = palette.textSecondary)
                            }
                            MonoPillTag(label = "Next Turn", onClick = { turnNumber.intValue++; feedback.play(SoundCue.TICK) })
                        }
                    }
                }
            }

            itemsIndexed(players) { idx, p ->
                val stepBig = if (startingLife.intValue >= 1000) 500 else 5
                val stepSmall = if (startingLife.intValue >= 1000) 100 else 1

                MonoCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(p.name, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                        Text(
                            text = "${p.life}",
                            style = MonoTabularStyle.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(-stepBig, -stepSmall, stepSmall, stepBig).forEach { delta ->
                            MonoPillTag(
                                label = if (delta > 0) "+$delta" else "$delta",
                                onClick = {
                                    players[idx] = p.copy(life = (p.life + delta).coerceAtLeast(0))
                                    feedback.play(SoundCue.CLICK)
                                },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("DMG: ${p.damage}", style = MonoTabularStyle.copy(fontSize = 13.sp), color = palette.textSecondary)
                            MonoPillTag(label = "+1", onClick = { players[idx] = p.copy(damage = p.damage + 1) })
                            MonoPillTag(label = "-1", onClick = { players[idx] = p.copy(damage = (p.damage - 1).coerceAtLeast(0)) })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("NRG: ${p.energy}", style = MonoTabularStyle.copy(fontSize = 13.sp), color = palette.textSecondary)
                            MonoPillTag(label = "+1", onClick = { players[idx] = p.copy(energy = p.energy + 1) })
                            MonoPillTag(label = "-1", onClick = { players[idx] = p.copy(energy = (p.energy - 1).coerceAtLeast(0)) })
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        statusOptions.forEach { st ->
                            val active = st in p.statuses
                            MonoPillTag(
                                label = st,
                                selected = active,
                                onClick = {
                                    val nextSet = if (active) p.statuses - st else p.statuses + st
                                    players[idx] = p.copy(statuses = nextSet)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
