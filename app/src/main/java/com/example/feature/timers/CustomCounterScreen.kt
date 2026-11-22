package com.example.feature.timers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PlusOne
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoEmptyState
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.launch

@Composable
fun CustomCounterScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val container = LocalAppContainer.current
    val feedback = container.feedback
    val repo = container.repository
    val scope = rememberCoroutineScope()

    val counters by repo.savedCounters.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    // Seed one default counter on first visit if empty
    var seeded by remember { mutableStateOf(false) }
    LaunchedEffect(counters.size) {
        if (!seeded && counters.isEmpty()) {
            seeded = true
            repo.addSavedCounter("Main Tally", 1)
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var stepStr by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("New Named Counter", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Counter Name (e.g. Points, Laps)") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = stepStr,
                        onValueChange = { stepStr = it.filter(Char::isDigit) },
                        label = { Text("Step Size") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repo.addSavedCounter(
                                name = name.ifBlank { "Counter ${counters.size + 1}" },
                                step = (stepStr.toIntOrNull() ?: 1).coerceAtLeast(1),
                            )
                        }
                        showAddDialog = false
                    },
                ) { Text("Create", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    MonoToolScaffold(
        title = "Multi-Counter Studio",
        subtitle = "Multiple Persistent Named Counters",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add counter", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (counters.isEmpty()) {
                item {
                    MonoEmptyState(
                        icon = Icons.Rounded.PlusOne,
                        title = "No counters yet",
                        description = "Create named counters to track turns, stock, points, or repetitions.",
                        actionLabel = "Create Counter",
                        onAction = { showAddDialog = true },
                    )
                }
            } else {
                items(counters, key = { it.id }) { counter ->
                    MonoCard(
                        cornerRadius = 22.dp,
                        contentPadding = PaddingValues(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(counter.name, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                Text("Step ±${counter.step}", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                            }
                            Row {
                                IconButton(
                                    onClick = {
                                        feedback.play(SoundCue.CLICK)
                                        scope.launch { repo.updateSavedCounter(counter.copy(value = 0)) }
                                    },
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = "Reset counter", tint = palette.textSecondary)
                                }
                                IconButton(
                                    onClick = { scope.launch { repo.deleteSavedCounter(counter.id) } },
                                ) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete counter", tint = palette.textMuted)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = {
                                    feedback.play(SoundCue.CLICK)
                                    scope.launch {
                                        repo.updateSavedCounter(counter.copy(value = counter.value - counter.step))
                                    }
                                },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(palette.surfaceSecondary),
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Subtract", tint = palette.textPrimary)
                            }

                            Text(
                                text = counter.value.toString(),
                                style = MonoTabularStyle.copy(fontSize = 44.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )

                            IconButton(
                                onClick = {
                                    feedback.play(SoundCue.CLICK)
                                    scope.launch {
                                        repo.updateSavedCounter(counter.copy(value = counter.value + counter.step))
                                    }
                                },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(palette.accentPrimary),
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = "Add",
                                    tint = if (palette.isDark) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color.White,
                                )
                            }
                        }
                    }
                }
            }

            item {
                MonoActionButton(
                    text = "Add Named Counter",
                    icon = Icons.Rounded.Add,
                    primary = false,
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
