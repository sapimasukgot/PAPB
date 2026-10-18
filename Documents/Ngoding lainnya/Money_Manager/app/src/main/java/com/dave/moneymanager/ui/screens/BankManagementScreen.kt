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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.Bank
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.ui.components.AddBankDialog
import com.dave.moneymanager.ui.components.AddTransactionDialog
import com.dave.moneymanager.ui.components.TransferDialog
import com.dave.moneymanager.ui.components.WalletHistoryDetailView
import com.dave.moneymanager.ui.theme.Blue500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import com.dave.moneymanager.ui.viewmodel.MoneyViewModel

@Composable
fun BankManagementScreen(viewModel: MoneyViewModel) {
    val banks by viewModel.banks.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val rawExpenseCategories by viewModel.expenseCategories.collectAsState()
    val rawIncomeCategories by viewModel.incomeCategories.collectAsState()
    val transactionEditHistories by viewModel.transactionEditHistories.collectAsState()
    val shields by viewModel.shields.collectAsState()
    val shieldHistories by viewModel.shieldHistories.collectAsState()
    val shieldContacts by viewModel.shieldContacts.collectAsState()

    var showAddBankDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var editingBank by remember { mutableStateOf<Bank?>(null) }
    var deletingBank by remember { mutableStateOf<Bank?>(null) }
    var selectedWalletForMutation by remember { mutableStateOf<Bank?>(null) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    BackHandler(enabled = selectedWalletForMutation != null) {
        selectedWalletForMutation = null
    }

    val activeWallet = selectedWalletForMutation?.let { sel ->
        banks.find { it.id == sel.id } ?: sel
    }

    Crossfade(targetState = activeWallet, label = "bank_management_crossfade") { wallet ->
        if (wallet != null) {
            WalletHistoryDetailView(
                wallet = wallet,
                allTransactions = allTransactions,
                viewModel = viewModel,
                expenseCategories = rawExpenseCategories,
                incomeCategories = rawIncomeCategories,
                transactionEditHistories = transactionEditHistories,
                onBack = { selectedWalletForMutation = null },
                onEditTransaction = { transactionToEdit = it },
                onDeleteTransaction = { transactionToDelete = it }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Total Balance across Accounts & Wallets",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.formatRupiah(totalBalance),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald500
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showAddBankDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Account", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showTransferDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Transfer", fontWeight = FontWeight.Bold)
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
                            text = "Accounts & Wallets (${banks.size})",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Ketuk akun untuk melihat mutasi",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                itemsIndexed(banks) { index, bank ->
                    BankItemRow(
                        bank = bank,
                        isFirst = index == 0,
                        isLast = index == banks.lastIndex,
                        formatRupiah = { viewModel.formatRupiah(it) },
                        onClick = { selectedWalletForMutation = bank },
                        onMoveUp = { viewModel.moveBankUp(bank.id) },
                        onMoveDown = { viewModel.moveBankDown(bank.id) },
                        onEdit = { editingBank = bank },
                        onDelete = { deletingBank = bank }
                    )
                }
            }
        }
    }

    if (showAddBankDialog) {
        AddBankDialog(
            onDismiss = { showAddBankDialog = false },
            onSave = { name, accNo, bal, type, color ->
                viewModel.addBank(name, accNo, bal, type, color)
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

    if (editingBank != null) {
        var newBalanceText by remember { mutableStateOf(editingBank!!.balance.toLong().toString()) }
        var newName by remember { mutableStateOf(editingBank!!.name) }
        var newAccountNo by remember { mutableStateOf(editingBank!!.accountNumber) }

        AlertDialog(
            onDismissRequest = { editingBank = null },
            title = { Text("Edit Account: ${editingBank!!.name}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Account / Wallet Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newAccountNo,
                        onValueChange = { newAccountNo = it },
                        label = { Text("Phone / Account Number") },
                        placeholder = { Text("e.g. 081234567890") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newBalanceText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) newBalanceText = it },
                        label = { Text("Current Balance (Rp)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bal = newBalanceText.toDoubleOrNull() ?: editingBank!!.balance
                        viewModel.updateBank(editingBank!!.copy(name = newName, accountNumber = newAccountNo, balance = bal))
                        editingBank = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBank = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (deletingBank != null) {
        AlertDialog(
            onDismissRequest = { deletingBank = null },
            title = { Text("Delete Account ${deletingBank!!.name}?") },
            text = {
                Text("All transaction history associated with this account will also be permanently deleted. Continue?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBank(deletingBank!!.id)
                        deletingBank = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingBank = null }) {
                    Text("Cancel")
                }
            }
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
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction of ${viewModel.formatRupiah(tx.amount)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Delete", color = Color.White)
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
private fun BankItemRow(
    bank: Bank,
    isFirst: Boolean,
    isLast: Boolean,
    formatRupiah: (Double) -> String,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                        .background(Color(bank.colorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (bank.type) {
                        Bank.TYPE_EWALLET -> Icons.Default.Wallet
                        Bank.TYPE_CASH -> Icons.Default.Payments
                        else -> Icons.Default.AccountBalance
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = bank.name,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = bank.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (bank.accountNumber.isNotEmpty()) {
                        Text(
                            text = bank.accountNumber,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatRupiah(bank.balance),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Emerald500
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Emerald500.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Mutasi",
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        tint = if (!isFirst) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move Down",
                        tint = if (!isLast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Balance",
                        tint = Blue500,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Rose500.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
