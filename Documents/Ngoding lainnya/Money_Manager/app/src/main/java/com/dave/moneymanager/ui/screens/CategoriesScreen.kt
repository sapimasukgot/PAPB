package com.dave.moneymanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import com.dave.moneymanager.ui.components.AddTransactionDialog
import com.dave.moneymanager.ui.components.ImportBudgetDialog
import com.dave.moneymanager.ui.components.WalletHistoryDetailView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.Bank
import com.dave.moneymanager.data.model.CategoryBudgetStatus
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionCategory
import com.dave.moneymanager.ui.components.AddEditCategoryDialog
import com.dave.moneymanager.ui.components.SetBudgetDialog
import com.dave.moneymanager.ui.components.TransactionItem
import com.dave.moneymanager.ui.theme.Amber500
import com.dave.moneymanager.ui.theme.Blue500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import com.dave.moneymanager.ui.theme.Slate700
import com.dave.moneymanager.ui.viewmodel.CategorySummary
import com.dave.moneymanager.ui.viewmodel.MoneyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CategoriesScreen(
    viewModel: MoneyViewModel,
    initialTab: Int = 0
) {
    var currentTopTab by remember { mutableIntStateOf(initialTab) }
    var isExpenseTab by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf<CategorySummary?>(null) }
    var selectedWallet by remember { mutableStateOf<Bank?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<TransactionCategory?>(null) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var showWalletPickerForMutation by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedWallet != null || selectedCategory != null) {
        if (selectedWallet != null) {
            selectedWallet = null
        } else {
            selectedCategory = null
        }
    }

    val banks by viewModel.banks.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val rawExpenseCategories by viewModel.expenseCategories.collectAsState()
    val rawIncomeCategories by viewModel.incomeCategories.collectAsState()
    val expenseCategories by viewModel.expenseCategorySummaries.collectAsState()
    val incomeCategories by viewModel.incomeCategorySummaries.collectAsState()
    val filteredExpense by viewModel.filteredExpense.collectAsState()
    val filteredIncome by viewModel.filteredIncome.collectAsState()
    val transactionEditHistories by viewModel.transactionEditHistories.collectAsState()
    val shields by viewModel.shields.collectAsState()
    val shieldHistories by viewModel.shieldHistories.collectAsState()
    val shieldContacts by viewModel.shieldContacts.collectAsState()
    val activeCategory = selectedCategory?.let { sel ->
        val allSummaries = expenseCategories + incomeCategories
        allSummaries.find { it.categoryName.equals(sel.categoryName, ignoreCase = true) } ?: sel
    }
    val activeWallet = selectedWallet?.let { sel ->
        banks.find { it.id == sel.id } ?: sel
    }

    val detailViewType = when {
        activeWallet != null -> "WALLET"
        activeCategory != null -> "CATEGORY"
        else -> "MAIN"
    }

    Crossfade(targetState = detailViewType, label = "categories_screen_transition") { viewType ->
        when (viewType) {
            "WALLET" -> {
                activeWallet?.let { wallet ->
                    WalletHistoryDetailView(
                        wallet = wallet,
                        allTransactions = allTransactions,
                        viewModel = viewModel,
                        expenseCategories = rawExpenseCategories,
                        incomeCategories = rawIncomeCategories,
                        transactionEditHistories = transactionEditHistories,
                        onBack = { selectedWallet = null },
                        onEditTransaction = { transactionToEdit = it },
                        onDeleteTransaction = { transactionToDelete = it }
                    )
                }
            }
            "CATEGORY" -> {
                activeCategory?.let { currentCategorySummary ->
                    CategoryHistoryDetailView(
                        category = currentCategorySummary,
                        viewModel = viewModel,
                        onBack = { selectedCategory = null },
                        onEdit = {
                            categoryToEdit = currentCategorySummary.toTransactionCategory()
                            showCategoryDialog = true
                        },
                        onEditTransaction = { transactionToEdit = it },
                        onDeleteTransaction = { transactionToDelete = it }
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (currentTopTab == 0) Emerald500 else Color.Transparent)
                                .clickable { currentTopTab = 0 }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = if (currentTopTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Categories",
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentTopTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (currentTopTab == 1) Emerald500 else Color.Transparent)
                                .clickable { currentTopTab = 1 }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = if (currentTopTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Monthly Budget",
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentTopTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentTopTab == 0) {

                        if (banks.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (banks.size == 1) {
                                            selectedWallet = banks.first()
                                        } else {
                                            showWalletPickerForMutation = true
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Emerald500.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = Emerald500,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Mutasi Wallet",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text(
                                                text = "Tekan untuk melihat riwayat mutasi dompet",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Emerald500)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Buka",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Category Breakdown",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Tap any category to view history",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }

                            Button(
                                onClick = {
                                    categoryToEdit = null
                                    showCategoryDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Category", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isExpenseTab) Rose500 else Color.Transparent)
                                    .clickable { isExpenseTab = true }
                                    .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Expenses (${viewModel.formatRupiah(filteredExpense)})",
                                fontWeight = FontWeight.Bold,
                                color = if (isExpenseTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isExpenseTab) Emerald500 else Color.Transparent)
                                .clickable { isExpenseTab = false }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Income (${viewModel.formatRupiah(filteredIncome)})",
                                fontWeight = FontWeight.Bold,
                                color = if (!isExpenseTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val currentList = if (isExpenseTab) expenseCategories else incomeCategories
                    val totalForTab = if (isExpenseTab) filteredExpense else filteredIncome

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(currentList) { catSummary ->
                            CategoryCardItem(
                                category = catSummary,
                                totalGroupAmount = totalForTab,
                                formatRupiah = { viewModel.formatRupiah(it) },
                                onClick = { selectedCategory = catSummary },
                                onEdit = {
                                    categoryToEdit = catSummary.toTransactionCategory()
                                    showCategoryDialog = true
                                }
                            )
                        }
                    }
                } else {

                    BudgetingSegmentView(
                        viewModel = viewModel,
                        onInspectCategoryHistory = { catName ->
                            val matchedSummary = expenseCategories.find { it.categoryName.equals(catName, ignoreCase = true) }
                            if (matchedSummary != null) {
                                selectedCategory = matchedSummary
                            }
                        }
                    )
                }
            }
        }
    }
}

    if (showCategoryDialog) {
        AddEditCategoryDialog(
            categoryToEdit = categoryToEdit,
            defaultIsExpense = isExpenseTab,
            onDismiss = {
                showCategoryDialog = false
                categoryToEdit = null
            },
            onSave = { name, icon, colorHex, isExpense ->
                if (categoryToEdit == null) {
                    viewModel.addCategory(name, icon, colorHex, isExpense)
                } else {
                    val oldName = categoryToEdit!!.name
                    viewModel.updateCategory(
                        oldName = oldName,
                        name = name,
                        icon = icon,
                        colorHex = colorHex,
                        isExpense = isExpense
                    )
                    if (selectedCategory?.categoryName.equals(oldName, ignoreCase = true)) {
                        selectedCategory = selectedCategory?.copy(
                            categoryName = name,
                            icon = icon,
                            colorHex = colorHex,
                            isExpense = isExpense
                        )
                    }
                }
                showCategoryDialog = false
                categoryToEdit = null
            },
            onDelete = if (categoryToEdit != null) {
                {
                    val nameToDelete = categoryToEdit!!.name
                    viewModel.deleteCategory(nameToDelete)
                    if (selectedCategory?.categoryName.equals(nameToDelete, ignoreCase = true)) {
                        selectedCategory = null
                    }
                    showCategoryDialog = false
                    categoryToEdit = null
                }
            } else null
        )
    }

    if (transactionToEdit != null) {
        val tx = transactionToEdit!!
        val editHistories = transactionEditHistories[tx.id] ?: emptyList()
        AddTransactionDialog(
            banks = banks,
            transactionToEdit = tx,
            expenseCategories = rawExpenseCategories,
            incomeCategories = rawIncomeCategories,
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
                androidx.compose.material3.TextButton(onClick = { transactionToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showWalletPickerForMutation) {
        AlertDialog(
            onDismissRequest = { showWalletPickerForMutation = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Emerald500.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Emerald500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pilih Wallet / Rekening",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pilih dompet untuk melihat detail mutasi transaksi:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(banks) { bank ->
                            val bankIcon = when (bank.type) {
                                Bank.TYPE_EWALLET -> Icons.Default.Wallet
                                Bank.TYPE_CASH -> Icons.Default.Payments
                                else -> Icons.Default.AccountBalance
                            }
                            val typeLabel = when (bank.type) {
                                Bank.TYPE_EWALLET -> "E-Wallet"
                                Bank.TYPE_CASH -> "Tunai / Cash"
                                Bank.TYPE_BANK -> "Rekening Bank"
                                else -> "Dompet"
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedWallet = bank
                                        showWalletPickerForMutation = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(bank.colorHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = bankIcon,
                                                contentDescription = bank.name,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = bank.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = typeLabel,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = viewModel.formatRupiah(bank.balance),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald500
                                            )
                                        )
                                        Text(
                                            text = "Lihat Mutasi →",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Emerald500,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showWalletPickerForMutation = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun BudgetingSegmentView(
    viewModel: MoneyViewModel,
    onInspectCategoryHistory: (String) -> Unit
) {
    val budgetStatuses by viewModel.categoryBudgetStatuses.collectAsState()
    val totalBudgetAmount by viewModel.totalBudgetAmount.collectAsState()
    val totalBudgetSpent by viewModel.totalBudgetSpent.collectAsState()
    val totalCurrentMonthExpense by viewModel.totalCurrentMonthExpense.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val currentBudgetMonthYear by viewModel.budgetMonthYear.collectAsState()
    val displayMonthYear = remember(currentBudgetMonthYear) {
        viewModel.formatMonthYearDisplay(currentBudgetMonthYear)
    }

    val context = LocalContext.current
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var showImportBudgetDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<String?>(null) }
    var editingLimit by remember { mutableStateOf<Double?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.prevBudgetMonth() }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }

                    Text(
                        text = displayMonthYear,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    IconButton(onClick = { viewModel.nextBudgetMonth() }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Monthly Budget Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total Budget",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = viewModel.formatRupiah(totalBudgetAmount),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Current Spending",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            val isTotalExceeded = totalBudgetSpent > totalBudgetAmount && totalBudgetAmount > 0
                            Text(
                                text = viewModel.formatRupiah(totalBudgetSpent),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTotalExceeded) Rose500 else Emerald500
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val overallPct = if (totalBudgetAmount > 0) {
                        (totalBudgetSpent / totalBudgetAmount).coerceIn(0.0, 1.0).toFloat()
                    } else 0f
                    val isExceeded = totalBudgetSpent > totalBudgetAmount && totalBudgetAmount > 0

                    LinearProgressIndicator(
                        progress = { overallPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            isExceeded -> Rose500
                            overallPct >= 0.8f -> Amber500
                            else -> Emerald500
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "All Expenses This Month",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = viewModel.formatRupiah(totalCurrentMonthExpense),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val remaining = totalBudgetAmount - totalBudgetSpent
                            Text(
                                text = if (remaining >= 0) "Remaining Budget" else "Over Budget Deficit",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (remaining >= 0) Emerald500 else Rose500,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = if (remaining >= 0) viewModel.formatRupiah(remaining) else "-" + viewModel.formatRupiah(-remaining),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining >= 0) Emerald500 else Rose500
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Limits (${budgetStatuses.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showImportBudgetDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Emerald500
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Emerald500)
                    }

                    Button(
                        onClick = {
                            editingCategory = null
                            editingLimit = null
                            showSetBudgetDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Set Budget", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (budgetStatuses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Budgets Configured",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set monthly spending limits for selected categories to avoid overspending.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { showImportBudgetDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Emerald500
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import Bulan Lalu", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Emerald500)
                            }

                            Button(
                                onClick = {
                                    editingCategory = null
                                    editingLimit = null
                                    showSetBudgetDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Atur Budget", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            items(budgetStatuses) { status ->
                BudgetStatusCardItem(
                    status = status,
                    formatRupiah = { viewModel.formatRupiah(it) },
                    onEdit = {
                        editingCategory = status.category
                        editingLimit = status.monthlyLimit
                        showSetBudgetDialog = true
                    },
                    onDelete = { viewModel.deleteBudget(status.budgetId) },
                    onClick = { onInspectCategoryHistory(status.category) }
                )
            }
        }
    }

    if (showSetBudgetDialog) {
        SetBudgetDialog(
            initialCategory = editingCategory,
            initialLimit = editingLimit,
            monthYearTitle = viewModel.getBudgetMonthYearDisplay(),
            expenseCategories = expenseCategories,
            onDismiss = { showSetBudgetDialog = false },
            onSave = { category, limit ->
                viewModel.setOrUpdateBudget(category, limit)
            }
        )
    }

    if (showImportBudgetDialog) {
        ImportBudgetDialog(
            viewModel = viewModel,
            onDismiss = { showImportBudgetDialog = false },
            onSuccess = { count ->
                Toast.makeText(
                    context,
                    "Berhasil mengimpor $count budget ke ${viewModel.getBudgetMonthYearDisplay()}!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
private fun BudgetStatusCardItem(
    status: CategoryBudgetStatus,
    formatRupiah: (Double) -> String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val icon = getCategoryIcon(status.iconName, status.category, isExpense = true)
    val pctDisplay = (status.percentageUsed * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(status.colorHex).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = status.category,
                            tint = Color(status.colorHex),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = status.category,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Limit: ${formatRupiah(status.monthlyLimit)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Blue500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Rose500.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { status.percentageUsed.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    status.isExceeded -> Rose500
                    status.percentageUsed >= 0.8f -> Amber500
                    else -> Emerald500
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spent: ${formatRupiah(status.currentSpent)} ($pctDisplay%)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                status.isExceeded -> Rose500.copy(alpha = 0.15f)
                                status.percentageUsed >= 0.8f -> Amber500.copy(alpha = 0.15f)
                                else -> Emerald500.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (status.isExceeded) {
                            "⚠️ Exceeded: +${formatRupiah(status.currentSpent - status.monthlyLimit)}"
                        } else {
                            "Remaining: ${formatRupiah(status.remainingBudget)}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when {
                                status.isExceeded -> Rose500
                                status.percentageUsed >= 0.8f -> Amber500
                                else -> Emerald500
                            },
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCardItem(
    category: CategorySummary,
    totalGroupAmount: Double,
    formatRupiah: (Double) -> String,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    val percentage = if (totalGroupAmount > 0) (category.totalAmount / totalGroupAmount).toFloat() else 0f
    val icon = getCategoryIcon(category.icon, category.categoryName, category.isExpense)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(category.colorHex).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = category.categoryName,
                            tint = Color(category.colorHex),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = category.categoryName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${category.transactionCount} transactions",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatRupiah(category.totalAmount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (category.isExpense) Rose500 else Emerald500
                            )
                        )
                        Text(
                            text = "${(percentage * 100).toInt()}% of total",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Category",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(category.colorHex),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryHistoryDetailView(
    category: CategorySummary,
    viewModel: MoneyViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit
) {
    val icon = getCategoryIcon(category.icon, category.categoryName, category.isExpense)
    val transactions = category.transactions
    val transactionEditHistories by viewModel.transactionEditHistories.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Category",
                    tint = Emerald500
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(category.colorHex).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.categoryName,
                        tint = Color(category.colorHex),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (category.isExpense) "Total Expense" else "Total Income",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                    Text(
                        text = viewModel.formatRupiah(category.totalAmount),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (category.isExpense) Rose500 else Emerald500
                        )
                    )
                    Text(
                        text = "${category.transactionCount} transactions recorded",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Transactions Found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "No activity in ${category.categoryName} for the selected period.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        } else {

            val grouped = remember(transactions) {
                val fullDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US)
                val todayFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
                val todayStr = todayFormat.format(Date())

                transactions.groupBy { tx ->
                    val txDateStr = todayFormat.format(Date(tx.timestamp))
                    if (txDateStr == todayStr) {
                        "Today - " + fullDateFormat.format(Date(tx.timestamp))
                    } else {
                        fullDateFormat.format(Date(tx.timestamp))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                grouped.forEach { (dateHeader, txList) ->
                    item {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
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
                            onClick = { onEditTransaction(tx) },
                            onEdit = { onEditTransaction(tx) },
                            onLongClick = { onDeleteTransaction(tx) },
                            onDelete = { onDeleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(iconKey: String, categoryName: String = "", isExpense: Boolean = true): ImageVector {
    val key = iconKey.lowercase().trim()
    val name = categoryName.lowercase().trim()
    return when {
        key == "shield" || name.contains("shield") -> Icons.Default.Shield
        key == "church" || name.contains("church") || name.contains("gereja") -> Icons.Default.Church
        key in listOf("love", "heart", "with_her") || name.contains("with her") || name.contains("love") || name.contains("pacar") -> Icons.Default.Favorite
        key == "food" || name.contains("food") || name.contains("makan") || name.contains("dining") -> Icons.Default.Fastfood
        key == "shopping" || name.contains("shop") || name.contains("belanja") -> Icons.Default.ShoppingBag
        key == "transport" || name.contains("transport") || name.contains("kendaraan") -> Icons.Default.DirectionsCar
        key == "bills" || name.contains("bill") || name.contains("tagihan") || name.contains("utilit") -> Icons.Default.Receipt
        key == "entertainment" || name.contains("entertain") || name.contains("hiburan") || name.contains("movie") -> Icons.Default.Movie
        key == "health" || name.contains("health") || name.contains("sehat") || name.contains("obat") -> Icons.Default.LocalHospital
        key == "education" || name.contains("educat") || name.contains("pendidikan") || name.contains("sekolah") || name.contains("kuliah") -> Icons.Default.School
        key == "reimburse" || name.contains("reimburse") || name.contains("transfer in") || name.contains("incoming transfer") -> Icons.Default.PriceCheck
        key == "salary" || name.contains("salary") || name.contains("gaji") -> Icons.Default.Payments
        key == "bonus" || name.contains("bonus") || name.contains("thr") || name.contains("allowance") -> Icons.Default.CardGiftcard
        key == "investment" || name.contains("invest") || name.contains("saham") -> Icons.Default.TrendingUp
        key in listOf("topup", "transfer_in") || name.contains("top up") || name.contains("deposit") -> Icons.Default.AccountBalanceWallet
        key == "cashback" || name.contains("cashback") || name.contains("reward") -> Icons.Default.LocalAtm
        key == "work" || name.contains("work") || name.contains("kerja") -> Icons.Default.Work
        key == "home" || name.contains("home") || name.contains("rumah") || name.contains("kost") -> Icons.Default.Home
        key == "travel" || name.contains("travel") || name.contains("liburan") || name.contains("trip") -> Icons.Default.Flight
        key == "fitness" || name.contains("fitness") || name.contains("gym") || name.contains("sport") -> Icons.Default.FitnessCenter
        key == "pets" || name.contains("pet") || name.contains("hewan") -> Icons.Default.Pets
        key == "family" || name.contains("family") || name.contains("keluarga") -> Icons.Default.FamilyRestroom
        key in listOf("donation", "charity") || name.contains("charity") || name.contains("donasi") || name.contains("amal") || name.contains("sedekah") -> Icons.Default.VolunteerActivism
        key == "coffee" || name.contains("coffee") || name.contains("kopi") -> Icons.Default.LocalCafe
        else -> if (isExpense) Icons.Default.Receipt else Icons.Default.LocalAtm
    }
}

