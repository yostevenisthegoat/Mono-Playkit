package com.example.feature.cards

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Style
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
import com.example.ui.theme.TeamRedAccent

private enum class DeckConfig(val label: String) {
    STANDARD_52("52-Card"),
    WITH_JOKERS("54 (+Jokers)"),
    EUCHRE_24("Euchre 24"),
    DOUBLE_104("Double 104"),
}

private data class PlayingCard(
    val rank: String,
    val suit: String,
    val isRed: Boolean,
)

private fun buildDeck(config: DeckConfig): List<PlayingCard> {
    val suits = listOf("♠" to false, "♥" to true, "♦" to true, "♣" to false)
    val ranks = when (config) {
        DeckConfig.EUCHRE_24 -> listOf("9", "10", "J", "Q", "K", "A")
        else -> listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    }
    val copies = if (config == DeckConfig.DOUBLE_104) 2 else 1
    val cards = mutableListOf<PlayingCard>()
    repeat(copies) {
        for ((suit, isRed) in suits) {
            for (rank in ranks) {
                cards.add(PlayingCard(rank, suit, isRed))
            }
        }
    }
    if (config == DeckConfig.WITH_JOKERS) {
        cards.add(PlayingCard("JKR", "★", false))
        cards.add(PlayingCard("JKR", "★", true))
    }
    return cards.shuffled()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayingCardsScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback

    var config by rememberSaveable { mutableStateOf(DeckConfig.STANDARD_52) }
    var drawCount by rememberSaveable { mutableIntStateOf(1) }
    val deck = remember(config) { mutableStateListOf<PlayingCard>().apply { addAll(buildDeck(config)) } }
    val drawnHistory = remember { mutableStateListOf<PlayingCard>() }

    fun reshuffle() {
        deck.clear()
        deck.addAll(buildDeck(config))
        drawnHistory.clear()
        feedback.play(SoundCue.CLICK)
    }

    fun drawCards() {
        if (deck.isEmpty()) return
        val n = minOf(drawCount, deck.size)
        repeat(n) {
            val card = deck.removeAt(0)
            drawnHistory.add(0, card)
        }
        feedback.play(SoundCue.TICK)
    }

    MonoToolScaffold(
        title = "Playing Card Deck",
        subtitle = "Shuffler, Picker & Draw History",
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DeckConfig.entries) { item ->
                        MonoPillTag(
                            label = item.label,
                            selected = config == item,
                            onClick = {
                                config = item
                                drawnHistory.clear()
                            },
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
                            text = "REMAINING IN DECK: ${deck.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textMuted,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        val latest = drawnHistory.firstOrNull()
                        if (latest == null) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(166.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(palette.surfaceSecondary)
                                    .border(1.5.dp, palette.border, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("DECK\nREADY", style = MaterialTheme.typography.labelLarge, color = palette.textMuted)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(124.dp)
                                    .height(172.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(palette.cardElevated)
                                    .border(2.dp, palette.accentPrimary, RoundedCornerShape(18.dp))
                                    .padding(14.dp),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = latest.rank,
                                        style = MonoTabularStyle.copy(fontSize = 42.sp, fontWeight = FontWeight.Bold),
                                        color = if (latest.isRed) TeamRedAccent else palette.textPrimary,
                                    )
                                    Text(
                                        text = latest.suit,
                                        fontSize = 44.sp,
                                        color = if (latest.isRed) TeamRedAccent else palette.textPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Draw:", style = MaterialTheme.typography.titleSmall, color = palette.textSecondary)
                    listOf(1, 2, 5, 7).forEach { n ->
                        MonoPillTag(label = "$n", selected = drawCount == n, onClick = { drawCount = n })
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MonoActionButton(
                        text = "Shuffle Deck",
                        icon = Icons.Rounded.Shuffle,
                        primary = false,
                        onClick = { reshuffle() },
                        modifier = Modifier.weight(0.45f),
                    )
                    MonoActionButton(
                        text = if (deck.isEmpty()) "Empty" else "Draw $drawCount",
                        icon = Icons.Rounded.Style,
                        enabled = deck.isNotEmpty(),
                        onClick = { drawCards() },
                        modifier = Modifier.weight(0.55f),
                    )
                }
            }

            if (drawnHistory.isNotEmpty()) {
                item {
                    Text("Draw History (${drawnHistory.size})", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        drawnHistory.forEach { card ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.cardElevated)
                                    .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = "${card.rank}${card.suit}",
                                    style = MonoTabularStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                    color = if (card.isRed) TeamRedAccent else palette.textPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
