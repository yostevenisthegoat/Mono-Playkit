package com.example.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.MonoBackground
import com.example.ui.components.MonoLogoMark
import com.example.ui.components.MonoSplashSpinner
import com.example.ui.theme.MonoTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val palette = MonoTheme.palette

    LaunchedEffect(Unit) {
        delay(950L)
        onFinished()
    }

    MonoBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MonoLogoMark(size = 104.dp)
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "Mono PlayKit",
                    style = MaterialTheme.typography.displayLarge,
                    color = palette.textPrimary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your Games. Your Tools.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.textSecondary,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp),
            ) {
                MonoSplashSpinner(size = 30.dp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "100% Free  •  Open Source  •  Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted,
                )
            }
        }
    }
}
