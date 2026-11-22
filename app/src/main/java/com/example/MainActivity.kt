package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.AppContainer
import com.example.core.LocalAppContainer
import com.example.data.preferences.UserPreferences
import com.example.ui.navigation.MonoPlayKitApp
import com.example.ui.theme.MonoPlayKitTheme

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by appContainer.repository.preferences
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            MonoPlayKitTheme(themeMode = preferences.themeMode) {
                CompositionLocalProvider(LocalAppContainer provides appContainer) {
                    MonoPlayKitApp(preferences = preferences)
                }
            }
        }
    }
}
