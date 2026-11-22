package com.example.core

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.local.MonoDatabase
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.PlayKitRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: MonoDatabase = MonoDatabase.getInstance(appContext)
    val settingsRepository: SettingsRepository = SettingsRepository(appContext)
    val repository: PlayKitRepository = PlayKitRepository(database, settingsRepository)
    val feedback: FeedbackManager = FeedbackManager(appContext)
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
