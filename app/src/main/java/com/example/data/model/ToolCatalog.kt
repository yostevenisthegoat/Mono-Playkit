package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.AvTimer
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.PlusOne
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.Scoreboard
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
) {
    SCOREBOARD(
        id = "scoreboard",
        title = "Scoreboard",
        subtitle = "Match & sport scoring",
        icon = Icons.Rounded.Scoreboard,
    ),
    PUZZLES(
        id = "puzzles",
        title = "Puzzles",
        subtitle = "Timers, solvers & guides",
        icon = Icons.Rounded.ViewInAr,
    ),
    BOARD_GAMES(
        id = "board_games",
        title = "Board Games",
        subtitle = "Chess, Monopoly & classics",
        icon = Icons.Rounded.GridView,
    ),
    DICE_RANDOMIZERS(
        id = "dice_randomizers",
        title = "Dice & Randomizers",
        subtitle = "Polyhedral dice & picks",
        icon = Icons.Rounded.Casino,
    ),
    CARD_GAMES(
        id = "card_games",
        title = "Card Games",
        subtitle = "TCG life & deck tools",
        icon = Icons.Rounded.Style,
    ),
    PHYSICAL_TOYS(
        id = "physical_toys",
        title = "Physical Toys",
        subtitle = "Trackers, kits & metronome",
        icon = Icons.Rounded.Toys,
    ),
    TIMERS_COUNTERS(
        id = "timers_counters",
        title = "Timers & Counters",
        subtitle = "Stopwatch, intervals & tallies",
        icon = Icons.Rounded.Timer,
    ),
}

data class PlayTool(
    val id: String,
    val name: String,
    val shortTitle: String,
    val category: ToolCategory,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val tags: List<String>,
    val keywords: List<String>,
)

object ToolCatalog {

    const val TOOL_SCOREBOARD = "scoreboard"
    const val TOOL_CUBE_TIMER = "cube_timer"
    const val TOOL_CUBE_SOLVER = "cube_solver"
    const val TOOL_PYRAMINX_SOLVER = "pyraminx_solver"
    const val TOOL_SKEWB_SOLVER = "skewb_solver"
    const val TOOL_PUZZLE_GUIDES = "puzzle_guides"
    const val TOOL_CHESS_CLOCK = "chess_clock"
    const val TOOL_MONOPOLY_BANK = "monopoly_bank"
    const val TOOL_SCRABBLE = "scrabble_tool"
    const val TOOL_LUDO_SNAKES = "ludo_snakes"
    const val TOOL_DOMINOES = "dominoes_calc"
    const val TOOL_BINGO = "bingo_caller"
    const val TOOL_JENGA = "jenga_selector"
    const val TOOL_TANGRAM = "tangram_guide"
    const val TOOL_DICE_ROLLER = "dice_roller"
    const val TOOL_COIN_FLIP = "coin_flip"
    const val TOOL_RNG = "random_number"
    const val TOOL_TEAM_GENERATOR = "team_generator"
    const val TOOL_TCG_COUNTER = "tcg_counter"
    const val TOOL_PLAYING_CARDS = "playing_cards"
    const val TOOL_FIDGET_SPINNER = "fidget_spinner"
    const val TOOL_YOYO_TRACKER = "yoyo_tracker"
    const val TOOL_KENDAMA_TRACKER = "kendama_tracker"
    const val TOOL_LEGO_INVENTORY = "lego_inventory"
    const val TOOL_MODEL_KITS = "model_kits"
    const val TOOL_METRONOME = "metronome"
    const val TOOL_STOPWATCH = "stopwatch"
    const val TOOL_COUNTDOWN = "countdown_timer"
    const val TOOL_INTERVAL_TIMER = "interval_timer"
    const val TOOL_CUSTOM_COUNTER = "custom_counter"

    val allTools: List<PlayTool> = listOf(
        // Scoreboard
        PlayTool(
            id = TOOL_SCOREBOARD,
            name = "Universal Scoreboard",
            shortTitle = "Scoreboard",
            category = ToolCategory.SCOREBOARD,
            description = "Opposing team scoreboard with Basketball, Football, Tennis, Boxing, Darts & custom rules.",
            icon = Icons.Rounded.Scoreboard,
            route = "tool/scoreboard",
            tags = listOf("Basketball", "Football", "Darts", "Fullscreen"),
            keywords = listOf("score", "match", "basketball", "volleyball", "badminton", "table tennis", "tennis", "football", "soccer", "boxing", "darts", "301", "501", "red", "blue"),
        ),

        // Puzzles
        PlayTool(
            id = TOOL_CUBE_TIMER,
            name = "Rubik's Cube Timer",
            shortTitle = "Cube Timer",
            category = ToolCategory.PUZZLES,
            description = "WCA inspection countdown, scramble generator, Ao5, Ao12, and personal best stats.",
            icon = Icons.Rounded.AvTimer,
            route = "tool/cube_timer",
            tags = listOf("WCA", "Ao5/Ao12", "Scramble"),
            keywords = listOf("rubik", "cube", "speedcube", "3x3", "wca", "ao5", "ao12", "inspection", "scramble"),
        ),
        PlayTool(
            id = TOOL_CUBE_SOLVER,
            name = "Rubik's Cube Solver",
            shortTitle = "3x3 Solver",
            category = ToolCategory.PUZZLES,
            description = "Interactive 3x3 color input, state validation, step-by-step CFOP solution & playback.",
            icon = Icons.Rounded.ViewInAr,
            route = "tool/cube_solver",
            tags = listOf("3x3x3", "Step-by-Step", "Offline"),
            keywords = listOf("rubik", "solver", "3x3", "notation", "algorithm", "cube"),
        ),
        PlayTool(
            id = TOOL_PYRAMINX_SOLVER,
            name = "Pyraminx Solver",
            shortTitle = "Pyraminx",
            category = ToolCategory.PUZZLES,
            description = "Interactive 4-face Pyraminx color picker with optimal offline step-by-step solver.",
            icon = Icons.Rounded.Extension,
            route = "tool/pyraminx_solver",
            tags = listOf("Optimal", "Solver", "4-Face"),
            keywords = listOf("pyraminx", "triangle", "pyramid", "solver", "puzzle"),
        ),
        PlayTool(
            id = TOOL_SKEWB_SOLVER,
            name = "Skewb Solver",
            shortTitle = "Skewb",
            category = ToolCategory.PUZZLES,
            description = "Interactive Skewb color entry with state validation and fast offline move solver.",
            icon = Icons.Rounded.Extension,
            route = "tool/skewb_solver",
            tags = listOf("Skewb", "Solver", "Offline"),
            keywords = listOf("skewb", "solver", "cube", "puzzle"),
        ),
        PlayTool(
            id = TOOL_PUZZLE_GUIDES,
            name = "Twisty Puzzle Guides",
            shortTitle = "Puzzle Guides",
            category = ToolCategory.PUZZLES,
            description = "Reference guides & notation for Megaminx, Square-1, Mirror Cube, Gear Cube & Clock.",
            icon = Icons.Rounded.MenuBook,
            route = "tool/puzzle_guides",
            tags = listOf("Megaminx", "Square-1", "Clock"),
            keywords = listOf("megaminx", "square-1", "mirror cube", "gear cube", "rubik clock", "guide", "parity", "algorithms"),
        ),

        // Board Games
        PlayTool(
            id = TOOL_CHESS_CLOCK,
            name = "Chess Clock & Tools",
            shortTitle = "Chess Clock",
            category = ToolCategory.BOARD_GAMES,
            description = "Bullet, Blitz, Rapid, Classical clocks with increment/delay, Notation & Chess960.",
            icon = Icons.Rounded.HourglassBottom,
            route = "tool/chess_clock",
            tags = listOf("Two-Player", "Chess960", "PGN"),
            keywords = listOf("chess", "clock", "bullet", "blitz", "rapid", "classical", "fischer", "delay", "increment", "960", "notation"),
        ),
        PlayTool(
            id = TOOL_MONOPOLY_BANK,
            name = "Monopoly Digital Bank",
            shortTitle = "Monopoly Bank",
            category = ToolCategory.BOARD_GAMES,
            description = "Player money tracker, instant transfers, Pass Go, rent calculator & transaction log.",
            icon = Icons.Rounded.AccountBalance,
            route = "tool/monopoly_bank",
            tags = listOf("Bank", "Rent Calc", "Log"),
            keywords = listOf("monopoly", "bank", "money", "rent", "cash", "property", "board game"),
        ),
        PlayTool(
            id = TOOL_SCRABBLE,
            name = "Scrabble Score & Word Tool",
            shortTitle = "Scrabble",
            category = ToolCategory.BOARD_GAMES,
            description = "Word score multiplier calculator, turn timer, tile checker & player score tracker.",
            icon = Icons.Rounded.Calculate,
            route = "tool/scrabble_tool",
            tags = listOf("Word Calc", "Turn Timer", "Checker"),
            keywords = listOf("scrabble", "word", "score", "tiles", "bingo", "letter", "checker"),
        ),
        PlayTool(
            id = TOOL_LUDO_SNAKES,
            name = "Ludo & Snakes Dice",
            shortTitle = "Ludo & Snakes",
            category = ToolCategory.BOARD_GAMES,
            description = "1-dice and 2-dice modes with 6-streak tracker, roll history & player turn tracker.",
            icon = Icons.Rounded.Casino,
            route = "tool/ludo_snakes",
            tags = listOf("1 & 2 Dice", "Turns", "History"),
            keywords = listOf("ludo", "snakes and ladders", "dice", "turn", "board"),
        ),
        PlayTool(
            id = TOOL_DOMINOES,
            name = "Dominoes Scoreboard",
            shortTitle = "Dominoes",
            category = ToolCategory.BOARD_GAMES,
            description = "Multi-player round-by-round pip score calculator with target score & round history.",
            icon = Icons.Rounded.GridView,
            route = "tool/dominoes_calc",
            tags = listOf("Pips", "Multi-Player", "Rounds"),
            keywords = listOf("domino", "dominoes", "pips", "score", "rounds"),
        ),
        PlayTool(
            id = TOOL_BINGO,
            name = "Bingo Number Caller",
            shortTitle = "Bingo Caller",
            category = ToolCategory.BOARD_GAMES,
            description = "Random 75-ball or 90-ball caller with duplicate prevention, board & call history.",
            icon = Icons.Rounded.ConfirmationNumber,
            route = "tool/bingo_caller",
            tags = listOf("1-75 / 1-90", "No Repeats", "Auto"),
            keywords = listOf("bingo", "caller", "numbers", "balls"),
        ),
        PlayTool(
            id = TOOL_JENGA,
            name = "Jenga Block Selector",
            shortTitle = "Jenga Picker",
            category = ToolCategory.BOARD_GAMES,
            description = "Random block number & layer position selector with custom range & no-repeat mode.",
            icon = Icons.Rounded.Numbers,
            route = "tool/jenga_selector",
            tags = listOf("Custom Range", "Layer", "Random"),
            keywords = listOf("jenga", "block", "tower", "number"),
        ),
        PlayTool(
            id = TOOL_TANGRAM,
            name = "Tangram Reference Guide",
            shortTitle = "Tangram",
            category = ToolCategory.BOARD_GAMES,
            description = "Classic silhouette puzzles with interactive toggleable 7-piece geometric solution overlays.",
            icon = Icons.Rounded.Extension,
            route = "tool/tangram_guide",
            tags = listOf("Silhouettes", "Solutions", "Shapes"),
            keywords = listOf("tangram", "shapes", "puzzle", "silhouette", "solution"),
        ),

        // Dice & Randomizers
        PlayTool(
            id = TOOL_DICE_ROLLER,
            name = "Dice Roller",
            shortTitle = "Dice Roller",
            category = ToolCategory.DICE_RANDOMIZERS,
            description = "Roll D4, D6, D8, D10, D12, D20, D100 or custom dice with modifiers & roll history.",
            icon = Icons.Rounded.Casino,
            route = "tool/dice_roller",
            tags = listOf("D4-D20", "Custom", "Multi-Dice"),
            keywords = listOf("dice", "roll", "d4", "d6", "d8", "d10", "d12", "d20", "dnd", "tabletop", "rpg"),
        ),
        PlayTool(
            id = TOOL_COIN_FLIP,
            name = "Coin Flip",
            shortTitle = "Coin Flip",
            category = ToolCategory.DICE_RANDOMIZERS,
            description = "Crisp animated coin toss with streak tracking and heads/tails session breakdown.",
            icon = Icons.Rounded.FlipCameraAndroid,
            route = "tool/coin_flip",
            tags = listOf("Heads/Tails", "Streaks", "Fast"),
            keywords = listOf("coin", "flip", "toss", "heads", "tails"),
        ),
        PlayTool(
            id = TOOL_RNG,
            name = "Random Number Generator",
            shortTitle = "Random Number",
            category = ToolCategory.DICE_RANDOMIZERS,
            description = "Generate single or batch numbers in any custom range with unique & sort options.",
            icon = Icons.Rounded.Pin,
            route = "tool/random_number",
            tags = listOf("Min-Max", "Unique", "Batch"),
            keywords = listOf("random", "number", "rng", "range", "lottery", "pick"),
        ),
        PlayTool(
            id = TOOL_TEAM_GENERATOR,
            name = "Players, Turns & Teams",
            shortTitle = "Team Picker",
            category = ToolCategory.DICE_RANDOMIZERS,
            description = "Pick a random player, shuffle turn order, or split a roster into balanced teams.",
            icon = Icons.Rounded.Groups,
            route = "tool/team_generator",
            tags = listOf("Teams", "Turn Order", "Picker"),
            keywords = listOf("player", "team", "generator", "turn", "order", "shuffle", "random"),
        ),

        // Card Games
        PlayTool(
            id = TOOL_TCG_COUNTER,
            name = "TCG Life & Status Tracker",
            shortTitle = "TCG Counter",
            category = ToolCategory.CARD_GAMES,
            description = "Life points, damage, energy, turn counter & status effects for physical card games.",
            icon = Icons.Rounded.SportsEsports,
            route = "tool/tcg_counter",
            tags = listOf("Life/HP", "Energy", "Status"),
            keywords = listOf("tcg", "trading card", "mtg", "magic", "yugioh", "pokemon", "life", "damage", "energy", "commander"),
        ),
        PlayTool(
            id = TOOL_PLAYING_CARDS,
            name = "Playing Card Deck Tool",
            shortTitle = "Card Shuffler",
            category = ToolCategory.CARD_GAMES,
            description = "Configure decks (52, Jokers, Euchre), shuffle, draw random cards & view draw history.",
            icon = Icons.Rounded.Style,
            route = "tool/playing_cards",
            tags = listOf("Shuffler", "Draw", "Deck"),
            keywords = listOf("cards", "playing cards", "deck", "shuffle", "picker", "poker", "draw"),
        ),

        // Physical Toys
        PlayTool(
            id = TOOL_FIDGET_SPINNER,
            name = "Fidget Spinner Tracker",
            shortTitle = "Spinner",
            category = ToolCategory.PHYSICAL_TOYS,
            description = "Measure spin duration, estimate RPM via revolution cadence & save personal records.",
            icon = Icons.Rounded.Loop,
            route = "tool/fidget_spinner",
            tags = listOf("Spin Timer", "RPM", "Records"),
            keywords = listOf("fidget", "spinner", "rpm", "spin", "duration"),
        ),
        PlayTool(
            id = TOOL_YOYO_TRACKER,
            name = "Yo-Yo Practice Tracker",
            shortTitle = "Yo-Yo",
            category = ToolCategory.PHYSICAL_TOYS,
            description = "Trick counter, trick mastery checklist, practice timer & personal best records.",
            icon = Icons.Rounded.Adjust,
            route = "tool/yoyo_tracker",
            tags = listOf("Tricks", "Practice", "Records"),
            keywords = listOf("yoyo", "yo-yo", "trick", "practice", "sleeper", "trapeze"),
        ),
        PlayTool(
            id = TOOL_KENDAMA_TRACKER,
            name = "Kendama Guide & Tracker",
            shortTitle = "Kendama",
            category = ToolCategory.PHYSICAL_TOYS,
            description = "Classic Kendama trick reference guide, land counter & timed practice logger.",
            icon = Icons.Rounded.RadioButtonChecked,
            route = "tool/kendama_tracker",
            tags = listOf("Guide", "Tricks", "Practice"),
            keywords = listOf("kendama", "spike", "cup", "lighthouse", "lunar", "trick"),
        ),
        PlayTool(
            id = TOOL_LEGO_INVENTORY,
            name = "LEGO Brick Inventory",
            shortTitle = "LEGO Tracker",
            category = ToolCategory.PHYSICAL_TOYS,
            description = "Track brick counts, building checklists & project completion progress offline.",
            icon = Icons.Rounded.Inventory2,
            route = "tool/lego_inventory",
            tags = listOf("Bricks", "Checklist", "Progress"),
            keywords = listOf("lego", "brick", "inventory", "building", "checklist", "set"),
        ),
        PlayTool(
            id = TOOL_MODEL_KITS,
            name = "Model Kit Checklist",
            shortTitle = "Model Kits",
            category = ToolCategory.PHYSICAL_TOYS,
            description = "Track assembly stages, decals, topcoat progress & personal build notes.",
            icon = Icons.Rounded.Build,
            route = "tool/model_kits",
            tags = listOf("Assembly", "Notes", "Progress"),
            keywords = listOf("model", "kit", "gundam", "gunpla", "scale", "assembly", "checklist"),
        ),
        PlayTool(
            id = TOOL_METRONOME,
            name = "Metronome & Practice Timer",
            shortTitle = "Metronome",
            category = ToolCategory.PHYSICAL_TOYS,
            description = "Accurate audio-click BPM metronome with tap tempo, time signatures & practice clock.",
            icon = Icons.Rounded.MusicNote,
            route = "tool/metronome",
            tags = listOf("BPM", "Tap Tempo", "Audio"),
            keywords = listOf("metronome", "bpm", "music", "instrument", "rhythm", "tempo", "practice"),
        ),

        // Timers & Counters
        PlayTool(
            id = TOOL_STOPWATCH,
            name = "Stopwatch",
            shortTitle = "Stopwatch",
            category = ToolCategory.TIMERS_COUNTERS,
            description = "High-precision monotonic stopwatch with laps, split deltas & session history.",
            icon = Icons.Rounded.Timer,
            route = "tool/stopwatch",
            tags = listOf("Laps", "Millis", "History"),
            keywords = listOf("stopwatch", "lap", "time", "chrono", "race"),
        ),
        PlayTool(
            id = TOOL_COUNTDOWN,
            name = "Countdown Timer",
            shortTitle = "Countdown",
            category = ToolCategory.TIMERS_COUNTERS,
            description = "Quick presets, custom duration & savable countdown configurations with alerts.",
            icon = Icons.Rounded.HourglassBottom,
            route = "tool/countdown_timer",
            tags = listOf("Presets", "Saved", "Alerts"),
            keywords = listOf("countdown", "timer", "alarm", "round", "turn"),
        ),
        PlayTool(
            id = TOOL_INTERVAL_TIMER,
            name = "Interval & Round Timer",
            shortTitle = "Intervals",
            category = ToolCategory.TIMERS_COUNTERS,
            description = "Configurable work/rest intervals & rounds with savable custom presets.",
            icon = Icons.Rounded.Shuffle,
            route = "tool/interval_timer",
            tags = listOf("Work/Rest", "Rounds", "Presets"),
            keywords = listOf("interval", "hiit", "round", "boxing", "practice", "timer"),
        ),
        PlayTool(
            id = TOOL_CUSTOM_COUNTER,
            name = "Multi-Counter Studio",
            shortTitle = "Counters",
            category = ToolCategory.TIMERS_COUNTERS,
            description = "Multiple named persistent counters with custom step increments & quick reset.",
            icon = Icons.Rounded.PlusOne,
            route = "tool/custom_counter",
            tags = listOf("Named", "Multi", "Saved"),
            keywords = listOf("counter", "tally", "points", "custom", "clicker"),
        ),
    )

    val featuredToolIds = listOf(
        TOOL_SCOREBOARD,
        TOOL_CHESS_CLOCK,
        TOOL_DICE_ROLLER,
        TOOL_CUBE_TIMER,
    )

    val quickToolIds = listOf(
        TOOL_SCOREBOARD,
        TOOL_CHESS_CLOCK,
        TOOL_DICE_ROLLER,
        TOOL_STOPWATCH,
        TOOL_CUBE_TIMER,
        TOOL_COIN_FLIP,
    )

    fun findById(id: String): PlayTool? = allTools.firstOrNull { it.id == id }

    fun toolsByCategory(category: ToolCategory): List<PlayTool> =
        allTools.filter { it.category == category }

    fun search(query: String, categoryFilter: ToolCategory? = null): List<PlayTool> {
        val q = query.trim().lowercase()
        return allTools.filter { tool ->
            val catMatch = categoryFilter == null || tool.category == categoryFilter
            val queryMatch = q.isEmpty() ||
                tool.name.lowercase().contains(q) ||
                tool.shortTitle.lowercase().contains(q) ||
                tool.description.lowercase().contains(q) ||
                tool.category.title.lowercase().contains(q) ||
                tool.tags.any { it.lowercase().contains(q) } ||
                tool.keywords.any { it.lowercase().contains(q) }
            catMatch && queryMatch
        }
    }
}
