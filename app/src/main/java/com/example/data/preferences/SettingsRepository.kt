package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mono_playkit_prefs")

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val cubeInspectionSeconds: Int = 15,
    val saveDiceHistory: Boolean = true,
    val defaultScoreboardPreset: String = "STANDARD",
    val recentTools: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val CUBE_INSPECTION_SEC = intPreferencesKey("cube_inspection_sec")
        val SAVE_DICE_HISTORY = booleanPreferencesKey("save_dice_history")
        val DEFAULT_SCOREBOARD_PRESET = stringPreferencesKey("default_scoreboard_preset")
        val RECENT_TOOLS = stringPreferencesKey("recent_tools")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name
        val themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.DARK)
        val recentTools = prefs[Keys.RECENT_TOOLS]
            ?.split("|")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val recentSearches = prefs[Keys.RECENT_SEARCHES]
            ?.split("|")
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        UserPreferences(
            themeMode = themeMode,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
            cubeInspectionSeconds = prefs[Keys.CUBE_INSPECTION_SEC] ?: 15,
            saveDiceHistory = prefs[Keys.SAVE_DICE_HISTORY] ?: true,
            defaultScoreboardPreset = prefs[Keys.DEFAULT_SCOREBOARD_PRESET] ?: "STANDARD",
            recentTools = recentTools,
            recentSearches = recentSearches,
        )
    }

    suspend fun getSnapshot(): UserPreferences = preferences.first()

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setCubeInspectionSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.CUBE_INSPECTION_SEC] = seconds.coerceIn(0, 30) }
    }

    suspend fun setSaveDiceHistory(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SAVE_DICE_HISTORY] = enabled }
    }

    suspend fun setDefaultScoreboardPreset(preset: String) {
        context.dataStore.edit { it[Keys.DEFAULT_SCOREBOARD_PRESET] = preset }
    }

    suspend fun recordRecentTool(toolId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_TOOLS]?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
            val updated = (listOf(toolId) + current.filterNot { it == toolId }).take(10)
            prefs[Keys.RECENT_TOOLS] = updated.joinToString("|")
        }
    }

    suspend fun recordRecentSearch(query: String) {
        val clean = query.trim()
        if (clean.isEmpty()) return
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_SEARCHES]?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
            val updated = (listOf(clean) + current.filterNot { it.equals(clean, ignoreCase = true) }).take(8)
            prefs[Keys.RECENT_SEARCHES] = updated.joinToString("|")
        }
    }

    suspend fun clearRecentSearches() {
        context.dataStore.edit { it.remove(Keys.RECENT_SEARCHES) }
    }

    suspend fun restorePreferences(prefsSnapshot: UserPreferences) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = prefsSnapshot.themeMode.name
            prefs[Keys.SOUND_ENABLED] = prefsSnapshot.soundEnabled
            prefs[Keys.HAPTIC_ENABLED] = prefsSnapshot.hapticEnabled
            prefs[Keys.KEEP_SCREEN_ON] = prefsSnapshot.keepScreenOn
            prefs[Keys.CUBE_INSPECTION_SEC] = prefsSnapshot.cubeInspectionSeconds
            prefs[Keys.SAVE_DICE_HISTORY] = prefsSnapshot.saveDiceHistory
            prefs[Keys.DEFAULT_SCOREBOARD_PRESET] = prefsSnapshot.defaultScoreboardPreset
        }
    }

    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }
}
