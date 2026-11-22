package com.example.feature.scoreboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SwapHoriz
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.SystemClock
import com.example.core.KeepScreenOnEffect
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
import com.example.ui.theme.TeamBlueAccent
import com.example.ui.theme.TeamBlueBorder
import com.example.ui.theme.TeamBlueSurface
import com.example.ui.theme.TeamRedAccent
import com.example.ui.theme.TeamRedBorder
import com.example.ui.theme.TeamRedSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class SportPreset(
    val id: String,
    val label: String,
    val periodLabel: String,
    val maxPeriods: Int,
    val defaultMinutes: Int,
    val increments: List<Int>,
    val startScore: Int = 0,
    val countDownScore: Boolean = false,
    val hasSets: Boolean = false,
) {
    STANDARD("STANDARD", "Standard", "Round", 99, 0, listOf(1, 5)),
    BASKETBALL("BASKETBALL", "Basketball", "Quarter", 4, 10, listOf(1, 2, 3)),
    VOLLEYBALL("VOLLEYBALL", "Volleyball", "Set", 5, 0, listOf(1), hasSets = true),
    BADMINTON("BADMINTON", "Badminton", "Game", 3, 0, listOf(1), hasSets = true),
    TABLE_TENNIS("TABLE_TENNIS", "Table Tennis", "Game", 5, 0, listOf(1), hasSets = true),
    TENNIS("TENNIS", "Tennis", "Set", 5, 0, listOf(1), hasSets = true),
    FOOTBALL("FOOTBALL", "Football", "Half", 2, 45, listOf(1)),
    BOXING("BOXING", "Boxing", "Round", 12, 3, listOf(1, 10, 9)),
    DARTS_301("DARTS_301", "Darts 301", "Leg", 5, 0, listOf(-1, -5, -10, -20, -60), startScore = 301, countDownScore = true, hasSets = true),
    DARTS_501("DARTS_501", "Darts 501", "Leg", 5, 0, listOf(-1, -5, -10, -20, -60), startScore = 501, countDownScore = true, hasSets = true),
    CUSTOM("CUSTOM", "Custom", "Period", 10, 15, listOf(1, 2, 5)),
}

private data class ScoreSnapshot(
    val leftScore: Int,
    val rightScore: Int,
    val leftSets: Int,
    val rightSets: Int,
    val period: Int,
)

private val teamColorOptions = listOf(
    "Red" to Triple(TeamRedAccent, TeamRedSurface, TeamRedBorder),
    "Blue" to Triple(TeamBlueAccent, TeamBlueSurface, TeamBlueBorder),
    "Mono Light" to Triple(Color(0xFFE8E8E8), Color(0xFF222222), Color(0xFF454545)),
    "Emerald" to Triple(Color(0xFF22C55E), Color(0xFF102418), Color(0xFF1E5233)),
    "Amber" to Triple(Color(0xFFF59E0B), Color(0xFF261D0D), Color(0xFF5A4018)),
)

@Composable
fun UniversalScoreboardScreen(
    defaultPresetId: String,
    isFavorite: Boolean,
    keepScreenOn: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val container = LocalAppContainer.current
    val feedback = container.feedback
    val repo = container.repository
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    KeepScreenOnEffect(keepScreenOn)

    var preset by rememberSaveable {
        mutableStateOf(
            SportPreset.entries.firstOrNull { it.id == defaultPresetId } ?: SportPreset.STANDARD,
        )
    }

    var leftName by rememberSaveable { mutableStateOf("Red Team") }
    var rightName by rememberSaveable { mutableStateOf("Blue Team") }
    var leftColorIdx by rememberSaveable { mutableIntStateOf(0) }
    var rightColorIdx by rememberSaveable { mutableIntStateOf(1) }

    var leftScore by rememberSaveable { mutableIntStateOf(preset.startScore) }
    var rightScore by rememberSaveable { mutableIntStateOf(preset.startScore) }
    var leftSets by rememberSaveable { mutableIntStateOf(0) }
    var rightSets by rememberSaveable { mutableIntStateOf(0) }
    var period by rememberSaveable { mutableIntStateOf(1) }
    var matchNotes by rememberSaveable { mutableStateOf("") }
    var fullscreen by rememberSaveable { mutableStateOf(false) }

    // Monotonic match timer (either countdown if defaultMinutes > 0, or elapsed stopwatch if 0)
    var customMinutes by rememberSaveable { mutableIntStateOf(preset.defaultMinutes) }
    var customPeriodLabel by rememberSaveable { mutableStateOf("Period") }
    var timerRunning by rememberSaveable { mutableStateOf(false) }
    var accumulatedTimerMs by rememberSaveable { mutableLongStateOf(0L) }
    var timerStartElapsed by rememberSaveable { mutableLongStateOf(0L) }
    var displayedTimerMs by remember { mutableLongStateOf(0L) }

    val undoStack = remember { mutableStateListOf<ScoreSnapshot>() }

    // Dialog states
    var editingTeam by remember { mutableStateOf<Boolean?>(null) } // true=left, false=right
    var manualScoreTeam by remember { mutableStateOf<Boolean?>(null) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var showCustomConfigDialog by remember { mutableStateOf(false) }
    var showDartsInputTeam by remember { mutableStateOf<Boolean?>(null) }

    fun pushUndo() {
        undoStack.add(ScoreSnapshot(leftScore, rightScore, leftSets, rightSets, period))
        if (undoStack.size > 50) undoStack.removeAt(0)
    }

    fun applyPreset(newPreset: SportPreset) {
        preset = newPreset
        leftScore = newPreset.startScore
        rightScore = newPreset.startScore
        leftSets = 0
        rightSets = 0
        period = 1
        customMinutes = newPreset.defaultMinutes
        timerRunning = false
        accumulatedTimerMs = 0L
        timerStartElapsed = 0L
        displayedTimerMs = if (newPreset.defaultMinutes > 0) newPreset.defaultMinutes * 60_000L else 0L
        undoStack.clear()
        feedback.play(SoundCue.CLICK)
    }

    LaunchedEffect(timerRunning, customMinutes, accumulatedTimerMs, timerStartElapsed) {
        val targetTotalMs = customMinutes * 60_000L
        while (timerRunning) {
            val elapsed = accumulatedTimerMs + (SystemClock.elapsedRealtime() - timerStartElapsed)
            if (targetTotalMs > 0L) {
                val remaining = (targetTotalMs - elapsed).coerceAtLeast(0L)
                displayedTimerMs = remaining
                if (remaining == 0L) {
                    timerRunning = false
                    accumulatedTimerMs = targetTotalMs
                    feedback.play(SoundCue.COMPLETE)
                }
            } else {
                displayedTimerMs = elapsed
            }
            delay(100L)
        }
        val currentElapsed = accumulatedTimerMs
        displayedTimerMs = if (targetTotalMs > 0L) (targetTotalMs - currentElapsed).coerceAtLeast(0L) else currentElapsed
    }

    fun toggleTimer() {
        if (timerRunning) {
            accumulatedTimerMs += SystemClock.elapsedRealtime() - timerStartElapsed
            timerRunning = false
        } else {
            val targetTotalMs = customMinutes * 60_000L
            if (targetTotalMs > 0L && accumulatedTimerMs >= targetTotalMs) {
                accumulatedTimerMs = 0L
            }
            timerStartElapsed = SystemClock.elapsedRealtime()
            timerRunning = true
        }
        feedback.play(SoundCue.CLICK)
    }

    fun resetTimer() {
        timerRunning = false
        accumulatedTimerMs = 0L
        timerStartElapsed = 0L
        displayedTimerMs = if (customMinutes > 0) customMinutes * 60_000L else 0L
        feedback.play(SoundCue.CLICK)
    }

    fun adjustScore(isLeft: Boolean, delta: Int) {
        pushUndo()
        if (preset.countDownScore) {
            val current = if (isLeft) leftScore else rightScore
            val next = current + delta
            if (next < 0) {
                // Darts bust!
                feedback.play(SoundCue.WARNING)
                Toast.makeText(context, "Bust! Score remains $current", Toast.LENGTH_SHORT).show()
                undoStack.removeAt(undoStack.lastIndex)
                return
            }
            if (isLeft) leftScore = next else rightScore = next
            if (next == 0) {
                if (isLeft) leftSets++ else rightSets++
                feedback.play(SoundCue.COMPLETE)
                Toast.makeText(context, "${if (isLeft) leftName else rightName} wins leg!", Toast.LENGTH_SHORT).show()
            } else {
                feedback.play(SoundCue.CLICK)
            }
        } else {
            if (isLeft) {
                leftScore = (leftScore + delta).coerceAtLeast(0)
            } else {
                rightScore = (rightScore + delta).coerceAtLeast(0)
            }
            feedback.play(SoundCue.CLICK)
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val last = undoStack.removeAt(undoStack.lastIndex)
        leftScore = last.leftScore
        rightScore = last.rightScore
        leftSets = last.leftSets
        rightSets = last.rightSets
        period = last.period
        feedback.play(SoundCue.CLICK)
    }

    fun swapSides() {
        pushUndo()
        val tmpName = leftName; leftName = rightName; rightName = tmpName
        val tmpScore = leftScore; leftScore = rightScore; rightScore = tmpScore
        val tmpSets = leftSets; leftSets = rightSets; rightSets = tmpSets
        val tmpCol = leftColorIdx; leftColorIdx = rightColorIdx; rightColorIdx = tmpCol
        feedback.play(SoundCue.CLICK)
    }

    fun resetMatch() {
        pushUndo()
        leftScore = preset.startScore
        rightScore = preset.startScore
        leftSets = 0
        rightSets = 0
        period = 1
        resetTimer()
    }

    fun saveMatchToHistory() {
        scope.launch {
            val periodName = if (preset == SportPreset.CUSTOM) customPeriodLabel else preset.periodLabel
            val summary = "$leftName $leftScore — $rightScore $rightName"
            val details = buildString {
                append("${preset.label} • $periodName $period")
                if (preset.hasSets) append(" • Sets $leftSets:$rightSets")
                if (matchNotes.isNotBlank()) append(" • Note: $matchNotes")
            }
            repo.recordHistory(
                toolId = ToolCatalog.TOOL_SCOREBOARD,
                category = "Scoreboard",
                title = "${preset.label} Match",
                summary = summary,
                detail = details,
            )
            feedback.play(SoundCue.COMPLETE)
            Toast.makeText(context, "Match saved to History", Toast.LENGTH_SHORT).show()
        }
    }

    // Dialogs
    editingTeam?.let { isLeft ->
        var nameInput by remember { mutableStateOf(if (isLeft) leftName else rightName) }
        var colorSelection by remember { mutableIntStateOf(if (isLeft) leftColorIdx else rightColorIdx) }
        AlertDialog(
            onDismissRequest = { editingTeam = null },
            containerColor = palette.cardElevated,
            title = { Text("Customize Team", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Team Name") },
                        singleLine = true,
                    )
                    Text("Panel Color", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(teamColorOptions.indices.toList()) { idx ->
                            val (label, triple) = teamColorOptions[idx]
                            val selected = colorSelection == idx
                            Surface(
                                onClick = { colorSelection = idx },
                                shape = RoundedCornerShape(12.dp),
                                color = triple.second,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) palette.textPrimary else triple.third,
                                ),
                            ) {
                                Text(
                                    text = label,
                                    color = triple.first,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleaned = nameInput.trim().ifEmpty { if (isLeft) "Red Team" else "Blue Team" }
                        if (isLeft) {
                            leftName = cleaned
                            leftColorIdx = colorSelection
                        } else {
                            rightName = cleaned
                            rightColorIdx = colorSelection
                        }
                        editingTeam = null
                    },
                ) { Text("Save", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { editingTeam = null }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    manualScoreTeam?.let { isLeft ->
        var scoreInput by remember { mutableStateOf((if (isLeft) leftScore else rightScore).toString()) }
        AlertDialog(
            onDismissRequest = { manualScoreTeam = null },
            containerColor = palette.cardElevated,
            title = { Text("Set Score (${if (isLeft) leftName else rightName})", color = palette.textPrimary) },
            text = {
                OutlinedTextField(
                    value = scoreInput,
                    onValueChange = { scoreInput = it.filter { ch -> ch.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    label = { Text("Score") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = scoreInput.toIntOrNull()
                        if (parsed != null) {
                            pushUndo()
                            if (isLeft) leftScore = parsed else rightScore = parsed
                        }
                        manualScoreTeam = null
                    },
                ) { Text("Apply", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { manualScoreTeam = null }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    showDartsInputTeam?.let { isLeft ->
        var throwInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDartsInputTeam = null },
            containerColor = palette.cardElevated,
            title = { Text("Subtract 3-Dart Turn (${if (isLeft) leftName else rightName})", color = palette.textPrimary) },
            text = {
                OutlinedTextField(
                    value = throwInput,
                    onValueChange = { throwInput = it.filter { ch -> ch.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    label = { Text("Turn Score (0-180)") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val value = (throwInput.toIntOrNull() ?: 0).coerceIn(0, 180)
                        adjustScore(isLeft, -value)
                        showDartsInputTeam = null
                    },
                ) { Text("Subtract", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDartsInputTeam = null }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    if (showNotesDialog) {
        var noteText by remember { mutableStateOf(matchNotes) }
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Match Notes", color = palette.textPrimary) },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Venue, rules, or highlights") },
                    maxLines = 4,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        matchNotes = noteText.trim()
                        showNotesDialog = false
                    },
                ) { Text("Save", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    if (showCustomConfigDialog) {
        var periodLbl by remember { mutableStateOf(customPeriodLabel) }
        var minsText by remember { mutableStateOf(customMinutes.toString()) }
        AlertDialog(
            onDismissRequest = { showCustomConfigDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Custom Match Rules", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = periodLbl,
                        onValueChange = { periodLbl = it },
                        label = { Text("Period Label (e.g. Half, Round)") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = minsText,
                        onValueChange = { minsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Timer Minutes (0 = Stopwatch)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        customPeriodLabel = periodLbl.trim().ifEmpty { "Period" }
                        customMinutes = (minsText.toIntOrNull() ?: 0).coerceIn(0, 180)
                        resetTimer()
                        showCustomConfigDialog = false
                    },
                ) { Text("Apply", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomConfigDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    val leftColors = teamColorOptions[leftColorIdx.coerceIn(teamColorOptions.indices)].second
    val rightColors = teamColorOptions[rightColorIdx.coerceIn(teamColorOptions.indices)].second
    val scoreDiff = abs(leftScore - rightScore)
    val activePeriodLabel = if (preset == SportPreset.CUSTOM) customPeriodLabel else preset.periodLabel

    MonoToolScaffold(
        title = "Universal Scoreboard",
        subtitle = preset.label,
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        fullscreen = fullscreen,
        actions = {
            IconButton(
                onClick = { saveMatchToHistory() },
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Icon(Icons.Rounded.Save, contentDescription = "Save match", tint = palette.textPrimary)
            }
            IconButton(
                onClick = { fullscreen = true },
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Icon(Icons.Rounded.Fullscreen, contentDescription = "Fullscreen mode", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        Column(
            modifier = contentModifier
                .let { if (fullscreen) it else it.verticalScroll(rememberScrollState()) }
                .padding(horizontal = 16.dp, vertical = if (fullscreen) 12.dp else 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!fullscreen) {
                // Sport Preset Selector
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SportPreset.entries) { item ->
                        MonoPillTag(
                            label = item.label,
                            selected = preset == item,
                            onClick = {
                                applyPreset(item)
                                if (item == SportPreset.CUSTOM) showCustomConfigDialog = true
                            },
                        )
                    }
                }
            }

            // Match Timer & Period Strip
            MonoCard(
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Period Control
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (period > 1) period-- },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Previous period", tint = palette.textSecondary, modifier = Modifier.size(18.dp))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = activePeriodLabel.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted,
                            )
                            Text(
                                text = "$period",
                                style = MonoTabularStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                        }
                        IconButton(
                            onClick = { if (period < preset.maxPeriods) period++ },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Next period", tint = palette.textSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Match Clock
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (customMinutes > 0) "MATCH CLOCK" else "ELAPSED",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted,
                            )
                            Text(
                                text = formatClockMillis(displayedTimerMs),
                                style = MonoTabularStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { toggleTimer() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(palette.surfaceSecondary),
                        ) {
                            Icon(
                                imageVector = if (timerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (timerRunning) "Pause clock" else "Start clock",
                                tint = palette.textPrimary,
                            )
                        }
                        if (fullscreen) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { fullscreen = false },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Icon(Icons.Rounded.FullscreenExit, contentDescription = "Exit fullscreen", tint = palette.textSecondary)
                            }
                        }
                    }
                }
            }

            // Dual Team Opposing Score Panels (Red Left / Blue Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (fullscreen) it.weight(1f) else it.height(320.dp) },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ScoreTeamPanel(
                    teamName = leftName,
                    score = leftScore,
                    sets = if (preset.hasSets) leftSets else null,
                    accentColor = leftColors.first,
                    surfaceColor = leftColors.second,
                    borderColor = leftColors.third,
                    increments = preset.increments,
                    isDarts = preset.countDownScore,
                    tennisDisplay = if (preset == SportPreset.TENNIS) tennisPointLabel(leftScore, rightScore) else null,
                    onIncrement = { delta -> adjustScore(true, delta) },
                    onDecrement = { adjustScore(true, if (preset.countDownScore) 1 else -1) },
                    onEditTeam = { editingTeam = true },
                    onManualScore = { if (preset.countDownScore) showDartsInputTeam = true else manualScoreTeam = true },
                    onIncrementSet = { pushUndo(); leftSets++ },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .testTag("scoreboard_left_panel"),
                )

                ScoreTeamPanel(
                    teamName = rightName,
                    score = rightScore,
                    sets = if (preset.hasSets) rightSets else null,
                    accentColor = rightColors.first,
                    surfaceColor = rightColors.second,
                    borderColor = rightColors.third,
                    increments = preset.increments,
                    isDarts = preset.countDownScore,
                    tennisDisplay = if (preset == SportPreset.TENNIS) tennisPointLabel(rightScore, leftScore) else null,
                    onIncrement = { delta -> adjustScore(false, delta) },
                    onDecrement = { adjustScore(false, if (preset.countDownScore) 1 else -1) },
                    onEditTeam = { editingTeam = false },
                    onManualScore = { if (preset.countDownScore) showDartsInputTeam = false else manualScoreTeam = false },
                    onIncrementSet = { pushUndo(); rightSets++ },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .testTag("scoreboard_right_panel"),
                )
            }

            // Bottom Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (scoreDiff == 0) "Tied Match" else "Margin: $scoreDiff pts",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.textSecondary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { undo() },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(palette.cardElevated),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Undo,
                            contentDescription = "Undo score",
                            tint = if (undoStack.isNotEmpty()) palette.textPrimary else palette.textMuted,
                        )
                    }
                    IconButton(
                        onClick = { swapSides() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(palette.cardElevated),
                    ) {
                        Icon(Icons.Rounded.SwapHoriz, contentDescription = "Swap sides", tint = palette.textPrimary)
                    }
                    IconButton(
                        onClick = { showNotesDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(palette.cardElevated),
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Match notes", tint = palette.textPrimary)
                    }
                    IconButton(
                        onClick = { resetMatch() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(palette.cardElevated),
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reset match", tint = palette.textPrimary)
                    }
                }
            }

            if (!fullscreen && matchNotes.isNotBlank()) {
                MonoCard(cornerRadius = 16.dp, contentPadding = PaddingValues(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Match Notes", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(matchNotes, style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                }
            }

            if (!fullscreen) {
                MonoActionButton(
                    text = "Save Match to History",
                    icon = Icons.Rounded.Save,
                    onClick = { saveMatchToHistory() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun tennisPointLabel(points: Int, opponentPoints: Int): String {
    if (points >= 3 && opponentPoints >= 3) {
        return when {
            points == opponentPoints -> "40"
            points > opponentPoints -> "AD"
            else -> "40"
        }
    }
    return when (points) {
        0 -> "0"
        1 -> "15"
        2 -> "30"
        else -> "40"
    }
}

@Composable
private fun ScoreTeamPanel(
    teamName: String,
    score: Int,
    sets: Int?,
    accentColor: Color,
    surfaceColor: Color,
    borderColor: Color,
    increments: List<Int>,
    isDarts: Boolean,
    tennisDisplay: String?,
    onIncrement: (Int) -> Unit,
    onDecrement: () -> Unit,
    onEditTeam: () -> Unit,
    onManualScore: () -> Unit,
    onIncrementSet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(surfaceColor)
            .border(1.5.dp, borderColor, shape)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Team Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onEditTeam)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = teamName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (sets != null) {
            Surface(
                onClick = onIncrementSet,
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.28f),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            ) {
                Text(
                    text = "SETS: $sets",
                    style = MonoTabularStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }

        // Giant Tap Score Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = { if (!isDarts) onIncrement(increments.first()) else onManualScore() }),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = tennisDisplay ?: score.toString(),
                    style = MonoTabularStyle.copy(
                        fontSize = if (score >= 100) 56.sp else 68.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (isDarts) "Tap to enter turn" else "Tap to +${increments.first()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }
        }

        // Quick Increment / Decrement Row
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                increments.take(3).forEach { inc ->
                    Surface(
                        onClick = { onIncrement(inc) },
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.22f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (inc > 0) "+$inc" else "$inc",
                                style = MonoTabularStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Surface(
                    onClick = onDecrement,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.30f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isDarts) "+1" else "-1",
                            style = MonoTabularStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
                Surface(
                    onClick = onManualScore,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.30f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.accentPrimary,
                        )
                    }
                }
            }
        }
    }
}
