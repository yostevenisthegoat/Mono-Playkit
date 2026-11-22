package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.example.ui.theme.MonoTheme

/**
 * Renders the signature Mono deep-black backdrop with large organic sweeping charcoal waves
 * and soft subtle radial lighting matching the Mono ecosystem reference design.
 */
@Composable
fun MonoBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = MonoTheme.palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bgMain),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-right soft radial ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.waveSecondary.copy(alpha = 0.55f),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.85f, h * 0.08f),
                    radius = w * 0.85f,
                ),
                center = Offset(w * 0.85f, h * 0.08f),
                radius = w * 0.85f,
            )

            // Upper sweeping abstract curve
            val upperWave = Path().apply {
                moveTo(0f, h * 0.22f)
                cubicTo(
                    w * 0.28f, h * 0.08f,
                    w * 0.65f, h * 0.34f,
                    w, h * 0.14f,
                )
                lineTo(w, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(
                path = upperWave,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.wavePrimary.copy(alpha = 0.72f),
                        palette.waveSecondary.copy(alpha = 0.18f),
                    ),
                    startY = 0f,
                    endY = h * 0.35f,
                ),
            )

            // Mid sweeping wave
            val midWave = Path().apply {
                moveTo(0f, h * 0.45f)
                cubicTo(
                    w * 0.32f, h * 0.32f,
                    w * 0.55f, h * 0.62f,
                    w, h * 0.44f,
                )
                lineTo(w, h * 0.76f)
                cubicTo(
                    w * 0.62f, h * 0.86f,
                    w * 0.28f, h * 0.58f,
                    0f, h * 0.74f,
                )
                close()
            }
            drawPath(
                path = midWave,
                brush = Brush.linearGradient(
                    colors = listOf(
                        palette.wavePrimary.copy(alpha = 0.45f),
                        palette.waveSecondary.copy(alpha = 0.22f),
                    ),
                    start = Offset(0f, h * 0.35f),
                    end = Offset(w, h * 0.8f),
                ),
            )

            // Lower diagonal soft curve
            val lowerWave = Path().apply {
                moveTo(0f, h * 0.78f)
                cubicTo(
                    w * 0.35f, h * 0.92f,
                    w * 0.72f, h * 0.70f,
                    w, h * 0.84f,
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = lowerWave,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.waveSecondary.copy(alpha = 0.35f),
                        palette.bgMain.copy(alpha = 0.9f),
                    ),
                    startY = h * 0.72f,
                    endY = h,
                ),
            )
        }
        content()
    }
}
