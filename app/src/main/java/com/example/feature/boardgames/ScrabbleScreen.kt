package com.example.feature.boardgames

import android.os.SystemClock
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.core.formatClockMillis
import com.example.data.model.ToolCatalog
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val scrabbleLetterPoints = mapOf(
    'A' to 1, 'B' to 3, 'C' to 3, 'D' to 2, 'E' to 1, 'F' to 4, 'G' to 2,
    'H' to 4, 'I' to 1, 'J' to 8, 'K' to 5, 'L' to 1, 'M' to 3, 'N' to 1,
    'O' to 1, 'P' to 3, 'Q' to 10, 'R' to 1, 'S' to 1, 'T' to 1, 'U' to 1,
    'V' to 4, 'W' to 4, 'X' to 8, 'Y' to 4, 'Z' to 10,
)

private val scrabbleBagLimits = mapOf(
    'A' to 9, 'B' to 2, 'C' to 2, 'D' to 4, 'E' to 12, 'F' to 2, 'G' to 3,
    'H' to 2, 'I' to 9, 'J' to 1, 'K' to 1, 'L' to 4, 'M' to 2, 'N' to 6,
    'O' to 8, 'P' to 2, 'Q' to 1, 'R' to 6, 'S' to 4, 'T' to 6, 'U' to 4,
    'V' to 2, 'W' to 2, 'X' to 1, 'Y' to 2, 'Z' to 1,
)

// Official 2-letter tournament words reference (offline check)
private val validTwoLetterWords = setOf(
    "AA", "AB", "AD", "AE", "AG", "AH", "AI", "AL", "AM", "AN", "AR", "AS", "AT", "AW", "AX", "AY",
    "BA", "BE", "BI", "BO", "BY", "DA", "DE", "DO", "ED", "EF", "EH", "EL", "EM", "EN", "ER", "ES",
    "ET", "EW", "EX", "FA", "FE", "GI", "GO", "HA", "HE", "HI", "HM", "HO", "ID", "IF", "IN", "IS",
    "IT", "JO", "KA", "KI", "LA", "LI", "LO", "MA", "ME", "MI", "MM", "MO", "MU", "MY", "NA", "NE",
    "NO", "NU", "OD", "OE", "OF", "OH", "OI", "OK", "OM", "ON", "OP", "OR", "OS", "OW", "OX", "OY",
    "PA", "PE", "PI", "PO", "QI", "RE", "SH", "SI", "SO", "TA", "TE", "TI", "TO", "UH", "UM", "UN",
    "UP", "US", "UT", "WE", "WO", "XI", "XU", "YA", "YE", "YO", "ZA",
)

private data class ScrabblePlayer(
    val name: String,
    val score: Int,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScrabbleScreen(
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

    var wordInput by remember { mutableStateOf("") }
    var wordMultiplier by remember { mutableIntStateOf(1) }
    var bingoBonus by remember { mutableStateOf(false) }
    var doubleLetterIndices by remember { mutableStateOf(setOf<Int>()) }
    var tripleLetterIndices by remember { mutableStateOf(setOf<Int>()) }

    val players = remember { mutableStateListOf(ScrabblePlayer("Player 1", 0), ScrabblePlayer("Player 2", 0)) }
    var activePlayerIdx by remember { mutableIntStateOf(0) }

    // Turn Timer (2 minutes default)
    var turnSeconds by remember { mutableIntStateOf(120) }
    var timerRunning by remember { mutableStateOf(false) }
    var elapsedAccumulated by remember { mutableLongStateOf(0L) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var remainingMs by remember { mutableLongStateOf(120_000L) }

    LaunchedEffect(timerRunning, turnSeconds, elapsedAccumulated, startedAt) {
        val total = turnSeconds * 1000L
        while (timerRunning) {
            val elapsed = elapsedAccumulated + (SystemClock.elapsedRealtime() - startedAt)
            val rem = (total - elapsed).coerceAtLeast(0L)
            remainingMs = rem
            if (rem == 0L) {
                timerRunning = false
                elapsedAccumulated = total
                feedback.play(SoundCue.COMPLETE)
            }
            delay(100L)
        }
        remainingMs = (total - elapsedAccumulated).coerceAtLeast(0L)
    }

    val cleanWord = wordInput.uppercase().filter { it.isLetter() }
    val wordScore = remember(cleanWord, wordMultiplier, bingoBonus, doubleLetterIndices, tripleLetterIndices) {
        if (cleanWord.isEmpty()) 0
        else {
            val letterSum = cleanWord.mapIndexed { idx, ch ->
                val base = scrabbleLetterPoints[ch] ?: 0
                when {
                    idx in tripleLetterIndices -> base * 3
                    idx in doubleLetterIndices -> base * 2
                    else -> base
                }
            }.sum()
            (letterSum * wordMultiplier) + if (bingoBonus) 50 else 0
        }
    }

    val tileBagCheck = remember(cleanWord) {
        val counts = cleanWord.groupingBy { it }.eachCount()
        val exceeded = counts.filter { (ch, cnt) -> cnt > (scrabbleBagLimits[ch] ?: 0) }
        if (exceeded.isEmpty()) null else exceeded.entries.joinToString { "${it.key} (${it.value}/${scrabbleBagLimits[it.key]})" }
    }

    val twoLetterStatus = remember(cleanWord) {
        if (cleanWord.length == 2) cleanWord in validTwoLetterWords else null
    }

    MonoToolScaffold(
        title = "Scrabble Utility",
        subtitle = "Word Score, Turn Timer & Tile Check",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(
                onClick = {
                    scope.launch {
                        repo.recordHistory(
                            toolId = ToolCatalog.TOOL_SCRABBLE,
                            category = "Board Games",
                            title = "Scrabble Match",
                            summary = players.joinToString(" • ") { "${it.name}: ${it.score}" },
                        )
                        feedback.play(SoundCue.COMPLETE)
                        Toast.makeText(context, "Scrabble scores saved", Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Icon(Icons.Rounded.Save, contentDescription = "Save match", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Turn Timer
            item {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("TURN TIMER", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                            Text(
                                text = formatClockMillis(remainingMs),
                                style = MonoTabularStyle.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    if (timerRunning) {
                                        elapsedAccumulated += SystemClock.elapsedRealtime() - startedAt
                                        timerRunning = false
                                    } else {
                                        if (remainingMs == 0L) elapsedAccumulated = 0L
                                        startedAt = SystemClock.elapsedRealtime()
                                        timerRunning = true
                                    }
                                    feedback.play(SoundCue.CLICK)
                                },
                            ) {
                                Icon(
                                    if (timerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Start or pause turn timer",
                                    tint = palette.textPrimary,
                                )
                            }
                            IconButton(
                                onClick = {
                                    timerRunning = false
                                    elapsedAccumulated = 0L
                                    remainingMs = turnSeconds * 1000L
                                },
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Reset turn timer", tint = palette.textSecondary)
                            }
                        }
                    }
                }
            }

            // Word Score Calculator & Tile Checker
            item {
                MonoCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                    Text("Word Score Calculator", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = wordInput,
                        onValueChange = {
                            wordInput = it.filter(Char::isLetter).uppercase()
                            doubleLetterIndices = emptySet()
                            tripleLetterIndices = emptySet()
                        },
                        label = { Text("Enter word (tap letters below for DL/TL)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (cleanWord.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            cleanWord.forEachIndexed { idx, ch ->
                                val tag = when {
                                    idx in tripleLetterIndices -> "3L"
                                    idx in doubleLetterIndices -> "2L"
                                    else -> ""
                                }
                                val pts = scrabbleLetterPoints[ch] ?: 0
                                MonoPillTag(
                                    label = if (tag.isEmpty()) "$ch ($pts)" else "$ch $tag",
                                    selected = tag.isNotEmpty(),
                                    onClick = {
                                        when {
                                            idx in doubleLetterIndices -> {
                                                doubleLetterIndices = doubleLetterIndices - idx
                                                tripleLetterIndices = tripleLetterIndices + idx
                                            }
                                            idx in tripleLetterIndices -> {
                                                tripleLetterIndices = tripleLetterIndices - idx
                                            }
                                            else -> {
                                                doubleLetterIndices = doubleLetterIndices + idx
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to "1x Word", 2 to "2x DW", 3 to "3x TW").forEach { (m, lbl) ->
                            MonoPillTag(label = lbl, selected = wordMultiplier == m, onClick = { wordMultiplier = m })
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("7-Tile Bingo (+50 pts)", style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                        Switch(checked = bingoBonus, onCheckedChange = { bingoBonus = it })
                    }

                    if (tileBagCheck != null) {
                        Text(
                            text = "Exceeds standard 100-tile bag: $tileBagCheck",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusWarning,
                        )
                    }
                    if (twoLetterStatus != null) {
                        Text(
                            text = if (twoLetterStatus) "✓ Valid 2-letter tournament word" else "✕ Not a standard 2-letter tournament word",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (twoLetterStatus) StatusSuccess else StatusWarning,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("WORD SCORE", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                            Text(
                                text = "$wordScore pts",
                                style = MonoTabularStyle.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                        }
                        MonoActionButton(
                            text = "Add to ${players.getOrNull(activePlayerIdx)?.name ?: "Player"}",
                            enabled = wordScore > 0,
                            onClick = {
                                val p = players.getOrNull(activePlayerIdx) ?: return@MonoActionButton
                                players[activePlayerIdx] = p.copy(score = p.score + wordScore)
                                activePlayerIdx = (activePlayerIdx + 1) % players.size
                                wordInput = ""
                                wordMultiplier = 1
                                bingoBonus = false
                                doubleLetterIndices = emptySet()
                                tripleLetterIndices = emptySet()
                                feedback.play(SoundCue.COMPLETE)
                            },
                        )
                    }
                }
            }

            // Player Scoreboard
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Players (tap to select active)", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                    IconButton(onClick = { if (players.size < 4) players.add(ScrabblePlayer("Player ${players.size + 1}", 0)) }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add player", tint = palette.textPrimary)
                    }
                }
            }
            itemsIndexed(players) { idx, p ->
                MonoCard(
                    onClick = { activePlayerIdx = idx },
                    cornerRadius = 16.dp,
                    borderColor = if (activePlayerIdx == idx) palette.accentPrimary else palette.border,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = if (activePlayerIdx == idx) "${p.name} • Active" else p.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary,
                            )
                        }
                        Text(
                            text = "${p.score}",
                            style = MonoTabularStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                    }
                }
            }
        }
    }
}
