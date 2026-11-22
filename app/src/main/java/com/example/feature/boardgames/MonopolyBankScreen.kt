package com.example.feature.boardgames

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LocalAppContainer
import com.example.core.SoundCue
import com.example.data.model.ToolCatalog
import com.example.ui.components.MonoActionButton
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoPillTag
import com.example.ui.components.MonoToolScaffold
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.launch

private data class MonopolyPlayer(
    val name: String,
    val balance: Int,
)

private data class MonopolyTx(
    val description: String,
    val amount: Int,
)

private data class PropertyGroupRent(
    val group: String,
    val baseRent: Int,
    val house1: Int,
    val house2: Int,
    val house3: Int,
    val house4: Int,
    val hotel: Int,
)

private val propertyGroups = listOf(
    PropertyGroupRent("Brown", 4, 20, 60, 180, 320, 450),
    PropertyGroupRent("Light Blue", 6, 30, 90, 270, 400, 550),
    PropertyGroupRent("Pink", 10, 50, 150, 450, 625, 750),
    PropertyGroupRent("Orange", 14, 70, 200, 550, 750, 950),
    PropertyGroupRent("Red", 18, 90, 250, 700, 875, 1050),
    PropertyGroupRent("Yellow", 22, 110, 330, 800, 975, 1150),
    PropertyGroupRent("Green", 26, 130, 390, 900, 1100, 1275),
    PropertyGroupRent("Dark Blue", 50, 200, 600, 1400, 1700, 2000),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonopolyBankScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val container = LocalAppContainer.current
    val feedback = container.feedback
    val repo = container.repository
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val players = remember {
        mutableStateListOf(
            MonopolyPlayer("Player 1", 1500),
            MonopolyPlayer("Player 2", 1500),
            MonopolyPlayer("Player 3", 1500),
        )
    }
    val txHistory = remember { mutableStateListOf<MonopolyTx>() }
    var freeParkingPot by remember { mutableIntStateOf(0) }

    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showRentDialog by remember { mutableStateOf(false) }
    var adjustPlayerIdx by remember { mutableStateOf<Int?>(null) }

    fun logTx(desc: String, amount: Int) {
        txHistory.add(0, MonopolyTx(desc, amount))
        if (txHistory.size > 40) txHistory.removeAt(txHistory.lastIndex)
    }

    if (showAddPlayerDialog) {
        var name by remember { mutableStateOf("Player ${players.size + 1}") }
        var startCash by remember { mutableStateOf("1500") }
        AlertDialog(
            onDismissRequest = { showAddPlayerDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Add Player", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(
                        value = startCash,
                        onValueChange = { startCash = it.filter(Char::isDigit) },
                        label = { Text("Starting Cash ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        players.add(MonopolyPlayer(name.trim().ifEmpty { "Player ${players.size + 1}" }, startCash.toIntOrNull() ?: 1500))
                        showAddPlayerDialog = false
                    },
                ) { Text("Add", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlayerDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    adjustPlayerIdx?.let { idx ->
        val p = players.getOrNull(idx)
        if (p != null) {
            var amountStr by remember { mutableStateOf("100") }
            var renameStr by remember { mutableStateOf(p.name) }
            AlertDialog(
                onDismissRequest = { adjustPlayerIdx = null },
                containerColor = palette.cardElevated,
                title = { Text("${p.name} ($${p.balance})", color = palette.textPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = renameStr, onValueChange = { renameStr = it }, label = { Text("Player Name") }, singleLine = true)
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it.filter(Char::isDigit) },
                            label = { Text("Amount ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(50, 100, 200, 500).forEach { quick ->
                                MonoPillTag(label = "$$quick", onClick = { amountStr = quick.toString() })
                            }
                        }
                    }
                },
                confirmButton = {
                    Row {
                        TextButton(
                            onClick = {
                                val amt = amountStr.toIntOrNull() ?: 0
                                val finalName = renameStr.trim().ifEmpty { p.name }
                                players[idx] = p.copy(name = finalName, balance = (p.balance - amt).coerceAtLeast(0))
                                if (amt > 0) logTx("$finalName paid Bank", -amt)
                                feedback.play(SoundCue.CLICK)
                                adjustPlayerIdx = null
                            },
                        ) { Text("- Subtract", color = StatusError) }
                        TextButton(
                            onClick = {
                                val amt = amountStr.toIntOrNull() ?: 0
                                val finalName = renameStr.trim().ifEmpty { p.name }
                                players[idx] = p.copy(name = finalName, balance = p.balance + amt)
                                if (amt > 0) logTx("Bank paid $finalName", amt)
                                feedback.play(SoundCue.CLICK)
                                adjustPlayerIdx = null
                            },
                        ) { Text("+ Add", color = StatusSuccess) }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { adjustPlayerIdx = null }) { Text("Close", color = palette.textSecondary) }
                },
            )
        }
    }

    if (showTransferDialog && players.size >= 2) {
        var fromIdx by remember { mutableIntStateOf(0) }
        var toIdx by remember { mutableIntStateOf(1) }
        var amountStr by remember { mutableStateOf("100") }
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Player Transfer", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("From:", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        players.forEachIndexed { idx, p ->
                            MonoPillTag(label = p.name, selected = fromIdx == idx, onClick = { fromIdx = idx })
                        }
                    }
                    Text("To:", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        players.forEachIndexed { idx, p ->
                            MonoPillTag(label = p.name, selected = toIdx == idx, onClick = { toIdx = idx })
                        }
                    }
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter(Char::isDigit) },
                        label = { Text("Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amt = amountStr.toIntOrNull() ?: 0
                        if (fromIdx != toIdx && amt > 0) {
                            val sender = players[fromIdx]
                            val receiver = players[toIdx]
                            players[fromIdx] = sender.copy(balance = (sender.balance - amt).coerceAtLeast(0))
                            players[toIdx] = receiver.copy(balance = receiver.balance + amt)
                            logTx("${sender.name} → ${receiver.name}", amt)
                            feedback.play(SoundCue.CLICK)
                        }
                        showTransferDialog = false
                    },
                ) { Text("Send", color = palette.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    if (showRentDialog) {
        var selectedGroupIdx by remember { mutableIntStateOf(0) }
        var devLevel by remember { mutableIntStateOf(0) } // 0=base, 1..4=houses, 5=hotel, 6=monopoly(2x)
        val prop = propertyGroups[selectedGroupIdx]
        val computedRent = when (devLevel) {
            0 -> prop.baseRent
            6 -> prop.baseRent * 2
            1 -> prop.house1
            2 -> prop.house2
            3 -> prop.house3
            4 -> prop.house4
            else -> prop.hotel
        }
        AlertDialog(
            onDismissRequest = { showRentDialog = false },
            containerColor = palette.cardElevated,
            title = { Text("Rent Calculator", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        propertyGroups.forEachIndexed { idx, g ->
                            MonoPillTag(label = g.group, selected = selectedGroupIdx == idx, onClick = { selectedGroupIdx = idx })
                        }
                    }
                    Text("Development:", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            0 to "Base",
                            6 to "Color Set (2x)",
                            1 to "1 House",
                            2 to "2 Houses",
                            3 to "3 Houses",
                            4 to "4 Houses",
                            5 to "Hotel",
                        ).forEach { (lvl, lbl) ->
                            MonoPillTag(label = lbl, selected = devLevel == lvl, onClick = { devLevel = lvl })
                        }
                    }
                    MonoCard(cornerRadius = 14.dp, modifier = Modifier.fillMaxWidth()) {
                        Text("RENT DUE", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                        Text(
                            text = "$$computedRent",
                            style = MonoTabularStyle.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRentDialog = false }) { Text("Done", color = palette.textPrimary) }
            },
        )
    }

    MonoToolScaffold(
        title = "Monopoly Digital Bank",
        subtitle = "Cash Tracker, Transfers & Rent Calc",
        onBack = onBack,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        actions = {
            IconButton(onClick = { showRentDialog = true }) {
                Icon(Icons.Rounded.Calculate, contentDescription = "Rent calculator", tint = palette.textPrimary)
            }
            IconButton(
                onClick = {
                    scope.launch {
                        val standings = players.sortedByDescending { it.balance }
                            .joinToString(" • ") { "${it.name}: $${it.balance}" }
                        repo.recordHistory(
                            toolId = ToolCatalog.TOOL_MONOPOLY_BANK,
                            category = "Board Games",
                            title = "Monopoly Bank Session",
                            summary = standings,
                            detail = "${txHistory.size} transactions",
                        )
                        feedback.play(SoundCue.COMPLETE)
                        Toast.makeText(context, "Bank session saved", Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Icon(Icons.Rounded.Save, contentDescription = "Save session", tint = palette.textPrimary)
            }
        },
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MonoActionButton(
                        text = "Transfer",
                        icon = Icons.Rounded.SwapHoriz,
                        onClick = { showTransferDialog = true },
                        modifier = Modifier.weight(1f),
                    )
                    MonoActionButton(
                        text = "Rent Calc",
                        icon = Icons.Rounded.Calculate,
                        primary = false,
                        onClick = { showRentDialog = true },
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showAddPlayerDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add player", tint = palette.textPrimary)
                    }
                    IconButton(
                        onClick = {
                            for (i in players.indices) players[i] = players[i].copy(balance = 1500)
                            freeParkingPot = 0
                            txHistory.clear()
                            feedback.play(SoundCue.CLICK)
                        },
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reset bank", tint = palette.textSecondary)
                    }
                }
            }

            // Free Parking Pot Card
            item {
                MonoCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("FREE PARKING POT", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                            Text(
                                text = "$$freeParkingPot",
                                style = MonoTabularStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MonoPillTag(label = "+$50", onClick = { freeParkingPot += 50; logTx("Added to Free Parking", 50) })
                            MonoPillTag(label = "+$100", onClick = { freeParkingPot += 100; logTx("Added to Free Parking", 100) })
                            if (freeParkingPot > 0) {
                                MonoPillTag(label = "Clear", onClick = { freeParkingPot = 0 })
                            }
                        }
                    }
                }
            }

            itemsIndexed(players) { idx, player ->
                MonoCard(
                    onClick = { adjustPlayerIdx = idx },
                    cornerRadius = 20.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(player.name, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            Text("Tap to add, subtract, or rename", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                        }
                        Text(
                            text = "$${player.balance}",
                            style = MonoTabularStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MonoPillTag(
                            label = "Pass Go +$200",
                            onClick = {
                                players[idx] = player.copy(balance = player.balance + 200)
                                logTx("${player.name} passed Go", 200)
                                feedback.play(SoundCue.CLICK)
                            },
                        )
                        MonoPillTag(
                            label = "-$50",
                            onClick = {
                                players[idx] = player.copy(balance = (player.balance - 50).coerceAtLeast(0))
                                logTx("${player.name} paid $50", -50)
                                feedback.play(SoundCue.CLICK)
                            },
                        )
                        if (freeParkingPot > 0) {
                            MonoPillTag(
                                label = "Claim Pot",
                                onClick = {
                                    val claimed = freeParkingPot
                                    players[idx] = player.copy(balance = player.balance + claimed)
                                    freeParkingPot = 0
                                    logTx("${player.name} claimed Free Parking", claimed)
                                    feedback.play(SoundCue.COMPLETE)
                                },
                            )
                        }
                    }
                }
            }

            if (txHistory.isNotEmpty()) {
                item {
                    Text("Transaction History", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
                items(txHistory) { tx ->
                    MonoCard(cornerRadius = 14.dp, contentPadding = PaddingValues(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(tx.description, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                            Text(
                                text = if (tx.amount >= 0) "+$${tx.amount}" else "-$${-tx.amount}",
                                style = MonoTabularStyle.copy(fontWeight = FontWeight.Bold),
                                color = if (tx.amount >= 0) StatusSuccess else StatusError,
                            )
                        }
                    }
                }
            }
        }
    }
}
