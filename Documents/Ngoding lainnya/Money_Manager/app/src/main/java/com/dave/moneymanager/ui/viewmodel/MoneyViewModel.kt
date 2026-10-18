package com.dave.moneymanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dave.moneymanager.data.local.MoneyRepository
import com.dave.moneymanager.data.model.Bank
import com.dave.moneymanager.data.model.Budget
import com.dave.moneymanager.data.model.BudgetAlertData
import com.dave.moneymanager.data.model.CategoryBudgetStatus
import com.dave.moneymanager.data.model.FilterOptions
import com.dave.moneymanager.data.model.ScrapedNotificationLog
import com.dave.moneymanager.data.model.ShieldContact
import com.dave.moneymanager.data.model.ShieldEntry
import com.dave.moneymanager.data.model.ShieldFriendInput
import com.dave.moneymanager.data.model.ShieldHistoryItem
import com.dave.moneymanager.data.model.ShieldTransactionStatus
import com.dave.moneymanager.data.model.ShieldWalletDeduction
import com.dave.moneymanager.data.model.TimeFilterMode
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionCategory
import com.dave.moneymanager.data.model.TransactionEditHistory
import com.dave.moneymanager.service.NotificationParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CategorySummary(
    val categoryId: Long = 0,
    val categoryName: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val isExpense: Boolean,
    val colorHex: Long,
    val icon: String,
    val transactions: List<Transaction>
) {
    fun toTransactionCategory(): TransactionCategory = TransactionCategory(
        id = categoryId,
        name = categoryName,
        icon = icon,
        colorHex = colorHex,
        isExpense = isExpense
    )
}

class MoneyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MoneyRepository.getInstance(application)

    val banks: StateFlow<List<Bank>> = repository.banks
    val filteredTransactions: StateFlow<List<Transaction>> = repository.filteredTransactions
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
    val filterOptions: StateFlow<FilterOptions> = repository.filterOptions
    val notificationLogs: StateFlow<List<ScrapedNotificationLog>> = repository.notificationLogs
    val budgets: StateFlow<List<Budget>> = repository.budgets
    val categories: StateFlow<List<TransactionCategory>> = repository.categories
    val shields: StateFlow<List<ShieldEntry>> = repository.shields
    val shieldHistories: StateFlow<List<ShieldHistoryItem>> = repository.shieldHistories
    val shieldContacts: StateFlow<List<ShieldContact>> = repository.shieldContacts
    val transactionEditHistories: StateFlow<Map<Long, List<TransactionEditHistory>>> = repository.transactionEditHistories

    val totalUnsettledShieldAmount: StateFlow<Double> = shields.map { list ->
        list.filter { !it.isSettled }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val unsettledShieldCount: StateFlow<Int> = shields.map { list ->
        list.count { !it.isSettled }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSettledShieldAmount: StateFlow<Double> = shields.map { list ->
        list.filter { it.isSettled }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expenseCategories: StateFlow<List<TransactionCategory>> = categories.map { list ->
        val exps = list.filter { it.isExpense }
        if (exps.isNotEmpty()) exps else TransactionCategory.defaultExpenseCategories
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionCategory.defaultExpenseCategories)

    val incomeCategories: StateFlow<List<TransactionCategory>> = categories.map { list ->
        val incs = list.filter { !it.isExpense }
        if (incs.isNotEmpty()) incs else TransactionCategory.defaultIncomeCategories
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionCategory.defaultIncomeCategories)

    val budgetMonthYear = MutableStateFlow(SimpleDateFormat("yyyy-MM", Locale.US).format(Date()))

    private val _budgetAlert = MutableStateFlow<BudgetAlertData?>(null)
    val budgetAlert: StateFlow<BudgetAlertData?> = _budgetAlert.asStateFlow()

    init {
        refreshDateFilterIfNeeded()
        viewModelScope.launch {
            if (repository.budgets.value.isEmpty()) {
                val curMY = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
                repository.setOrUpdateBudget("Food & Beverages", 1500000.0, curMY)
                repository.setOrUpdateBudget("Shopping & Groceries", 1000000.0, curMY)
                repository.setOrUpdateBudget("Transportation", 500000.0, curMY)
            }
        }
    }

    fun parseAmountFromText(raw: String): Double? {
        val clean = raw.trim()
        if (clean.isEmpty()) return null
        return try {
            if (clean.contains(",") && clean.contains(".")) {
                if (clean.lastIndexOf(".") > clean.lastIndexOf(",")) {
                    clean.replace(",", "").toDoubleOrNull()
                } else {
                    clean.replace(".", "").replace(",", ".").toDoubleOrNull()
                }
            } else if (clean.contains(",")) {
                clean.replace(",", "").toDoubleOrNull()
            } else if (clean.contains(".")) {
                val parts = clean.split(".")
                if (parts.size == 2 && parts[1].length == 3) {
                    clean.replace(".", "").toDoubleOrNull()
                } else if (parts.size > 2) {
                    clean.replace(".", "").toDoubleOrNull()
                } else {
                    clean.toDoubleOrNull()
                }
            } else {
                clean.toDoubleOrNull()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getShieldAmountForTransaction(
        tx: Transaction,
        shieldHistoriesList: List<ShieldHistoryItem> = shieldHistories.value,
        shieldsList: List<ShieldEntry> = shields.value
    ): Double {
        if (tx.type != Transaction.TYPE_EXPENSE) return 0.0

        if (tx.id > 0) {
            val histTotal = shieldHistoriesList
                .filter { it.transactionId == tx.id && it.amount > 0 }
                .sumOf { it.amount }
            if (histTotal > 0.0) {
                return histTotal.coerceIn(0.0, tx.amount)
            }

            val shieldTotal = shieldsList
                .filter { it.transactionId == tx.id }
                .sumOf { it.amount }
            if (shieldTotal > 0.0) {
                return shieldTotal.coerceIn(0.0, tx.amount)
            }
        }

        val note = tx.note
        if (note.isNotBlank()) {
            val pribadiRegex = Regex("""Pribadi(?:\s*:)?\s*Rp\s*([\d,.]+)""", RegexOption.IGNORE_CASE)
            pribadiRegex.find(note)?.let { match ->
                val numStr = match.groupValues[1]
                parseAmountFromText(numStr)?.let { pribadiAmt ->
                    return (tx.amount - pribadiAmt).coerceIn(0.0, tx.amount)
                }
            }

            val shieldRegex = Regex("""Ditalangi(?:\s*Shield)?(?:\s*:)?\s*Rp\s*([\d,.]+)""", RegexOption.IGNORE_CASE)
            shieldRegex.find(note)?.let { match ->
                val numStr = match.groupValues[1]
                parseAmountFromText(numStr)?.let { shieldAmt ->
                    return shieldAmt.coerceIn(0.0, tx.amount)
                }
            }
        }

        return 0.0
    }

    fun getNetExpenseAmount(
        tx: Transaction,
        shieldHistoriesList: List<ShieldHistoryItem> = shieldHistories.value,
        shieldsList: List<ShieldEntry> = shields.value
    ): Double {
        if (tx.type != Transaction.TYPE_EXPENSE) return 0.0
        val shielded = getShieldAmountForTransaction(tx, shieldHistoriesList, shieldsList)
        return (tx.amount - shielded).coerceAtLeast(0.0)
    }

    val totalBalance: StateFlow<Double> = banks.map { list ->
        list.sumOf { it.balance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val filteredIncome: StateFlow<Double> = filteredTransactions.map { list ->
        list.filter { it.type == Transaction.TYPE_INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val filteredExpense: StateFlow<Double> = combine(
        filteredTransactions,
        shieldHistories,
        shields
    ) { txList, hList, sList ->
        txList.filter { it.type == Transaction.TYPE_EXPENSE }
            .sumOf { getNetExpenseAmount(it, hList, sList) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val filteredNet: StateFlow<Double> = combine(
        filteredTransactions,
        shieldHistories,
        shields
    ) { txList, hList, sList ->
        val inc = txList.filter { it.type == Transaction.TYPE_INCOME }.sumOf { it.amount }
        val exp = txList.filter { it.type == Transaction.TYPE_EXPENSE }
            .sumOf { getNetExpenseAmount(it, hList, sList) }
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expenseCategorySummaries: StateFlow<List<CategorySummary>> = combine(
        filteredTransactions,
        expenseCategories,
        shieldHistories,
        shields
    ) { txList, cats, hList, sList ->
        val expenses = txList.filter { it.type == Transaction.TYPE_EXPENSE }
        cats.map { cat ->
            val matched = expenses.filter { it.category.equals(cat.name, ignoreCase = true) }
            CategorySummary(
                categoryId = cat.id,
                categoryName = cat.name,
                totalAmount = matched.sumOf { getNetExpenseAmount(it, hList, sList) },
                transactionCount = matched.size,
                isExpense = true,
                colorHex = cat.colorHex,
                icon = cat.icon,
                transactions = matched
            )
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategorySummaries: StateFlow<List<CategorySummary>> = combine(
        filteredTransactions,
        incomeCategories
    ) { txList, cats ->
        val incomes = txList.filter { it.type == Transaction.TYPE_INCOME }
        cats.map { cat ->
            val matched = incomes.filter { it.category.equals(cat.name, ignoreCase = true) }
            CategorySummary(
                categoryId = cat.id,
                categoryName = cat.name,
                totalAmount = matched.sumOf { it.amount },
                transactionCount = matched.size,
                isExpense = false,
                colorHex = cat.colorHex,
                icon = cat.icon,
                transactions = matched
            )
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryBudgetStatuses: StateFlow<List<CategoryBudgetStatus>> = combine(
        budgets,
        allTransactions,
        budgetMonthYear,
        shieldHistories,
        shields
    ) { bList, txList, mYear, hList, sList ->
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val cal = Calendar.getInstance().apply {
            try { time = sdf.parse(mYear) ?: Date() } catch (_: Exception) {}
        }
        val startMonthMillis = Calendar.getInstance().apply {
            set(Calendar.YEAR, cal.get(Calendar.YEAR))
            set(Calendar.MONTH, cal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endMonthMillis = Calendar.getInstance().apply {
            set(Calendar.YEAR, cal.get(Calendar.YEAR))
            set(Calendar.MONTH, cal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val monthExpenses = txList.filter {
            it.type == Transaction.TYPE_EXPENSE && it.timestamp in startMonthMillis..endMonthMillis
        }

        bList.map { b ->
            val spent = monthExpenses
                .filter { it.category.equals(b.category, ignoreCase = true) }
                .sumOf { getNetExpenseAmount(it, hList, sList) }
            val remaining = b.monthlyLimit - spent
            val pct = if (b.monthlyLimit > 0) (spent / b.monthlyLimit).toFloat() else 0f
            val catObj = categories.value.find { it.name.equals(b.category, ignoreCase = true) }
                ?: TransactionCategory.defaultExpenseCategories.find { it.name.equals(b.category, ignoreCase = true) }
            CategoryBudgetStatus(
                budgetId = b.id,
                category = b.category,
                monthlyLimit = b.monthlyLimit,
                currentSpent = spent,
                remainingBudget = remaining,
                percentageUsed = pct,
                isExceeded = spent > b.monthlyLimit,
                colorHex = catObj?.colorHex ?: 0xFFEF4444L,
                iconName = catObj?.icon ?: "receipt"
            )
        }.sortedByDescending { it.percentageUsed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBudgetAmount: StateFlow<Double> = categoryBudgetStatuses.map { list ->
        list.sumOf { it.monthlyLimit }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBudgetSpent: StateFlow<Double> = categoryBudgetStatuses.map { list ->
        list.sumOf { it.currentSpent }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCurrentMonthExpense: StateFlow<Double> = combine(
        allTransactions,
        budgetMonthYear,
        shieldHistories,
        shields
    ) { txList, mYear, hList, sList ->
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val cal = Calendar.getInstance().apply {
            try { time = sdf.parse(mYear) ?: Date() } catch (_: Exception) {}
        }
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, cal.get(Calendar.YEAR))
            set(Calendar.MONTH, cal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(Calendar.YEAR, cal.get(Calendar.YEAR))
            set(Calendar.MONTH, cal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        txList.filter { it.type == Transaction.TYPE_EXPENSE && it.timestamp in start..end }
            .sumOf { getNetExpenseAmount(it, hList, sList) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addBank(name: String, accountNumber: String, balance: Double, type: String, colorHex: Long) {
        viewModelScope.launch {
            repository.addBank(
                Bank(
                    name = name.trim(),
                    accountNumber = accountNumber.trim(),
                    balance = balance,
                    type = type,
                    colorHex = colorHex,
                    iconName = name.lowercase().trim()
                )
            )
        }
    }

    fun updateBank(bank: Bank) {
        viewModelScope.launch {
            repository.updateBank(bank)
        }
    }

    fun deleteBank(bankId: Long) {
        viewModelScope.launch {
            repository.deleteBank(bankId)
        }
    }

    fun moveBankUp(bankId: Long) {
        viewModelScope.launch {
            val currentList = repository.banks.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == bankId }
            if (index > 0) {
                val item = currentList.removeAt(index)
                currentList.add(index - 1, item)
                repository.reorderBanks(currentList.map { it.id })
            }
        }
    }

    fun moveBankDown(bankId: Long) {
        viewModelScope.launch {
            val currentList = repository.banks.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == bankId }
            if (index in 0 until currentList.lastIndex) {
                val item = currentList.removeAt(index)
                currentList.add(index + 1, item)
                repository.reorderBanks(currentList.map { it.id })
            }
        }
    }

    fun addTransaction(
        bankId: Long,
        type: String,
        amount: Double,
        category: String,
        note: String,
        timestamp: Long = System.currentTimeMillis(),
        targetBankId: Long? = null,
        destination: String? = null,
        shieldFriends: List<ShieldFriendInput>? = null
    ) {
        viewModelScope.launch {
            val isShieldTx = !shieldFriends.isNullOrEmpty() || category.equals("Shield", ignoreCase = true)
            val finalCategory = if (category.isNotBlank() && !category.equals("Shield", ignoreCase = true)) {
                category
            } else if (isShieldTx) {
                "Shield"
            } else {
                category
            }
            val bank = repository.banks.value.find { it.id == bankId }
            val tx = Transaction(
                bankId = bankId,
                bankName = bank?.name ?: "",
                targetBankId = targetBankId,
                type = type,
                amount = amount,
                category = finalCategory,
                note = note,
                timestamp = timestamp,
                isAutoScraped = false,
                sourceApp = "MANUAL",
                destination = destination
            )
            val txId = repository.addTransaction(tx)

            if (!shieldFriends.isNullOrEmpty()) {
                val defaultShieldNote = when {
                    destination != null && destination.isNotBlank() -> "$finalCategory di $destination"
                    note.isNotBlank() -> note.trim()
                    else -> finalCategory
                }
                val validFriends = shieldFriends.filter { it.name.isNotBlank() && it.amount > 0 }
                if (validFriends.isNotEmpty()) {
                    repository.addShieldFriendsForTransaction(
                        txId = txId,
                        friends = validFriends,
                        defaultNote = defaultShieldNote,
                        timestamp = timestamp
                    )
                }
            }

            if (type == Transaction.TYPE_EXPENSE) {
                checkAndTriggerBudgetAlert(finalCategory, timestamp)
            }
        }
    }

    fun transferMoney(
        sourceBankId: Long,
        targetBankId: Long,
        amount: Double,
        note: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.transferBetweenBanks(
                sourceBankId = sourceBankId,
                targetBankId = targetBankId,
                amount = amount,
                note = note.ifEmpty { "Account Transfer" },
                timestamp = timestamp
            )
        }
    }

    fun updateTransaction(
        oldTx: Transaction,
        newTx: Transaction,
        shieldFriends: List<ShieldFriendInput>? = null
    ) {
        viewModelScope.launch {
            repository.updateTransaction(oldTx, newTx, shieldFriends)
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun setBankFilter(bankId: Long?) {
        repository.updateFilter(filterOptions.value.copy(selectedBankId = bankId))
    }

    fun setTimeFilterMode(mode: TimeFilterMode) {
        val now = Calendar.getInstance()
        when (mode) {
            TimeFilterMode.THIS_MONTH -> {
                repository.updateFilter(
                    filterOptions.value.copy(
                        timeFilterMode = mode,
                        selectedYear = now.get(Calendar.YEAR),
                        selectedMonth = now.get(Calendar.MONTH),
                        selectedDateMillis = now.timeInMillis,
                        isManualMonthSelection = false
                    )
                )
            }
            TimeFilterMode.TODAY -> {
                repository.updateFilter(
                    filterOptions.value.copy(
                        timeFilterMode = mode,
                        selectedDateMillis = now.timeInMillis,
                        selectedYear = now.get(Calendar.YEAR),
                        selectedMonth = now.get(Calendar.MONTH)
                    )
                )
            }
            TimeFilterMode.THIS_WEEK -> {
                repository.updateFilter(
                    filterOptions.value.copy(
                        timeFilterMode = mode,
                        selectedDateMillis = now.timeInMillis,
                        selectedYear = now.get(Calendar.YEAR),
                        selectedMonth = now.get(Calendar.MONTH)
                    )
                )
            }
            else -> {
                repository.updateFilter(filterOptions.value.copy(timeFilterMode = mode))
            }
        }
    }

    fun setSpecificDate(dateMillis: Long) {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        repository.updateFilter(
            filterOptions.value.copy(
                timeFilterMode = TimeFilterMode.SPECIFIC_DATE,
                selectedDateMillis = dateMillis,
                selectedYear = cal.get(Calendar.YEAR),
                selectedMonth = cal.get(Calendar.MONTH)
            )
        )
    }

    fun setMonthYear(year: Int, month: Int, isManual: Boolean = true) {
        val now = Calendar.getInstance()
        val isNow = (year == now.get(Calendar.YEAR) && month == now.get(Calendar.MONTH))
        repository.updateFilter(
            filterOptions.value.copy(
                timeFilterMode = TimeFilterMode.THIS_MONTH,
                selectedYear = year,
                selectedMonth = month,
                isManualMonthSelection = if (isNow) false else isManual
            )
        )
    }

    fun nextMonth() {
        val cur = filterOptions.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, cur.selectedYear)
            set(Calendar.MONTH, cur.selectedMonth)
            add(Calendar.MONTH, 1)
        }
        setMonthYear(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), isManual = true)
    }

    fun prevMonth() {
        val cur = filterOptions.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, cur.selectedYear)
            set(Calendar.MONTH, cur.selectedMonth)
            add(Calendar.MONTH, -1)
        }
        setMonthYear(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), isManual = true)
    }

    fun resetFilters() {
        val now = Calendar.getInstance()
        repository.updateFilter(
            FilterOptions(
                selectedBankId = null,
                timeFilterMode = TimeFilterMode.THIS_MONTH,
                selectedYear = now.get(Calendar.YEAR),
                selectedMonth = now.get(Calendar.MONTH),
                selectedDateMillis = now.timeInMillis,
                typeFilter = "ALL",
                isManualMonthSelection = false
            )
        )
    }

    fun refreshDateFilterIfNeeded() {
        val curMY = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        if (!isManualBudgetMonth && budgetMonthYear.value != curMY) {
            budgetMonthYear.value = curMY
            viewModelScope.launch {
                repository.loadBudgets(curMY)
            }
        }
        repository.refreshCurrentDateFilter()
    }

    fun setTypeFilter(type: String) {
        repository.updateFilter(filterOptions.value.copy(typeFilter = type))
    }

    fun simulateNotificationScrape(packageName: String, title: String, content: String): String {
        return kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            val result = repository.processScrapedNotification(
                packageName = packageName,
                title = title,
                content = content,
                postTime = System.currentTimeMillis()
            )
            if (result.transaction?.type == Transaction.TYPE_EXPENSE) {
                checkAndTriggerBudgetAlert(result.transaction.category, result.transaction.timestamp)
            }
            result.message
        }
    }

    fun clearNotificationLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun setOrUpdateBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.setOrUpdateBudget(category, limit, budgetMonthYear.value)
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            repository.deleteBudget(budgetId, budgetMonthYear.value)
        }
    }

    private var isManualBudgetMonth: Boolean = false
    private var budgetLoadJob: kotlinx.coroutines.Job? = null

    private fun loadBudgetsSafely(monthYear: String) {
        budgetLoadJob?.cancel()
        budgetLoadJob = viewModelScope.launch(Dispatchers.IO) {
            repository.loadBudgets(monthYear)
        }
    }

    fun prevBudgetMonth() {
        isManualBudgetMonth = true
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val cal = Calendar.getInstance().apply {
            try { time = sdf.parse(budgetMonthYear.value) ?: Date() } catch (_: Exception) {}
            add(Calendar.MONTH, -1)
        }
        val newMY = sdf.format(cal.time)
        budgetMonthYear.value = newMY
        loadBudgetsSafely(newMY)
    }

    fun nextBudgetMonth() {
        isManualBudgetMonth = true
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val cal = Calendar.getInstance().apply {
            try { time = sdf.parse(budgetMonthYear.value) ?: Date() } catch (_: Exception) {}
            add(Calendar.MONTH, 1)
        }
        val newMY = sdf.format(cal.time)
        budgetMonthYear.value = newMY
        loadBudgetsSafely(newMY)
    }

    fun resetBudgetMonthToCurrent() {
        isManualBudgetMonth = false
        val curMY = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        budgetMonthYear.value = curMY
        loadBudgetsSafely(curMY)
    }

    fun getBudgetMonthYearDisplay(): String {
        return formatMonthYearDisplay(budgetMonthYear.value)
    }

    fun setBudgetMonth(monthYear: String) {
        isManualBudgetMonth = true
        budgetMonthYear.value = monthYear
        loadBudgetsSafely(monthYear)
    }

    fun getPreviousBudgetMonthYear(fromMY: String = budgetMonthYear.value): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val cal = Calendar.getInstance().apply {
            try { time = sdf.parse(fromMY) ?: Date() } catch (_: Exception) {}
            add(Calendar.MONTH, -1)
        }
        return sdf.format(cal.time)
    }

    fun formatMonthYearDisplay(monthYear: String): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val outSdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        return try {
            val d = sdf.parse(monthYear) ?: Date()
            outSdf.format(d)
        } catch (_: Exception) {
            monthYear
        }
    }

    fun getPreviousBudgetMonthYearDisplay(): String {
        return formatMonthYearDisplay(getPreviousBudgetMonthYear())
    }

    suspend fun getBudgetsForMonth(monthYear: String): List<Budget> {
        return repository.getBudgetsForMonth(monthYear)
    }

    suspend fun getAvailableBudgetMonths(): List<String> {
        return repository.getAvailableBudgetMonths()
    }

    fun importBudgets(
        fromMonthYear: String = getPreviousBudgetMonthYear(),
        toMonthYear: String = budgetMonthYear.value,
        replaceAll: Boolean = false,
        onComplete: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val count = repository.importBudgets(fromMonthYear, toMonthYear, replaceAll)
            onComplete(count)
        }
    }

    fun dismissBudgetAlert() {
        _budgetAlert.value = null
    }

    fun checkAndTriggerBudgetAlert(category: String, timestamp: Long = System.currentTimeMillis()) {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val txMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time)

        val budget = repository.budgets.value.find {
            it.category.equals(category, ignoreCase = true) &&
            (it.monthYear == txMonthYear || it.monthYear == budgetMonthYear.value)
        } ?: return

        val startCal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val totalSpent = repository.allTransactions.value.filter {
            it.type == Transaction.TYPE_EXPENSE &&
            it.category.equals(category, ignoreCase = true) &&
            it.timestamp in startCal.timeInMillis..endCal.timeInMillis
        }.sumOf { getNetExpenseAmount(it) }

        if (totalSpent > budget.monthlyLimit) {
            val remaining = budget.monthlyLimit - totalSpent
            val over = totalSpent - budget.monthlyLimit
            _budgetAlert.value = BudgetAlertData(
                category = category,
                monthlyLimit = budget.monthlyLimit,
                currentSpent = totalSpent,
                remainingBudget = remaining,
                overBudgetAmount = over
            )
        }
    }

    fun addCategory(name: String, icon: String, colorHex: Long, isExpense: Boolean) {
        viewModelScope.launch {
            repository.addCategory(
                TransactionCategory(
                    name = name.trim(),
                    icon = icon,
                    colorHex = colorHex,
                    isExpense = isExpense
                )
            )
        }
    }

    fun updateCategory(oldName: String, name: String, icon: String, colorHex: Long, isExpense: Boolean) {
        viewModelScope.launch {
            repository.updateCategory(
                oldName = oldName.trim(),
                updated = TransactionCategory(
                    name = name.trim(),
                    icon = icon,
                    colorHex = colorHex,
                    isExpense = isExpense
                )
            )
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            repository.deleteCategory(name.trim())
        }
    }

    fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getNumberInstance(Locale.US)
        return "Rp " + format.format(amount)
    }

    fun formatFullDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun formatShortTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun formatFullDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm:ss", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun getMonthYearTitle(): String {
        val cur = filterOptions.value
        val now = Calendar.getInstance()
        val year = if (cur.timeFilterMode == TimeFilterMode.THIS_MONTH && !cur.isManualMonthSelection) {
            now.get(Calendar.YEAR)
        } else {
            cur.selectedYear
        }
        val month = if (cur.timeFilterMode == TimeFilterMode.THIS_MONTH && !cur.isManualMonthSelection) {
            now.get(Calendar.MONTH)
        } else {
            cur.selectedMonth
        }
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
        }
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        return sdf.format(cal.time)
    }

    fun addShield(
        friendName: String,
        amount: Double,
        note: String = "",
        transactionId: Long? = null,
        timestamp: Long = System.currentTimeMillis(),
        avatarUri: String? = null
    ) {
        viewModelScope.launch {
            repository.addShield(
                ShieldEntry(
                    transactionId = transactionId,
                    friendName = friendName.trim(),
                    amount = amount,
                    note = note.trim(),
                    timestamp = timestamp,
                    avatarUri = avatarUri
                )
            )
        }
    }

    fun settleShield(
        shieldId: Long,
        bankId: Long? = null,
        bankName: String? = null,
        recordIncome: Boolean = false
    ) {
        viewModelScope.launch {
            repository.settleShield(shieldId, bankId, bankName, recordIncome)
        }
    }

    fun unsettleShield(shieldId: Long) {
        viewModelScope.launch {
            repository.unsettleShield(shieldId)
        }
    }

    fun deleteShield(shieldId: Long) {
        viewModelScope.launch {
            repository.deleteShield(shieldId)
        }
    }

    fun updateShield(entry: ShieldEntry) {
        viewModelScope.launch {
            repository.updateShield(entry)
        }
    }

    fun updateShieldWithWallets(
        entry: ShieldEntry,
        walletDeductions: List<ShieldWalletDeduction> = emptyList(),
        historyNote: String? = null
    ) {
        viewModelScope.launch {
            repository.updateShieldWithWallets(entry, walletDeductions, historyNote)
        }
    }

    fun addShieldWithWallets(
        friendName: String,
        amount: Double,
        note: String = "",
        timestamp: Long = System.currentTimeMillis(),
        avatarUri: String? = null,
        walletDeductions: List<ShieldWalletDeduction> = emptyList()
    ) {
        viewModelScope.launch {
            repository.addShieldWithWallets(
                ShieldEntry(
                    friendName = friendName.trim(),
                    amount = amount,
                    note = note.trim(),
                    timestamp = timestamp,
                    avatarUri = avatarUri
                ),
                walletDeductions
            )
        }
    }

    fun updateShieldAvatar(shieldId: Long, friendName: String, avatarUri: String?) {
        viewModelScope.launch {
            repository.updateShieldAvatar(shieldId, friendName, avatarUri)
        }
    }

    fun recordPartialPayment(
        shieldId: Long,
        amount: Double,
        isTransfer: Boolean,
        bankId: Long? = null,
        bankName: String? = null,
        note: String = "",
        updatedFriendName: String? = null,
        updatedNote: String? = null
    ) {
        viewModelScope.launch {
            repository.recordPartialPayment(
                shieldId = shieldId,
                amount = amount,
                isTransfer = isTransfer,
                bankId = bankId,
                bankName = bankName,
                note = note,
                updatedFriendName = updatedFriendName,
                updatedNote = updatedNote
            )
        }
    }

    fun getShieldStatusForTransaction(txId: Long, note: String = "", category: String = ""): ShieldTransactionStatus {
        val sList = shields.value
        val hList = shieldHistories.value

        val sIdsFromHist = if (txId > 0) hList.filter { it.transactionId == txId }.map { it.shieldId } else emptyList()
        val sIdsFromShields = if (txId > 0) sList.filter { it.transactionId == txId }.map { it.id } else emptyList()
        val allLinkedShieldIds = (sIdsFromHist + sIdsFromShields).distinct()

        if (allLinkedShieldIds.isNotEmpty()) {
            val matchedShields = sList.filter { it.id in allLinkedShieldIds }
            return if (matchedShields.isNotEmpty() && matchedShields.all { it.isSettled }) {
                ShieldTransactionStatus.SETTLED
            } else {
                ShieldTransactionStatus.UNSETTLED
            }
        }

        if (note.contains("Ditalangi Shield", ignoreCase = true) ||
            note.contains("Ditalangi:", ignoreCase = true) ||
            category.equals("Shield", ignoreCase = true)
        ) {
            val hasUnsettled = sList.any { !it.isSettled }
            return if (!hasUnsettled) ShieldTransactionStatus.SETTLED else ShieldTransactionStatus.UNSETTLED
        }

        return ShieldTransactionStatus.NONE
    }
}
