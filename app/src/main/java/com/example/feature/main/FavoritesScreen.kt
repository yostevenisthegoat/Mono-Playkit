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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayTool
import com.example.data.model.ToolCatalog
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoEmptyState
import com.example.ui.components.MonoIconTile
import com.example.ui.theme.MonoTheme

@Composable
fun FavoritesScreen(
    favoriteIds: List<String>,
    onOpenTool: (PlayTool) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onMoveFavorite: (String, Int) -> Unit,
    onBrowseExplore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val favoriteTools = remember(favoriteIds) {
        favoriteIds.mapNotNull { ToolCatalog.findById(it) }
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.displayMedium,
                        color = palette.textPrimary,
                    )
                    Text(
                        text = "${favoriteTools.size} pinned utilities for quick launch",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                    )
                }
            }

            if (favoriteTools.isEmpty()) {
                item {
                    MonoEmptyState(
                        icon = Icons.Rounded.Star,
                        title = "No pinned favorites yet",
                        description = "Tap the star icon inside any tool or in Explore to pin your go-to utilities here.",
                        actionLabel = "Browse Utilities",
                        onAction = onBrowseExplore,
                    )
                }
            } else {
                itemsIndexed(favoriteTools, key = { _, tool -> tool.id }) { index, tool ->
                    MonoCard(
                        onClick = { onOpenTool(tool) },
                        cornerRadius = 20.dp,
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("favorite_card_${tool.id}"),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MonoIconTile(
                                icon = tool.icon,
                                contentDescription = tool.name,
                                size = 46.dp,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tool.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = tool.category.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary,
                                )
                            }
                            IconButton(
                                onClick = { onMoveFavorite(tool.id, -1) },
                                enabled = index > 0,
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowUpward,
                                    contentDescription = "Move up",
                                    tint = if (index > 0) palette.textSecondary else palette.textMuted.copy(alpha = 0.35f),
                                )
                            }
                            IconButton(
                                onClick = { onMoveFavorite(tool.id, 1) },
                                enabled = index < favoriteTools.lastIndex,
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDownward,
                                    contentDescription = "Move down",
                                    tint = if (index < favoriteTools.lastIndex) palette.textSecondary else palette.textMuted.copy(alpha = 0.35f),
                                )
                            }
                            IconButton(
                                onClick = { onToggleFavorite(tool.id) },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "Unpin favorite",
                                    tint = palette.textPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
