package com.example.feature.boardgames

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTheme

private data class TangramPiecePoly(
    val name: String,
    val points: List<Pair<Float, Float>>, // normalized 0..1 coordinates
)

private data class TangramPuzzle(
    val name: String,
    val hint: String,
    val pieces: List<TangramPiecePoly>,
)

private val tangramPuzzles = listOf(
    TangramPuzzle(
        name = "Classic Square",
        hint = "The canonical 7-tan starting square configuration.",
        pieces = listOf(
            TangramPiecePoly("Large Triangle 1", listOf(0.15f to 0.15f, 0.85f to 0.15f, 0.50f to 0.50f)),
            TangramPiecePoly("Large Triangle 2", listOf(0.15f to 0.15f, 0.50f to 0.50f, 0.15f to 0.85f)),
            TangramPiecePoly("Medium Triangle", listOf(0.85f to 0.50f, 0.85f to 0.85f, 0.50f to 0.85f)),
            TangramPiecePoly("Small Triangle 1", listOf(0.85f to 0.15f, 0.85f to 0.50f, 0.675f to 0.325f)),
            TangramPiecePoly("Small Triangle 2", listOf(0.50f to 0.50f, 0.675f to 0.675f, 0.325f to 0.675f)),
            TangramPiecePoly("Square", listOf(0.50f to 0.50f, 0.675f to 0.325f, 0.85f to 0.50f, 0.675f to 0.675f)),
            TangramPiecePoly("Parallelogram", listOf(0.15f to 0.85f, 0.325f to 0.675f, 0.675f to 0.675f, 0.50f to 0.85f)),
        ),
    ),
    TangramPuzzle(
        name = "House",
        hint = "Place the two large triangles together for the roof and stack the square & medium triangle below.",
        pieces = listOf(
            TangramPiecePoly("Roof Left", listOf(0.15f to 0.45f, 0.50f to 0.12f, 0.50f to 0.45f)),
            TangramPiecePoly("Roof Right", listOf(0.50f to 0.12f, 0.85f to 0.45f, 0.50f to 0.45f)),
            TangramPiecePoly("Chimney", listOf(0.68f to 0.18f, 0.78f to 0.18f, 0.78f to 0.35f, 0.68f to 0.35f)),
            TangramPiecePoly("Wall Left", listOf(0.24f to 0.45f, 0.54f to 0.45f, 0.54f to 0.85f, 0.24f to 0.85f)),
            TangramPiecePoly("Wall Right Upper", listOf(0.54f to 0.45f, 0.76f to 0.45f, 0.76f to 0.67f)),
            TangramPiecePoly("Wall Right Lower", listOf(0.54f to 0.45f, 0.76f to 0.67f, 0.54f to 0.85f)),
            TangramPiecePoly("Step", listOf(0.54f to 0.85f, 0.76f to 0.67f, 0.76f to 0.85f)),
        ),
    ),
    TangramPuzzle(
        name = "Sailboat",
        hint = "The parallelogram forms the hull while the two large triangles create twin sails.",
        pieces = listOf(
            TangramPiecePoly("Hull", listOf(0.18f to 0.72f, 0.82f to 0.72f, 0.70f to 0.88f, 0.30f to 0.88f)),
            TangramPiecePoly("Main Sail", listOf(0.50f to 0.14f, 0.50f to 0.68f, 0.22f to 0.68f)),
            TangramPiecePoly("Front Sail", listOf(0.53f to 0.24f, 0.78f to 0.68f, 0.53f to 0.68f)),
            TangramPiecePoly("Flag", listOf(0.50f to 0.14f, 0.62f to 0.20f, 0.50f to 0.26f)),
            TangramPiecePoly("Stern Accent", listOf(0.18f to 0.60f, 0.30f to 0.72f, 0.18f to 0.72f)),
        ),
    ),
    TangramPuzzle(
        name = "Rocket",
        hint = "Align the square & parallelogram down the fuselage with small triangles as side fins.",
        pieces = listOf(
            TangramPiecePoly("Nose Cone", listOf(0.50f to 0.10f, 0.64f to 0.28f, 0.36f to 0.28f)),
            TangramPiecePoly("Upper Body", listOf(0.36f to 0.28f, 0.64f to 0.28f, 0.64f to 0.54f, 0.36f to 0.54f)),
            TangramPiecePoly("Lower Body", listOf(0.36f to 0.54f, 0.64f to 0.54f, 0.64f to 0.76f, 0.36f to 0.76f)),
            TangramPiecePoly("Left Fin", listOf(0.36f to 0.58f, 0.20f to 0.80f, 0.36f to 0.76f)),
            TangramPiecePoly("Right Fin", listOf(0.64f to 0.58f, 0.80f to 0.80f, 0.64f to 0.76f)),
            TangramPiecePoly("Exhaust", listOf(0.40f to 0.76f, 0.60f to 0.76f, 0.50f to 0.90f)),
        ),
    ),
    TangramPuzzle(
        name = "Cat",
        hint = "Use the square rotated 45° as the head with two small triangles for ears.",
        pieces = listOf(
            TangramPiecePoly("Left Ear", listOf(0.28f to 0.14f, 0.36f to 0.26f, 0.24f to 0.28f)),
            TangramPiecePoly("Right Ear", listOf(0.48f to 0.14f, 0.52f to 0.28f, 0.40f to 0.26f)),
            TangramPiecePoly("Head", listOf(0.24f to 0.28f, 0.38f to 0.22f, 0.52f to 0.28f, 0.38f to 0.42f)),
            TangramPiecePoly("Torso Upper", listOf(0.38f to 0.42f, 0.58f to 0.56f, 0.34f to 0.66f)),
            TangramPiecePoly("Torso Lower", listOf(0.34f to 0.66f, 0.58f to 0.56f, 0.58f to 0.84f, 0.28f to 0.84f)),
            TangramPiecePoly("Tail", listOf(0.58f to 0.84f, 0.78f to 0.66f, 0.84f to 0.72f, 0.66f to 0.88f)),
        ),
    ),
)

@Composable
fun TangramGuideScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    var selectedIdx by rememberSaveable { mutableIntStateOf(0) }
    var showSolution by rememberSaveable { mutableStateOf(false) }

    val puzzle = tangramPuzzles[selectedIdx.coerceIn(tangramPuzzles.indices)]

    MonoToolScaffold(
        title = "Tangram Reference",
        subtitle = "Silhouettes & Solution Overlays",
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(tangramPuzzles) { idx, item ->
                        MonoPillTag(
                            label = item.name,
                            selected = selectedIdx == idx,
                            onClick = { selectedIdx = idx },
                        )
                    }
                }
            }

            item {
                MonoCard(cornerRadius = 24.dp, contentPadding = PaddingValues(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    ) {
                        val w = size.width
                        val h = size.height
                        puzzle.pieces.forEachIndexed { idx, piece ->
                            val path = Path()
                            piece.points.forEachIndexed { pIdx, (nx, ny) ->
                                val x = nx * w
                                val y = ny * h
                                if (pIdx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            path.close()
                            val shade = if (showSolution) {
                                val factor = 0.22f + (idx % 5) * 0.08f
                                palette.accentPrimary.copy(alpha = factor)
                            } else {
                                palette.accentPrimary.copy(alpha = 0.85f)
                            }
                            drawPath(path = path, color = shade)
                            if (showSolution) {
                                drawPath(
                                    path = path,
                                    color = palette.bgMain,
                                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                                )
                            }
                        }
                    }
                }
            }

            item {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show Piece Solution Lines", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            Text("Toggle between silhouette challenge and cut lines", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                        }
                        Switch(checked = showSolution, onCheckedChange = { showSolution = it })
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = puzzle.hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
            }
        }
    }
}
