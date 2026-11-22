package com.example.feature.main

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.formatRelativeTimestamp
import com.example.data.local.HistoryRecordEntity
import com.example.data.model.ToolCatalog
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoEmptyState
import com.example.ui.components.MonoIconTile
import com.example.ui.components.MonoPillTag
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme

@Composable
fun HistoryScreen(
    records: List<HistoryRecordEntity>,
    onDeleteRecord: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onOpenToolId: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showClearDialog by remember { mutableStateOf(false) }

    val categories = remember(records) {
        listOf("ALL") + records.map { it.category }.distinct()
    }
    val filtered = remember(records, selectedFilter) {
        if (selectedFilter == "ALL") records else records.filter { it.category == selectedFilter }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textSecondary,
            title = { Text("Clear all history?") },
            text = { Text("This permanently deletes all saved cube solves, scoreboard sessions, timer records, and dice logs.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearAllHistory()
                    },
                ) {
                    Text("Clear All", color = palette.textPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = palette.textSecondary)
                }
            },
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.displayMedium,
                            color = palette.textPrimary,
                        )
                        Text(
                            text = "${records.size} saved records",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                        )
                    }
                    if (records.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_history_button"),
                        ) {
                            Text("Clear All", color = palette.textSecondary)
                        }
                    }
                }
            }

            if (categories.size > 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            MonoPillTag(
                                label = if (cat == "ALL") "All" else cat,
                                selected = selectedFilter == cat,
                                onClick = { selectedFilter = cat },
                            )
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    MonoEmptyState(
                        icon = Icons.Rounded.History,
                        title = "No session history yet",
                        description = "Completed cube solves, saved scoreboard matches, timer laps, and dice rolls appear here automatically.",
                    )
                }
            } else {
                items(filtered, key = { it.id }) { record ->
                    val tool = remember(record.toolId) { ToolCatalog.findById(record.toolId) }
                    MonoCard(
                        onClick = { onOpenToolId(record.toolId) },
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_record_${record.id}"),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MonoIconTile(
                                icon = tool?.icon ?: Icons.Rounded.History,
                                contentDescription = record.title,
                                size = 42.dp,
                                iconSize = 20.dp,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = record.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = palette.textPrimary,
                                    )
                                    Text(
                                        text = formatRelativeTimestamp(record.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textMuted,
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = record.summary,
                                    style = MonoTabularStyle.copy(
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = palette.textPrimary,
                                )
                                if (record.detail.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = record.detail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary,
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onDeleteRecord(record.id) },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete record",
                                    tint = palette.textMuted,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
