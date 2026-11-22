package com.example.feature.toys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoEmptyState
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.launch

@Composable
fun LegoInventoryScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    ProjectChecklistScreen(
        toolType = "LEGO",
        title = "LEGO Brick Inventory",
        subtitle = "Brick Counts & Building Checklists",
        defaultItemTemplates = listOf(
            Triple("2x4 Bricks", "Base structure", 12),
            Triple("1x2 Plates", "Trim & tiling", 16),
            Triple("Slope Pieces", "Exterior detail", 8),
            Triple("Bag 1 Assembly", "Building step", 1),
            Triple("Bag 2 Assembly", "Building step", 1),
        ),
        isFavorite = isFavorite,
        onBack = onBack,
        onToggleFavorite = onToggleFavorite,
    )
}

@Composable
fun ModelKitsScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    ProjectChecklistScreen(
        toolType = "MODEL_KIT",
        title = "Model Kit Checklist",
        subtitle = "Assembly Stages, Decals & Notes",
        defaultItemTemplates = listOf(
            Triple("Unboxing & Runner Inventory", "Verify all sprues", 1),
            Triple("Nub Cleanup & Snap Fit", "Frame & armor", 1),
            Triple("Panel Lining", "Fine detail accent", 1),
            Triple("Waterslide / Sticker Decals", "Surface markings", 1),
            Triple("Matte / Gloss Topcoat", "Final finish", 1),
        ),
        isFavorite = isFavorite,
        onBack = onBack,
        onToggleFavorite = onToggleFavorite,
    )
}

@Composable
private fun ProjectChecklistScreen(
    toolType: String,
    title: String,
    subtitle: String,
    defaultItemTemplates: List<Triple<String, String, Int>>,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val container = LocalAppContainer.current
    val repo = container.repository
    val feedback = container.feedback
    val scope = rememberCoroutineScope()

    val projects by repo.observeProjects(toolType).collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    val activeProject = remember(projects, selectedProjectId) {
        projects.firstOrNull { it.id == selectedProjectId } ?: projects.firstOrNull()
    }
    val items by (activeProject?.let { repo.observeProjectItems(it.id) } ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }

    if (showNewProjectDialog) {
        var name by remember { mutableStateOf("") }
        var setNum by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            containerColor = palette.cardElevated,
            title = { Text(if (toolType == "LEGO") "New LEGO Project" else "New Model Kit", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Project Name") }, singleLine = true)
                    OutlinedTextField(value = setNum, onValueChange = { setNum = it }, label = { Text("Set / Kit # (optional)") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, maxLines = 3)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val newId = repo.addProject(
                                toolType = toolType,
                                name = name.ifBlank { if (toolType == "LEGO") "LEGO Build" else "Scale Model" },
                                setNumber = setNum,
                                notes = notes,
                                defaultItems = defaultItemTemplates,
                            )
                            selectedProjectId = newId
                        }
                        showNewProjectDialog = false
                    },
                ) { Text("Create", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    if (showAddItemDialog && activeProject != null) {
        var itemTitle by remember { mutableStateOf("") }
        var itemSub by remember { mutableStateOf("") }
        var qtyStr by remember { mutableStateOf(if (toolType == "LEGO") "4" else "1") }
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            containerColor = palette.cardElevated,
            title = { Text(if (toolType == "LEGO") "Add Brick / Step" else "Add Assembly Step", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = itemTitle, onValueChange = { itemTitle = it }, label = { Text("Title / Part Name") }, singleLine = true)
                    OutlinedTextField(value = itemSub, onValueChange = { itemSub = it }, label = { Text("Color / Stage Note") }, singleLine = true)
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it.filter(Char::isDigit) },
                        label = { Text("Target Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (itemTitle.isNotBlank()) {
                            scope.launch {
                                repo.addChecklistItem(
                                    projectId = activeProject.id,
                                    title = itemTitle,
                                    subtitle = itemSub,
                                    targetCount = (qtyStr.toIntOrNull() ?: 1).coerceAtLeast(1),
                                )
                            }
                        }
                        showAddItemDialog = false
                    },
                ) { Text("Add", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    if (showNotesDialog && activeProject != null) {
        var noteText by remember { mutableStateOf(activeProject.notes) }
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Project Notes", color = palette.textPrimary) },
            text = {
                OutlinedTextField(value = noteText, onValueChange = { noteText = it }, label = { Text("Personal Notes") }, maxLines = 5)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { repo.updateProjectNotes(activeProject, noteText.trim()) }
                        showNotesDialog = false
                    },
                ) { Text("Save", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    MonoToolScaffold(
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(onClick = { showNewProjectDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "New project", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (projects.isEmpty()) {
                item {
                    MonoEmptyState(
                        icon = Icons.Rounded.Inventory2,
                        title = "No projects yet",
                        description = "Create a project to track parts, assembly steps, completion percentage, and build notes offline.",
                        actionLabel = "Create First Project",
                        onAction = { showNewProjectDialog = true },
                    )
                }
            } else {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(projects, key = { it.id }) { proj ->
                            MonoPillTag(
                                label = if (proj.setNumber.isNotBlank()) "${proj.name} (#${proj.setNumber})" else proj.name,
                                selected = activeProject?.id == proj.id,
                                onClick = { selectedProjectId = proj.id },
                            )
                        }
                    }
                }

                activeProject?.let { proj ->
                    val completedCount = items.count { it.isDone || it.currentCount >= it.targetCount }
                    val progress = if (items.isEmpty()) 0f else completedCount.toFloat() / items.size.toFloat()

                    item {
                        MonoCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(proj.name, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                                    if (proj.setNumber.isNotBlank()) {
                                        Text("Set / Kit #${proj.setNumber}", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                                    }
                                }
                                IconButton(
                                    onClick = { scope.launch { repo.deleteProject(proj.id) } },
                                ) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete project", tint = palette.textMuted)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                color = StatusSuccess,
                                trackColor = palette.surfaceSecondary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "$completedCount / ${items.size} complete (${(progress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.textSecondary,
                                )
                                TextButton(onClick = { showNotesDialog = true }) {
                                    Text(if (proj.notes.isBlank()) "Add Notes" else "Edit Notes", color = palette.textPrimary)
                                }
                            }
                            if (proj.notes.isNotBlank()) {
                                Text(proj.notes, style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                            }
                        }
                    }

                    items(items, key = { it.id }) { item ->
                        val done = item.isDone || item.currentCount >= item.targetCount
                        MonoCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = {
                                        val nextDone = !done
                                        val nextCount = if (nextDone) item.targetCount else 0
                                        scope.launch {
                                            repo.updateChecklistItem(item.copy(isDone = nextDone, currentCount = nextCount))
                                        }
                                        feedback.play(SoundCue.TICK)
                                    },
                                ) {
                                    Icon(
                                        imageVector = if (done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = "Toggle item",
                                        tint = if (done) StatusSuccess else palette.textMuted,
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                                    if (item.subtitle.isNotBlank()) {
                                        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                                    }
                                }
                                if (item.targetCount > 1) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                val next = (item.currentCount - 1).coerceAtLeast(0)
                                                scope.launch {
                                                    repo.updateChecklistItem(item.copy(currentCount = next, isDone = next >= item.targetCount))
                                                }
                                            },
                                        ) {
                                            Icon(Icons.Rounded.Remove, contentDescription = "Decrease count", tint = palette.textSecondary)
                                        }
                                        Text(
                                            text = "${item.currentCount}/${item.targetCount}",
                                            style = MonoTabularStyle.copy(fontWeight = FontWeight.Bold),
                                            color = palette.textPrimary,
                                        )
                                        IconButton(
                                            onClick = {
                                                val next = (item.currentCount + 1).coerceAtMost(item.targetCount)
                                                scope.launch {
                                                    repo.updateChecklistItem(item.copy(currentCount = next, isDone = next >= item.targetCount))
                                                }
                                                feedback.play(SoundCue.CLICK)
                                            },
                                        ) {
                                            Icon(Icons.Rounded.Add, contentDescription = "Increase count", tint = palette.textPrimary)
                                        }
                                    }
                                }
                                IconButton(onClick = { scope.launch { repo.deleteChecklistItem(item.id) } }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove item", tint = palette.textMuted)
                                }
                            }
                        }
                    }

                    item {
                        MonoActionButton(
                            text = if (toolType == "LEGO") "Add Brick or Step" else "Add Assembly Step",
                            icon = Icons.Rounded.Add,
                            primary = false,
                            onClick = { showAddItemDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
