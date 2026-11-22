package com.example.feature.boardgames

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import kotlin.random.Random

private enum class BoardDiceGame(val label: String) {
    LUDO("Ludo"),
    SNAKES("Snakes & Ladders"),
}

@Composable
fun LudoSnakesScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback

    var game by rememberSaveable { mutableStateOf(BoardDiceGame.LUDO) }
    var twoDice by rememberSaveable { mutableStateOf(false) }
    val players = remember { mutableStateListOf("Player 1", "Player 2", "Player 3", "Player 4") }
    var activePlayer by rememberSaveable { mutableIntStateOf(0) }
    var consecutiveSixes by rememberSaveable { mutableIntStateOf(0) }
    var currentRoll by remember { mutableStateOf(listOf(6)) }
    val rollHistory = remember { mutableStateListOf<String>() }

    fun rollAndAdvance() {
        val count = if (game == BoardDiceGame.LUDO) 1 else if (twoDice) 2 else 1
        val rolled = List(count) { Random.nextInt(1, 7) }
        currentRoll = rolled
        val currentName = players.getOrNull(activePlayer) ?: "Player 1"
        val total = rolled.sum()

        if (game == BoardDiceGame.LUDO) {
            if (rolled.first() == 6) {
                consecutiveSixes++
                if (consecutiveSixes >= 3) {
                    rollHistory.add(0, "$currentName rolled 6 (3rd in a row — turn forfeit!)")
                    consecutiveSixes = 0
                    activePlayer = (activePlayer + 1) % players.size
                    feedback.play(SoundCue.WARNING)
                } else {
                    rollHistory.add(0, "$currentName rolled 6 (Bonus roll!)")
                    feedback.play(SoundCue.ACCENT_TICK)
                }
            } else {
                rollHistory.add(0, "$currentName rolled ${rolled.first()}")
                consecutiveSixes = 0
                activePlayer = (activePlayer + 1) % players.size
                feedback.play(SoundCue.CLICK)
            }
        } else {
            rollHistory.add(0, "$currentName rolled ${rolled.joinToString("+")} = $total")
            activePlayer = (activePlayer + 1) % players.size
            feedback.play(SoundCue.CLICK)
        }
        if (rollHistory.size > 30) rollHistory.removeAt(rollHistory.lastIndex)
    }

    MonoToolScaffold(
        title = "Ludo & Snakes Dice",
        subtitle = "Turn Tracker & Roll History",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(
                onClick = {
                    activePlayer = 0
                    consecutiveSixes = 0
                    rollHistory.clear()
                },
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset session", tint = palette.textPrimary)
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
                    BoardDiceGame.entries.forEach { g ->
                        MonoPillTag(label = g.label, selected = game == g, onClick = { game = g })
                    }
                    if (game == BoardDiceGame.SNAKES) {
                        MonoPillTag(
                            label = if (twoDice) "2 Dice" else "1 Die",
                            selected = twoDice,
                            onClick = { twoDice = !twoDice },
                        )
                    }
                }
            }

            item {
                MonoCard(cornerRadius = 24.dp, contentPadding = PaddingValues(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "TURN: ${players.getOrNull(activePlayer)?.uppercase() ?: ""}",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textSecondary,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            currentRoll.forEach { die ->
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(palette.surfaceSecondary)
                                        .border(1.5.dp, palette.accentPrimary, RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = die.toString(),
                                        style = MonoTabularStyle.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold),
                                        color = palette.textPrimary,
                                    )
                                }
                            }
                        }
                        if (game == BoardDiceGame.LUDO && consecutiveSixes > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Bonus Roll! ($consecutiveSixes consecutive 6)",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textPrimary,
                            )
                        }
                    }
                }
            }

            item {
                MonoActionButton(
                    text = "Roll for ${players.getOrNull(activePlayer) ?: "Player"}",
                    icon = Icons.Rounded.Casino,
                    onClick = { rollAndAdvance() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Players", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                    IconButton(
                        onClick = {
                            if (players.size < 6) players.add("Player ${players.size + 1}")
                        },
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add player", tint = palette.textPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    players.forEachIndexed { idx, name ->
                        MonoPillTag(
                            label = name,
                            selected = idx == activePlayer,
                            onClick = { activePlayer = idx; consecutiveSixes = 0 },
                        )
                    }
                }
            }

            if (rollHistory.isNotEmpty()) {
                item {
                    Text("Roll History", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
                items(rollHistory) { entry ->
                    MonoCard(cornerRadius = 14.dp, contentPadding = PaddingValues(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(entry, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                    }
                }
            }
        }
    }
}
