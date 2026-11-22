package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.ChecklistItemEntity
import com.example.data.local.ChecklistProjectEntity
import com.example.data.local.CustomTimerPresetEntity
import com.example.data.local.FavoriteToolEntity
import com.example.data.local.HistoryRecordEntity
import com.example.data.local.MonoDatabase
import com.example.data.local.SavedCounterEntity
import com.example.data.preferences.SettingsRepository
import com.example.data.preferences.UserPreferences
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class PlayKitRepository(
    private val db: MonoDatabase,
    private val settings: SettingsRepository,
) {
    private val favoriteDao = db.favoriteDao()
    private val historyDao = db.historyDao()
    private val checklistDao = db.checklistDao()
    private val timerPresetDao = db.timerPresetDao()
    private val savedCounterDao = db.savedCounterDao()

    val preferences: Flow<UserPreferences> = settings.preferences
    val favorites: Flow<List<FavoriteToolEntity>> = favoriteDao.observeFavorites()
    val favoriteIds: Flow<Set<String>> = favorites.map { list -> list.map { it.toolId }.toSet() }
    val allHistory: Flow<List<HistoryRecordEntity>> = historyDao.observeAll()
    val timerPresets: Flow<List<CustomTimerPresetEntity>> = timerPresetDao.observeAll()
    val savedCounters: Flow<List<SavedCounterEntity>> = savedCounterDao.observeAll()

    fun observeHistoryByTool(toolId: String): Flow<List<HistoryRecordEntity>> =
        historyDao.observeByTool(toolId)

    fun observeProjects(toolType: String): Flow<List<ChecklistProjectEntity>> =
        checklistDao.observeProjects(toolType)

    fun observeProjectItems(projectId: Long): Flow<List<ChecklistItemEntity>> =
        checklistDao.observeItems(projectId)

    suspend fun toggleFavorite(toolId: String) {
        val current = favoriteDao.getAllSync()
        if (current.any { it.toolId == toolId }) {
            favoriteDao.deleteByToolId(toolId)
        } else {
            val nextOrder = favoriteDao.getMaxOrder() + 1
            favoriteDao.upsert(FavoriteToolEntity(toolId = toolId, sortOrder = nextOrder))
        }
    }

    suspend fun moveFavorite(toolId: String, delta: Int) {
        val current = favoriteDao.getAllSync().toMutableList()
        val index = current.indexOfFirst { it.toolId == toolId }
        val target = index + delta
        if (index !in current.indices || target !in current.indices) return
        val item = current.removeAt(index)
        current.add(target, item)
        val reindexed = current.mapIndexed { idx, entity -> entity.copy(sortOrder = idx) }
        favoriteDao.upsertAll(reindexed)
    }

    suspend fun recordHistory(
        toolId: String,
        category: String,
        title: String,
        summary: String,
        detail: String = "",
        metricMs: Long? = null,
    ) {
        historyDao.insert(
            HistoryRecordEntity(
                toolId = toolId,
                category = category,
                title = title,
                summary = summary,
                detail = detail,
                metricMs = metricMs,
                timestamp = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteHistoryItem(id: Long) = historyDao.deleteById(id)
    suspend fun deleteHistoryForTool(toolId: String) = historyDao.deleteByToolId(toolId)
    suspend fun clearAllHistory() = historyDao.clearAll()

    suspend fun addProject(
        toolType: String,
        name: String,
        setNumber: String,
        notes: String,
        defaultItems: List<Triple<String, String, Int>>,
    ): Long {
        val projectId = checklistDao.insertProject(
            ChecklistProjectEntity(
                toolType = toolType,
                name = name.trim(),
                setNumber = setNumber.trim(),
                notes = notes.trim(),
            ),
        )
        defaultItems.forEach { (title, subtitle, target) ->
            checklistDao.insertItem(
                ChecklistItemEntity(
                    projectId = projectId,
                    title = title,
                    subtitle = subtitle,
                    currentCount = 0,
                    targetCount = target.coerceAtLeast(1),
                    isDone = false,
                ),
            )
        }
        return projectId
    }

    suspend fun updateProjectNotes(project: ChecklistProjectEntity, notes: String) {
        checklistDao.updateProject(project.copy(notes = notes))
    }

    suspend fun deleteProject(projectId: Long) {
        checklistDao.deleteItemsByProject(projectId)
        checklistDao.deleteProject(projectId)
    }

    suspend fun addChecklistItem(projectId: Long, title: String, subtitle: String, targetCount: Int) {
        checklistDao.insertItem(
            ChecklistItemEntity(
                projectId = projectId,
                title = title.trim(),
                subtitle = subtitle.trim(),
                currentCount = 0,
                targetCount = targetCount.coerceAtLeast(1),
                isDone = false,
            ),
        )
    }

    suspend fun updateChecklistItem(item: ChecklistItemEntity) {
        checklistDao.updateItem(item)
    }

    suspend fun deleteChecklistItem(itemId: Long) {
        checklistDao.deleteItem(itemId)
    }

    suspend fun saveTimerPreset(
        name: String,
        timerType: String,
        workSeconds: Int,
        restSeconds: Int = 0,
        rounds: Int = 1,
    ) {
        timerPresetDao.insert(
            CustomTimerPresetEntity(
                name = name.trim().ifEmpty { "Custom Preset" },
                timerType = timerType,
                workSeconds = workSeconds,
                restSeconds = restSeconds,
                rounds = rounds,
            ),
        )
    }

    suspend fun deleteTimerPreset(id: Long) = timerPresetDao.deleteById(id)

    suspend fun addSavedCounter(name: String, step: Int) {
        savedCounterDao.insert(
            SavedCounterEntity(
                name = name.trim().ifEmpty { "Counter" },
                value = 0,
                step = step.coerceAtLeast(1),
            ),
        )
    }

    suspend fun updateSavedCounter(counter: SavedCounterEntity) {
        savedCounterDao.update(counter.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSavedCounter(id: Long) = savedCounterDao.deleteById(id)

    // Settings delegations
    suspend fun setThemeMode(mode: ThemeMode) = settings.setThemeMode(mode)
    suspend fun setSoundEnabled(enabled: Boolean) = settings.setSoundEnabled(enabled)
    suspend fun setHapticEnabled(enabled: Boolean) = settings.setHapticEnabled(enabled)
    suspend fun setKeepScreenOn(enabled: Boolean) = settings.setKeepScreenOn(enabled)
    suspend fun setCubeInspectionSeconds(sec: Int) = settings.setCubeInspectionSeconds(sec)
    suspend fun setSaveDiceHistory(enabled: Boolean) = settings.setSaveDiceHistory(enabled)
    suspend fun setDefaultScoreboardPreset(preset: String) = settings.setDefaultScoreboardPreset(preset)
    suspend fun recordRecentTool(toolId: String) = settings.recordRecentTool(toolId)
    suspend fun recordRecentSearch(query: String) = settings.recordRecentSearch(query)
    suspend fun clearRecentSearches() = settings.clearRecentSearches()

    suspend fun resetAllApplicationData() {
        historyDao.clearAll()
        favoriteDao.clearAll()
        checklistDao.clearItems()
        checklistDao.clearProjects()
        timerPresetDao.clearAll()
        savedCounterDao.clearAll()
        settings.resetAll()
    }

    suspend fun exportBackupJson(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val prefs = settings.getSnapshot()
            val favs = favoriteDao.getAllSync()
            val hist = historyDao.getAllSync()
            val projects = checklistDao.getAllProjectsSync()
            val items = checklistDao.getAllItemsSync()
            val presets = timerPresetDao.getAllSync()
            val counters = savedCounterDao.getAllSync()

            val root = JSONObject().apply {
                put("version", 1)
                put("exportedAt", System.currentTimeMillis())
                put("preferences", JSONObject().apply {
                    put("themeMode", prefs.themeMode.name)
                    put("soundEnabled", prefs.soundEnabled)
                    put("hapticEnabled", prefs.hapticEnabled)
                    put("keepScreenOn", prefs.keepScreenOn)
                    put("cubeInspectionSeconds", prefs.cubeInspectionSeconds)
                    put("saveDiceHistory", prefs.saveDiceHistory)
                    put("defaultScoreboardPreset", prefs.defaultScoreboardPreset)
                })
                put("favorites", JSONArray().apply {
                    favs.forEach {
                        put(JSONObject().apply {
                            put("toolId", it.toolId)
                            put("sortOrder", it.sortOrder)
                            put("addedAt", it.addedAt)
                        })
                    }
                })
                put("history", JSONArray().apply {
                    hist.forEach {
                        put(JSONObject().apply {
                            put("toolId", it.toolId)
                            put("category", it.category)
                            put("title", it.title)
                            put("summary", it.summary)
                            put("detail", it.detail)
                            if (it.metricMs != null) put("metricMs", it.metricMs)
                            put("timestamp", it.timestamp)
                        })
                    }
                })
                put("projects", JSONArray().apply {
                    projects.forEach { p ->
                        put(JSONObject().apply {
                            put("id", p.id)
                            put("toolType", p.toolType)
                            put("name", p.name)
                            put("setNumber", p.setNumber)
                            put("notes", p.notes)
                            put("createdAt", p.createdAt)
                        })
                    }
                })
                put("projectItems", JSONArray().apply {
                    items.forEach { item ->
                        put(JSONObject().apply {
                            put("projectId", item.projectId)
                            put("title", item.title)
                            put("subtitle", item.subtitle)
                            put("currentCount", item.currentCount)
                            put("targetCount", item.targetCount)
                            put("isDone", item.isDone)
                        })
                    }
                })
                put("timerPresets", JSONArray().apply {
                    presets.forEach { tp ->
                        put(JSONObject().apply {
                            put("name", tp.name)
                            put("timerType", tp.timerType)
                            put("workSeconds", tp.workSeconds)
                            put("restSeconds", tp.restSeconds)
                            put("rounds", tp.rounds)
                        })
                    }
                })
                put("counters", JSONArray().apply {
                    counters.forEach { c ->
                        put(JSONObject().apply {
                            put("name", c.name)
                            put("value", c.value)
                            put("step", c.step)
                        })
                    }
                })
            }

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(root.toString(2).toByteArray(Charsets.UTF_8))
            } ?: error("Unable to open output stream")

            favs.size + hist.size + projects.size + presets.size + counters.size
        }
    }

    suspend fun importBackupJson(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: error("Unable to read backup file")

            val root = JSONObject(raw)
            var importedCount = 0

            root.optJSONObject("preferences")?.let { p ->
                val theme = runCatching {
                    ThemeMode.valueOf(p.optString("themeMode", ThemeMode.DARK.name))
                }.getOrDefault(ThemeMode.DARK)
                settings.restorePreferences(
                    UserPreferences(
                        themeMode = theme,
                        soundEnabled = p.optBoolean("soundEnabled", true),
                        hapticEnabled = p.optBoolean("hapticEnabled", true),
                        keepScreenOn = p.optBoolean("keepScreenOn", true),
                        cubeInspectionSeconds = p.optInt("cubeInspectionSeconds", 15),
                        saveDiceHistory = p.optBoolean("saveDiceHistory", true),
                        defaultScoreboardPreset = p.optString("defaultScoreboardPreset", "STANDARD"),
                    ),
                )
            }

            root.optJSONArray("favorites")?.let { arr ->
                val list = buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        add(
                            FavoriteToolEntity(
                                toolId = obj.getString("toolId"),
                                sortOrder = obj.optInt("sortOrder", i),
                                addedAt = obj.optLong("addedAt", System.currentTimeMillis()),
                            ),
                        )
                    }
                }
                favoriteDao.clearAll()
                favoriteDao.upsertAll(list)
                importedCount += list.size
            }

            root.optJSONArray("history")?.let { arr ->
                val list = buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        add(
                            HistoryRecordEntity(
                                toolId = obj.getString("toolId"),
                                category = obj.getString("category"),
                                title = obj.getString("title"),
                                summary = obj.getString("summary"),
                                detail = obj.optString("detail", ""),
                                metricMs = if (obj.has("metricMs") && !obj.isNull("metricMs")) obj.getLong("metricMs") else null,
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            ),
                        )
                    }
                }
                historyDao.clearAll()
                historyDao.insertAll(list)
                importedCount += list.size
            }

            val projectIdMap = mutableMapOf<Long, Long>()
            root.optJSONArray("projects")?.let { arr ->
                checklistDao.clearItems()
                checklistDao.clearProjects()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val oldId = obj.optLong("id", i.toLong())
                    val newId = checklistDao.insertProject(
                        ChecklistProjectEntity(
                            toolType = obj.getString("toolType"),
                            name = obj.getString("name"),
                            setNumber = obj.optString("setNumber", ""),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        ),
                    )
                    projectIdMap[oldId] = newId
                    importedCount++
                }
            }
            root.optJSONArray("projectItems")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val mappedProjectId = projectIdMap[obj.optLong("projectId")] ?: continue
                    checklistDao.insertItem(
                        ChecklistItemEntity(
                            projectId = mappedProjectId,
                            title = obj.getString("title"),
                            subtitle = obj.optString("subtitle", ""),
                            currentCount = obj.optInt("currentCount", 0),
                            targetCount = obj.optInt("targetCount", 1),
                            isDone = obj.optBoolean("isDone", false),
                        ),
                    )
                }
            }

            root.optJSONArray("timerPresets")?.let { arr ->
                timerPresetDao.clearAll()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    timerPresetDao.insert(
                        CustomTimerPresetEntity(
                            name = obj.getString("name"),
                            timerType = obj.getString("timerType"),
                            workSeconds = obj.getInt("workSeconds"),
                            restSeconds = obj.optInt("restSeconds", 0),
                            rounds = obj.optInt("rounds", 1),
                        ),
                    )
                    importedCount++
                }
            }

            root.optJSONArray("counters")?.let { arr ->
                savedCounterDao.clearAll()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    savedCounterDao.insert(
                        SavedCounterEntity(
                            name = obj.getString("name"),
                            value = obj.optInt("value", 0),
                            step = obj.optInt("step", 1),
                        ),
                    )
                    importedCount++
                }
            }

            importedCount
        }
    }
}
