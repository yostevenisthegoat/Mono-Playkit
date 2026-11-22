package com.example.feature.dice

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RandomNumberScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback

    var minStr by rememberSaveable { mutableStateOf("1") }
    var maxStr by rememberSaveable { mutableStateOf("100") }
    var count by rememberSaveable { mutableIntStateOf(1) }
    var uniqueOnly by rememberSaveable { mutableStateOf(true) }
    var sortResults by rememberSaveable { mutableStateOf(false) }
    var results by remember { mutableStateOf(listOf(Random.nextInt(1, 101))) }

    fun generate() {
        val rawMin = minStr.toIntOrNull() ?: 1
        val rawMax = maxStr.toIntOrNull() ?: 100
        val low = minOf(rawMin, rawMax)
        val high = maxOf(rawMin, rawMax)
        val span = (high.toLong() - low.toLong() + 1L).coerceAtLeast(1L)

        val generated = if (uniqueOnly && span >= count) {
            val picked = mutableSetOf<Int>()
            while (picked.size < count) {
                picked.add(Random.nextInt(low, high + 1))
            }
            picked.toList()
        } else {
            List(count) { Random.nextInt(low, high + 1) }
        }
        results = if (sortResults) generated.sorted() else generated
        feedback.play(SoundCue.CLICK)
    }

    MonoToolScaffold(
        title = "Random Number",
        subtitle = "Range & Batch Generator",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                MonoCard(cornerRadius = 24.dp, contentPadding = PaddingValues(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("RESULT", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        if (results.size == 1) {
                            Text(
                                text = results.first().toString(),
                                style = MonoTabularStyle.copy(fontSize = 54.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                results.forEach { num ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(palette.surfaceSecondary)
                                            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                    ) {
                                        Text(
                                            text = num.toString(),
                                            style = MonoTabularStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                            color = palette.textPrimary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = minStr,
                        onValueChange = { minStr = it.filter { ch -> ch.isDigit() || ch == '-' } },
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = maxStr,
                        onValueChange = { maxStr = it.filter { ch -> ch.isDigit() || ch == '-' } },
                        label = { Text("Max") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Text("Quantity: $count", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3, 5, 6, 10).forEach { q ->
                            MonoPillTag(label = "$q", selected = count == q, onClick = { count = q })
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("No duplicates", style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                        Switch(checked = uniqueOnly, onCheckedChange = { uniqueOnly = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Sort ascending", style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                        Switch(checked = sortResults, onCheckedChange = { sortResults = it })
                    }
                }
            }

            item {
                MonoActionButton(
                    text = "Generate",
                    icon = Icons.Rounded.Casino,
                    onClick = { generate() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
