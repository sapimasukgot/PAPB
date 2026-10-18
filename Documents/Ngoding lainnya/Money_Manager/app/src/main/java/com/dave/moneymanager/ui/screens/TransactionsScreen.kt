package com.dave.moneymanager.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.TimeFilterMode
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.ui.components.AddTransactionDialog
import com.dave.moneymanager.ui.components.TransactionItem
import com.dave.moneymanager.ui.theme.Blue500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import com.dave.moneymanager.ui.viewmodel.MoneyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(viewModel: MoneyViewModel) {
    val context = LocalContext.current
    val banks by viewModel.banks.collectAsState()
    val filterOptions by viewModel.filterOptions.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val filteredIncome by viewModel.filteredIncome.collectAsState()
    val filteredExpense by viewModel.filteredExpense.collectAsState()
    val filteredNet by viewModel.filteredNet.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val shields by viewModel.shields.collectAsState()
    val shieldHistories by viewModel.shieldHistories.collectAsState()
    val shieldContacts by viewModel.shieldContacts.collectAsState()
    val transactionEditHistories by viewModel.transactionEditHistories.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    val calendar = remember { Calendar.getInstance() }
    val dayFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Emerald500,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isFilterActive = filterOptions.selectedBankId != null ||
                filterOptions.timeFilterMode != TimeFilterMode.THIS_MONTH ||
                !filterOptions.isViewingCurrentMonth() ||
                filterOptions.typeFilter != "ALL"

            if (isFilterActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtered View",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Reset Filters",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clickable { viewModel.resetFilters() }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                item {
                    val isAllSelected = filterOptions.selectedBankId == null
                    FilterChip(
                        label = "All Accounts",
                        isSelected = isAllSelected,
                        onClick = { viewModel.setBankFilter(null) }
                    )
                }

                items(banks) { bank ->
                    val isSelected = filterOptions.selectedBankId == bank.id
                    FilterChip(
                        label = bank.name,
                        isSelected = isSelected,
                        onClick = { viewModel.setBankFilter(bank.id) }
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeFilterMode.values().forEach { mode ->
                    val isSelected = when (mode) {
                        TimeFilterMode.THIS_MONTH -> filterOptions.timeFilterMode == mode && filterOptions.isViewingCurrentMonth()
                        else -> filterOptions.timeFilterMode == mode
                    }
                    item {
                        FilterChip(
                            label = mode.label,
                            isSelected = isSelected,
                            onClick = {
                                if (mode == TimeFilterMode.SPECIFIC_DATE) {

                                    calendar.timeInMillis = filterOptions.selectedDateMillis
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            calendar.set(Calendar.YEAR, y)
                                            calendar.set(Calendar.MONTH, m)
                                            calendar.set(Calendar.DAY_OF_MONTH, d)
                                            viewModel.setSpecificDate(calendar.timeInMillis)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                } else {
                                    viewModel.setTimeFilterMode(mode)
                                }
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.prevMonth() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            calendar.timeInMillis = filterOptions.selectedDateMillis
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    calendar.set(Calendar.YEAR, y)
                                    calendar.set(Calendar.MONTH, m)
                                    calendar.set(Calendar.DAY_OF_MONTH, d)
                                    viewModel.setSpecificDate(calendar.timeInMillis)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Select Date",
                            tint = Emerald500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (filterOptions.timeFilterMode) {
                                TimeFilterMode.SPECIFIC_DATE -> dayFormat.format(Date(filterOptions.selectedDateMillis))
                                TimeFilterMode.TODAY -> "Today (${SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())})"
                                else -> viewModel.getMonthYearTitle()
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Income", style = MaterialTheme.typography.labelSmall.copy(color = Emerald500))
                        Text(
                            viewModel.formatRupiah(filteredIncome),
                            style = MaterialTheme.typography.titleMedium.copy(color = Emerald500, fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Expenses", style = MaterialTheme.typography.labelSmall.copy(color = Rose500))
                        Text(
                            viewModel.formatRupiah(filteredExpense),
                            style = MaterialTheme.typography.titleMedium.copy(color = Rose500, fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Net Balance", style = MaterialTheme.typography.labelSmall)
                        val netColor = if (filteredNet >= 0) Emerald500 else Rose500
                        Text(
                            viewModel.formatRupiah(filteredNet),
                            style = MaterialTheme.typography.titleMedium.copy(color = netColor, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Transactions Found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Try changing your filters or add a new transaction.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        )
                    }
                }
            } else {

                val grouped = remember(filteredTransactions) {
                    val dateHeaderFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US)
                    val todayFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
                    val todayStr = todayFormat.format(Date())

                    filteredTransactions.groupBy { tx ->
                        val txDateStr = todayFormat.format(Date(tx.timestamp))
                        if (txDateStr == todayStr) {
                            "Today - " + dateHeaderFormat.format(Date(tx.timestamp))
                        } else {
                            dateHeaderFormat.format(Date(tx.timestamp))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp)
                ) {
                    grouped.forEach { (dateHeader, txList) ->
                        item {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                            )
                        }

                        items(txList) { tx ->
                            val shieldStatus = viewModel.getShieldStatusForTransaction(tx.id, tx.note, tx.category)
                            val editHistories = transactionEditHistories[tx.id] ?: emptyList()
                            val shieldAmount = viewModel.getShieldAmountForTransaction(tx)
                            val netAmount = viewModel.getNetExpenseAmount(tx)
                            TransactionItem(
                                transaction = tx,
                                shieldStatus = shieldStatus,
                                shieldAmount = shieldAmount,
                                netExpenseAmount = netAmount,
                                editHistories = editHistories,
                                expenseCategories = expenseCategories,
                                incomeCategories = incomeCategories,
                                onUpdateCategory = { itemTx, newCat ->
                                    viewModel.updateTransaction(
                                        oldTx = itemTx,
                                        newTx = itemTx.copy(category = newCat)
                                    )
                                },
                                onClick = { transactionToEdit = tx },
                                onEdit = { transactionToEdit = tx },
                                onLongClick = { transactionToDelete = tx },
                                onDelete = { transactionToDelete = tx }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            banks = banks,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            shieldContacts = shieldContacts,
            onDismiss = { showAddDialog = false },
            onAddCategory = { name, icon, colorHex, isExp ->
                viewModel.addCategory(name, icon, colorHex, isExp)
            },
            onUpdateCategory = { oldName, name, icon, colorHex, isExp ->
                viewModel.updateCategory(oldName, name, icon, colorHex, isExp)
            },
            onDeleteCategory = { name ->
                viewModel.deleteCategory(name)
            },
            onSave = { bankId, type, amount, category, note, timestamp, destination, shieldFriends, targetBankId ->
                viewModel.addTransaction(
                    bankId = bankId,
                    type = type,
                    amount = amount,
                    category = category,
                    note = note,
                    timestamp = timestamp,
                    targetBankId = targetBankId,
                    destination = destination,
                    shieldFriends = shieldFriends
                )
            }
        )
    }

    if (transactionToEdit != null) {
        val tx = transactionToEdit!!
        val editHistories = transactionEditHistories[tx.id] ?: emptyList()
        AddTransactionDialog(
            banks = banks,
            transactionToEdit = tx,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            shieldContacts = shieldContacts,
            editHistories = editHistories,
            initialShields = shields,
            initialShieldHistories = shieldHistories,
            onDismiss = { transactionToEdit = null },
            onAddCategory = { name, icon, colorHex, isExp ->
                viewModel.addCategory(name, icon, colorHex, isExp)
            },
            onUpdateCategory = { oldName, name, icon, colorHex, isExp ->
                viewModel.updateCategory(oldName, name, icon, colorHex, isExp)
            },
            onDeleteCategory = { name ->
                viewModel.deleteCategory(name)
            },
            onSave = { bankId, type, amount, category, note, timestamp, destination, shieldFriends, targetBankId ->
                viewModel.updateTransaction(
                    oldTx = tx,
                    newTx = tx.copy(
                        bankId = bankId,
                        targetBankId = targetBankId,
                        type = type,
                        amount = amount,
                        category = category,
                        note = note,
                        timestamp = timestamp,
                        destination = destination
                    ),
                    shieldFriends = shieldFriends
                )
                transactionToEdit = null
            }
        )
    }

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        val isIncome = tx.type == Transaction.TYPE_INCOME
        val isTransfer = tx.type == Transaction.TYPE_TRANSFER
        val typeLabel = when {
            isTransfer -> "Transfer"
            isIncome -> "Pemasukan"
            else -> "Pengeluaran"
        }

        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Rose500,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus Riwayat Transaksi",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus transaksi ini?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${tx.category} • $typeLabel",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.formatRupiah(tx.amount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isIncome) Emerald500 else if (isTransfer) Blue500 else Rose500
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isTransfer && tx.targetBankName != null) {
                                    "${tx.bankName} ➔ ${tx.targetBankName}"
                                } else {
                                    tx.bankName
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Saldo akun serta total pemasukan & pengeluaran di dashboard akan diperbarui otomatis.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Emerald500 else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}
