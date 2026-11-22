package com.example.feature.main

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayTool
import com.example.data.model.ToolCatalog
import com.example.data.model.ToolCategory
import com.example.ui.components.MonoEmptyState
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    recentSearches: List<String>,
    favoriteIds: Set<String>,
    onBack: () -> Unit,
    onOpenTool: (PlayTool, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
) {
    val palette = MonoTheme.palette
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ToolCategory?>(null) }

    val results = remember(query, selectedCategory) {
        ToolCatalog.search(query, selectedCategory)
    }

    val suggestions = remember {
        listOf("Chess", "Dice", "Basketball", "Cube", "Stopwatch", "Scoreboard", "Monopoly", "Metronome")
    }

    MonoToolScaffold(
        title = "Search Utilities",
        subtitle = "Find any companion tool",
        onBack = onBack,
    ) { contentModifier ->
        Column(modifier = contentModifier.padding(horizontal = 20.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        text = "Search Chess, Dice, Basketball, Cube...",
                        color = palette.textMuted,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = palette.textSecondary,
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = palette.textSecondary,
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {}),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = palette.cardPrimary,
                    unfocusedContainerColor = palette.cardPrimary,
                    focusedBorderColor = palette.accentPrimary,
                    unfocusedBorderColor = palette.border,
                    focusedTextColor = palette.textPrimary,
                    unfocusedTextColor = palette.textPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    MonoPillTag(
                        label = "All",
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                    )
                }
                items(ToolCategory.entries) { cat ->
                    MonoPillTag(
                        label = cat.title,
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (query.isBlank() && selectedCategory == null) {
                    if (recentSearches.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textPrimary,
                                )
                                TextButton(onClick = onClearRecentSearches) {
                                    Text("Clear", color = palette.textSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                recentSearches.forEach { term ->
                                    MonoPillTag(
                                        label = term,
                                        onClick = { query = term },
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Popular Searches",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            suggestions.forEach { term ->
                                MonoPillTag(
                                    label = term,
                                    onClick = { query = term },
                                )
                            }
                        }
                    }
                }

                if (results.isEmpty()) {
                    item {
                        MonoEmptyState(
                            icon = Icons.Rounded.Search,
                            title = "No matching utilities",
                            description = "Try searching for Chess, Dice, Scoreboard, Cube, or Monopoly.",
                            actionLabel = "Reset Search",
                            onAction = {
                                query = ""
                                selectedCategory = null
                            },
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "${results.size} utilities",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textSecondary,
                        )
                    }
                    items(results, key = { it.id }) { tool ->
                        ToolListCard(
                            tool = tool,
                            isFavorite = tool.id in favoriteIds,
                            onClick = { onOpenTool(tool, query) },
                            onToggleFavorite = { onToggleFavorite(tool.id) },
                        )
                    }
                }
            }
        }
    }
}
