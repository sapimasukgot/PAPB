package com.dave.moneymanager.data.local

import android.content.Context
import com.dave.moneymanager.data.model.Bank
import com.dave.moneymanager.data.model.Budget
import com.dave.moneymanager.data.model.FilterOptions
import com.dave.moneymanager.data.model.ScrapedNotificationLog
import com.dave.moneymanager.data.model.ShieldContact
import com.dave.moneymanager.data.model.ShieldEntry
import com.dave.moneymanager.data.model.ShieldFriendInput
import com.dave.moneymanager.data.model.ShieldHistoryItem
import com.dave.moneymanager.data.model.ShieldWalletDeduction
import com.dave.moneymanager.data.model.TimeFilterMode
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionCategory
import com.dave.moneymanager.data.model.TransactionEditHistory
import com.dave.moneymanager.data.model.isFoodCategory
import com.dave.moneymanager.data.model.isSameDay
import com.dave.moneymanager.service.NotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ScrapedProcessResult(
    val success: Boolean,
    val isTransfer: Boolean,
    val message: String,
    val transaction: Transaction? = null,
    val logStatus: String = "PROCESSED"
)

class MoneyRepository private constructor(context: Context) {

    private val dbHelper = DatabaseHelper.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _banks = MutableStateFlow<List<Bank>>(emptyList())
    val banks: StateFlow<List<Bank>> = _banks.asStateFlow()

    private val _categories = MutableStateFlow<List<TransactionCategory>>(emptyList())
    val categories: StateFlow<List<TransactionCategory>> = _categories.asStateFlow()

    private val _filterOptions = MutableStateFlow(FilterOptions.default())
    val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()

    private val _filteredTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val filteredTransactions: StateFlow<List<Transaction>> = _filteredTransactions.asStateFlow()

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions.asStateFlow()

    private val _notificationLogs = MutableStateFlow<List<ScrapedNotificationLog>>(emptyList())
    val notificationLogs: StateFlow<List<ScrapedNotificationLog>> = _notificationLogs.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    private val _shields = MutableStateFlow<List<ShieldEntry>>(emptyList())
    val shields: StateFlow<List<ShieldEntry>> = _shields.asStateFlow()

    private val _shieldHistories = MutableStateFlow<List<ShieldHistoryItem>>(emptyList())
    val shieldHistories: StateFlow<List<ShieldHistoryItem>> = _shieldHistories.asStateFlow()

    private val _shieldContacts = MutableStateFlow<List<ShieldContact>>(emptyList())
    val shieldContacts: StateFlow<List<ShieldContact>> = _shieldContacts.asStateFlow()

    private val _transactionEditHistories = MutableStateFlow<Map<Long, List<TransactionEditHistory>>>(emptyMap())
    val transactionEditHistories: StateFlow<Map<Long, List<TransactionEditHistory>>> = _transactionEditHistories.asStateFlow()

    var activeBudgetMonthYear: String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        private set

    init {
        refreshAll()
    }

    fun refreshAll() {
        scope.launch {
            loadBanks()
            loadCategories()
            loadAllTransactions()
            loadFilteredTransactions()
            loadNotificationLogs()
            loadBudgets()
            loadShields()
            loadTransactionEditHistories()
        }
    }

    suspend fun loadBanks() = withContext(Dispatchers.IO) {
        val list = dbHelper.getAllBanks()
        _banks.value = list
    }

    suspend fun loadAllTransactions() = withContext(Dispatchers.IO) {
        val list = dbHelper.getAllTransactions()
        _allTransactions.value = list
    }

    suspend fun loadTransactionEditHistories() = withContext(Dispatchers.IO) {
        val map = dbHelper.getAllEditHistories()
        _transactionEditHistories.value = map
    }

    suspend fun loadNotificationLogs() = withContext(Dispatchers.IO) {
        val logs = dbHelper.getAllNotificationLogs(onlyProcessed = true)
        _notificationLogs.value = logs
    }

    fun updateFilter(filter: FilterOptions) {
        _filterOptions.value = filter
        scope.launch {
            loadFilteredTransactions()
        }
    }

    suspend fun loadFilteredTransactions() = withContext(Dispatchers.IO) {
        val filter = _filterOptions.value
        val (startTime, endTime) = calculateTimeRange(filter)
        val list = dbHelper.getFilteredTransactions(
            bankId = filter.selectedBankId,
            startTimeMillis = startTime,
            endTimeMillis = endTime,
            type = if (filter.typeFilter == "ALL") null else filter.typeFilter
        )
        _filteredTransactions.value = list
    }

    fun refreshCurrentDateFilter(): Boolean {
        val now = Calendar.getInstance()
        val curYear = now.get(Calendar.YEAR)
        val curMonth = now.get(Calendar.MONTH)
        val curDay = now.get(Calendar.DAY_OF_YEAR)

        val current = _filterOptions.value
        var updated = current
        var changed = false

        if (current.timeFilterMode == TimeFilterMode.THIS_MONTH) {
            if (!current.isManualMonthSelection) {
                if (current.selectedYear != curYear || current.selectedMonth != curMonth) {
                    updated = current.copy(
                        selectedYear = curYear,
                        selectedMonth = curMonth,
                        selectedDateMillis = now.timeInMillis
                    )
                    changed = true
                }
            }
        } else if (current.timeFilterMode == TimeFilterMode.TODAY) {
            val calSel = Calendar.getInstance().apply { timeInMillis = current.selectedDateMillis }
            if (calSel.get(Calendar.YEAR) != curYear || calSel.get(Calendar.DAY_OF_YEAR) != curDay) {
                updated = current.copy(
                    selectedDateMillis = now.timeInMillis,
                    selectedYear = curYear,
                    selectedMonth = curMonth
                )
                changed = true
            }
        } else if (current.timeFilterMode == TimeFilterMode.THIS_WEEK) {
            changed = true
        }

        if (changed) {
            _filterOptions.value = updated
        }
        scope.launch {
            loadFilteredTransactions()
        }
        return changed
    }

    private fun calculateTimeRange(filter: FilterOptions): Pair<Long?, Long?> {
        val cal = Calendar.getInstance()
        return when (filter.timeFilterMode) {
            TimeFilterMode.ALL -> Pair(null, null)

            TimeFilterMode.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }

            TimeFilterMode.THIS_WEEK -> {
                val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.add(Calendar.DAY_OF_MONTH, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }

            TimeFilterMode.THIS_MONTH -> {
                val now = Calendar.getInstance()
                val targetYear = if (!filter.isManualMonthSelection) now.get(Calendar.YEAR) else filter.selectedYear
                val targetMonth = if (!filter.isManualMonthSelection) now.get(Calendar.MONTH) else filter.selectedMonth

                cal.set(Calendar.YEAR, targetYear)
                cal.set(Calendar.MONTH, targetMonth)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }

            TimeFilterMode.SPECIFIC_DATE -> {
                cal.timeInMillis = filter.selectedDateMillis
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }

            TimeFilterMode.CUSTOM_RANGE -> {
                Pair(filter.rangeStartMillis, filter.rangeEndMillis)
            }
        }
    }

    suspend fun addBank(bank: Bank): Long = withContext(Dispatchers.IO) {
        val id = dbHelper.insertBank(bank)
        loadBanks()
        id
    }

    suspend fun updateBank(bank: Bank): Boolean = withContext(Dispatchers.IO) {
        val ok = dbHelper.updateBank(bank)
        loadBanks()
        ok
    }

    suspend fun deleteBank(bankId: Long): Boolean = withContext(Dispatchers.IO) {
        val ok = dbHelper.deleteBank(bankId)
        loadBanks()
        loadAllTransactions()
        loadFilteredTransactions()
        ok
    }

    suspend fun reorderBanks(bankIdsInOrder: List<Long>) = withContext(Dispatchers.IO) {
        dbHelper.updateBankOrders(bankIdsInOrder)
        loadBanks()
    }

    suspend fun getOrCreateBankByName(name: String, defaultColor: Long = 0xFF10B981L): Bank = withContext(Dispatchers.IO) {
        val existing = dbHelper.getBankByName(name)
        if (existing != null) return@withContext existing

        val defaultAccountNo = when {
            name.equals("GoPay Clone", ignoreCase = true) -> "081234567891"
            name.equals("GoPay", ignoreCase = true) -> "081234567890"
            name.equals("ShopeePay", ignoreCase = true) -> "081234567890"
            name.equals("OVO", ignoreCase = true) -> "081234567890"
            name.equals("Bank BNI", ignoreCase = true) || name.equals("BNI", ignoreCase = true) -> "0234567890"
            name.equals("Bank CIMB", ignoreCase = true) || name.equals("CIMB", ignoreCase = true) || name.equals("CIMB Niaga", ignoreCase = true) -> "701234567890"
            name.equals("OctoPay", ignoreCase = true) -> "081234567890"
            else -> ""
        }
        val defaultType = when {
            name.contains("bni", ignoreCase = true) || name.contains("cimb", ignoreCase = true) -> Bank.TYPE_BANK
            else -> Bank.TYPE_EWALLET
        }
        val resolvedColor = when {
            name.equals("Bank BNI", ignoreCase = true) || name.equals("BNI", ignoreCase = true) -> 0xFFE05A10L
            name.equals("Bank CIMB", ignoreCase = true) || name.equals("CIMB", ignoreCase = true) || name.equals("CIMB Niaga", ignoreCase = true) -> 0xFFED1C24L
            name.equals("OctoPay", ignoreCase = true) -> 0xFF8A1538L
            name.equals("GoPay", ignoreCase = true) -> 0xFF00AED6L
            name.equals("GoPay Clone", ignoreCase = true) -> 0xFF00838FL
            name.equals("ShopeePay", ignoreCase = true) -> 0xFFEE4D2DL
            name.equals("OVO", ignoreCase = true) -> 0xFF4C3494L
            else -> defaultColor
        }
        val defaultIcon = when {
            name.contains("gopay", ignoreCase = true) -> "gopay"
            name.contains("shopee", ignoreCase = true) -> "shopeepay"
            name.contains("ovo", ignoreCase = true) -> "ovo"
            name.contains("bni", ignoreCase = true) -> "bni"
            name.contains("cimb", ignoreCase = true) -> "cimb"
            name.contains("octopay", ignoreCase = true) -> "octopay"
            else -> "wallet"
        }

        val newBank = Bank(
            name = name,
            accountNumber = defaultAccountNo,
            balance = 0.0,
            type = defaultType,
            colorHex = resolvedColor,
            iconName = defaultIcon
        )
        val id = dbHelper.insertBank(newBank)
        loadBanks()
        newBank.copy(id = id)
    }

    suspend fun addTransaction(tx: Transaction): Long = withContext(Dispatchers.IO) {
        val id = dbHelper.insertTransaction(tx)
        loadBanks()
        loadAllTransactions()
        loadFilteredTransactions()
        id
    }

    suspend fun updateTransaction(
        oldTx: Transaction,
        newTx: Transaction,
        shieldFriends: List<ShieldFriendInput>? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val ok = dbHelper.updateTransaction(oldTx, newTx)
        if (ok) {
            if (shieldFriends != null) {
                dbHelper.deleteShieldsForTransaction(oldTx.id)
                val validFriends = shieldFriends.filter { it.name.isNotBlank() && it.amount > 0 }
                if (validFriends.isNotEmpty()) {
                    val defaultShieldNote = when {
                        newTx.destination != null && newTx.destination.isNotBlank() -> "${newTx.category} di ${newTx.destination}"
                        newTx.note.isNotBlank() -> {
                            val cleaned = newTx.note
                                .replace(Regex("""(?i)\s*•\s*Kasir:\s*Rp\s*[\d,.]+(?:\s*\([^)]*\))?"""), "")
                                .replace(Regex("""(?i)^Kasir:\s*Rp\s*[\d,.]+(?:\s*\([^)]*\))?"""), "")
                                .trim()
                            if (cleaned.isNotBlank()) cleaned else newTx.category
                        }
                        else -> newTx.category
                    }
                    addShieldFriendsForTransaction(
                        txId = oldTx.id,
                        friends = validFriends,
                        defaultNote = defaultShieldNote,
                        timestamp = newTx.timestamp
                    )
                }
                loadShields()
                loadShieldHistories()
                loadShieldContacts()
            }
        }
        loadBanks()
        loadAllTransactions()
        loadFilteredTransactions()
        loadTransactionEditHistories()
        loadShields()
        loadShieldHistories()
        ok
    }

    suspend fun deleteTransaction(tx: Transaction): Boolean = withContext(Dispatchers.IO) {
        val ok = dbHelper.deleteTransaction(tx)
        loadBanks()
        loadAllTransactions()
        loadFilteredTransactions()
        loadShields()
        loadTransactionEditHistories()
        ok
    }

    suspend fun transferBetweenBanks(
        sourceBankId: Long,
        targetBankId: Long,
        amount: Double,
        note: String = "Account Transfer",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val tx = Transaction(
            bankId = sourceBankId,
            targetBankId = targetBankId,
            type = Transaction.TYPE_TRANSFER,
            amount = amount,
            category = "Account Transfer",
            note = note,
            timestamp = timestamp
        )
        addTransaction(tx)
    }

    suspend fun processScrapedNotification(
        packageName: String,
        title: String,
        content: String,
        postTime: Long,
        isClonedUser: Boolean = false
    ): ScrapedProcessResult = withContext(Dispatchers.IO) {
        val timestamp = if (postTime > 0) postTime else System.currentTimeMillis()
        val fullText = "$title $content".trim()

        if (NotificationParser.containsEmoji(fullText)) {
            return@withContext ScrapedProcessResult(
                success = false,
                isTransfer = false,
                message = "Notifikasi diabaikan: Mengandung emoji/promosi.",
                logStatus = "IGNORED (Promosi / Mengandung Emoji)"
            )
        }

        if (NotificationParser.isCashbackOrCoinNotification(title, content)) {
            return@withContext ScrapedProcessResult(
                success = false,
                isTransfer = false,
                message = "Notifikasi diabaikan: Cashback & Koin/Points tidak dicatat.",
                logStatus = "IGNORED (Cashback/Koin Diabaikan)"
            )
        }

        if (NotificationParser.isPromotionalOrMarketingNotification(title, content)) {
            return@withContext ScrapedProcessResult(
                success = false,
                isTransfer = false,
                message = "Notifikasi diabaikan: Pesan promosi, penawaran, atau marketing.",
                logStatus = "IGNORED (Promosi / Marketing)"
            )
        }

        if (!NotificationParser.hasTransactionConfirmation(fullText)) {
            return@withContext ScrapedProcessResult(
                success = false,
                isTransfer = false,
                message = "Notifikasi diabaikan: Tidak ada bukti transaksi riil.",
                logStatus = "IGNORED (Bukan Bukti Transaksi Riil)"
            )
        }

        val parsed = NotificationParser.parse(packageName, title, content, isClonedUser)
        if (parsed == null || !parsed.isValidTransaction) {
            val status = if (NotificationParser.extractAmount(fullText) == null) {
                "IGNORED (Nominal Rp tidak ditemukan dalam notifikasi)"
            } else {
                "IGNORED (Bukan Transaksi Keuangan)"
            }
            return@withContext ScrapedProcessResult(
                success = false,
                isTransfer = false,
                message = "Notifikasi diabaikan: $status",
                logStatus = status
            )
        }

        val bank = getOrCreateBankByName(parsed.targetBankName)

        val recentTxs = dbHelper.getAllTransactions()

        val dedupeWindowMs = 90_000L
        val isDuplicateTx = recentTxs.any { existing ->
            existing.isAutoScraped &&
            existing.type == parsed.type &&
            existing.amount == parsed.amount &&
            kotlin.math.abs(existing.timestamp - timestamp) <= dedupeWindowMs &&
            (
                existing.bankId == bank.id ||
                isSameBankFamily(existing.bankName, bank.name)
            ) &&
            (
                existing.destination.equals(parsed.destination, ignoreCase = true) ||
                existing.note.equals(parsed.note, ignoreCase = true) ||
                (parsed.destination.isNullOrEmpty() && existing.destination.isNullOrEmpty())
            )
        }

        if (isDuplicateTx) {
            val amtFormatted = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(parsed.amount)
            val logStatus = "IGNORED (Duplikat Transaksi)"
            recordNotificationLog(
                ScrapedNotificationLog(
                    packageName = packageName,
                    appName = parsed.appName,
                    title = title,
                    content = content,
                    timestamp = timestamp,
                    status = logStatus,
                    amount = parsed.amount,
                    detectedType = parsed.type,
                    targetBank = parsed.targetBankName,
                    destination = parsed.destination
                )
            )
            return@withContext ScrapedProcessResult(
                success = false,
                isTransfer = false,
                message = "Notifikasi diabaikan: Transaksi serupa (Rp $amtFormatted) baru saja dicatat kurang dari 90 detik yang lalu.",
                logStatus = logStatus
            )
        }

        val matchWindowMs = 120_000L
        val matchingOppositeTx = recentTxs.firstOrNull { existing ->
            existing.type != Transaction.TYPE_TRANSFER &&
            existing.amount == parsed.amount &&
            existing.bankId != bank.id &&
            kotlin.math.abs(existing.timestamp - timestamp) <= matchWindowMs &&
            ((existing.type == Transaction.TYPE_EXPENSE && parsed.type == Transaction.TYPE_INCOME) ||
             (existing.type == Transaction.TYPE_INCOME && parsed.type == Transaction.TYPE_EXPENSE))
        }

        if (matchingOppositeTx != null) {

            val sourceBankId: Long
            val sourceBankName: String
            val targetBankId: Long
            val targetBankName: String

            if (matchingOppositeTx.type == Transaction.TYPE_EXPENSE) {
                sourceBankId = matchingOppositeTx.bankId
                sourceBankName = matchingOppositeTx.bankName
                targetBankId = bank.id
                targetBankName = bank.name
            } else {
                sourceBankId = bank.id
                sourceBankName = bank.name
                targetBankId = matchingOppositeTx.bankId
                targetBankName = matchingOppositeTx.bankName
            }

            dbHelper.deleteTransaction(matchingOppositeTx)

            val transferTx = Transaction(
                bankId = sourceBankId,
                bankName = sourceBankName,
                targetBankId = targetBankId,
                targetBankName = targetBankName,
                type = Transaction.TYPE_TRANSFER,
                amount = parsed.amount,
                category = "Account Transfer",
                note = "Transfer: $sourceBankName ➔ $targetBankName",
                timestamp = timestamp,
                isAutoScraped = true,
                sourceApp = parsed.appName,
                destination = targetBankName
            )
            val txId = dbHelper.insertTransaction(transferTx)

            loadBanks()
            loadAllTransactions()
            loadFilteredTransactions()

            val logStatus = "PROCESSED (Transfer Otomatis: $sourceBankName ➔ $targetBankName)"
            recordNotificationLog(
                ScrapedNotificationLog(
                    packageName = packageName,
                    appName = parsed.appName,
                    title = title,
                    content = content,
                    timestamp = timestamp,
                    status = logStatus,
                    amount = parsed.amount,
                    detectedType = Transaction.TYPE_TRANSFER,
                    targetBank = targetBankName,
                    destination = targetBankName
                )
            )

            val amtFormatted = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(parsed.amount)
            return@withContext ScrapedProcessResult(
                success = true,
                isTransfer = true,
                message = "Berhasil dicatat sebagai Transfer: $sourceBankName ➔ $targetBankName (Rp $amtFormatted)",
                transaction = transferTx.copy(id = txId),
                logStatus = logStatus
            )
        }

        if (parsed.type == Transaction.TYPE_EXPENSE && isFoodCategory(parsed.category)) {
            val allTxs = dbHelper.getAllTransactions()
            val existingFoodTx = allTxs.firstOrNull { existing ->
                existing.type == Transaction.TYPE_EXPENSE &&
                existing.bankId == bank.id &&
                isFoodCategory(existing.category) &&
                isSameDay(existing.timestamp, timestamp)
            }

            if (existingFoodTx != null) {
                val newAmount = existingFoodTx.amount + parsed.amount
                val mergedDestination = when {
                    existingFoodTx.destination.isNullOrBlank() -> parsed.destination
                    parsed.destination.isNullOrBlank() -> existingFoodTx.destination
                    existingFoodTx.destination.contains(parsed.destination, ignoreCase = true) -> existingFoodTx.destination
                    parsed.destination.contains(existingFoodTx.destination, ignoreCase = true) -> parsed.destination
                    else -> "${existingFoodTx.destination}, ${parsed.destination}"
                }

                val mergedNote = when {
                    existingFoodTx.note.isBlank() -> parsed.note
                    parsed.note.isBlank() -> existingFoodTx.note
                    existingFoodTx.note.contains(parsed.note, ignoreCase = true) -> existingFoodTx.note
                    parsed.note.contains(existingFoodTx.note, ignoreCase = true) -> parsed.note
                    existingFoodTx.note.startsWith("To: ", ignoreCase = true) && parsed.note.startsWith("To: ", ignoreCase = true) -> {
                        if (!mergedDestination.isNullOrBlank()) "To: $mergedDestination" else "${existingFoodTx.note} | ${parsed.note}"
                    }
                    else -> "${existingFoodTx.note} | ${parsed.note}"
                }

                val updatedTx = existingFoodTx.copy(
                    amount = newAmount,
                    category = "Food & Dining",
                    destination = mergedDestination,
                    note = mergedNote,
                    timestamp = maxOf(existingFoodTx.timestamp, timestamp)
                )

                dbHelper.updateTransaction(existingFoodTx, updatedTx)

                loadBanks()
                loadAllTransactions()
                loadFilteredTransactions()

                val amtFormatted = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(parsed.amount)
                val totalFormatted = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(updatedTx.amount)
                val destInfo = if (!parsed.destination.isNullOrEmpty()) " [To: ${parsed.destination}]" else ""
                val logStatus = "PROCESSED (Update Food & Dining: +Rp $amtFormatted)"
                recordNotificationLog(
                    ScrapedNotificationLog(
                        packageName = packageName,
                        appName = parsed.appName,
                        title = title,
                        content = content,
                        timestamp = timestamp,
                        status = logStatus,
                        amount = parsed.amount,
                        detectedType = parsed.type,
                        targetBank = parsed.targetBankName,
                        destination = parsed.destination
                    )
                )

                return@withContext ScrapedProcessResult(
                    success = true,
                    isTransfer = false,
                    message = "Berhasil mengupdate Food & Dining di ${bank.name}: +Rp $amtFormatted$destInfo (Total: Rp $totalFormatted)",
                    transaction = updatedTx,
                    logStatus = logStatus
                )
            }
        }

        val categoryToSave = if (isFoodCategory(parsed.category)) "Food & Dining" else parsed.category
        val transaction = Transaction(
            bankId = bank.id,
            bankName = bank.name,
            type = parsed.type,
            amount = parsed.amount,
            category = categoryToSave,
            note = parsed.note,
            timestamp = timestamp,
            isAutoScraped = true,
            sourceApp = parsed.appName,
            destination = parsed.destination
        )
        val txId = dbHelper.insertTransaction(transaction)

        loadBanks()
        loadAllTransactions()
        loadFilteredTransactions()

        val logStatus = "PROCESSED"
        recordNotificationLog(
            ScrapedNotificationLog(
                packageName = packageName,
                appName = parsed.appName,
                title = title,
                content = content,
                timestamp = timestamp,
                status = logStatus,
                amount = parsed.amount,
                detectedType = parsed.type,
                targetBank = parsed.targetBankName,
                destination = parsed.destination
            )
        )

        val amtFormatted = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(parsed.amount)
        val destInfo = if (!parsed.destination.isNullOrEmpty()) " [To/From: ${parsed.destination}]" else ""
        return@withContext ScrapedProcessResult(
            success = true,
            isTransfer = false,
            message = "Berhasil dicatat: ${parsed.type} Rp $amtFormatted ke ${bank.name}$destInfo",
            transaction = transaction.copy(id = txId),
            logStatus = logStatus
        )
    }

    suspend fun recordNotificationLog(log: ScrapedNotificationLog) = withContext(Dispatchers.IO) {
        dbHelper.insertNotificationLog(log)
        loadNotificationLogs()
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        dbHelper.clearNotificationLogs()
        loadNotificationLogs()
    }

    suspend fun loadBudgets(monthYear: String = activeBudgetMonthYear) = withContext(Dispatchers.IO) {
        activeBudgetMonthYear = monthYear
        val list = dbHelper.getBudgets(monthYear)
        if (activeBudgetMonthYear == monthYear) {
            _budgets.value = list
        }
    }

    suspend fun setOrUpdateBudget(category: String, limit: Double, monthYear: String = activeBudgetMonthYear) = withContext(Dispatchers.IO) {
        dbHelper.setOrUpdateBudget(category, limit, monthYear)
        loadBudgets(monthYear)
    }

    suspend fun deleteBudget(id: Long, monthYear: String = activeBudgetMonthYear) = withContext(Dispatchers.IO) {
        dbHelper.deleteBudget(id)
        loadBudgets(monthYear)
    }

    suspend fun getBudgetsForMonth(monthYear: String): List<Budget> = withContext(Dispatchers.IO) {
        dbHelper.getBudgets(monthYear)
    }

    suspend fun getAvailableBudgetMonths(): List<String> = withContext(Dispatchers.IO) {
        dbHelper.getAvailableBudgetMonths()
    }

    suspend fun importBudgets(fromMonthYear: String, toMonthYear: String, replaceAll: Boolean = false): Int = withContext(Dispatchers.IO) {
        val count = dbHelper.importBudgetsFromMonth(fromMonthYear, toMonthYear, replaceAll)
        if (toMonthYear == activeBudgetMonthYear) {
            loadBudgets(toMonthYear)
        }
        count
    }

    suspend fun loadCategories() = withContext(Dispatchers.IO) {
        val list = dbHelper.getAllCategories()
        _categories.value = list
    }

    suspend fun addCategory(cat: TransactionCategory): Long = withContext(Dispatchers.IO) {
        val id = dbHelper.insertCategory(cat)
        loadCategories()
        id
    }

    suspend fun updateCategory(oldName: String, updated: TransactionCategory): Boolean = withContext(Dispatchers.IO) {
        val ok = dbHelper.updateCategory(oldName, updated)
        loadCategories()
        loadBudgets()
        loadAllTransactions()
        loadFilteredTransactions()
        ok
    }

    suspend fun deleteCategory(name: String): Boolean = withContext(Dispatchers.IO) {
        val ok = dbHelper.deleteCategory(name)
        loadCategories()
        loadBudgets()
        loadAllTransactions()
        loadFilteredTransactions()
        ok
    }

    suspend fun loadShields() = withContext(Dispatchers.IO) {
        val list = dbHelper.getAllShields()
        _shields.value = list
        loadShieldHistories()
        loadShieldContacts()
    }

    suspend fun loadShieldHistories() = withContext(Dispatchers.IO) {
        val list = dbHelper.getAllShieldHistories()
        _shieldHistories.value = list
    }

    suspend fun loadShieldContacts() = withContext(Dispatchers.IO) {
        val list = dbHelper.getAllShieldContacts()
        _shieldContacts.value = list
    }

    fun getAvatarForFriend(name: String): String? {
        return dbHelper.getAvatarByFriendName(name)
    }

    suspend fun addShield(entry: ShieldEntry): Long = withContext(Dispatchers.IO) {
        val id = dbHelper.insertOrAccumulateShield(entry)
        loadShields()
        id
    }

    suspend fun addShields(entries: List<ShieldEntry>) = withContext(Dispatchers.IO) {
        for (e in entries) {
            dbHelper.insertOrAccumulateShield(e)
        }
        loadShields()
    }

    suspend fun addShieldFriendsForTransaction(
        txId: Long,
        friends: List<ShieldFriendInput>,
        defaultNote: String,
        timestamp: Long
    ) = withContext(Dispatchers.IO) {
        var banksOrTxChanged = false

        for (f in friends) {
            val name = f.name.trim()
            if (name.isBlank() || f.amount <= 0) continue

            val shieldNote = if (f.note.isNotBlank()) f.note.trim() else defaultNote
            val pastAvatar = dbHelper.getAvatarByFriendName(name)
            dbHelper.saveOrUpdateShieldContact(name, pastAvatar)

            val isFullySettled = f.isSettled || (f.paidAmount >= f.amount && f.paidAmount > 0)
            val isPartial = !isFullySettled && f.paidAmount > 0 && f.paidAmount < f.amount

            if (isFullySettled) {
                var incomeTxId: Long? = null
                if (f.isTransfer && f.settleBankId != null) {
                    val incTx = Transaction(
                        bankId = f.settleBankId,
                        bankName = f.settleBankName ?: "",
                        type = Transaction.TYPE_INCOME,
                        amount = f.amount,
                        category = "Reimburse",
                        note = "Pelunasan talangan (Shield): $name${if (f.note.isNotBlank()) " - ${f.note.trim()}" else ""}",
                        timestamp = timestamp,
                        destination = name
                    )
                    val insertedTxId = dbHelper.insertTransaction(incTx)
                    if (insertedTxId > 0) {
                        incomeTxId = insertedTxId
                        banksOrTxChanged = true
                    }
                }

                val settledShield = ShieldEntry(
                    transactionId = if (txId > 0) txId else null,
                    friendName = name,
                    amount = f.amount,
                    note = shieldNote,
                    timestamp = timestamp,
                    isSettled = true,
                    settledAt = timestamp,
                    settledBankId = if (f.isTransfer) f.settleBankId else null,
                    settledBankName = if (f.isTransfer) f.settleBankName else null,
                    avatarUri = pastAvatar
                )
                val sId = dbHelper.insertShield(settledShield)
                if (sId > 0) {
                    dbHelper.insertShieldHistory(
                        ShieldHistoryItem(
                            shieldId = sId,
                            transactionId = if (txId > 0) txId else null,
                            friendName = name,
                            amount = f.amount,
                            note = shieldNote,
                            timestamp = timestamp
                        )
                    )
                    val settleHistNote = if (f.isTransfer) {
                        "Ditransfer (${f.settleBankName ?: "Rekening"}): Lunas langsung"
                    } else {
                        "Lunas langsung (Tunai/Lainnya)"
                    }
                    dbHelper.insertShieldHistory(
                        ShieldHistoryItem(
                            shieldId = sId,
                            transactionId = incomeTxId ?: (if (txId > 0) txId else null),
                            friendName = name,
                            amount = -f.amount,
                            note = settleHistNote,
                            timestamp = timestamp + 1
                        )
                    )
                }
            } else if (isPartial) {
                var incomeTxId: Long? = null
                if (f.isTransfer && f.settleBankId != null) {
                    val incTx = Transaction(
                        bankId = f.settleBankId,
                        bankName = f.settleBankName ?: "",
                        type = Transaction.TYPE_INCOME,
                        amount = f.paidAmount,
                        category = "Reimburse",
                        note = "Pembayaran sebagian talangan (Shield): $name${if (f.note.isNotBlank()) " - ${f.note.trim()}" else ""}",
                        timestamp = timestamp,
                        destination = name
                    )
                    val insertedTxId = dbHelper.insertTransaction(incTx)
                    if (insertedTxId > 0) {
                        incomeTxId = insertedTxId
                        banksOrTxChanged = true
                    }
                }

                val remainingDebt = f.amount - f.paidAmount
                val existing = dbHelper.getActiveShieldByFriendName(name)
                val sId: Long
                if (existing != null) {
                    val combinedAmount = existing.amount + remainingDebt
                    val combinedNote = when {
                        existing.note.isNotBlank() && f.note.isNotBlank() -> "${existing.note}, ${f.note.trim()}"
                        f.note.isNotBlank() -> f.note.trim()
                        else -> existing.note
                    }
                    val updated = existing.copy(
                        amount = combinedAmount,
                        note = combinedNote,
                        timestamp = timestamp,
                        avatarUri = pastAvatar ?: existing.avatarUri
                    )
                    dbHelper.updateShield(updated)
                    sId = existing.id
                } else {
                    val newShield = ShieldEntry(
                        transactionId = if (txId > 0) txId else null,
                        friendName = name,
                        amount = remainingDebt,
                        note = shieldNote,
                        timestamp = timestamp,
                        isSettled = false,
                        avatarUri = pastAvatar
                    )
                    sId = dbHelper.insertShield(newShield)
                }

                if (sId > 0) {
                    dbHelper.insertShieldHistory(
                        ShieldHistoryItem(
                            shieldId = sId,
                            transactionId = if (txId > 0) txId else null,
                            friendName = name,
                            amount = f.amount,
                            note = shieldNote,
                            timestamp = timestamp
                        )
                    )
                    val deductionHistNote = if (f.isTransfer) {
                        "Ditransfer (${f.settleBankName ?: "Rekening"}): Bayar sebagian saat transaksi"
                    } else {
                        "Bayar sebagian saat transaksi (Tunai/Lainnya)"
                    }
                    dbHelper.insertShieldHistory(
                        ShieldHistoryItem(
                            shieldId = sId,
                            transactionId = incomeTxId ?: (if (txId > 0) txId else null),
                            friendName = name,
                            amount = -f.paidAmount,
                            note = deductionHistNote,
                            timestamp = timestamp + 1
                        )
                    )
                }
            } else {
                val entry = ShieldEntry(
                    transactionId = if (txId > 0) txId else null,
                    friendName = name,
                    amount = f.amount,
                    note = shieldNote,
                    timestamp = timestamp,
                    avatarUri = pastAvatar
                )
                dbHelper.insertOrAccumulateShield(entry)
            }
        }

        if (banksOrTxChanged) {
            loadBanks()
            loadAllTransactions()
            loadFilteredTransactions()
        }
        loadShields()
        loadShieldHistories()
        loadShieldContacts()
    }

    suspend fun updateShield(entry: ShieldEntry): Boolean = withContext(Dispatchers.IO) {
        val ok = dbHelper.updateShield(entry)
        loadShields()
        ok
    }

    suspend fun updateShieldWithWallets(
        entry: ShieldEntry,
        walletDeductions: List<ShieldWalletDeduction> = emptyList(),
        historyNote: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val oldEntry = _shields.value.find { it.id == entry.id }
        val ok = dbHelper.updateShield(entry)

        for (d in walletDeductions) {
            if (d.amount > 0 && d.bankId > 0) {
                val txNote = if (entry.note.isNotBlank()) {
                    "Talangan Shield: ${entry.friendName} - ${entry.note}"
                } else {
                    "Talangan Shield: ${entry.friendName}"
                }
                val tx = Transaction(
                    bankId = d.bankId,
                    bankName = d.bankName,
                    type = Transaction.TYPE_EXPENSE,
                    amount = d.amount,
                    category = "Shield",
                    note = txNote,
                    timestamp = System.currentTimeMillis(),
                    destination = entry.friendName
                )
                val txId = dbHelper.insertTransaction(tx)

                val hNote = historyNote ?: "Keluar dari ${d.bankName}: Rp ${java.text.NumberFormat.getNumberInstance(Locale.US).format(d.amount)}"
                dbHelper.insertShieldHistory(
                    ShieldHistoryItem(
                        shieldId = entry.id,
                        transactionId = if (txId > 0) txId else null,
                        friendName = entry.friendName.trim(),
                        amount = d.amount,
                        note = hNote,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        if (walletDeductions.isEmpty() && oldEntry != null && oldEntry.amount != entry.amount) {
            val diff = entry.amount - oldEntry.amount
            dbHelper.insertShieldHistory(
                ShieldHistoryItem(
                    shieldId = entry.id,
                    transactionId = null,
                    friendName = entry.friendName.trim(),
                    amount = diff,
                    note = "Ubah jumlah shield: Rp ${java.text.NumberFormat.getNumberInstance(Locale.US).format(oldEntry.amount)} ➔ Rp ${java.text.NumberFormat.getNumberInstance(Locale.US).format(entry.amount)}",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        loadBanks()
        loadAllTransactions()
        loadFilteredTransactions()
        loadShields()
        loadShieldHistories()
        ok
    }

    suspend fun addShieldWithWallets(
        entry: ShieldEntry,
        walletDeductions: List<ShieldWalletDeduction> = emptyList()
    ): Long = withContext(Dispatchers.IO) {
        val shieldId = dbHelper.insertOrAccumulateShield(entry)
        if (shieldId > 0) {
            for (d in walletDeductions) {
                if (d.amount > 0 && d.bankId > 0) {
                    val txNote = if (entry.note.isNotBlank()) {
                        "Talangan Shield: ${entry.friendName} - ${entry.note}"
                    } else {
                        "Talangan Shield: ${entry.friendName}"
                    }
                    val tx = Transaction(
                        bankId = d.bankId,
                        bankName = d.bankName,
                        type = Transaction.TYPE_EXPENSE,
                        amount = d.amount,
                        category = "Shield",
                        note = txNote,
                        timestamp = System.currentTimeMillis(),
                        destination = entry.friendName
                    )
                    val txId = dbHelper.insertTransaction(tx)
                    if (txId > 0) {
                        dbHelper.insertShieldHistory(
                            ShieldHistoryItem(
                                shieldId = shieldId,
                                transactionId = txId,
                                friendName = entry.friendName.trim(),
                                amount = d.amount,
                                note = "Keluar dari ${d.bankName}: Rp ${java.text.NumberFormat.getNumberInstance(Locale.US).format(d.amount)}",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }
        loadBanks()
        loadAllTransactions()
        loadFilteredTransactions()
        loadShields()
        loadShieldHistories()
        shieldId
    }

    suspend fun updateShieldAvatar(shieldId: Long, friendName: String, avatarUri: String?): Boolean = withContext(Dispatchers.IO) {
        val ok = dbHelper.updateShieldAvatar(shieldId, friendName, avatarUri)
        loadShields()
        ok
    }

    suspend fun recordPartialPayment(
        shieldId: Long,
        amount: Double,
        isTransfer: Boolean,
        bankId: Long? = null,
        bankName: String? = null,
        note: String = "",
        updatedFriendName: String? = null,
        updatedNote: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val shield = _shields.value.find { it.id == shieldId } ?: return@withContext false
        if (amount <= 0) return@withContext false

        val deduction = minOf(amount, shield.amount)
        val remainingAmount = shield.amount - deduction
        val isFullySettled = remainingAmount <= 0.0

        var incomeTxId: Long? = null
        if (isTransfer && bankId != null) {
            val incomeTxNote = if (note.isNotBlank()) {
                "Pembayaran sebagian talangan (Shield): ${shield.friendName} - $note"
            } else {
                "Pembayaran sebagian talangan (Shield): ${shield.friendName}"
            }
            val tx = Transaction(
                bankId = bankId,
                type = Transaction.TYPE_INCOME,
                amount = deduction,
                category = "Reimburse",
                note = incomeTxNote,
                timestamp = System.currentTimeMillis(),
                destination = shield.friendName
            )
            val txId = dbHelper.insertTransaction(tx)
            if (txId > 0) {
                incomeTxId = txId
            }
            loadBanks()
            loadAllTransactions()
            loadFilteredTransactions()
        }

        val historyNote = when {
            isTransfer && note.isNotBlank() -> "Ditransfer ($bankName): $note"
            isTransfer -> "Ditransfer ($bankName)"
            note.isNotBlank() -> "Dibayarin hal lain: $note"
            else -> "Dibayarin hal lain"
        }
        dbHelper.insertShieldHistory(
            ShieldHistoryItem(
                shieldId = shield.id,
                transactionId = incomeTxId,
                friendName = if (!updatedFriendName.isNullOrBlank()) updatedFriendName.trim() else shield.friendName,
                amount = -deduction,
                note = historyNote,
                timestamp = System.currentTimeMillis()
            )
        )

        val finalFriendName = if (!updatedFriendName.isNullOrBlank()) updatedFriendName.trim() else shield.friendName
        val finalNote = if (updatedNote != null) updatedNote.trim() else shield.note
        val updatedShield = shield.copy(
            friendName = finalFriendName,
            amount = if (isFullySettled) shield.amount else remainingAmount,
            note = finalNote,
            isSettled = if (isFullySettled) true else shield.isSettled,
            settledAt = if (isFullySettled) System.currentTimeMillis() else shield.settledAt,
            settledBankId = if (isFullySettled && isTransfer) bankId else shield.settledBankId,
            settledBankName = if (isFullySettled && isTransfer) bankName else shield.settledBankName
        )
        val ok = dbHelper.updateShield(updatedShield)
        loadShields()
        ok
    }

    suspend fun deleteShield(id: Long): Boolean = withContext(Dispatchers.IO) {
        val ok = dbHelper.deleteShield(id)
        loadShields()
        ok
    }

    suspend fun settleShield(
        shieldId: Long,
        bankId: Long?,
        bankName: String?,
        recordIncome: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        val shield = _shields.value.find { it.id == shieldId }
        val ok = dbHelper.settleShield(shieldId, bankId, bankName)
        if (ok) {
            if (recordIncome && bankId != null && shield != null) {
                val tx = Transaction(
                    bankId = bankId,
                    type = Transaction.TYPE_INCOME,
                    amount = shield.amount,
                    category = "Reimburse",
                    note = "Pelunasan talangan (Shield): ${shield.friendName}",
                    timestamp = System.currentTimeMillis(),
                    destination = shield.friendName
                )
                dbHelper.insertTransaction(tx)
                loadBanks()
                loadAllTransactions()
                loadFilteredTransactions()
            }
            loadShields()
        }
        ok
    }

    suspend fun unsettleShield(shieldId: Long): Boolean = withContext(Dispatchers.IO) {
        val ok = dbHelper.unsettleShield(shieldId)
        loadShields()
        ok
    }

    private fun isSameBankFamily(bank1: String?, bank2: String?): Boolean {
        if (bank1.isNullOrBlank() || bank2.isNullOrBlank()) return false
        val b1 = bank1.lowercase().trim()
        val b2 = bank2.lowercase().trim()
        if (b1 == b2) return true
        if ((b1.contains("gopay") || b1.contains("gojek")) && (b2.contains("gopay") || b2.contains("gojek"))) return true
        if (b1.contains("shopee") && b2.contains("shopee")) return true
        if (b1.contains("ovo") && b2.contains("ovo")) return true
        if (b1.contains("dana") && b2.contains("dana")) return true
        if ((b1.contains("bca") || b1.contains("mybca")) && (b2.contains("bca") || b2.contains("mybca"))) return true
        return false
    }

    companion object {
        @Volatile
        private var instance: MoneyRepository? = null

        fun getInstance(context: Context): MoneyRepository {
            return instance ?: synchronized(this) {
                instance ?: MoneyRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
