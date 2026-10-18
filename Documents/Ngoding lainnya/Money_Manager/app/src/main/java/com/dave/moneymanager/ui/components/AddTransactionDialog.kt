package com.dave.moneymanager.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dave.moneymanager.data.model.Bank
import com.dave.moneymanager.data.model.ShieldContact
import com.dave.moneymanager.data.model.ShieldEntry
import com.dave.moneymanager.data.model.ShieldFriendInput
import com.dave.moneymanager.data.model.ShieldHistoryItem
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionCategory
import com.dave.moneymanager.data.model.TransactionEditHistory
import com.dave.moneymanager.ui.theme.Amber500
import com.dave.moneymanager.ui.theme.Blue500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class FriendShieldPaymentStatus {
    UNPAID,
    PAID_FULL,
    PARTIAL
}

data class FriendShieldDraft(
    val id: Long = System.currentTimeMillis(),
    val name: String = "",
    val amountText: String = "",
    val note: String = "",
    val paymentStatus: FriendShieldPaymentStatus = FriendShieldPaymentStatus.UNPAID,
    val partialPaidText: String = "",
    val isTransfer: Boolean = true,
    val settleBankId: Long? = null
)

fun mergeDuplicateFriends(list: List<FriendShieldDraft>): List<FriendShieldDraft> {
    val merged = mutableListOf<FriendShieldDraft>()
    for (item in list) {
        val trimmedName = item.name.trim()
        if (trimmedName.isEmpty()) {
            merged.add(item)
            continue
        }
        val existingIndex = merged.indexOfFirst { it.name.trim().equals(trimmedName, ignoreCase = true) }
        if (existingIndex >= 0) {
            val existing = merged[existingIndex]
            val existingAmt = existing.amountText.toDoubleOrNull() ?: 0.0
            val newAmt = item.amountText.toDoubleOrNull() ?: 0.0
            val totalAmt = existingAmt + newAmt
            val totalAmtStr = if (totalAmt > 0) {
                if (totalAmt % 1.0 == 0.0) totalAmt.toLong().toString() else totalAmt.toString()
            } else ""

            val combinedNote = when {
                existing.note.isNotBlank() && item.note.isNotBlank() -> "${existing.note.trim()}, ${item.note.trim()}"
                item.note.isNotBlank() -> item.note.trim()
                else -> existing.note.trim()
            }

            val existingPaid = when (existing.paymentStatus) {
                FriendShieldPaymentStatus.PAID_FULL -> existingAmt
                FriendShieldPaymentStatus.PARTIAL -> existing.partialPaidText.toDoubleOrNull() ?: 0.0
                FriendShieldPaymentStatus.UNPAID -> 0.0
            }
            val newPaid = when (item.paymentStatus) {
                FriendShieldPaymentStatus.PAID_FULL -> newAmt
                FriendShieldPaymentStatus.PARTIAL -> item.partialPaidText.toDoubleOrNull() ?: 0.0
                FriendShieldPaymentStatus.UNPAID -> 0.0
            }
            val totalPaid = existingPaid + newPaid

            val finalStatus = when {
                totalPaid >= totalAmt && totalAmt > 0 -> FriendShieldPaymentStatus.PAID_FULL
                totalPaid > 0 -> FriendShieldPaymentStatus.PARTIAL
                else -> FriendShieldPaymentStatus.UNPAID
            }
            val finalPaidStr = if (finalStatus == FriendShieldPaymentStatus.PARTIAL) {
                if (totalPaid % 1.0 == 0.0) totalPaid.toLong().toString() else totalPaid.toString()
            } else ""

            merged[existingIndex] = existing.copy(
                amountText = totalAmtStr,
                note = combinedNote,
                paymentStatus = finalStatus,
                partialPaidText = finalPaidStr,
                isTransfer = existing.isTransfer || item.isTransfer,
                settleBankId = existing.settleBankId ?: item.settleBankId
            )
        } else {
            merged.add(item)
        }
    }
    return if (merged.isEmpty()) listOf(FriendShieldDraft()) else merged
}

fun isFoodCategory(catName: String): Boolean = com.dave.moneymanager.data.model.isFoodCategory(catName)

fun isShieldEligibleCategory(catName: String): Boolean {
    val lower = catName.lowercase().trim()
    val nonEligible = listOf("salary", "gaji", "transfer", "top up", "topup", "cashback", "investment", "investasi")
    return !nonEligible.contains(lower)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun AddTransactionDialog(
    banks: List<Bank>,
    transactionToEdit: Transaction? = null,
    initialType: String = Transaction.TYPE_EXPENSE,
    expenseCategories: List<TransactionCategory> = TransactionCategory.expenseCategories,
    incomeCategories: List<TransactionCategory> = TransactionCategory.incomeCategories,
    shieldContacts: List<ShieldContact> = emptyList(),
    editHistories: List<TransactionEditHistory> = emptyList(),
    initialShields: List<ShieldEntry> = emptyList(),
    initialShieldHistories: List<ShieldHistoryItem> = emptyList(),
    onDismiss: () -> Unit,
    onAddCategory: ((name: String, icon: String, colorHex: Long, isExpense: Boolean) -> Unit)? = null,
    onUpdateCategory: ((oldName: String, name: String, icon: String, colorHex: Long, isExpense: Boolean) -> Unit)? = null,
    onDeleteCategory: ((name: String) -> Unit)? = null,
    onSave: (
        bankId: Long,
        type: String,
        amount: Double,
        category: String,
        note: String,
        timestamp: Long,
        destination: String?,
        shieldFriends: List<ShieldFriendInput>?,
        targetBankId: Long?
    ) -> Unit
) {
    val context = LocalContext.current

    var selectedType by remember {
        mutableStateOf(transactionToEdit?.type ?: initialType)
    }
    var amountText by remember {
        mutableStateOf(
            if (transactionToEdit != null) {
                if (transactionToEdit.amount % 1.0 == 0.0) {
                    transactionToEdit.amount.toLong().toString()
                } else {
                    transactionToEdit.amount.toString()
                }
            } else ""
        )
    }
    var showCalculator by remember { mutableStateOf(false) }

    var destination by remember {
        mutableStateOf(transactionToEdit?.destination ?: "")
    }
    var note by remember {
        mutableStateOf(transactionToEdit?.note ?: "")
    }

    var selectedBank by remember {
        mutableStateOf<Bank?>(
            if (transactionToEdit != null) {
                banks.find { it.id == transactionToEdit.bankId }
            } else {
                null
            }
        )
    }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var bankSelectionError by remember { mutableStateOf(false) }

    var selectedTargetBank by remember {
        mutableStateOf<Bank?>(
            if (transactionToEdit != null && transactionToEdit.targetBankId != null) {
                banks.find { it.id == transactionToEdit.targetBankId }
            } else {
                null
            }
        )
    }
    var targetBankDropdownExpanded by remember { mutableStateOf(false) }
    var targetBankSelectionError by remember { mutableStateOf(false) }

    var showContactPickerDialog by remember { mutableStateOf(false) }
    var targetDraftForContactPicker by remember { mutableStateOf<Long?>(null) }

    val categories = if (selectedType == Transaction.TYPE_EXPENSE) {
        expenseCategories
    } else {
        incomeCategories
    }
    var selectedCategory by remember(selectedType, categories) {
        mutableStateOf(
            if (transactionToEdit != null && transactionToEdit.type == selectedType) {
                transactionToEdit.category
            } else {
                categories.firstOrNull()?.name ?: "Other"
            }
        )
    }

    var showCategoryDialogInAddTx by remember { mutableStateOf(false) }
    var categoryToEditInDialog by remember { mutableStateOf<TransactionCategory?>(null) }

    if (showCategoryDialogInAddTx) {
        AddEditCategoryDialog(
            categoryToEdit = categoryToEditInDialog,
            defaultIsExpense = (selectedType == Transaction.TYPE_EXPENSE),
            onDismiss = {
                showCategoryDialogInAddTx = false
                categoryToEditInDialog = null
            },
            onSave = { name, icon, colorHex, isExpense ->
                if (categoryToEditInDialog == null) {
                    onAddCategory?.invoke(name, icon, colorHex, isExpense)
                    selectedCategory = name
                } else {
                    val oldName = categoryToEditInDialog!!.name
                    onUpdateCategory?.invoke(oldName, name, icon, colorHex, isExpense)
                    if (selectedCategory.equals(oldName, ignoreCase = true)) {
                        selectedCategory = name
                    }
                }
                showCategoryDialogInAddTx = false
                categoryToEditInDialog = null
            },
            onDelete = if (categoryToEditInDialog != null && onDeleteCategory != null) {
                {
                    val nameToDelete = categoryToEditInDialog!!.name
                    onDeleteCategory.invoke(nameToDelete)
                    if (selectedCategory.equals(nameToDelete, ignoreCase = true)) {
                        selectedCategory = categories.firstOrNull { !it.name.equals(nameToDelete, ignoreCase = true) }?.name ?: "Other"
                    }
                    showCategoryDialogInAddTx = false
                    categoryToEditInDialog = null
                }
            } else null
        )
    }

    val isShieldEligible = selectedType == Transaction.TYPE_EXPENSE

    val linkedHistories = remember(transactionToEdit, initialShieldHistories) {
        if (transactionToEdit != null) {
            initialShieldHistories.filter { it.transactionId == transactionToEdit.id && it.amount > 0 }
        } else emptyList()
    }
    val linkedShields = remember(transactionToEdit, initialShields) {
        if (transactionToEdit != null) {
            initialShields.filter { it.transactionId == transactionToEdit.id }
        } else emptyList()
    }
    val hasLinkedShields = linkedHistories.isNotEmpty() || linkedShields.isNotEmpty() ||
            (transactionToEdit != null && (
                transactionToEdit.note.contains("Ditalangi Shield", ignoreCase = true) ||
                transactionToEdit.category.equals("Shield", ignoreCase = true)
            ))

    var isShieldEnabled by remember {
        mutableStateOf(hasLinkedShields)
    }
    var friendShieldList by remember {
        mutableStateOf(
            if (linkedHistories.isNotEmpty()) {
                linkedHistories.mapIndexed { idx, hist ->
                    val matchingShield = initialShields.find { it.id == hist.shieldId }
                    val isSettled = matchingShield?.isSettled == true
                    FriendShieldDraft(
                        id = hist.id.takeIf { it > 0 } ?: (idx + 1L),
                        name = hist.friendName,
                        amountText = if (hist.amount % 1.0 == 0.0) hist.amount.toLong().toString() else hist.amount.toString(),
                        note = hist.note,
                        paymentStatus = if (isSettled) FriendShieldPaymentStatus.PAID_FULL else FriendShieldPaymentStatus.UNPAID,
                        isTransfer = matchingShield?.settledBankId != null,
                        settleBankId = matchingShield?.settledBankId ?: selectedBank?.id
                    )
                }
            } else if (linkedShields.isNotEmpty()) {
                linkedShields.mapIndexed { idx, s ->
                    FriendShieldDraft(
                        id = s.id.takeIf { it > 0 } ?: (idx + 1L),
                        name = s.friendName,
                        amountText = if (s.amount % 1.0 == 0.0) s.amount.toLong().toString() else s.amount.toString(),
                        note = s.note,
                        paymentStatus = if (s.isSettled) FriendShieldPaymentStatus.PAID_FULL else FriendShieldPaymentStatus.UNPAID,
                        isTransfer = s.settledBankId != null,
                        settleBankId = s.settledBankId ?: selectedBank?.id
                    )
                }
            } else if (transactionToEdit != null && transactionToEdit.note.isNotBlank()) {
                val shieldRegex = Regex("""Ditalangi(?:\s*Shield)?(?:\s*:)?\s*Rp\s*([\d,.]+)""", RegexOption.IGNORE_CASE)
                val match = shieldRegex.find(transactionToEdit.note)
                val rawNum = match?.groupValues?.get(1)?.replace(",", "")?.replace(".", "")
                val amt = rawNum?.toDoubleOrNull()
                if (amt != null && amt > 0) {
                    listOf(
                        FriendShieldDraft(
                            id = 1L,
                            name = "Teman (Shield)",
                            amountText = if (amt % 1.0 == 0.0) amt.toLong().toString() else amt.toString(),
                            note = ""
                        )
                    )
                } else {
                    listOf(FriendShieldDraft(id = 1L, name = "", amountText = "", note = ""))
                }
            } else {
                listOf(FriendShieldDraft(id = 1L, name = "", amountText = "", note = ""))
            }
        )
    }
    var friendCalcTargetId by remember { mutableStateOf<Long?>(null) }
    var friendPartialCalcTargetId by remember { mutableStateOf<Long?>(null) }

    if (showContactPickerDialog) {
        SearchableContactDialog(
            contacts = shieldContacts,
            onDismiss = {
                showContactPickerDialog = false
                targetDraftForContactPicker = null
            },
            onSelectContact = { chosenName, _ ->
                val draftId = targetDraftForContactPicker
                if (draftId != null) {
                    val idx = friendShieldList.indexOfFirst { it.id == draftId }
                    if (idx >= 0) {
                        friendShieldList = friendShieldList.toMutableList().also {
                            it[idx] = it[idx].copy(name = chosenName)
                        }
                    }
                } else {
                    val emptyIdx = friendShieldList.indexOfFirst { it.name.isBlank() }
                    if (emptyIdx >= 0) {
                        friendShieldList = friendShieldList.toMutableList().also {
                            it[emptyIdx] = it[emptyIdx].copy(name = chosenName)
                        }
                    } else {
                        friendShieldList = friendShieldList + FriendShieldDraft(
                            id = System.currentTimeMillis(),
                            name = chosenName,
                            amountText = "",
                            note = "",
                            paymentStatus = FriendShieldPaymentStatus.UNPAID,
                            partialPaidText = "",
                            isTransfer = true,
                            settleBankId = selectedBank?.id
                        )
                    }
                }
                showContactPickerDialog = false
                targetDraftForContactPicker = null
            }
        )
    }

    if (friendCalcTargetId != null) {
        val targetDraft = friendShieldList.find { it.id == friendCalcTargetId }
        if (targetDraft != null) {
            CalculatorDialog(
                initialAmount = targetDraft.amountText,
                onDismiss = { friendCalcTargetId = null },
                onApply = { res ->
                    val resStr = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
                    friendShieldList = friendShieldList.toMutableList().also { list ->
                        val idx = list.indexOfFirst { it.id == targetDraft.id }
                        if (idx >= 0) {
                            list[idx] = targetDraft.copy(amountText = resStr)
                        }
                    }
                    friendCalcTargetId = null
                }
            )
        } else {
            friendCalcTargetId = null
        }
    }

    if (friendPartialCalcTargetId != null) {
        val targetDraft = friendShieldList.find { it.id == friendPartialCalcTargetId }
        if (targetDraft != null) {
            CalculatorDialog(
                initialAmount = targetDraft.partialPaidText,
                onDismiss = { friendPartialCalcTargetId = null },
                onApply = { res ->
                    val resStr = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
                    friendShieldList = friendShieldList.toMutableList().also { list ->
                        val idx = list.indexOfFirst { it.id == targetDraft.id }
                        if (idx >= 0) {
                            list[idx] = targetDraft.copy(partialPaidText = resStr)
                        }
                    }
                    friendPartialCalcTargetId = null
                }
            )
        } else {
            friendPartialCalcTargetId = null
        }
    }

    val calendar = remember {
        Calendar.getInstance().apply {
            if (transactionToEdit != null) {
                timeInMillis = transactionToEdit.timestamp
            }
        }
    }
    var timestamp by remember {
        mutableLongStateOf(transactionToEdit?.timestamp ?: System.currentTimeMillis())
    }

    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US)
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    if (showCalculator) {
        CalculatorDialog(
            initialAmount = amountText,
            onDismiss = { showCalculator = false },
            onApply = { evaluatedResult ->
                amountText = if (evaluatedResult % 1.0 == 0.0) {
                    evaluatedResult.toLong().toString()
                } else {
                    evaluatedResult.toString()
                }
                showCalculator = false
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (transactionToEdit == null) "Record Transaction" else "Edit Transaction",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    val isExp = selectedType == Transaction.TYPE_EXPENSE
                    val isInc = selectedType == Transaction.TYPE_INCOME
                    val isTsf = selectedType == Transaction.TYPE_TRANSFER

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExp) Rose500 else Color.Transparent)
                            .clickable { selectedType = Transaction.TYPE_EXPENSE }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            fontWeight = FontWeight.Bold,
                            color = if (isExp) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isInc) Emerald500 else Color.Transparent)
                            .clickable { selectedType = Transaction.TYPE_INCOME }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            fontWeight = FontWeight.Bold,
                            color = if (isInc) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isTsf) Blue500 else Color.Transparent)
                            .clickable { selectedType = Transaction.TYPE_TRANSFER }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Transfer",
                            fontWeight = FontWeight.Bold,
                            color = if (isTsf) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            amountText = input
                        }
                    },
                    label = { Text("Amount (Rp)") },
                    placeholder = { Text("0") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                    trailingIcon = {
                        IconButton(onClick = { showCalculator = true }) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Buka Kalkulator",
                                tint = Emerald500
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                val isExp = selectedType == Transaction.TYPE_EXPENSE
                val isTsf = selectedType == Transaction.TYPE_TRANSFER

                val bankLabel = when {
                    isTsf -> "Dari Wallet / Akun Asal *"
                    isExp -> "Keluar dari Wallet / Akun *"
                    else -> "Masuk ke Wallet / Akun *"
                }
                val bankPlaceholder = when {
                    isTsf -> "Pilih wallet sumber transfer"
                    isExp -> "Pilih wallet asal pengeluaran"
                    else -> "Pilih wallet tujuan pemasukan"
                }

                ExposedDropdownMenuBox(
                    expanded = bankDropdownExpanded,
                    onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBank?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(bankLabel) },
                        placeholder = { Text(bankPlaceholder) },
                        isError = bankSelectionError,
                        supportingText = if (bankSelectionError) {
                            { Text("Silakan pilih wallet terlebih dahulu!", color = Rose500) }
                        } else null,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false }
                    ) {
                        banks.forEach { bank ->
                            val balanceFormatted = NumberFormat.getNumberInstance(Locale.US).format(bank.balance)
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(bank.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            "Balance: Rp $balanceFormatted",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                },
                                onClick = {
                                    selectedBank = bank
                                    bankSelectionError = false
                                    bankDropdownExpanded = false
                                    if (selectedTargetBank?.id == bank.id) {
                                        selectedTargetBank = null
                                    }
                                }
                            )
                        }
                    }
                }

                if (isTsf) {
                    Spacer(modifier = Modifier.height(14.dp))
                    ExposedDropdownMenuBox(
                        expanded = targetBankDropdownExpanded,
                        onExpandedChange = { targetBankDropdownExpanded = !targetBankDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTargetBank?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ke Wallet / Akun Tujuan *") },
                            placeholder = { Text("Pilih wallet tujuan penerima transfer") },
                            isError = targetBankSelectionError,
                            supportingText = if (targetBankSelectionError) {
                                { Text("Silakan pilih wallet tujuan transfer!", color = Rose500) }
                            } else null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetBankDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = targetBankDropdownExpanded,
                            onDismissRequest = { targetBankDropdownExpanded = false }
                        ) {
                            val availableTargets = banks.filter { it.id != selectedBank?.id }
                            if (availableTargets.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada wallet lain untuk transfer", style = MaterialTheme.typography.bodySmall) },
                                    onClick = { targetBankDropdownExpanded = false }
                                )
                            } else {
                                availableTargets.forEach { bank ->
                                    val balanceFormatted = NumberFormat.getNumberInstance(Locale.US).format(bank.balance)
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(bank.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "Balance: Rp $balanceFormatted",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedTargetBank = bank
                                            targetBankSelectionError = false
                                            targetBankDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isTsf) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            if (onUpdateCategory != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(Tahan untuk edit)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        if (onAddCategory != null) {
                            TextButton(
                                onClick = {
                                    categoryToEditInDialog = null
                                    showCategoryDialogInAddTx = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Emerald500
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+ Kategori",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Emerald500,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val allDisplayCategories = remember(categories, selectedCategory) {
                        if (selectedCategory.isNotBlank() && categories.none { it.name.equals(selectedCategory, ignoreCase = true) }) {
                            categories + TransactionCategory(
                                id = -1L,
                                name = selectedCategory,
                                icon = "other",
                                colorHex = 0xFF6B7280L,
                                isExpense = isExp
                            )
                        } else {
                            categories
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allDisplayCategories.forEach { cat ->
                            val isCatSelected = selectedCategory == cat.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isCatSelected) Color(cat.colorHex) else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .combinedClickable(
                                        onClick = { selectedCategory = cat.name },
                                        onLongClick = {
                                            if (onUpdateCategory != null && cat.id > 0) {
                                                categoryToEditInDialog = cat
                                                showCategoryDialogInAddTx = true
                                            }
                                        }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isCatSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }

                        if (onAddCategory != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        Emerald500.copy(alpha = 0.5f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        categoryToEditInDialog = null
                                        showCategoryDialogInAddTx = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Emerald500
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Kategori Baru",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Emerald500,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                val destLabel = when {
                    isTsf -> "Penerima / Keterangan Transfer (Opsional)"
                    isExp -> "Destination / Merchant / Recipient (Optional)"
                    else -> "From / Sender (Optional)"
                }
                val destPlaceholder = when {
                    isTsf -> "e.g. Pembayaran, Tabungan, atau Nama Penerima"
                    isExp -> "e.g. Starbucks, Indomaret, John"
                    else -> "e.g. Company Payroll, John Doe"
                }
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text(destLabel) },
                    placeholder = { Text(destPlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description (Optional)") },
                    placeholder = { Text("e.g. Team lunch celebration\n(Bisa enter untuk baris baru)") },
                    singleLine = false,
                    minLines = 3,
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                if (isShieldEligible) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = if (isShieldEnabled) Emerald500.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isShieldEnabled) Emerald500.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isShieldEnabled) Emerald500 else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Shield",
                                            tint = if (isShieldEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Shield (Talangan Teman)",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Bayarin temanmu dulu? Lacak hutang mereka di Shield Tracker.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Switch(
                                    checked = isShieldEnabled,
                                    onCheckedChange = { isShieldEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Emerald500
                                    )
                                )
                            }

                            if (isShieldEnabled) {
                                Spacer(modifier = Modifier.height(14.dp))

                                val totalBill = amountText.toDoubleOrNull() ?: 0.0
                                val totalShielded = friendShieldList.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
                                val totalPaidByFriends = friendShieldList.sumOf { draft ->
                                    val fAmt = draft.amountText.toDoubleOrNull() ?: 0.0
                                    when (draft.paymentStatus) {
                                        FriendShieldPaymentStatus.PAID_FULL -> fAmt
                                        FriendShieldPaymentStatus.PARTIAL -> (draft.partialPaidText.toDoubleOrNull() ?: 0.0).coerceAtMost(fAmt)
                                        else -> 0.0
                                    }
                                }
                                val totalUnpaidByFriends = (totalShielded - totalPaidByFriends).coerceAtLeast(0.0)
                                val myShare = (totalBill - totalShielded).coerceAtLeast(0.0)

                                if (shieldContacts.isNotEmpty()) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                targetDraftForContactPicker = null
                                                showContactPickerDialog = true
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Emerald500.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.PersonSearch,
                                                    contentDescription = null,
                                                    tint = Emerald500,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Pilih dari Kontak yang Pernah Shield",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Emerald500
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = Emerald500,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                if (totalBill > 0) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.35f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Bolt,
                                                        contentDescription = null,
                                                        tint = Amber500,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Alokasi Cepat Shield:",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    )
                                                }
                                                val fmt = NumberFormat.getNumberInstance(Locale.US)
                                                Text(
                                                    text = "Total Rp ${fmt.format(totalBill)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {

                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1.1f)
                                                        .clickable {
                                                            val count = friendShieldList.size
                                                            if (count <= 1) {
                                                                val first = friendShieldList.firstOrNull() ?: FriendShieldDraft(id = 1L)
                                                                friendShieldList = listOf(
                                                                    first.copy(
                                                                        amountText = if (totalBill % 1.0 == 0.0) totalBill.toLong().toString() else totalBill.toString()
                                                                    )
                                                                )
                                                            } else {
                                                                val perFriend = (totalBill / count).toLong()
                                                                friendShieldList = friendShieldList.mapIndexed { i, f ->
                                                                    val amt = if (i == count - 1) {
                                                                        (totalBill - (perFriend * (count - 1))).toLong()
                                                                    } else perFriend
                                                                    f.copy(amountText = amt.toString())
                                                                }
                                                            }
                                                            Toast.makeText(context, "100% Pengeluaran dijadikan Shield!", Toast.LENGTH_SHORT).show()
                                                        },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Emerald500.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.5f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.DoneAll,
                                                            contentDescription = null,
                                                            tint = Emerald500,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Semua (100%)",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Emerald500
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1.1f)
                                                        .clickable {
                                                            val count = friendShieldList.size
                                                            val perPerson = (totalBill / (count + 1)).toLong()
                                                            friendShieldList = friendShieldList.map { f ->
                                                                f.copy(amountText = perPerson.toString())
                                                            }
                                                            Toast.makeText(context, "Sebagian dijadikan Shield (Bagi Rata)", Toast.LENGTH_SHORT).show()
                                                        },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Blue500.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, Blue500.copy(alpha = 0.5f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.CallSplit,
                                                            contentDescription = null,
                                                            tint = Blue500,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (friendShieldList.size > 1) "Bagi Rata (+Saya)" else "Bagi 50:50",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Blue500
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    modifier = Modifier
                                                        .clickable {
                                                            friendShieldList = friendShieldList.map { it.copy(amountText = "") }
                                                        },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "Reset",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    friendShieldList.forEachIndexed { index, draft ->
                                        val trimmedName = draft.name.trim()
                                        val duplicateMatchIndex = if (trimmedName.isNotEmpty()) {
                                            friendShieldList.indexOfFirst { other ->
                                                other.id != draft.id && other.name.trim().equals(trimmedName, ignoreCase = true)
                                            }
                                        } else -1

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Teman #${index + 1}",
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Emerald500
                                                        )
                                                    )
                                                    if (friendShieldList.size > 1) {
                                                        IconButton(
                                                            onClick = {
                                                                friendShieldList = friendShieldList.toMutableList().also { it.removeAt(index) }
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = "Hapus",
                                                                tint = Rose500,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedTextField(
                                                        value = draft.name,
                                                        onValueChange = { newName ->
                                                            friendShieldList = friendShieldList.toMutableList().also {
                                                                it[index] = draft.copy(name = newName)
                                                            }
                                                        },
                                                        label = { Text("Nama Teman") },
                                                        placeholder = { Text("e.g. Budi") },
                                                        singleLine = true,
                                                        trailingIcon = if (shieldContacts.isNotEmpty()) {
                                                            {
                                                                IconButton(onClick = {
                                                                    targetDraftForContactPicker = draft.id
                                                                    showContactPickerDialog = true
                                                                }) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.PersonSearch,
                                                                        contentDescription = "Cari Kontak",
                                                                        tint = Emerald500,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                        } else null,
                                                        modifier = Modifier.weight(1.2f),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )

                                                    OutlinedTextField(
                                                        value = draft.amountText,
                                                        onValueChange = { newAmt ->
                                                            if (newAmt.all { it.isDigit() }) {
                                                                friendShieldList = friendShieldList.toMutableList().also {
                                                                    it[index] = draft.copy(amountText = newAmt)
                                                                }
                                                            }
                                                        },
                                                        label = { Text("Hutang (Rp)") },
                                                        placeholder = { Text("0") },
                                                        singleLine = true,
                                                        trailingIcon = {
                                                            IconButton(onClick = {
                                                                friendCalcTargetId = draft.id
                                                            }) {
                                                                Icon(Icons.Default.Calculate, contentDescription = "Hitung", tint = Emerald500, modifier = Modifier.size(18.dp))
                                                            }
                                                        },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                }

                                                if (totalBill > 0) {
                                                    val curOtherSum = friendShieldList.filter { it.id != draft.id }.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
                                                    val remainingForThisFriend = (totalBill - curOtherSum).coerceAtLeast(0.0)
                                                    val fmt = NumberFormat.getNumberInstance(Locale.US)
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 4.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        if (remainingForThisFriend > 0 && remainingForThisFriend != (draft.amountText.toDoubleOrNull() ?: 0.0)) {
                                                            val label = if (curOtherSum == 0.0) "Semua: Rp ${fmt.format(remainingForThisFriend.toLong())}" else "Sisa: Rp ${fmt.format(remainingForThisFriend.toLong())}"
                                                            Surface(
                                                                modifier = Modifier.clickable {
                                                                    val amtStr = if (remainingForThisFriend % 1.0 == 0.0) remainingForThisFriend.toLong().toString() else remainingForThisFriend.toString()
                                                                    friendShieldList = friendShieldList.toMutableList().also {
                                                                        it[index] = draft.copy(amountText = amtStr)
                                                                    }
                                                                },
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = Emerald500.copy(alpha = 0.12f),
                                                                border = BorderStroke(0.8.dp, Emerald500.copy(alpha = 0.35f))
                                                            ) {
                                                                Text(
                                                                    text = label,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Emerald500,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }
                                                        val halfAmt = (totalBill * 0.5).toLong()
                                                        if (halfAmt > 0 && halfAmt.toDouble() != (draft.amountText.toDoubleOrNull() ?: 0.0)) {
                                                            Surface(
                                                                modifier = Modifier.clickable {
                                                                    friendShieldList = friendShieldList.toMutableList().also {
                                                                        it[index] = draft.copy(amountText = halfAmt.toString())
                                                                    }
                                                                },
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = Blue500.copy(alpha = 0.12f),
                                                                border = BorderStroke(0.8.dp, Blue500.copy(alpha = 0.35f))
                                                            ) {
                                                                Text(
                                                                    text = "50%: Rp ${fmt.format(halfAmt)}",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Blue500,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                if (duplicateMatchIndex >= 0) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Amber500.copy(alpha = 0.12f))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = "Nama '${draft.name}' sama dengan Teman #${duplicateMatchIndex + 1}",
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Amber500)
                                                                )
                                                                Text(
                                                                    text = "Nominal langsung digabungkan ke Teman #${duplicateMatchIndex + 1} tanpa kolom baru.",
                                                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 10.sp)
                                                                )
                                                            }
                                                            TextButton(
                                                                onClick = {
                                                                    friendShieldList = mergeDuplicateFriends(friendShieldList)
                                                                    Toast.makeText(context, "Nominal digabungkan ke Teman #${duplicateMatchIndex + 1}!", Toast.LENGTH_SHORT).show()
                                                                }
                                                            ) {
                                                                Text("Gabung", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Amber500)
                                                            }
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                OutlinedTextField(
                                                    value = draft.note,
                                                    onValueChange = { newNote ->
                                                        friendShieldList = friendShieldList.toMutableList().also {
                                                            it[index] = draft.copy(note = newNote)
                                                        }
                                                    },
                                                    label = { Text("Pesanan / Menu (Opsional)") },
                                                    placeholder = { Text("e.g. Nasi Goreng + Es Teh") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(10.dp)
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "Status Pembayaran Teman:",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(5.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {

                                                    val isUnpaid = draft.paymentStatus == FriendShieldPaymentStatus.UNPAID
                                                    Surface(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clickable {
                                                                friendShieldList = friendShieldList.toMutableList().also {
                                                                    it[index] = draft.copy(paymentStatus = FriendShieldPaymentStatus.UNPAID)
                                                                }
                                                            },
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (isUnpaid) Amber500.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                        border = BorderStroke(1.dp, if (isUnpaid) Amber500 else Color.Transparent)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.HourglassTop,
                                                                contentDescription = null,
                                                                tint = if (isUnpaid) Amber500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "Belum Bayar",
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isUnpaid) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isUnpaid) Amber500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                            )
                                                        }
                                                    }

                                                    val isPaid = draft.paymentStatus == FriendShieldPaymentStatus.PAID_FULL
                                                    Surface(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clickable {
                                                                friendShieldList = friendShieldList.toMutableList().also {
                                                                    it[index] = draft.copy(
                                                                        paymentStatus = FriendShieldPaymentStatus.PAID_FULL,
                                                                        settleBankId = draft.settleBankId ?: selectedBank?.id
                                                                    )
                                                                }
                                                            },
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (isPaid) Emerald500.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                        border = BorderStroke(1.dp, if (isPaid) Emerald500 else Color.Transparent)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.CheckCircle,
                                                                contentDescription = null,
                                                                tint = if (isPaid) Emerald500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "Sudah Lunas",
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isPaid) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isPaid) Emerald500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                            )
                                                        }
                                                    }

                                                    val isPartial = draft.paymentStatus == FriendShieldPaymentStatus.PARTIAL
                                                    Surface(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clickable {
                                                                friendShieldList = friendShieldList.toMutableList().also {
                                                                    it[index] = draft.copy(
                                                                        paymentStatus = FriendShieldPaymentStatus.PARTIAL,
                                                                        settleBankId = draft.settleBankId ?: selectedBank?.id
                                                                    )
                                                                }
                                                            },
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (isPartial) Blue500.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                        border = BorderStroke(1.dp, if (isPartial) Blue500 else Color.Transparent)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Payments,
                                                                contentDescription = null,
                                                                tint = if (isPartial) Blue500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "Bayar Sebagian",
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isPartial) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isPartial) Blue500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                            )
                                                        }
                                                    }
                                                }

                                                if (draft.paymentStatus == FriendShieldPaymentStatus.PAID_FULL) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Emerald500.copy(alpha = 0.08f)),
                                                        border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.3f))
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            val fAmt = draft.amountText.toDoubleOrNull() ?: 0.0
                                                            val formatter = NumberFormat.getNumberInstance(Locale.US)
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = "${draft.name.ifBlank { "Teman" }} langsung melunasi Rp ${formatter.format(fAmt)}",
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Emerald500)
                                                                )
                                                            }

                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                Surface(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .clickable {
                                                                            friendShieldList = friendShieldList.toMutableList().also {
                                                                                it[index] = draft.copy(isTransfer = true)
                                                                            }
                                                                        },
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = if (draft.isTransfer) Emerald500.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                                                                    border = BorderStroke(1.dp, if (draft.isTransfer) Emerald500 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        RadioButton(
                                                                            selected = draft.isTransfer,
                                                                            onClick = {
                                                                                friendShieldList = friendShieldList.toMutableList().also {
                                                                                    it[index] = draft.copy(isTransfer = true)
                                                                                }
                                                                            },
                                                                            colors = RadioButtonDefaults.colors(selectedColor = Emerald500),
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text("Ditransfer (Pemasukan)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                                    }
                                                                }

                                                                Surface(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .clickable {
                                                                            friendShieldList = friendShieldList.toMutableList().also {
                                                                                it[index] = draft.copy(isTransfer = false)
                                                                            }
                                                                        },
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = if (!draft.isTransfer) Emerald500.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                                                                    border = BorderStroke(1.dp, if (!draft.isTransfer) Emerald500 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        RadioButton(
                                                                            selected = !draft.isTransfer,
                                                                            onClick = {
                                                                                friendShieldList = friendShieldList.toMutableList().also {
                                                                                    it[index] = draft.copy(isTransfer = false)
                                                                                }
                                                                            },
                                                                            colors = RadioButtonDefaults.colors(selectedColor = Emerald500),
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text("Tunai / Tanpa Catat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                                    }
                                                                }
                                                            }

                                                            if (draft.isTransfer && banks.isNotEmpty()) {
                                                                val currentBankId = draft.settleBankId ?: selectedBank?.id
                                                                val curBank = banks.find { it.id == currentBankId } ?: selectedBank
                                                                var bankExpanded by remember { mutableStateOf(false) }

                                                                ExposedDropdownMenuBox(
                                                                    expanded = bankExpanded,
                                                                    onExpandedChange = { bankExpanded = !bankExpanded }
                                                                ) {
                                                                    OutlinedTextField(
                                                                        value = curBank?.name ?: "Pilih Rekening",
                                                                        onValueChange = {},
                                                                        readOnly = true,
                                                                        label = { Text("Rekening Penerima", fontSize = 10.sp) },
                                                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .menuAnchor(),
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    )
                                                                    ExposedDropdownMenu(
                                                                        expanded = bankExpanded,
                                                                        onDismissRequest = { bankExpanded = false }
                                                                    ) {
                                                                        banks.forEach { b ->
                                                                            DropdownMenuItem(
                                                                                text = { Text(b.name, fontSize = 12.sp) },
                                                                                onClick = {
                                                                                    friendShieldList = friendShieldList.toMutableList().also {
                                                                                        it[index] = draft.copy(settleBankId = b.id)
                                                                                    }
                                                                                    bankExpanded = false
                                                                                }
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                if (draft.paymentStatus == FriendShieldPaymentStatus.PARTIAL) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Blue500.copy(alpha = 0.08f)),
                                                        border = BorderStroke(1.dp, Blue500.copy(alpha = 0.3f))
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            val totalFriendAmt = draft.amountText.toDoubleOrNull() ?: 0.0
                                                            val paidAmt = draft.partialPaidText.toDoubleOrNull() ?: 0.0
                                                            val remainingAmt = (totalFriendAmt - paidAmt).coerceAtLeast(0.0)
                                                            val formatter = NumberFormat.getNumberInstance(Locale.US)

                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Icon(Icons.Default.Payments, contentDescription = null, tint = Blue500, modifier = Modifier.size(16.dp))
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = "Bayar Sebagian Saat Transaksi",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Blue500)
                                                                    )
                                                                }
                                                                if (totalFriendAmt > 0 && paidAmt > 0) {
                                                                    Text(
                                                                        text = "Sisa: Rp ${formatter.format(remainingAmt)}",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Rose500)
                                                                    )
                                                                }
                                                            }

                                                            OutlinedTextField(
                                                                value = draft.partialPaidText,
                                                                onValueChange = { newPaid ->
                                                                    if (newPaid.all { it.isDigit() }) {
                                                                        friendShieldList = friendShieldList.toMutableList().also {
                                                                            it[index] = draft.copy(partialPaidText = newPaid)
                                                                        }
                                                                    }
                                                                },
                                                                label = { Text("Nominal yang Sudah Dibayar (Rp)") },
                                                                placeholder = { Text("0") },
                                                                singleLine = true,
                                                                trailingIcon = {
                                                                    IconButton(onClick = { friendPartialCalcTargetId = draft.id }) {
                                                                        Icon(Icons.Default.Calculate, contentDescription = "Hitung", tint = Blue500, modifier = Modifier.size(18.dp))
                                                                    }
                                                                },
                                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                                modifier = Modifier.fillMaxWidth(),
                                                                shape = RoundedCornerShape(8.dp)
                                                            )

                                                            if (totalFriendAmt > 0) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    listOf(0.25 to "25%", 0.50 to "50%", 0.75 to "75%").forEach { (pct, label) ->
                                                                        val calcAmt = (totalFriendAmt * pct).toLong()
                                                                        val isSelected = draft.partialPaidText == calcAmt.toString()
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .weight(1f)
                                                                                .clip(RoundedCornerShape(6.dp))
                                                                                .background(if (isSelected) Blue500.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                                                                .border(1.dp, if (isSelected) Blue500 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                                                .clickable {
                                                                                    friendShieldList = friendShieldList.toMutableList().also {
                                                                                        it[index] = draft.copy(partialPaidText = calcAmt.toString())
                                                                                    }
                                                                                }
                                                                                .padding(vertical = 4.dp),
                                                                            contentAlignment = Alignment.Center
                                                                        ) {
                                                                            Text(
                                                                                text = "$label (Rp ${formatter.format(calcAmt)})",
                                                                                fontSize = 10.sp,
                                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                                color = if (isSelected) Blue500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                Surface(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .clickable {
                                                                            friendShieldList = friendShieldList.toMutableList().also {
                                                                                it[index] = draft.copy(isTransfer = true)
                                                                            }
                                                                        },
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = if (draft.isTransfer) Blue500.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                                                                    border = BorderStroke(1.dp, if (draft.isTransfer) Blue500 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        RadioButton(
                                                                            selected = draft.isTransfer,
                                                                            onClick = {
                                                                                friendShieldList = friendShieldList.toMutableList().also {
                                                                                    it[index] = draft.copy(isTransfer = true)
                                                                                }
                                                                            },
                                                                            colors = RadioButtonDefaults.colors(selectedColor = Blue500),
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text("Ditransfer (Pemasukan)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                                    }
                                                                }

                                                                Surface(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .clickable {
                                                                            friendShieldList = friendShieldList.toMutableList().also {
                                                                                it[index] = draft.copy(isTransfer = false)
                                                                            }
                                                                        },
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = if (!draft.isTransfer) Blue500.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                                                                    border = BorderStroke(1.dp, if (!draft.isTransfer) Blue500 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        RadioButton(
                                                                            selected = !draft.isTransfer,
                                                                            onClick = {
                                                                                friendShieldList = friendShieldList.toMutableList().also {
                                                                                    it[index] = draft.copy(isTransfer = false)
                                                                                }
                                                                            },
                                                                            colors = RadioButtonDefaults.colors(selectedColor = Blue500),
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text("Tunai / Tanpa Catat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                                    }
                                                                }
                                                            }

                                                            if (draft.isTransfer && banks.isNotEmpty()) {
                                                                val currentBankId = draft.settleBankId ?: selectedBank?.id
                                                                val curBank = banks.find { it.id == currentBankId } ?: selectedBank
                                                                var bankExpanded by remember { mutableStateOf(false) }

                                                                ExposedDropdownMenuBox(
                                                                    expanded = bankExpanded,
                                                                    onExpandedChange = { bankExpanded = !bankExpanded }
                                                                ) {
                                                                    OutlinedTextField(
                                                                        value = curBank?.name ?: "Pilih Rekening",
                                                                        onValueChange = {},
                                                                        readOnly = true,
                                                                        label = { Text("Rekening Penerima", fontSize = 10.sp) },
                                                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .menuAnchor(),
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    )
                                                                    ExposedDropdownMenu(
                                                                        expanded = bankExpanded,
                                                                        onDismissRequest = { bankExpanded = false }
                                                                    ) {
                                                                        banks.forEach { b ->
                                                                            DropdownMenuItem(
                                                                                text = { Text(b.name, fontSize = 12.sp) },
                                                                                onClick = {
                                                                                    friendShieldList = friendShieldList.toMutableList().also {
                                                                                        it[index] = draft.copy(settleBankId = b.id)
                                                                                    }
                                                                                    bankExpanded = false
                                                                                }
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {

                                            val merged = mergeDuplicateFriends(friendShieldList)
                                            friendShieldList = merged + FriendShieldDraft(
                                                id = System.currentTimeMillis(),
                                                name = "",
                                                amountText = "",
                                                note = "",
                                                paymentStatus = FriendShieldPaymentStatus.UNPAID,
                                                partialPaidText = "",
                                                isTransfer = true,
                                                settleBankId = selectedBank?.id
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("+ Tambah Teman Lain", fontWeight = FontWeight.SemiBold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            val formatter = NumberFormat.getNumberInstance(Locale.US)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Total Bayar di Kasir:", style = MaterialTheme.typography.labelSmall)
                                                Text("Rp ${formatter.format(totalBill)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Ditalangi Teman (Shield):", style = MaterialTheme.typography.labelSmall, color = Emerald500)
                                                Text("- Rp ${formatter.format(totalShielded)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Emerald500)
                                            }
                                            if (totalPaidByFriends > 0) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("• Langsung Dibayar / Lunas:", style = MaterialTheme.typography.labelSmall, color = Emerald500)
                                                    Text("Rp ${formatter.format(totalPaidByFriends)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Emerald500)
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("• Sisa Hutang Aktif:", style = MaterialTheme.typography.labelSmall, color = if (totalUnpaidByFriends > 0) Amber500 else Emerald500)
                                                    Text("Rp ${formatter.format(totalUnpaidByFriends)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = if (totalUnpaidByFriends > 0) Amber500 else Emerald500)
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Porsi Pribadi:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                Text("Rp ${formatter.format(myShare)}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall, color = if (myShare > 0) Rose500 else Emerald500)
                                            }

                                            if (totalBill > 0 && totalShielded > totalBill) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "⚠️ Total talangan melebihi total bayar di kasir!",
                                                    color = Rose500,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else if (totalShielded > 0) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                if (totalShielded >= totalBill && totalBill > 0) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Emerald500.copy(alpha = 0.15f),
                                                        border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.4f)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Emerald500,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "Seluruh pengeluaran (100%) ditalangi sebagai Shield! (Porsi pribadi: Rp 0)",
                                                                color = Emerald500,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Blue500.copy(alpha = 0.12f),
                                                        border = BorderStroke(1.dp, Blue500.copy(alpha = 0.4f)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Shield,
                                                                contentDescription = null,
                                                                tint = Blue500,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "Sebagian pengeluaran (Rp ${formatter.format(totalShielded)}) ditalangi Shield, sisa Rp ${formatter.format(myShare)} porsi pribadi.",
                                                                color = Blue500,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                if (totalPaidByFriends > 0) {
                                                    Text(
                                                        text = "✓ Saldo dompet terpotong Rp ${formatter.format(totalBill)} untuk kasir. Sebesar Rp ${formatter.format(totalPaidByFriends)} dicatat lunas/reimburse, dan sisa Rp ${formatter.format(totalUnpaidByFriends)} dicatat sebagai hutang aktif di Shield Tracker.",
                                                        color = Emerald500,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                } else {
                                                    Text(
                                                        text = "✓ Saldo wallet terpotong sesuai total kasir (Rp ${formatter.format(totalBill)}). Saat teman melunasi di Shield Tracker, Anda dapat memilih opsi reimburse ke wallet.",
                                                        color = Emerald500,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Transaction Date & Time (Device Sync)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                calendar.timeInMillis = timestamp
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(Calendar.YEAR, year)
                                        calendar.set(Calendar.MONTH, month)
                                        calendar.set(Calendar.DAY_OF_MONTH, day)
                                        timestamp = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = dateFormat.format(Date(timestamp)),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                calendar.timeInMillis = timestamp
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        calendar.set(Calendar.MINUTE, minute)
                                        timestamp = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Time: " + timeFormat.format(Date(timestamp)),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                if (transactionToEdit != null && editHistories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Blue500.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Blue500.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = Blue500,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Histori Perubahan (${editHistories.size}x Diedit)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Blue500)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val numFormat = NumberFormat.getNumberInstance(Locale.US)
                            val editDateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

                            editHistories.forEach { history ->
                                val hasDiff = history.diffAmount != 0.0
                                val isPositive = history.diffAmount > 0
                                val diffColor = if (isPositive) Emerald500 else Rose500
                                val diffSign = if (isPositive) "+" else if (history.diffAmount < 0) "-" else ""
                                val diffText = if (hasDiff) {
                                    "$diffSign Rp ${numFormat.format(abs(history.diffAmount))}"
                                } else "Nominal Tetap"

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = editDateFormat.format(Date(history.editTimestamp)),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    fontSize = 11.sp
                                                )
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(diffColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = diffText,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = diffColor,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }

                                        if (history.oldAmount != history.newAmount) {
                                            Text(
                                                text = "Rp ${numFormat.format(history.oldAmount)} ➔ Rp ${numFormat.format(history.newAmount)}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }

                                        Text(
                                            text = history.changeSummary,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount <= 0.0) {
                            Toast.makeText(context, "Silakan isi nominal harga terlebih dahulu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (selectedType == Transaction.TYPE_TRANSFER) {
                            if (selectedBank == null) {
                                bankSelectionError = true
                                bankDropdownExpanded = true
                                Toast.makeText(context, "Silakan pilih wallet asal transfer!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedTargetBank == null) {
                                targetBankSelectionError = true
                                targetBankDropdownExpanded = true
                                Toast.makeText(context, "Silakan pilih wallet tujuan transfer!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedBank?.id == selectedTargetBank?.id) {
                                Toast.makeText(context, "Wallet asal dan wallet tujuan transfer tidak boleh sama!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        } else {
                            if (selectedBank == null) {
                                bankSelectionError = true
                                bankDropdownExpanded = true
                                val actionWord = if (selectedType == Transaction.TYPE_EXPENSE) "keluar dari" else "masuk ke"
                                Toast.makeText(
                                    context,
                                    "Silakan pilih wallet terlebih dahulu ($actionWord wallet mana)!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                        }

                        val cleanedFriends = mergeDuplicateFriends(friendShieldList)
                            .filter { it.name.isNotBlank() && (it.amountText.toDoubleOrNull() ?: 0.0) > 0 }

                        val totalShielded = if (isShieldEnabled && isShieldEligible) {
                            cleanedFriends.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
                        } else 0.0

                        if (isShieldEnabled && isShieldEligible && totalShielded > amount) {
                            Toast.makeText(
                                context,
                                "Total talangan teman tidak boleh melebihi total tagihan kasir!",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (isShieldEnabled && isShieldEligible) {
                            for (f in cleanedFriends) {
                                if (f.paymentStatus == FriendShieldPaymentStatus.PARTIAL) {
                                    val fAmt = f.amountText.toDoubleOrNull() ?: 0.0
                                    val pAmt = f.partialPaidText.toDoubleOrNull() ?: 0.0
                                    if (pAmt <= 0.0) {
                                        Toast.makeText(
                                            context,
                                            "Nominal bayar sebagian untuk '${f.name}' harus lebih dari 0!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                    if (pAmt >= fAmt) {
                                        Toast.makeText(
                                            context,
                                            "Nominal bayar sebagian untuk '${f.name}' sama/melebihi total hutang. Silakan pilih 'Sudah Lunas'!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                }
                            }
                        }

                        val myShare = if (isShieldEnabled && isShieldEligible && totalShielded > 0) {
                            (amount - totalShielded).coerceAtLeast(0.0)
                        } else amount

                        val shieldFriendsToSave = if (isShieldEnabled && isShieldEligible) {
                            cleanedFriends.map { f ->
                                val totalAmt = f.amountText.toDoubleOrNull() ?: 0.0
                                val isSettled = f.paymentStatus == FriendShieldPaymentStatus.PAID_FULL
                                val paidAmt = when (f.paymentStatus) {
                                    FriendShieldPaymentStatus.PAID_FULL -> totalAmt
                                    FriendShieldPaymentStatus.PARTIAL -> (f.partialPaidText.toDoubleOrNull() ?: 0.0).coerceAtMost(totalAmt)
                                    FriendShieldPaymentStatus.UNPAID -> 0.0
                                }
                                val targetBankId = f.settleBankId ?: selectedBank?.id
                                val targetBank = banks.find { it.id == targetBankId } ?: selectedBank

                                ShieldFriendInput(
                                    name = f.name.trim(),
                                    amount = totalAmt,
                                    note = f.note.trim(),
                                    isSettled = isSettled,
                                    paidAmount = paidAmt,
                                    isTransfer = f.isTransfer,
                                    settleBankId = if (f.isTransfer) targetBank?.id else null,
                                    settleBankName = if (f.isTransfer) targetBank?.name else null
                                )
                            }
                        } else if (transactionToEdit != null) {
                            emptyList()
                        } else null

                        val totalPaidByFriends = if (isShieldEnabled && isShieldEligible) {
                            cleanedFriends.sumOf { f ->
                                val totalAmt = f.amountText.toDoubleOrNull() ?: 0.0
                                when (f.paymentStatus) {
                                    FriendShieldPaymentStatus.PAID_FULL -> totalAmt
                                    FriendShieldPaymentStatus.PARTIAL -> (f.partialPaidText.toDoubleOrNull() ?: 0.0).coerceAtMost(totalAmt)
                                    else -> 0.0
                                }
                            }
                        } else 0.0
                        val totalUnpaidByFriends = (totalShielded - totalPaidByFriends).coerceAtLeast(0.0)

                        val cleanBaseNote = note
                            .replace(Regex("""(?i)\s*•\s*Kasir:\s*Rp\s*[\d,.]+(?:\s*\([^)]*\))?"""), "")
                            .replace(Regex("""(?i)^Kasir:\s*Rp\s*[\d,.]+(?:\s*\([^)]*\))?"""), "")
                            .trim()

                        val finalNote = if (isShieldEnabled && isShieldEligible && totalShielded > 0) {
                            val formatter = NumberFormat.getNumberInstance(Locale.US)
                            val statusSummary = if (totalPaidByFriends > 0) {
                                if (totalUnpaidByFriends == 0.0) " (Lunas Langsung)"
                                else " (Dibayar: Rp ${formatter.format(totalPaidByFriends)}, Sisa: Rp ${formatter.format(totalUnpaidByFriends)})"
                            } else ""
                            val shieldSummary = "Kasir: Rp ${formatter.format(amount)} (Ditalangi Shield: Rp ${formatter.format(totalShielded)}$statusSummary, Pribadi: Rp ${formatter.format(myShare)})"
                            if (cleanBaseNote.isBlank()) shieldSummary else "$cleanBaseNote • $shieldSummary"
                        } else cleanBaseNote

                        val finalCategory = when {
                            selectedType == Transaction.TYPE_TRANSFER -> "Transfer"
                            else -> selectedCategory
                        }

                        onSave(
                            selectedBank!!.id,
                            selectedType,
                            amount,
                            finalCategory,
                            finalNote,
                            timestamp,
                            destination.trim().ifEmpty { null },
                            shieldFriendsToSave,
                            if (selectedType == Transaction.TYPE_TRANSFER) selectedTargetBank?.id else null
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (selectedType) {
                            Transaction.TYPE_EXPENSE -> Rose500
                            Transaction.TYPE_INCOME -> Emerald500
                            else -> Blue500
                        }
                    )
                ) {
                    val totalBill = amountText.toDoubleOrNull() ?: 0.0
                    val curShielded = if (isShieldEnabled && isShieldEligible) {
                        friendShieldList.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
                    } else 0.0
                    val formatter = NumberFormat.getNumberInstance(Locale.US)
                    Text(
                        text = if (transactionToEdit == null) {
                            if (isShieldEnabled && isShieldEligible && totalBill > 0) {
                                "Simpan Transaksi (Rp ${formatter.format(totalBill)})"
                            } else {
                                "Save Transaction"
                            }
                        } else {
                            if (isShieldEnabled && isShieldEligible && curShielded > 0) {
                                if (curShielded >= totalBill && totalBill > 0) {
                                    "Simpan (100% Shield: Rp ${formatter.format(curShielded)})"
                                } else {
                                    "Simpan (Sebagian Shield: Rp ${formatter.format(curShielded)})"
                                }
                            } else {
                                "Save Changes"
                            }
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
