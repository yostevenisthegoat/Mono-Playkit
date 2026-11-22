package com.example.feature.dice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun CoinFlipScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val feedback = LocalAppContainer.current.feedback
    val scope = rememberCoroutineScope()

    var isHeads by rememberSaveable { mutableStateOf(true) }
    var headsCount by rememberSaveable { mutableIntStateOf(0) }
    var tailsCount by rememberSaveable { mutableIntStateOf(0) }
    var streak by rememberSaveable { mutableIntStateOf(0) }
    var lastSide by rememberSaveable { mutableStateOf<Boolean?>(null) }

    val flipRotation = remember { Animatable(0f) }

    fun flip() {
        val result = Random.nextBoolean()
        isHeads = result
        if (result) headsCount++ else tailsCount++
        streak = if (lastSide == result) streak + 1 else 1
        lastSide = result
        feedback.play(SoundCue.CLICK)
        scope.launch {
            flipRotation.snapTo(0f)
            flipRotation.animateTo(
                targetValue = 720f,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            )
        }
    }

    MonoToolScaffold(
        title = "Coin Flip",
        subtitle = "Instant Toss & Streak Tracker",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
    ) { contentModifier ->
        Column(
            modifier = contentModifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            MonoCard(
                onClick = { flip() },
                cornerRadius = 28.dp,
                contentPadding = PaddingValues(32.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(164.dp)
                            .graphicsLayer {
                                rotationX = flipRotation.value
                                cameraDistance = 14f * density
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(palette.surfaceSecondary, palette.cardBottom),
                                ),
                            )
                            .border(3.dp, palette.accentPrimary.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isHeads) "H" else "T",
                                style = MonoTabularStyle.copy(fontSize = 54.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                            Text(
                                text = if (isHeads) "HEADS" else "TAILS",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.textSecondary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    val total = headsCount + tailsCount
                    Text(
                        text = if (total == 0) "Tap coin or button below to flip" else "Streak: $streak ${if (isHeads) "Heads" else "Tails"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textSecondary,
                    )
                }
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.weight(1f)) {
                    Text("HEADS", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$headsCount",
                        style = MonoTabularStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                    )
                }
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.weight(1f)) {
                    Text("TAILS", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$tailsCount",
                        style = MonoTabularStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                    )
                }
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.weight(1f)) {
                    Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${headsCount + tailsCount}",
                        style = MonoTabularStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MonoActionButton(
                    text = "Reset",
                    icon = Icons.Rounded.Refresh,
                    primary = false,
                    onClick = {
                        headsCount = 0
                        tailsCount = 0
                        streak = 0
                        lastSide = null
                    },
                    modifier = Modifier.weight(0.38f),
                )
                MonoActionButton(
                    text = "Flip Coin",
                    icon = Icons.Rounded.FlipCameraAndroid,
                    onClick = { flip() },
                    testTag = "flip_coin_button",
                    modifier = Modifier.weight(0.62f),
                )
            }
        }
    }
}
