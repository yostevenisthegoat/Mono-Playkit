package com.example.feature.dice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Remove
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.data.model.ToolCatalog
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class DiceRollRecord(
    val formula: String,
    val rolls: List<Int>,
    val modifier: Int,
    val total: Int,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiceRollerScreen(
    isFavorite: Boolean,
    saveDiceHistory: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val container = LocalAppContainer.current
    val feedback = container.feedback
    val repo = container.repository
    val scope = rememberCoroutineScope()

    var selectedSides by rememberSaveable { mutableIntStateOf(6) }
    var diceCount by rememberSaveable { mutableIntStateOf(2) }
    var modifierVal by rememberSaveable { mutableIntStateOf(0) }
    var customSidesDialog by remember { mutableStateOf(false) }

    var lastRolls by remember { mutableStateOf(listOf(4, 6)) }
    val sessionHistory = remember { mutableStateListOf<DiceRollRecord>() }

    val scaleAnim = remember { Animatable(1f) }
    val rotateAnim = remember { Animatable(0f) }

    val standardDice = remember { listOf(4, 6, 8, 10, 12, 20, 100) }

    fun rollDice() {
        val rolls = List(diceCount) { Random.nextInt(1, selectedSides + 1) }
        val total = rolls.sum() + modifierVal
        lastRolls = rolls
        val modStr = when {
            modifierVal > 0 -> "+$modifierVal"
            modifierVal < 0 -> "$modifierVal"
            else -> ""
        }
        val formula = "${diceCount}D$selectedSides$modStr"
        sessionHistory.add(0, DiceRollRecord(formula, rolls, modifierVal, total))
        if (sessionHistory.size > 30) sessionHistory.removeAt(sessionHistory.lastIndex)

        feedback.play(SoundCue.CLICK)

        scope.launch {
            scaleAnim.snapTo(0.82f)
            rotateAnim.snapTo(-14f)
            launch { scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
            launch { rotateAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        }

        if (saveDiceHistory) {
            scope.launch {
                repo.recordHistory(
                    toolId = ToolCatalog.TOOL_DICE_ROLLER,
                    category = "Dice",
                    title = "Dice Roll ($formula)",
                    summary = "Total: $total",
                    detail = "Dice: ${rolls.joinToString(", ")}",
                )
            }
        }
    }

    if (customSidesDialog) {
        var sidesInput by remember { mutableStateOf(selectedSides.toString()) }
        AlertDialog(
            onDismissRequest = { customSidesDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Custom Die Sides", color = palette.textPrimary) },
            text = {
                OutlinedTextField(
                    value = sidesInput,
                    onValueChange = { sidesInput = it.filter(Char::isDigit) },
                    label = { Text("Sides (2–1000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedSides = (sidesInput.toIntOrNull() ?: 6).coerceIn(2, 1000)
                        customSidesDialog = false
                    },
                ) { Text("Set", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { customSidesDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    val currentTotal = lastRolls.sum() + modifierVal

    MonoToolScaffold(
        title = "Dice Roller",
        subtitle = "Polyhedral & Custom Dice",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Die Type Pills
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(standardDice) { sides ->
                        MonoPillTag(
                            label = "D$sides",
                            selected = selectedSides == sides,
                            onClick = { selectedSides = sides },
                        )
                    }
                    item {
                        MonoPillTag(
                            label = if (selectedSides !in standardDice) "D$selectedSides (Custom)" else "Custom",
                            selected = selectedSides !in standardDice,
                            onClick = { customSidesDialog = true },
                        )
                    }
                }
            }

            // Centered Mono Result Display
            item {
                MonoCard(
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .scale(scaleAnim.value)
                                .rotate(rotateAnim.value),
                            contentAlignment = Alignment.Center,
                        ) {
                            MonoDieShape(sides = selectedSides, modifier = Modifier.fillMaxSize())
                            Text(
                                text = currentTotal.toString(),
                                style = MonoTabularStyle.copy(
                                    fontSize = if (currentTotal >= 100) 30.sp else 38.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = palette.textPrimary,
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val modLabel = when {
                            modifierVal > 0 -> " + $modifierVal"
                            modifierVal < 0 -> " - ${-modifierVal}"
                            else -> ""
                        }
                        Text(
                            text = "${diceCount}D$selectedSides$modLabel",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textSecondary,
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Individual Die Results
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            lastRolls.forEach { dieVal ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(palette.surfaceSecondary)
                                        .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = dieVal.toString(),
                                        style = MonoTabularStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                        color = palette.textPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quantity & Modifier Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MonoCard(
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Quantity", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { if (diceCount > 1) diceCount-- },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(palette.surfaceSecondary),
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease dice", tint = palette.textPrimary)
                            }
                            Text(
                                text = "$diceCount",
                                style = MonoTabularStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                            IconButton(
                                onClick = { if (diceCount < 20) diceCount++ },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(palette.surfaceSecondary),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Increase dice", tint = palette.textPrimary)
                            }
                        }
                    }

                    MonoCard(
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Modifier", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { if (modifierVal > -50) modifierVal-- },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(palette.surfaceSecondary),
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease modifier", tint = palette.textPrimary)
                            }
                            Text(
                                text = if (modifierVal > 0) "+$modifierVal" else "$modifierVal",
                                style = MonoTabularStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                            IconButton(
                                onClick = { if (modifierVal < 50) modifierVal++ },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(palette.surfaceSecondary),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Increase modifier", tint = palette.textPrimary)
                            }
                        }
                    }
                }
            }

            item {
                MonoActionButton(
                    text = "Roll ${diceCount}D$selectedSides",
                    icon = Icons.Rounded.Casino,
                    onClick = { rollDice() },
                    testTag = "roll_dice_button",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (sessionHistory.isNotEmpty()) {
                item {
                    Text("Roll History", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
                items(sessionHistory) { item ->
                    MonoCard(
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(item.formula, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                                Text(
                                    text = "Rolled: ${item.rolls.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary,
                                )
                            }
                            Text(
                                text = "${item.total}",
                                style = MonoTabularStyle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonoDieShape(
    sides: Int,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.44f
        val vertices = when (sides) {
            4 -> 3
            6 -> 4
            8 -> 4
            10 -> 5
            12 -> 6
            20 -> 6
            else -> 8
        }
        val angleOffset = when (sides) {
            4 -> -Math.PI / 2.0
            6 -> Math.PI / 4.0
            8 -> 0.0
            else -> -Math.PI / 2.0
        }
        val path = Path()
        for (i in 0 until vertices) {
            val angle = angleOffset + (2.0 * Math.PI * i / vertices)
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path = path, color = palette.surfaceSecondary)
        drawPath(
            path = path,
            color = palette.accentPrimary.copy(alpha = 0.7f),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
