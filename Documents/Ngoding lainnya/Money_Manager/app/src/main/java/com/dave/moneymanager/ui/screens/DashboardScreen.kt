package com.dave.moneymanager.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.TimeFilterMode
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.ui.components.AddBankDialog
import com.dave.moneymanager.ui.components.AddTransactionDialog
import com.dave.moneymanager.ui.components.BankCard
import com.dave.moneymanager.ui.components.NavigationScreen
import com.dave.moneymanager.ui.components.StatSummaryCard
import com.dave.moneymanager.ui.components.TransactionItem
import com.dave.moneymanager.ui.components.TransferDialog
import com.dave.moneymanager.ui.theme.Amber500
import com.dave.moneymanager.ui.theme.Blue500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import com.dave.moneymanager.ui.viewmodel.MoneyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MoneyViewModel,
    onNavigateToScreen: (NavigationScreen) -> Unit
) {
    val context = LocalContext.current
    val banks by viewModel.banks.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val filteredIncome by viewModel.filteredIncome.collectAsState()
    val filteredExpense by viewModel.filteredExpense.collectAsState()
    val filterOptions by viewModel.filterOptions.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val totalUnsettledShield by viewModel.totalUnsettledShieldAmount.collectAsState()
    val unsettledShieldCount by viewModel.unsettledShieldCount.collectAsState()
    val shields by viewModel.shields.collectAsState()
    val shieldHistories by viewModel.shieldHistories.collectAsState()
    val shieldContacts by viewModel.shieldContacts.collectAsState()
    val transactionEditHistories by viewModel.transactionEditHistories.collectAsState()

    val prefs = remember { context.getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE) }
    var isBalanceHidden by remember { mutableStateOf(prefs.getBoolean("hide_balance", false)) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    var showAddTxDialog by remember { mutableStateOf(false) }
    var initialTxType by remember { mutableStateOf(Transaction.TYPE_EXPENSE) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showAddBankDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance() }
    val dayFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US) }

    val activePeriodLabel = remember(filterOptions) {
        when (filterOptions.timeFilterMode) {
            TimeFilterMode.SPECIFIC_DATE -> SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(filterOptions.selectedDateMillis))
            TimeFilterMode.TODAY -> "Today"
            TimeFilterMode.THIS_WEEK -> "This Week"
            TimeFilterMode.ALL -> "All Time"
            TimeFilterMode.CUSTOM_RANGE -> "Custom Range"
            TimeFilterMode.THIS_MONTH -> viewModel.getMonthYearTitle()
        }
    }

    val displayedTransactions = remember(filteredTransactions, searchQuery) {
        if (searchQuery.isBlank()) {
            filteredTransactions
        } else {
            val q = searchQuery.trim()
            filteredTransactions.filter { tx ->
                tx.category.contains(q, ignoreCase = true) ||
                    tx.note.contains(q, ignoreCase = true) ||
                    tx.bankName.contains(q, ignoreCase = true) ||
                    (tx.destination?.contains(q, ignoreCase = true) == true) ||
                    (tx.targetBankName?.contains(q, ignoreCase = true) == true)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    initialTxType = Transaction.TYPE_EXPENSE
                    showAddTxDialog = true
                },
                containerColor = Emerald500,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                StatSummaryCard(
                    totalBalance = totalBalance,
                    periodIncome = filteredIncome,
                    periodExpense = filteredExpense,
                    periodLabel = activePeriodLabel,
                    isBalanceHidden = isBalanceHidden,
                    onToggleHideBalance = {
                        isBalanceHidden = !isBalanceHidden
                        prefs.edit().putBoolean("hide_balance", isBalanceHidden).apply()
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.ArrowUpward,
                        label = "Expense",
                        color = Rose500,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            initialTxType = Transaction.TYPE_EXPENSE
                            showAddTxDialog = true
                        }
                    )

                    QuickActionButton(
                        icon = Icons.Default.ArrowDownward,
                        label = "Income",
                        color = Emerald500,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            initialTxType = Transaction.TYPE_INCOME
                            showAddTxDialog = true
                        }
                    )

                    QuickActionButton(
                        icon = Icons.Default.SyncAlt,
                        label = "Transfer",
                        color = Blue500,
                        modifier = Modifier.weight(1f),
                        onClick = { showTransferDialog = true }
                    )

                    QuickActionButton(
                        icon = Icons.Default.NotificationsActive,
                        label = "Auto-Write",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen(NavigationScreen.AUTO_WRITE) }
                    )
                }
            }

            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Accounts & Wallets",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            if (filterOptions.selectedBankId != null) {
                                val selectedBankName = banks.find { it.id == filterOptions.selectedBankId }?.name ?: "Filtered"
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Emerald500.copy(alpha = 0.15f))
                                        .clickable { viewModel.setBankFilter(null) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = selectedBankName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Emerald500,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear Filter",
                                            tint = Emerald500,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        TextButton(onClick = { onNavigateToScreen(NavigationScreen.BANKS) }) {
                            Text("Manage", color = Emerald500, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(banks) { bank ->
                            val isSelected = filterOptions.selectedBankId == bank.id
                            BankCard(
                                bank = bank,
                                isSelected = isSelected,
                                isBalanceHidden = isBalanceHidden,
                                onClick = {
                                    if (isSelected) {
                                        viewModel.setBankFilter(null)
                                    } else {
                                        viewModel.setBankFilter(bank.id)
                                    }
                                }
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(138.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { showAddBankDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Emerald500.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Emerald500)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Add Account",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Emerald500
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                val totalLimit by viewModel.totalBudgetAmount.collectAsState()
                val totalSpent by viewModel.totalBudgetSpent.collectAsState()
                val pct = if (totalLimit > 0) (totalSpent / totalLimit).coerceIn(0.0, 1.0).toFloat() else 0f
                val isExceeded = totalSpent > totalLimit && totalLimit > 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onNavigateToScreen(NavigationScreen.CATEGORIES) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PieChart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Monthly Budget",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = viewModel.getBudgetMonthYearDisplay(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }

                            TextButton(onClick = { onNavigateToScreen(NavigationScreen.CATEGORIES) }) {
                                Text("Details", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { pct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                isExceeded -> Rose500
                                pct >= 0.8f -> Amber500
                                else -> Emerald500
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: ${viewModel.formatRupiah(totalSpent)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (isExceeded) Rose500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            )
                            Text(
                                text = if (totalLimit > 0) "Limit: ${viewModel.formatRupiah(totalLimit)}" else "Tap to set budget",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onNavigateToScreen(NavigationScreen.SHIELD) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Shield",
                                    tint = if (unsettledShieldCount > 0) Rose500 else Emerald500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Shield & Debt Tracker",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (unsettledShieldCount > 0) {
                                        "$unsettledShieldCount pending receivables (${viewModel.formatRupiah(totalUnsettledShield)})"
                                    } else {
                                        "All receivables settled"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (unsettledShieldCount > 0) Rose500 else Emerald500,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Shield",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Transactions",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${displayedTransactions.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isFilterActive = filterOptions.selectedBankId != null ||
                                filterOptions.timeFilterMode != TimeFilterMode.THIS_MONTH ||
                                !filterOptions.isViewingCurrentMonth() ||
                                filterOptions.typeFilter != "ALL" ||
                                searchQuery.isNotBlank()

                            if (isFilterActive) {
                                TextButton(onClick = {
                                    searchQuery = ""
                                    viewModel.resetFilters()
                                }) {
                                    Text("Reset", color = Rose500, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            IconButton(
                                onClick = {
                                    showSearchBar = !showSearchBar
                                    if (!showSearchBar) searchQuery = ""
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search Transactions",
                                    tint = if (showSearchBar || searchQuery.isNotBlank()) Emerald500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (showSearchBar) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search category, note, merchant, or wallet...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald500,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
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
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = "Select Date",
                                    tint = Emerald500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (filterOptions.timeFilterMode) {
                                        TimeFilterMode.SPECIFIC_DATE -> dayFormat.format(Date(filterOptions.selectedDateMillis))
                                        TimeFilterMode.TODAY -> "Today (${SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())})"
                                        else -> viewModel.getMonthYearTitle()
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
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

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        TimeFilterMode.values().forEach { mode ->
                            val isSelected = when (mode) {
                                TimeFilterMode.THIS_MONTH -> filterOptions.timeFilterMode == mode && filterOptions.isViewingCurrentMonth()
                                else -> filterOptions.timeFilterMode == mode
                            }
                            item {
                                DashboardFilterChip(
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val typeFilters = listOf(
                            "ALL" to "All Types",
                            Transaction.TYPE_EXPENSE to "Expenses",
                            Transaction.TYPE_INCOME to "Income",
                            Transaction.TYPE_TRANSFER to "Transfers"
                        )
                        typeFilters.forEach { (typeKey, label) ->
                            val isSelected = filterOptions.typeFilter == typeKey
                            val activeColor = when (typeKey) {
                                Transaction.TYPE_EXPENSE -> Rose500
                                Transaction.TYPE_INCOME -> Emerald500
                                Transaction.TYPE_TRANSFER -> Blue500
                                else -> Emerald500
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) activeColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.setTypeFilter(typeKey) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            if (displayedTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Transactions Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your filters or tap + to record a transaction.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            } else {
                val grouped = displayedTransactions.groupBy { tx ->
                    val todayFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
                    val dateHeaderFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US)
                    val todayStr = todayFormat.format(Date())
                    val txDateStr = todayFormat.format(Date(tx.timestamp))

                    if (txDateStr == todayStr) {
                        "Today • " + dateHeaderFormat.format(Date(tx.timestamp))
                    } else {
                        dateHeaderFormat.format(Date(tx.timestamp))
                    }
                }

                grouped.forEach { (dateHeader, txList) ->
                    item {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
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

    if (showAddTxDialog) {
        AddTransactionDialog(
            banks = banks,
            initialType = initialTxType,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            shieldContacts = shieldContacts,
            onDismiss = { showAddTxDialog = false },
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

    if (showTransferDialog) {
        TransferDialog(
            banks = banks,
            onDismiss = { showTransferDialog = false },
            onTransfer = { srcId, targetId, amount, note ->
                viewModel.transferMoney(srcId, targetId, amount, note)
            }
        )
    }

    if (showAddBankDialog) {
        AddBankDialog(
            onDismiss = { showAddBankDialog = false },
            onSave = { name, accNo, bal, type, color ->
                viewModel.addBank(name, accNo, bal, type, color)
            }
        )
    }

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        val isIncome = tx.type == Transaction.TYPE_INCOME
        val isTransfer = tx.type == Transaction.TYPE_TRANSFER
        val typeLabel = when {
            isTransfer -> "Transfer"
            isIncome -> "Income"
            else -> "Expense"
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
                        text = "Delete Transaction",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete this transaction?",
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
                        text = "Account balances and period totals on the dashboard will be updated automatically.",
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
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DashboardFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.09f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }
    }
}
