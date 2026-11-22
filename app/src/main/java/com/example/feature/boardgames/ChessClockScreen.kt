package com.example.feature.boardgames

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.SystemClock
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.KeepScreenOnEffect
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.core.formatClockMillis
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.StatusError
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class ChessTimeControl(
    val id: String,
    val category: String,
    val label: String,
    val baseMinutes: Int,
    val incrementSec: Int,
    val delaySec: Int = 0,
)

private val presetControls = listOf(
    ChessTimeControl("1+0", "Bullet", "1+0", 1, 0),
    ChessTimeControl("2+1", "Bullet", "2+1", 2, 1),
    ChessTimeControl("3+0", "Blitz", "3+0", 3, 0),
    ChessTimeControl("3+2", "Blitz", "3+2", 3, 2),
    ChessTimeControl("5+0", "Blitz", "5+0", 5, 0),
    ChessTimeControl("10+0", "Rapid", "10+0", 10, 0),
    ChessTimeControl("15+10", "Rapid", "15+10", 15, 10),
    ChessTimeControl("30+0", "Classical", "30+0", 30, 0),
    ChessTimeControl("90+30", "Classical", "90+30", 90, 30),
)

private enum class ChessToolTab(val label: String) {
    CLOCK("Clock"),
    NOTATION("Notation"),
    CHESS960("Chess960"),
}

@Composable
fun ChessClockScreen(
    isFavorite: Boolean,
    keepScreenOn: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback
    KeepScreenOnEffect(keepScreenOn)

    var activeTab by rememberSaveable { mutableStateOf(ChessToolTab.CLOCK) }
    var fullscreen by rememberSaveable { mutableStateOf(false) }
    var rotateTopPlayer by rememberSaveable { mutableStateOf(true) }

    var baseMinutes by rememberSaveable { mutableIntStateOf(5) }
    var incrementSec by rememberSaveable { mutableIntStateOf(0) }
    var delaySec by rememberSaveable { mutableIntStateOf(0) }
    var selectedLabel by rememberSaveable { mutableStateOf("5+0") }

    var whiteRemainingMs by rememberSaveable { mutableLongStateOf(5 * 60_000L) }
    var blackRemainingMs by rememberSaveable { mutableLongStateOf(5 * 60_000L) }
    var whiteMoves by rememberSaveable { mutableIntStateOf(0) }
    var blackMoves by rememberSaveable { mutableIntStateOf(0) }

    // 0 = idle, 1 = white running, 2 = black running
    var activePlayer by rememberSaveable { mutableIntStateOf(0) }
    var paused by rememberSaveable { mutableStateOf(false) }
    var turnStartedAt by rememberSaveable { mutableLongStateOf(0L) }
    var turnStartBankMs by rememberSaveable { mutableLongStateOf(0L) }
    var flaggedPlayer by rememberSaveable { mutableIntStateOf(0) }

    var showCustomDialog by remember { mutableStateOf(false) }

    fun applyControl(mins: Int, inc: Int, del: Int, label: String) {
        baseMinutes = mins
        incrementSec = inc
        delaySec = del
        selectedLabel = label
        whiteRemainingMs = mins * 60_000L
        blackRemainingMs = mins * 60_000L
        whiteMoves = 0
        blackMoves = 0
        activePlayer = 0
        paused = false
        flaggedPlayer = 0
        feedback.play(SoundCue.CLICK)
    }

    LaunchedEffect(activePlayer, paused, turnStartedAt, turnStartBankMs, delaySec) {
        while (activePlayer != 0 && !paused && flaggedPlayer == 0) {
            val now = SystemClock.elapsedRealtime()
            val elapsedSinceTap = (now - turnStartedAt).coerceAtLeast(0L)
            val delayMs = delaySec * 1000L
            val effectiveDrain = (elapsedSinceTap - delayMs).coerceAtLeast(0L)
            val currentRemaining = (turnStartBankMs - effectiveDrain).coerceAtLeast(0L)
            if (activePlayer == 1) {
                whiteRemainingMs = currentRemaining
            } else {
                blackRemainingMs = currentRemaining
            }
            if (currentRemaining == 0L) {
                flaggedPlayer = activePlayer
                paused = true
                feedback.play(SoundCue.COMPLETE)
            }
            delay(60L)
        }
    }

    fun onTapPlayer(player: Int) {
        if (flaggedPlayer != 0) return
        val now = SystemClock.elapsedRealtime()
        if (activePlayer == 0) {
            // Tapping player 1 starts player 2, and vice versa
            val next = if (player == 1) 2 else 1
            activePlayer = next
            paused = false
            turnStartedAt = now
            turnStartBankMs = if (next == 1) whiteRemainingMs else blackRemainingMs
            feedback.play(SoundCue.TICK)
            return
        }
        if (paused) {
            paused = false
            turnStartedAt = now
            turnStartBankMs = if (activePlayer == 1) whiteRemainingMs else blackRemainingMs
            feedback.play(SoundCue.TICK)
            return
        }
        if (activePlayer != player) return

        // Commit elapsed for current player & add Fischer increment
        val elapsedSinceTap = (now - turnStartedAt).coerceAtLeast(0L)
        val effectiveDrain = (elapsedSinceTap - delaySec * 1000L).coerceAtLeast(0L)
        val remainingAfter = (turnStartBankMs - effectiveDrain).coerceAtLeast(0L) + incrementSec * 1000L
        if (player == 1) {
            whiteRemainingMs = remainingAfter
            whiteMoves++
            activePlayer = 2
            turnStartBankMs = blackRemainingMs
        } else {
            blackRemainingMs = remainingAfter
            blackMoves++
            activePlayer = 1
            turnStartBankMs = whiteRemainingMs
        }
        turnStartedAt = now
        feedback.play(SoundCue.TICK)
    }

    fun togglePause() {
        if (activePlayer == 0 || flaggedPlayer != 0) return
        val now = SystemClock.elapsedRealtime()
        if (!paused) {
            val elapsedSinceTap = (now - turnStartedAt).coerceAtLeast(0L)
            val effectiveDrain = (elapsedSinceTap - delaySec * 1000L).coerceAtLeast(0L)
            val remaining = (turnStartBankMs - effectiveDrain).coerceAtLeast(0L)
            if (activePlayer == 1) whiteRemainingMs = remaining else blackRemainingMs = remaining
            paused = true
        } else {
            turnStartedAt = now
            turnStartBankMs = if (activePlayer == 1) whiteRemainingMs else blackRemainingMs
            paused = false
        }
        feedback.play(SoundCue.CLICK)
    }

    if (showCustomDialog) {
        var minsInput by remember { mutableStateOf(baseMinutes.toString()) }
        var incInput by remember { mutableStateOf(incrementSec.toString()) }
        var delInput by remember { mutableStateOf(delaySec.toString()) }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Custom Time Control", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = minsInput,
                        onValueChange = { minsInput = it.filter(Char::isDigit) },
                        label = { Text("Base Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = incInput,
                        onValueChange = { incInput = it.filter(Char::isDigit) },
                        label = { Text("Fischer Increment (sec)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = delInput,
                        onValueChange = { delInput = it.filter(Char::isDigit) },
                        label = { Text("Simple Delay (sec)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val m = (minsInput.toIntOrNull() ?: 5).coerceIn(1, 180)
                        val i = (incInput.toIntOrNull() ?: 0).coerceIn(0, 120)
                        val d = (delInput.toIntOrNull() ?: 0).coerceIn(0, 60)
                        applyControl(m, i, d, "$m+$i" + if (d > 0) " d$d" else "")
                        showCustomDialog = false
                    },
                ) { Text("Apply", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    MonoToolScaffold(
        title = "Chess Clock & Tools",
        subtitle = if (activeTab == ChessToolTab.CLOCK) "$selectedLabel • ${if (delaySec > 0) "${delaySec}s delay" else "Fischer"}" else activeTab.label,
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        fullscreen = fullscreen,
        actions = {
            if (activeTab == ChessToolTab.CLOCK) {
                IconButton(
                    onClick = { rotateTopPlayer = !rotateTopPlayer },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Icon(Icons.Rounded.ScreenRotation, contentDescription = "Rotate top clock", tint = palette.textPrimary)
                }
                IconButton(
                    onClick = { fullscreen = true },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Icon(Icons.Rounded.Fullscreen, contentDescription = "Fullscreen", tint = palette.textPrimary)
                }
            }
        },
    ) { contentModifier ->
        Column(
            modifier = contentModifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!fullscreen) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChessToolTab.entries.forEach { tab ->
                        MonoPillTag(
                            label = tab.label,
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                        )
                    }
                }
            }

            when (activeTab) {
                ChessToolTab.CLOCK -> {
                    if (!fullscreen) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(presetControls) { ctrl ->
                                MonoPillTag(
                                    label = "${ctrl.category} ${ctrl.label}",
                                    selected = selectedLabel == ctrl.label,
                                    onClick = { applyControl(ctrl.baseMinutes, ctrl.incrementSec, ctrl.delaySec, ctrl.label) },
                                )
                            }
                            item {
                                MonoPillTag(
                                    label = "Custom...",
                                    onClick = { showCustomDialog = true },
                                )
                            }
                        }
                    }

                    // Top Player Clock (Black by default)
                    ChessPlayerPanel(
                        playerLabel = "BLACK",
                        millis = blackRemainingMs,
                        moves = blackMoves,
                        isActive = activePlayer == 2 && !paused,
                        isFlagged = flaggedPlayer == 2,
                        rotated = rotateTopPlayer,
                        onTap = { onTapPlayer(2) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("chess_black_panel"),
                    )

                    // Center Control Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { applyControl(baseMinutes, incrementSec, delaySec, selectedLabel) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(palette.cardElevated),
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Reset clock", tint = palette.textPrimary)
                        }
                        IconButton(
                            onClick = { togglePause() },
                            enabled = activePlayer != 0 && flaggedPlayer == 0,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(palette.accentPrimary),
                        ) {
                            Icon(
                                imageVector = if (paused || activePlayer == 0) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                contentDescription = "Pause or resume",
                                tint = if (palette.isDark) Color(0xFF0A0A0A) else Color.White,
                            )
                        }
                        IconButton(
                            onClick = { if (fullscreen) fullscreen = false else showCustomDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(palette.cardElevated),
                        ) {
                            Icon(
                                imageVector = if (fullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Tune,
                                contentDescription = if (fullscreen) "Exit fullscreen" else "Time settings",
                                tint = palette.textPrimary,
                            )
                        }
                    }

                    // Bottom Player Clock (White)
                    ChessPlayerPanel(
                        playerLabel = "WHITE",
                        millis = whiteRemainingMs,
                        moves = whiteMoves,
                        isActive = activePlayer == 1 && !paused,
                        isFlagged = flaggedPlayer == 1,
                        rotated = false,
                        onTap = { onTapPlayer(1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("chess_white_panel"),
                    )
                }

                ChessToolTab.NOTATION -> {
                    ChessNotationRecorder(modifier = Modifier.fillMaxSize())
                }

                ChessToolTab.CHESS960 -> {
                    Chess960Generator(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun ChessPlayerPanel(
    playerLabel: String,
    millis: Long,
    moves: Int,
    isActive: Boolean,
    isFlagged: Boolean,
    rotated: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val bgColor = when {
        isFlagged -> Color(0xFF331414)
        isActive -> palette.cardElevated
        else -> palette.cardPrimary
    }
    val borderColor = when {
        isFlagged -> StatusError
        isActive -> palette.accentPrimary
        else -> palette.border
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(width = if (isActive || isFlagged) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(24.dp))
            .clickable(onClick = onTap)
            .rotate(if (rotated) 180f else 0f)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isFlagged) "$playerLabel • TIME OUT" else playerLabel,
                style = MaterialTheme.typography.labelLarge,
                color = if (isFlagged) StatusError else if (isActive) palette.textPrimary else palette.textMuted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatClockMillis(millis),
                style = MonoTabularStyle.copy(fontSize = 62.sp, fontWeight = FontWeight.Bold),
                color = if (isActive || isFlagged) palette.textPrimary else palette.textSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Moves: $moves",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChessNotationRecorder(modifier: Modifier = Modifier) {
    val palette = MonoTheme.palette
    val context = LocalContext.current
    val moves = remember { mutableStateListOf<String>() }
    var currentDraft by remember { mutableStateOf("") }

    fun addMove() {
        val clean = currentDraft.trim()
        if (clean.isNotEmpty()) {
            moves.add(clean)
            currentDraft = ""
        }
    }

    val pgnText = remember(moves.size) {
        moves.chunked(2).mapIndexed { idx, pair ->
            if (pair.size == 2) "${idx + 1}. ${pair[0]} ${pair[1]}" else "${idx + 1}. ${pair[0]}"
        }.joinToString("  ")
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Game Moves (${moves.size})", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                Row {
                    IconButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cm?.setPrimaryClip(ClipData.newPlainText("Chess Notation", pgnText))
                            Toast.makeText(context, "PGN copied", Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy PGN", tint = palette.textPrimary)
                    }
                    IconButton(onClick = { moves.clear(); currentDraft = "" }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Clear notation", tint = palette.textSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = pgnText.ifEmpty { "Tap algebraic keys below or type moves (e.g., e4, Nf3, O-O)." },
                style = MonoTabularStyle.copy(fontSize = 14.sp),
                color = if (pgnText.isEmpty()) palette.textMuted else palette.textPrimary,
            )
        }

        OutlinedTextField(
            value = currentDraft,
            onValueChange = { currentDraft = it },
            label = { Text("Current Move") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf("K", "Q", "R", "B", "N", "x", "+", "#", "O-O", "O-O-O").forEach { sym ->
                MonoPillTag(
                    label = sym,
                    onClick = {
                        if (sym == "O-O" || sym == "O-O-O") {
                            moves.add(sym)
                            currentDraft = ""
                        } else {
                            currentDraft += sym
                        }
                    },
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf("a", "b", "c", "d", "e", "f", "g", "h", "1", "2", "3", "4", "5", "6", "7", "8").forEach { ch ->
                MonoPillTag(label = ch, onClick = { currentDraft += ch })
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MonoActionButton(
                text = "Undo Move",
                primary = false,
                enabled = moves.isNotEmpty(),
                onClick = { if (moves.isNotEmpty()) moves.removeAt(moves.lastIndex) },
                modifier = Modifier.weight(1f),
            )
            MonoActionButton(
                text = "Record Move",
                onClick = { addMove() },
                enabled = currentDraft.isNotBlank(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Chess960Generator(modifier: Modifier = Modifier) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback
    var positionId by rememberSaveable { mutableIntStateOf(Random.nextInt(960)) }
    val pieces = remember(positionId) { generateChess960Rank(positionId) }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            MonoCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FISCHER RANDOM POSITION",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "#$positionId",
                    style = MonoTabularStyle.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold),
                    color = palette.textPrimary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    pieces.forEachIndexed { idx, piece ->
                        val isLightSquare = idx % 2 == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLightSquare) palette.surfaceSecondary else palette.bgSecondary)
                                .border(1.dp, palette.border, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = pieceSymbol(piece),
                                    fontSize = 22.sp,
                                    color = palette.textPrimary,
                                )
                                Text(
                                    text = piece.toString(),
                                    style = MonoTabularStyle.copy(fontSize = 10.sp),
                                    color = palette.textSecondary,
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Files a–h: ${pieces.joinToString(" ")}",
                    style = MonoTabularStyle.copy(fontSize = 13.sp),
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            MonoActionButton(
                text = "Generate Random Position",
                icon = Icons.Rounded.Casino,
                onClick = {
                    positionId = Random.nextInt(960)
                    feedback.play(SoundCue.CLICK)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun pieceSymbol(ch: Char): String = when (ch) {
    'K' -> "♔"
    'Q' -> "♕"
    'R' -> "♖"
    'B' -> "♗"
    'N' -> "♘"
    else -> ch.toString()
}

/**
 * Exact Scharnagl algorithm generating all 960 valid Chess960 back-rank setups.
 */
private fun generateChess960Rank(n: Int): List<Char> {
    val rank = CharArray(8) { ' ' }
    val q = n / 4
    val b1 = n % 4
    rank[b1 * 2 + 1] = 'B'
    val q2 = q / 4
    val b2 = q % 4
    rank[b2 * 2] = 'B'
    val q3 = q2 / 6
    val queenIdx = q2 % 6
    var emptyCount = 0
    for (i in 0 until 8) {
        if (rank[i] == ' ') {
            if (emptyCount == queenIdx) {
                rank[i] = 'Q'
                break
            }
            emptyCount++
        }
    }
    val knightTable = arrayOf(
        0 to 1, 0 to 2, 0 to 3, 0 to 4,
        1 to 2, 1 to 3, 1 to 4,
        2 to 3, 2 to 4, 3 to 4,
    )
    val (k1, k2) = knightTable[q3.coerceIn(0, 9)]
    val emptySlots = (0 until 8).filter { rank[it] == ' ' }
    rank[emptySlots[k1]] = 'N'
    rank[emptySlots[k2]] = 'N'
    val remaining = (0 until 8).filter { rank[it] == ' ' }
    rank[remaining[0]] = 'R'
    rank[remaining[1]] = 'K'
    rank[remaining[2]] = 'R'
    return rank.toList()
}
