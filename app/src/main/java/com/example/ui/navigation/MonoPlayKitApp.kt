package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.LocalAppContainer
import com.example.data.model.PlayTool
import com.example.data.model.ToolCatalog
import com.example.data.model.ToolCategory
import com.example.data.preferences.UserPreferences
import com.example.feature.boardgames.BingoCallerScreen
import com.example.feature.boardgames.ChessClockScreen
import com.example.feature.boardgames.DominoesScreen
import com.example.feature.boardgames.JengaSelectorScreen
import com.example.feature.boardgames.LudoSnakesScreen
import com.example.feature.boardgames.MonopolyBankScreen
import com.example.feature.boardgames.ScrabbleScreen
import com.example.feature.boardgames.TangramGuideScreen
import com.example.feature.cards.PlayingCardsScreen
import com.example.feature.cards.TcgCounterScreen
import com.example.feature.dice.CoinFlipScreen
import com.example.feature.dice.DiceRollerScreen
import com.example.feature.dice.RandomNumberScreen
import com.example.feature.dice.TeamGeneratorScreen
import com.example.feature.main.ExploreScreen
import com.example.feature.main.FavoritesScreen
import com.example.feature.main.HistoryScreen
import com.example.feature.main.HomeScreen
import com.example.feature.main.SearchScreen
import com.example.feature.main.SettingsScreen
import com.example.feature.main.SplashScreen
import com.example.feature.puzzles.CubeSolverScreen
import com.example.feature.puzzles.CubeTimerScreen
import com.example.feature.puzzles.PuzzleGuidesScreen
import com.example.feature.puzzles.PyraminxSolverScreen
import com.example.feature.puzzles.SkewbSolverScreen
import com.example.feature.scoreboard.UniversalScoreboardScreen
import com.example.feature.timers.CountdownTimerScreen
import com.example.feature.timers.CustomCounterScreen
import com.example.feature.timers.IntervalTimerScreen
import com.example.feature.timers.StopwatchScreen
import com.example.feature.toys.FidgetSpinnerScreen
import com.example.feature.toys.KendamaTrackerScreen
import com.example.feature.toys.LegoInventoryScreen
import com.example.feature.toys.MetronomeScreen
import com.example.feature.toys.ModelKitsScreen
import com.example.feature.toys.YoYoTrackerScreen
import com.example.ui.components.MonoBackground
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.launch

private enum class MainTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("tab/home", "Home", Icons.Rounded.Home, Icons.Outlined.Home),
    EXPLORE("tab/explore", "Explore", Icons.Rounded.Explore, Icons.Outlined.Explore),
    FAVORITES("tab/favorites", "Favorites", Icons.Rounded.Star, Icons.Outlined.StarOutline),
    HISTORY("tab/history", "History", Icons.Rounded.History, Icons.Outlined.History),
    SETTINGS("tab/settings", "Settings", Icons.Rounded.Settings, Icons.Outlined.Settings),
}

private const val ROUTE_SEARCH = "screen/search"

@Composable
fun MonoPlayKitApp(preferences: UserPreferences) {
    val container = LocalAppContainer.current
    val repo = container.repository
    val feedback = container.feedback
    val scope = rememberCoroutineScope()

    LaunchedEffect(preferences.soundEnabled, preferences.hapticEnabled) {
        feedback.soundEnabled = preferences.soundEnabled
        feedback.hapticEnabled = preferences.hapticEnabled
    }

    var showSplash by rememberSaveable { mutableStateOf(true) }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val favoritesList by repo.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteIdsOrdered = remember(favoritesList) { favoritesList.map { it.toolId } }
    val favoriteIdsSet = remember(favoriteIdsOrdered) { favoriteIdsOrdered.toSet() }
    val allHistory by repo.allHistory.collectAsStateWithLifecycle(initialValue = emptyList())

    var exploreCategoryFilter by rememberSaveable { mutableStateOf<ToolCategory?>(null) }

    fun navigateToTab(tab: MainTab) {
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openTool(tool: PlayTool) {
        scope.launch { repo.recordRecentTool(tool.id) }
        navController.navigate(tool.route)
    }

    fun openToolById(toolId: String) {
        ToolCatalog.findById(toolId)?.let { openTool(it) }
    }

    val isMainTabRoute = MainTab.entries.any { it.route == currentRoute }

    Box(modifier = Modifier.fillMaxSize()) {
        MonoBackground {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 840.dp && isMainTabRoute

                Row(modifier = Modifier.fillMaxSize()) {
                    if (useNavigationRail) {
                        val palette = MonoTheme.palette
                        NavigationRail(
                            containerColor = palette.navBarBg,
                            modifier = Modifier.fillMaxHeight(),
                        ) {
                            Spacer(modifier = Modifier.height(24.dp))
                            MainTab.entries.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label,
                                        )
                                    },
                                    label = { Text(tab.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = palette.textPrimary,
                                        selectedTextColor = palette.textPrimary,
                                        unselectedIconColor = palette.textMuted,
                                        unselectedTextColor = palette.textMuted,
                                        indicatorColor = palette.surfaceSecondary,
                                    ),
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = MainTab.HOME.route,
                        ) {
                            composable(MainTab.HOME.route) {
                                HomeScreen(
                                    favoriteIds = favoriteIdsOrdered,
                                    recentToolIds = preferences.recentTools,
                                    onOpenTool = ::openTool,
                                    onOpenCategory = { cat ->
                                        exploreCategoryFilter = cat
                                        navigateToTab(MainTab.EXPLORE)
                                    },
                                    onOpenExplore = {
                                        exploreCategoryFilter = null
                                        navigateToTab(MainTab.EXPLORE)
                                    },
                                    onOpenSearch = { navController.navigate(ROUTE_SEARCH) },
                                    onOpenSettings = { navigateToTab(MainTab.SETTINGS) },
                                )
                            }
                            composable(MainTab.EXPLORE.route) {
                                ExploreScreen(
                                    selectedCategory = exploreCategoryFilter,
                                    favoriteIds = favoriteIdsSet,
                                    onSelectCategory = { exploreCategoryFilter = it },
                                    onOpenTool = ::openTool,
                                    onToggleFavorite = { id -> scope.launch { repo.toggleFavorite(id) } },
                                    onOpenSearch = { navController.navigate(ROUTE_SEARCH) },
                                )
                            }
                            composable(MainTab.FAVORITES.route) {
                                FavoritesScreen(
                                    favoriteIds = favoriteIdsOrdered,
                                    onOpenTool = ::openTool,
                                    onToggleFavorite = { id -> scope.launch { repo.toggleFavorite(id) } },
                                    onMoveFavorite = { id, delta -> scope.launch { repo.moveFavorite(id, delta) } },
                                    onBrowseExplore = { navigateToTab(MainTab.EXPLORE) },
                                )
                            }
                            composable(MainTab.HISTORY.route) {
                                HistoryScreen(
                                    records = allHistory,
                                    onDeleteRecord = { id -> scope.launch { repo.deleteHistoryItem(id) } },
                                    onClearAllHistory = { scope.launch { repo.clearAllHistory() } },
                                    onOpenToolId = ::openToolById,
                                )
                            }
                            composable(MainTab.SETTINGS.route) {
                                SettingsScreen(
                                    preferences = preferences,
                                    onOpenSearch = { navController.navigate(ROUTE_SEARCH) },
                                )
                            }
                            composable(ROUTE_SEARCH) {
                                SearchScreen(
                                    recentSearches = preferences.recentSearches,
                                    favoriteIds = favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onOpenTool = { tool, q ->
                                        if (q.isNotBlank()) scope.launch { repo.recordRecentSearch(q) }
                                        openTool(tool)
                                    },
                                    onToggleFavorite = { id -> scope.launch { repo.toggleFavorite(id) } },
                                    onClearRecentSearches = { scope.launch { repo.clearRecentSearches() } },
                                )
                            }

                            // Utility Routes
                            composable("tool/scoreboard") {
                                UniversalScoreboardScreen(
                                    defaultPresetId = preferences.defaultScoreboardPreset,
                                    isFavorite = ToolCatalog.TOOL_SCOREBOARD in favoriteIdsSet,
                                    keepScreenOn = preferences.keepScreenOn,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_SCOREBOARD) } },
                                )
                            }
                            composable("tool/cube_timer") {
                                CubeTimerScreen(
                                    inspectionSeconds = preferences.cubeInspectionSeconds,
                                    isFavorite = ToolCatalog.TOOL_CUBE_TIMER in favoriteIdsSet,
                                    keepScreenOn = preferences.keepScreenOn,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_CUBE_TIMER) } },
                                )
                            }
                            composable("tool/cube_solver") {
                                CubeSolverScreen(
                                    isFavorite = ToolCatalog.TOOL_CUBE_SOLVER in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_CUBE_SOLVER) } },
                                )
                            }
                            composable("tool/pyraminx_solver") {
                                PyraminxSolverScreen(
                                    isFavorite = ToolCatalog.TOOL_PYRAMINX_SOLVER in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_PYRAMINX_SOLVER) } },
                                )
                            }
                            composable("tool/skewb_solver") {
                                SkewbSolverScreen(
                                    isFavorite = ToolCatalog.TOOL_SKEWB_SOLVER in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_SKEWB_SOLVER) } },
                                )
                            }
                            composable("tool/puzzle_guides") {
                                PuzzleGuidesScreen(
                                    isFavorite = ToolCatalog.TOOL_PUZZLE_GUIDES in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_PUZZLE_GUIDES) } },
                                )
                            }
                            composable("tool/chess_clock") {
                                ChessClockScreen(
                                    isFavorite = ToolCatalog.TOOL_CHESS_CLOCK in favoriteIdsSet,
                                    keepScreenOn = preferences.keepScreenOn,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_CHESS_CLOCK) } },
                                )
                            }
                            composable("tool/monopoly_bank") {
                                MonopolyBankScreen(
                                    isFavorite = ToolCatalog.TOOL_MONOPOLY_BANK in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_MONOPOLY_BANK) } },
                                )
                            }
                            composable("tool/scrabble_tool") {
                                ScrabbleScreen(
                                    isFavorite = ToolCatalog.TOOL_SCRABBLE in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_SCRABBLE) } },
                                )
                            }
                            composable("tool/ludo_snakes") {
                                LudoSnakesScreen(
                                    isFavorite = ToolCatalog.TOOL_LUDO_SNAKES in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_LUDO_SNAKES) } },
                                )
                            }
                            composable("tool/dominoes_calc") {
                                DominoesScreen(
                                    isFavorite = ToolCatalog.TOOL_DOMINOES in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_DOMINOES) } },
                                )
                            }
                            composable("tool/bingo_caller") {
                                BingoCallerScreen(
                                    isFavorite = ToolCatalog.TOOL_BINGO in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_BINGO) } },
                                )
                            }
                            composable("tool/jenga_selector") {
                                JengaSelectorScreen(
                                    isFavorite = ToolCatalog.TOOL_JENGA in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_JENGA) } },
                                )
                            }
                            composable("tool/tangram_guide") {
                                TangramGuideScreen(
                                    isFavorite = ToolCatalog.TOOL_TANGRAM in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_TANGRAM) } },
                                )
                            }
                            composable("tool/dice_roller") {
                                DiceRollerScreen(
                                    isFavorite = ToolCatalog.TOOL_DICE_ROLLER in favoriteIdsSet,
                                    saveDiceHistory = preferences.saveDiceHistory,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_DICE_ROLLER) } },
                                )
                            }
                            composable("tool/coin_flip") {
                                CoinFlipScreen(
                                    isFavorite = ToolCatalog.TOOL_COIN_FLIP in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_COIN_FLIP) } },
                                )
                            }
                            composable("tool/random_number") {
                                RandomNumberScreen(
                                    isFavorite = ToolCatalog.TOOL_RNG in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_RNG) } },
                                )
                            }
                            composable("tool/team_generator") {
                                TeamGeneratorScreen(
                                    isFavorite = ToolCatalog.TOOL_TEAM_GENERATOR in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_TEAM_GENERATOR) } },
                                )
                            }
                            composable("tool/tcg_counter") {
                                TcgCounterScreen(
                                    isFavorite = ToolCatalog.TOOL_TCG_COUNTER in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_TCG_COUNTER) } },
                                )
                            }
                            composable("tool/playing_cards") {
                                PlayingCardsScreen(
                                    isFavorite = ToolCatalog.TOOL_PLAYING_CARDS in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_PLAYING_CARDS) } },
                                )
                            }
                            composable("tool/fidget_spinner") {
                                FidgetSpinnerScreen(
                                    isFavorite = ToolCatalog.TOOL_FIDGET_SPINNER in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_FIDGET_SPINNER) } },
                                )
                            }
                            composable("tool/yoyo_tracker") {
                                YoYoTrackerScreen(
                                    isFavorite = ToolCatalog.TOOL_YOYO_TRACKER in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_YOYO_TRACKER) } },
                                )
                            }
                            composable("tool/kendama_tracker") {
                                KendamaTrackerScreen(
                                    isFavorite = ToolCatalog.TOOL_KENDAMA_TRACKER in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_KENDAMA_TRACKER) } },
                                )
                            }
                            composable("tool/lego_inventory") {
                                LegoInventoryScreen(
                                    isFavorite = ToolCatalog.TOOL_LEGO_INVENTORY in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_LEGO_INVENTORY) } },
                                )
                            }
                            composable("tool/model_kits") {
                                ModelKitsScreen(
                                    isFavorite = ToolCatalog.TOOL_MODEL_KITS in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_MODEL_KITS) } },
                                )
                            }
                            composable("tool/metronome") {
                                MetronomeScreen(
                                    isFavorite = ToolCatalog.TOOL_METRONOME in favoriteIdsSet,
                                    keepScreenOn = preferences.keepScreenOn,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_METRONOME) } },
                                )
                            }
                            composable("tool/stopwatch") {
                                StopwatchScreen(
                                    isFavorite = ToolCatalog.TOOL_STOPWATCH in favoriteIdsSet,
                                    keepScreenOn = preferences.keepScreenOn,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_STOPWATCH) } },
                                )
                            }
                            composable("tool/countdown_timer") {
                                CountdownTimerScreen(
                                    isFavorite = ToolCatalog.TOOL_COUNTDOWN in favoriteIdsSet,
                                    keepScreenOn = preferences.keepScreenOn,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_COUNTDOWN) } },
                                )
                            }
                            composable("tool/interval_timer") {
                                IntervalTimerScreen(
                                    isFavorite = ToolCatalog.TOOL_INTERVAL_TIMER in favoriteIdsSet,
                                    keepScreenOn = preferences.keepScreenOn,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_INTERVAL_TIMER) } },
                                )
                            }
                            composable("tool/custom_counter") {
                                CustomCounterScreen(
                                    isFavorite = ToolCatalog.TOOL_CUSTOM_COUNTER in favoriteIdsSet,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { scope.launch { repo.toggleFavorite(ToolCatalog.TOOL_CUSTOM_COUNTER) } },
                                )
                            }
                        }

                        if (!useNavigationRail && isMainTabRoute) {
                            MonoBottomNavigationBar(
                                currentRoute = currentRoute,
                                onSelectTab = ::navigateToTab,
                                modifier = Modifier.align(Alignment.BottomCenter),
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SplashScreen(onFinished = { showSplash = false })
        }
    }
}

@Composable
private fun MonoBottomNavigationBar(
    currentRoute: String?,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.navBarBg)
            .border(1.dp, palette.border.copy(alpha = 0.7f), shape)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                val selected = currentRoute == tab.route
                val tint = if (selected) palette.textPrimary else palette.textMuted
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 6.dp)
                        .testTag("bottom_nav_${tab.label.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .width(if (selected) 16.dp else 0.dp)
                            .clip(CircleShape)
                            .background(palette.accentPrimary),
                    )
                }
            }
        }
    }
}
