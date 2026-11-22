package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MonoTheme

/**
 * Draws the official Mono three-bar ascending slanted mark seen in the reference splash,
 * home banner, and settings profile card.
 */
@Composable
fun MonoLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val isDark = MonoTheme.palette.isDark
    val highlight = if (isDark) Color(0xFFF2F2F2) else Color(0xFF2B2B2B)
    val midtone = if (isDark) Color(0xFFCCCCCC) else Color(0xFF1A1A1A)
    val shadow = if (isDark) Color(0xFF9E9E9E) else Color(0xFF0A0A0A)

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val brush = Brush.linearGradient(
            colors = listOf(highlight, midtone, shadow),
            start = Offset(0f, 0f),
            end = Offset(w, h),
        )
        val radius = CornerRadius(w * 0.09f, w * 0.09f)

        rotate(degrees = 24f, pivot = Offset(w * 0.5f, h * 0.52f)) {
            // Left tall bar
            drawRoundRect(
                brush = brush,
                topLeft = Offset(w * 0.14f, h * 0.14f),
                size = Size(w * 0.22f, h * 0.72f),
                cornerRadius = radius,
            )
            // Middle medium bar
            drawRoundRect(
                brush = brush,
                topLeft = Offset(w * 0.42f, h * 0.34f),
                size = Size(w * 0.22f, h * 0.52f),
                cornerRadius = radius,
            )
            // Right short bar
            drawRoundRect(
                brush = brush,
                topLeft = Offset(w * 0.70f, h * 0.56f),
                size = Size(w * 0.22f, h * 0.30f),
                cornerRadius = radius,
            )
        }
    }
}

@Composable
fun MonoSplashSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    val palette = MonoTheme.palette
    val infiniteTransition = rememberInfiniteTransition(label = "mono_splash_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spinner_angle",
    )

    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 3.dp.toPx()
        drawCircle(
            color = palette.border.copy(alpha = 0.5f),
            style = Stroke(width = strokeWidth),
        )
        rotate(degrees = angle) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        palette.textSecondary,
                        palette.textPrimary,
                    ),
                ),
                startAngle = 0f,
                sweepAngle = 140f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
    }
}
