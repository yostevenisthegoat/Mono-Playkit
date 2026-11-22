package com.example.feature.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayTool
import com.example.data.model.ToolCatalog
import com.example.data.model.ToolCategory
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoIconTile
import com.example.ui.components.MonoLogoMark
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    favoriteIds: List<String>,
    recentToolIds: List<String>,
    onOpenTool: (PlayTool) -> Unit,
    onOpenCategory: (ToolCategory) -> Unit,
    onOpenExplore: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val featuredTools = remember {
        ToolCatalog.featuredToolIds.mapNotNull { ToolCatalog.findById(it) }
    }
    val quickTools = remember {
        ToolCatalog.quickToolIds.mapNotNull { ToolCatalog.findById(it) }
    }
    val favoriteTools = remember(favoriteIds) {
        favoriteIds.mapNotNull { ToolCatalog.findById(it) }
    }
    val recentTools = remember(recentToolIds) {
        recentToolIds.mapNotNull { ToolCatalog.findById(it) }
    }

    var featuredIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(featuredTools.size) {
        while (true) {
            delay(5000L)
            if (featuredTools.isNotEmpty()) {
                featuredIndex = (featuredIndex + 1) % featuredTools.size
            }
        }
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
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            // Header matching reference "Hello there! / Welcome to Mono" + Search + Settings
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello there!",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textSecondary,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Welcome to Mono PlayKit",
                            style = MaterialTheme.typography.headlineLarge,
                            color = palette.textPrimary,
                        )
                    }
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_search_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search utilities",
                            tint = palette.textPrimary,
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_settings_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = palette.textPrimary,
                        )
                    }
                }
            }

            // Rotating Featured Utility Card (inspired by reference ecosystem banner)
            item {
                val activeFeatured = featuredTools.getOrNull(featuredIndex)
                if (activeFeatured != null) {
                    MonoCard(
                        onClick = { onOpenTool(activeFeatured) },
                        cornerRadius = 22.dp,
                        contentPadding = PaddingValues(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("featured_tool_card"),
                    ) {
                        AnimatedContent(
                            targetState = activeFeatured,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "featured_tool_anim",
                        ) { tool ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(palette.surfaceSecondary)
                                        .border(1.dp, palette.border, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = tool.name,
                                        tint = palette.textPrimary,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "FEATURED TOOL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textMuted,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tool.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = palette.textPrimary,
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = tool.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = palette.textSecondary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            featuredTools.forEachIndexed { idx, _ ->
                                val active = idx == featuredIndex
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .height(6.dp)
                                        .width(if (active) 20.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (active) palette.accentPrimary else palette.border)
                                        .clickable { featuredIndex = idx },
                                )
                            }
                        }
                    }
                }
            }

            // Quick Utilities Grid (2 columns matching reference "Our Apps" grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Quick Utilities",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary,
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onOpenExplore)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("see_all_tools_button"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "See all",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textSecondary,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = palette.textSecondary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    quickTools.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            rowItems.forEach { tool ->
                                QuickUtilityGridCard(
                                    tool = tool,
                                    onClick = { onOpenTool(tool) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Favorites Section (if pinned)
            if (favoriteTools.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Pinned Favorites",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(favoriteTools, key = { it.id }) { tool ->
                                CompactHorizontalToolCard(
                                    tool = tool,
                                    badgeIcon = Icons.Rounded.Star,
                                    onClick = { onOpenTool(tool) },
                                )
                            }
                        }
                    }
                }
            }

            // Recently Used Section
            if (recentTools.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Recently Used",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(recentTools, key = { it.id }) { tool ->
                                CompactHorizontalToolCard(
                                    tool = tool,
                                    badgeIcon = null,
                                    onClick = { onOpenTool(tool) },
                                )
                            }
                        }
                    }
                }
            }

            // Categories Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary,
                    )
                    ToolCategory.entries.forEach { category ->
                        val count = remember(category) { ToolCatalog.toolsByCategory(category).size }
                        MonoCard(
                            onClick = { onOpenCategory(category) },
                            cornerRadius = 18.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_card_${category.id}"),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MonoIconTile(
                                    icon = category.icon,
                                    contentDescription = category.title,
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.textPrimary,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${category.subtitle} • $count tools",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary,
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = palette.textMuted,
                                )
                            }
                        }
                    }
                }
            }

            // Mono Open-Source Philosophy footer card (matches reference bottom card)
            item {
                MonoCard(
                    onClick = onOpenSettings,
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(palette.surfaceSecondary)
                                .border(1.dp, palette.border, RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            MonoLogoMark(size = 28.dp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "100% Free & Offline",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary,
                            )
                            Text(
                                text = "No ads, no accounts, no tracking — built for physical play.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = palette.textMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickUtilityGridCard(
    tool: PlayTool,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    MonoCard(
        onClick = onClick,
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 18.dp),
        modifier = modifier.testTag("quick_tool_${tool.id}"),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MonoIconTile(
                icon = tool.icon,
                contentDescription = tool.name,
                size = 46.dp,
                iconSize = 24.dp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tool.shortTitle,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tool.category.title,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CompactHorizontalToolCard(
    tool: PlayTool,
    badgeIcon: androidx.compose.ui.graphics.vector.ImageVector?,
    onClick: () -> Unit,
) {
    val palette = MonoTheme.palette
    MonoCard(
        onClick = onClick,
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(14.dp),
        modifier = Modifier
            .width(152.dp)
            .testTag("compact_tool_${tool.id}"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            MonoIconTile(
                icon = tool.icon,
                contentDescription = tool.name,
                size = 40.dp,
                iconSize = 20.dp,
            )
            if (badgeIcon != null) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = palette.textSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = tool.shortTitle,
            style = MaterialTheme.typography.titleSmall,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = tool.category.title,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
