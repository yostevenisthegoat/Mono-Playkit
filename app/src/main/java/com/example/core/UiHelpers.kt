package com.example.core

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import java.util.Locale

@Composable
fun KeepScreenOnEffect(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        val previous = view.keepScreenOn
        view.keepScreenOn = enabled
        onDispose {
            view.keepScreenOn = previous
        }
    }
}

fun formatDurationMillis(millis: Long, showHundredths: Boolean = true): String {
    val safe = millis.coerceAtLeast(0L)
    val totalSeconds = safe / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    val hundredths = (safe % 1000L) / 10L
    return when {
        hours > 0L && showHundredths -> String.format(
            Locale.US,
            "%d:%02d:%02d.%02d",
            hours,
            minutes,
            seconds,
            hundredths,
        )
        hours > 0L -> String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        minutes > 0L && showHundredths -> String.format(
            Locale.US,
            "%d:%02d.%02d",
            minutes,
            seconds,
            hundredths,
        )
        minutes > 0L -> String.format(Locale.US, "%02d:%02d", minutes, seconds)
        showHundredths -> String.format(Locale.US, "%d.%02d", seconds, hundredths)
        else -> String.format(Locale.US, "00:%02d", seconds)
    }
}

fun formatClockMillis(millis: Long): String {
    val safe = millis.coerceAtLeast(0L)
    val totalSeconds = (safe + 999L) / 1000L // ceiling display for clocks when >0
    val actualSec = if (safe == 0L) 0L else totalSeconds
    val hours = actualSec / 3600L
    val minutes = (actualSec % 3600L) / 60L
    val seconds = actualSec % 60L
    return if (hours > 0L) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else if (safe < 20_000L && safe > 0L) {
        val tenths = (safe % 1000L) / 100L
        val secFloor = safe / 1000L
        String.format(Locale.US, "%d.%d", secFloor, tenths)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

fun formatRelativeTimestamp(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}
