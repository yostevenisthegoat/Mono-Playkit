package com.example.feature.boardgames

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import kotlin.random.Random

@Composable
fun JengaSelectorScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback

    var minBlock by rememberSaveable { mutableIntStateOf(1) }
    var maxBlock by rememberSaveable { mutableIntStateOf(54) }
    var noRepeat by rememberSaveable { mutableStateOf(true) }
    var selectedBlock by rememberSaveable { mutableIntStateOf(27) }
    val drawnBlocks = remember { mutableStateListOf<Int>() }

    fun pickBlock() {
        val low = minOf(minBlock, maxBlock)
        val high = maxOf(minBlock, maxBlock)
        val available = if (noRepeat) (low..high).filterNot { it in drawnBlocks } else (low..high).toList()
        if (available.isEmpty()) {
            drawnBlocks.clear()
        }
        val pool = if (available.isEmpty()) (low..high).toList() else available
        val picked = pool[Random.nextInt(pool.size)]
        selectedBlock = picked
        drawnBlocks.add(0, picked)
        if (drawnBlocks.size > 60) drawnBlocks.removeAt(drawnBlocks.lastIndex)
        feedback.play(SoundCue.CLICK)
    }

    val layer = ((selectedBlock - 1).coerceAtLeast(0) / 3) + 1
    val position = when ((selectedBlock - 1).coerceAtLeast(0) % 3) {
        0 -> "Left"
        1 -> "Center"
        else -> "Right"
    }

    MonoToolScaffold(
        title = "Jenga Block Selector",
        subtitle = "Custom Range & Tower Layer Guide",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(onClick = { drawnBlocks.clear() }) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset pool", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                MonoCard(cornerRadius = 26.dp, contentPadding = PaddingValues(28.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("PULL BLOCK", style = MaterialTheme.typography.labelLarge, color = palette.textMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "#$selectedBlock",
                            style = MonoTabularStyle.copy(fontSize = 64.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Layer $layer • $position Slot",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textSecondary,
                        )
                    }
                }
            }

            item {
                MonoActionButton(
                    text = "Pick Random Block",
                    icon = Icons.Rounded.Casino,
                    onClick = { pickBlock() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = minBlock.toString(),
                            onValueChange = { minBlock = (it.filter(Char::isDigit).toIntOrNull() ?: 1).coerceIn(1, 200) },
                            label = { Text("Min Block") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = maxBlock.toString(),
                            onValueChange = { maxBlock = (it.filter(Char::isDigit).toIntOrNull() ?: 54).coerceIn(1, 200) },
                            label = { Text("Max Block") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Prevent repeating drawn blocks", style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                        Switch(checked = noRepeat, onCheckedChange = { noRepeat = it })
                    }
                }
            }

            if (drawnBlocks.isNotEmpty()) {
                item {
                    Text("Drawn History (${drawnBlocks.size})", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
                items(drawnBlocks.chunked(6)) { row ->
                    MonoCard(cornerRadius = 14.dp, contentPadding = PaddingValues(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = row.joinToString("   ") { "#$it" },
                            style = MonoTabularStyle.copy(fontSize = 14.sp),
                            color = palette.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
