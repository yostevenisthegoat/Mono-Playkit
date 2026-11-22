package com.example.feature.dice

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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTheme

private enum class TeamMode(val label: String) {
    PICK_ONE("Pick Player"),
    TURN_ORDER("Turn Order"),
    TEAMS("Split Teams"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamGeneratorScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback

    val players = remember { mutableStateListOf("Player 1", "Player 2", "Player 3", "Player 4") }
    var newPlayerName by remember { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf(TeamMode.PICK_ONE) }
    var teamCount by rememberSaveable { mutableIntStateOf(2) }

    var pickedPlayer by remember { mutableStateOf<String?>(null) }
    var turnOrder by remember { mutableStateOf<List<String>>(emptyList()) }
    var teamsResult by remember { mutableStateOf<List<List<String>>>(emptyList()) }

    fun addPlayer() {
        val clean = newPlayerName.trim()
        if (clean.isNotEmpty()) {
            players.add(clean)
            newPlayerName = ""
        }
    }

    fun runRandomizer() {
        if (players.isEmpty()) return
        feedback.play(SoundCue.CLICK)
        when (mode) {
            TeamMode.PICK_ONE -> {
                pickedPlayer = players.random()
            }
            TeamMode.TURN_ORDER -> {
                turnOrder = players.shuffled()
            }
            TeamMode.TEAMS -> {
                val shuffled = players.shuffled()
                val groups = List(teamCount.coerceIn(2, players.size.coerceAtLeast(2))) { mutableListOf<String>() }
                shuffled.forEachIndexed { idx, name ->
                    groups[idx % groups.size].add(name)
                }
                teamsResult = groups
            }
        }
    }

    MonoToolScaffold(
        title = "Players, Turns & Teams",
        subtitle = "Selector, Turn Order & Team Splitter",
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TeamMode.entries.forEach { m ->
                        MonoPillTag(
                            label = m.label,
                            selected = mode == m,
                            onClick = { mode = m },
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        label = { Text("Add player name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { addPlayer() }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add player", tint = palette.textPrimary)
                    }
                }
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    players.forEachIndexed { idx, name ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(name) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { if (players.size > 1) players.removeAt(idx) },
                                    modifier = Modifier.height(18.dp),
                                ) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Remove $name")
                                }
                            },
                        )
                    }
                }
            }

            if (mode == TeamMode.TEAMS) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Teams:", style = MaterialTheme.typography.titleSmall, color = palette.textSecondary)
                        listOf(2, 3, 4).forEach { count ->
                            MonoPillTag(label = "$count", selected = teamCount == count, onClick = { teamCount = count })
                        }
                    }
                }
            }

            item {
                MonoActionButton(
                    text = when (mode) {
                        TeamMode.PICK_ONE -> "Pick Random Player"
                        TeamMode.TURN_ORDER -> "Shuffle Turn Order"
                        TeamMode.TEAMS -> "Generate $teamCount Teams"
                    },
                    icon = Icons.Rounded.Shuffle,
                    onClick = { runRandomizer() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (mode == TeamMode.PICK_ONE && pickedPlayer != null) {
                item {
                    MonoCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                        Text("SELECTED PLAYER", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = pickedPlayer.orEmpty(),
                            style = MaterialTheme.typography.displayMedium,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (mode == TeamMode.TURN_ORDER && turnOrder.isNotEmpty()) {
                itemsIndexed(turnOrder) { index, name ->
                    MonoCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("#${index + 1}", style = MaterialTheme.typography.titleMedium, color = palette.textMuted)
                            Text(name, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                        }
                    }
                }
            }

            if (mode == TeamMode.TEAMS && teamsResult.isNotEmpty()) {
                itemsIndexed(teamsResult) { index, team ->
                    MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                        Text("Team ${index + 1}", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (team.isEmpty()) "—" else team.joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
