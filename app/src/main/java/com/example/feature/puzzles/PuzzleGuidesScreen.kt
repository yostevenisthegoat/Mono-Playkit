package com.example.feature.puzzles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

private data class PuzzleGuideSection(
    val title: String,
    val detail: String,
    val algorithm: String = "",
)

private data class PuzzleGuideEntry(
    val name: String,
    val subtitle: String,
    val scrambleGenerator: (() -> String)? = null,
    val sections: List<PuzzleGuideSection>,
)

private fun generateMegaminxScramble(): String {
    val lines = mutableListOf<String>()
    repeat(5) {
        val moves = (0..9).joinToString(" ") { idx ->
            val face = if (idx % 2 == 0) "R" else "D"
            val dir = if (Random.nextBoolean()) "++" else "--"
            "$face$dir"
        }
        val uMove = if (Random.nextBoolean()) "U" else "U'"
        lines.add("$moves $uMove")
    }
    return lines.joinToString("\n")
}

private fun generateSquare1Scramble(): String {
    val steps = mutableListOf<String>()
    repeat(12) {
        val top = Random.nextInt(-5, 7)
        val bot = Random.nextInt(-5, 7)
        if (top != 0 || bot != 0) steps.add("($top, $bot)")
    }
    return steps.joinToString(" / ") + " /"
}

private val puzzleGuides = listOf(
    PuzzleGuideEntry(
        name = "Megaminx",
        subtitle = "12-face dodecahedron WCA scramble & last-layer guide",
        scrambleGenerator = ::generateMegaminxScramble,
        sections = listOf(
            PuzzleGuideSection("Star & F2L", "Solve the bottom white star, then insert 5 F2L corner-edge pairs around the lower hemisphere."),
            PuzzleGuideSection("S2L (Second Two Layers)", "Build the 5 upper-middle edge-corner slots clockwise around the equator before the grey/top face."),
            PuzzleGuideSection("Orient Last Layer Edges", "Line / Hook / Dot cases on top face:", "F R U R' U' F'"),
            PuzzleGuideSection("Orient Last Layer Corners", "Cycle twist top corners using R' D' R D triggers until top face is uniform.", "(R' D' R D)x2  U  (R' D' R D)x4"),
            PuzzleGuideSection("Permute Last Layer", "Edge 3-cycle and corner 3-cycle:", "R U R' F' R U R' U' R' F R2 U' R'"),
        ),
    ),
    PuzzleGuideEntry(
        name = "Square-1",
        subtitle = "Shape-shifting puzzle scramble & parity algorithms",
        scrambleGenerator = ::generateSquare1Scramble,
        sections = listOf(
            PuzzleGuideSection("Notation (Top, Bottom) /", "Numbers count 30° increments (+ clockwise, - counter-clockwise). '/' performs a 180° slice turn."),
            PuzzleGuideSection("Cube Shape (CSP / Scallop-Kite)", "Group all 8 edges together into a star/scallop, then slice into kite-kite and square-square."),
            PuzzleGuideSection("Corner Orientation (CO)", "Separate white and yellow corners to opposite layers.", "(1,0) / (-1,-1) / (0,1)"),
            PuzzleGuideSection("Edge Permutation (Adj-Adj)", "Swap adjacent edges on both top and bottom layers:", "(1,0) / (-1,-1) / (-2,4) / (1,1) / (-3,0)"),
            PuzzleGuideSection("Adjacent Edge Parity", "Swap two adjacent top edges when parity occurs:", "/ (-3,0) / (0,3) / (0,-3) / (0,3) / (2,0) / (0,2) / (-2,0) / (4,0) / (0,-2) / (0,2) / (-1,4) / (0,-3) / (0,3)"),
        ),
    ),
    PuzzleGuideEntry(
        name = "Mirror Cube",
        subtitle = "Shape-mod 3x3 solved by piece thickness & height",
        sections = listOf(
            PuzzleGuideSection("Core Principle", "Identical mechanically to a 3x3x3 Rubik's Cube, except pieces are distinguished by size/thickness instead of color."),
            PuzzleGuideSection("Recommended Start Face", "Start with the thinnest or thickest center face so cross pieces are unmistakable by touch and height."),
            PuzzleGuideSection("Last Layer Orientation", "Check whether stickers sit flush with the center height; apply standard 3x3 OLL:", "F R U R' U' F'   •   R U R' U R U2 R'"),
            PuzzleGuideSection("Center Orientation Note", "If using a brushed-metallic Mirror Cube, check center grain alignment using T-perm twice if needed."),
        ),
    ),
    PuzzleGuideEntry(
        name = "Gear Cube",
        subtitle = "Geared 3x3 edge-flipping & corner alignment guide",
        sections = listOf(
            PuzzleGuideSection("180° Face Turns", "Each 90° face turn rotates the middle gear slice by 45°. Always make 180° (2x) turns to keep gears aligned!"),
            PuzzleGuideSection("Step 1: Corners", "Align all 8 corners first using R2, U2, F2 moves (just like a Domino cube)."),
            PuzzleGuideSection("Step 2: Swap Opposite Edges", "Swap two opposite edges without disturbing corners:", "R2 U R2 U"),
            PuzzleGuideSection("Step 3: Spin Middle Gear Edges", "Rotate twisted middle-layer gear teeth into alignment:", "(R U R U R U) or (R2 U2)x3"),
        ),
    ),
    PuzzleGuideEntry(
        name = "Rubik's Clock",
        subtitle = "Pin-state WCA 7-move dial solving method",
        sections = listOf(
            PuzzleGuideSection("Pin Rules", "Pushing a pin UP couples the 4 adjacent clocks on the front face; pins DOWN move the back clocks when turning corners."),
            PuzzleGuideSection("Step 1: Front Cross", "Match the 4 edge clocks to the center clock using 1-pin and 2-pin combinations, then set all 5 to 12 o'clock."),
            PuzzleGuideSection("Step 2: Flip (y2) & Back Cross", "Flip horizontally (keep 12 o'clock at top). Match back edge clocks to back center, then align to 12 o'clock."),
            PuzzleGuideSection("Step 3: Corners", "Raise 3 pins and turn the 4th corner wheel to match the 12 o'clock cross. Repeat for all 4 corners."),
        ),
    ),
)

@Composable
fun PuzzleGuidesScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback
    var selectedIdx by rememberSaveable { mutableIntStateOf(0) }
    val guide = puzzleGuides[selectedIdx.coerceIn(puzzleGuides.indices)]
    var currentScramble by remember(selectedIdx) {
        mutableStateOf(guide.scrambleGenerator?.invoke().orEmpty())
    }

    MonoToolScaffold(
        title = "Twisty Puzzle Guides",
        subtitle = "Megaminx, Square-1, Mirror, Gear & Clock",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(puzzleGuides) { idx, item ->
                        MonoPillTag(
                            label = item.name,
                            selected = selectedIdx == idx,
                            onClick = { selectedIdx = idx },
                        )
                    }
                }
            }

            item {
                MonoCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                    Text(guide.name, style = MaterialTheme.typography.headlineMedium, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(guide.subtitle, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)

                    if (guide.scrambleGenerator != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("SCRAMBLE", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentScramble,
                            style = MonoTabularStyle.copy(fontSize = 13.sp),
                            color = palette.textPrimary,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        MonoActionButton(
                            text = "New ${guide.name} Scramble",
                            icon = Icons.Rounded.Casino,
                            primary = false,
                            onClick = {
                                currentScramble = guide.scrambleGenerator.invoke()
                                feedback.play(SoundCue.CLICK)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            items(guide.sections) { sec ->
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Text(sec.title, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(sec.detail, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                    if (sec.algorithm.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = sec.algorithm,
                            style = MonoTabularStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                    }
                }
            }
        }
    }
}
