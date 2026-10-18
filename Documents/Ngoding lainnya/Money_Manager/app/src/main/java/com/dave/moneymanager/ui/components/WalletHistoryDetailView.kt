package com.dave.moneymanager.ui.components

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.Bank
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionCategory
import com.dave.moneymanager.data.model.TransactionEditHistory
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import com.dave.moneymanager.ui.viewmodel.MoneyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class WalletFlowFilter(val label: String) {
    ALL("Semua"),
    OUTFLOW("Dari Wallet (Keluar)"),
    INFLOW("Ke Wallet (Masuk)")
}

enum class WalletTimeFilterMode(val label: String) {
    THIS_MONTH("Bulan Ini"),
    TODAY("Hari Ini"),
    THIS_WEEK("Minggu Ini"),
    SPECIFIC_DATE("Pilih Hari"),
    CUSTOM_RANGE("Rentang Kustom"),
    ALL("Semua Waktu")
}

data class WalletPeriodMetrics(
    val inflow: Double,
    val outflow: Double,
    val net: Double,
    val inflowCount: Int,
    val outflowCount: Int,
    val totalCount: Int
)

fun matchesBank(bankId: Long?, bankName: String?, wallet: Bank): Boolean {
    if (bankId != null && bankId != 0L && bankId == wallet.id) return true
    if (wallet.id > 0 && bankId == wallet.id) return true
    if (!bankName.isNullOrBlank() && bankName.equals(wallet.name, ignoreCase = true)) return true
    return false
}

fun isTxOutflowFromWallet(tx: Transaction, wallet: Bank): Boolean {
    val isSource = matchesBank(tx.bankId, tx.bankName, wallet)
    return isSource && (tx.type == Transaction.TYPE_EXPENSE || tx.type == Transaction.TYPE_TRANSFER)
}

fun isTxInflowToWallet(tx: Transaction, wallet: Bank): Boolean {
    val isSource = matchesBank(tx.bankId, tx.bankName, wallet)
    val isTarget = matchesBank(tx.targetBankId, tx.targetBankName, wallet)
    return (isSource && tx.type == Transaction.TYPE_INCOME) || (isTarget && tx.type == Transaction.TYPE_TRANSFER)
}

fun isTxConnectedToWallet(tx: Transaction, wallet: Bank): Boolean {
    return isTxOutflowFromWallet(tx, wallet) || isTxInflowToWallet(tx, wallet)
}

@Composable
fun WalletHistoryDetailView(
    wallet: Bank,
    allTransactions: List<Transaction>,
    viewModel: MoneyViewModel,
    expenseCategories: List<TransactionCategory>,
    incomeCategories: List<TransactionCategory>,
    transactionEditHistories: Map<Long, List<TransactionEditHistory>>,
    onBack: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var timeMode by remember { mutableStateOf(WalletTimeFilterMode.THIS_MONTH) }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var customRangeStart by remember { mutableLongStateOf(System.currentTimeMillis() - 7L * 24 * 3600 * 1000) }
    var customRangeEnd by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var flowFilter by remember { mutableStateOf(WalletFlowFilter.ALL) }

    val (startTimeMillis, endTimeMillis) = remember(
        timeMode,
        selectedYear,
        selectedMonth,
        selectedDateMillis,
        customRangeStart,
        customRangeEnd
    ) {
        val cal = Calendar.getInstance()
        when (timeMode) {
            WalletTimeFilterMode.ALL -> Pair(null, null)
            WalletTimeFilterMode.TODAY -> {
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
            WalletTimeFilterMode.THIS_WEEK -> {
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
            WalletTimeFilterMode.THIS_MONTH -> {
                cal.set(Calendar.YEAR, selectedYear)
                cal.set(Calendar.MONTH, selectedMonth)
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
            WalletTimeFilterMode.SPECIFIC_DATE -> {
                cal.timeInMillis = selectedDateMillis
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
            WalletTimeFilterMode.CUSTOM_RANGE -> {
                val sCal = Calendar.getInstance().apply {
                    timeInMillis = customRangeStart
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val eCal = Calendar.getInstance().apply {
                    timeInMillis = customRangeEnd
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                Pair(sCal.timeInMillis, eCal.timeInMillis)
            }
        }
    }

    val metrics = remember(
        allTransactions,
        wallet,
        startTimeMillis,
        endTimeMillis
    ) {
        var inSum = 0.0
        var outSum = 0.0
        var inCount = 0
        var outCount = 0

        for (tx in allTransactions) {
            val withinTime = if (startTimeMillis != null && endTimeMillis != null) {
                tx.timestamp in startTimeMillis..endTimeMillis
            } else {
                true
            }
            if (!withinTime) continue

            if (isTxInflowToWallet(tx, wallet)) {
                inSum += tx.amount
                inCount++
            }
            if (isTxOutflowFromWallet(tx, wallet)) {
                outSum += tx.amount
                outCount++
            }
        }
        val total = inCount + outCount
        WalletPeriodMetrics(
            inflow = inSum,
            outflow = outSum,
            net = inSum - outSum,
            inflowCount = inCount,
            outflowCount = outCount,
            totalCount = total
        )
    }

    val filteredTransactions = remember(
        allTransactions,
        wallet,
        startTimeMillis,
        endTimeMillis,
        flowFilter
    ) {
        allTransactions.filter { tx ->
            val withinTime = if (startTimeMillis != null && endTimeMillis != null) {
                tx.timestamp in startTimeMillis..endTimeMillis
            } else {
                true
            }
            if (!withinTime) return@filter false

            when (flowFilter) {
                WalletFlowFilter.ALL -> isTxConnectedToWallet(tx, wallet)
                WalletFlowFilter.OUTFLOW -> isTxOutflowFromWallet(tx, wallet)
                WalletFlowFilter.INFLOW -> isTxInflowToWallet(tx, wallet)
            }
        }
    }

    val displayDateTitle = remember(
        timeMode,
        selectedYear,
        selectedMonth,
        selectedDateMillis,
        customRangeStart,
        customRangeEnd
    ) {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val dayFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        val shortDayFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, selectedYear)
        cal.set(Calendar.MONTH, selectedMonth)

        when (timeMode) {
            WalletTimeFilterMode.THIS_MONTH -> monthFormat.format(Date(cal.timeInMillis))
            WalletTimeFilterMode.TODAY -> "Hari Ini (" + shortDayFormat.format(Date()) + ")"
            WalletTimeFilterMode.THIS_WEEK -> "Minggu Ini"
            WalletTimeFilterMode.SPECIFIC_DATE -> dayFormat.format(Date(selectedDateMillis))
            WalletTimeFilterMode.CUSTOM_RANGE -> "${shortDayFormat.format(Date(customRangeStart))} - ${shortDayFormat.format(Date(customRangeEnd))}"
            WalletTimeFilterMode.ALL -> "Semua Waktu"
        }
    }

    val onPickDate = {
        calendar.timeInMillis = selectedDateMillis
        DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                selectedYear = y
                selectedMonth = m
                selectedDateMillis = calendar.timeInMillis
                if (timeMode == WalletTimeFilterMode.THIS_MONTH) {

                } else {
                    timeMode = WalletTimeFilterMode.SPECIFIC_DATE
                }
            },
            selectedYear,
            selectedMonth,
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val onPickCustomRange = {
        calendar.timeInMillis = customRangeStart
        val startDateDialog = DatePickerDialog(
            context,
            { _, y1, m1, d1 ->
                val startCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, y1)
                    set(Calendar.MONTH, m1)
                    set(Calendar.DAY_OF_MONTH, d1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val sMillis = startCal.timeInMillis
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = customRangeEnd.coerceAtLeast(sMillis)
                }
                val endDateDialog = DatePickerDialog(
                    context,
                    { _, y2, m2, d2 ->
                        val endFinalCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, y2)
                            set(Calendar.MONTH, m2)
                            set(Calendar.DAY_OF_MONTH, d2)
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        customRangeStart = sMillis
                        customRangeEnd = endFinalCal.timeInMillis
                        timeMode = WalletTimeFilterMode.CUSTOM_RANGE
                    },
                    endCal.get(Calendar.YEAR),
                    endCal.get(Calendar.MONTH),
                    endCal.get(Calendar.DAY_OF_MONTH)
                )
                endDateDialog.setTitle("Pilih Tanggal Selesai")
                endDateDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        startDateDialog.setTitle("Pilih Tanggal Mulai")
        startDateDialog.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(wallet.colorHex).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    val walletIcon = when (wallet.type) {
                        Bank.TYPE_EWALLET -> Icons.Default.Wallet
                        Bank.TYPE_CASH -> Icons.Default.Payments
                        else -> Icons.Default.AccountBalance
                    }
                    Icon(
                        imageVector = walletIcon,
                        contentDescription = wallet.name,
                        tint = Color(wallet.colorHex),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = wallet.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val typeStr = when (wallet.type) {
                        Bank.TYPE_EWALLET -> "E-Wallet"
                        Bank.TYPE_CASH -> "Tunai / Cash"
                        Bank.TYPE_BANK -> "Rekening Bank"
                        else -> "Dompet"
                    }
                    Text(
                        text = typeStr,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Saldo Saat Ini",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    )
                    Text(
                        text = viewModel.formatRupiah(wallet.balance),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (wallet.balance < 0) Rose500 else Emerald500
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val grouped = remember(filteredTransactions) {
            val fullDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val todayFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
            val todayStr = todayFormat.format(Date())

            filteredTransactions.groupBy { tx ->
                val txDateStr = todayFormat.format(Date(tx.timestamp))
                if (txDateStr == todayStr) {
                    "Hari Ini - " + fullDateFormat.format(Date(tx.timestamp))
                } else {
                    fullDateFormat.format(Date(tx.timestamp))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ringkasan Mutasi",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = displayDateTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = Emerald500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Ke Wallet",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Emerald500,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "+" + viewModel.formatRupiah(metrics.inflow),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald500
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${metrics.inflowCount} transaksi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = Rose500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Dari Wallet",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Rose500,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "-" + viewModel.formatRupiah(metrics.outflow),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Rose500
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${metrics.outflowCount} transaksi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Arus Kas Bersih",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                val netColor = if (metrics.net >= 0) Emerald500 else Rose500
                                val netPrefix = if (metrics.net >= 0) "+" else "-"
                                Text(
                                    text = netPrefix + viewModel.formatRupiah(kotlin.math.abs(metrics.net)),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = netColor
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${metrics.totalCount} mutasi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WalletTimeFilterMode.values().forEach { mode ->
                        val isSelected = timeMode == mode
                        item {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Emerald500 else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (mode == WalletTimeFilterMode.SPECIFIC_DATE) {
                                            onPickDate()
                                        } else if (mode == WalletTimeFilterMode.CUSTOM_RANGE) {
                                            onPickCustomRange()
                                        } else {
                                            timeMode = mode
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (timeMode != WalletTimeFilterMode.ALL) {
                item {
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
                                onClick = {
                                    when (timeMode) {
                                        WalletTimeFilterMode.THIS_MONTH -> {
                                            if (selectedMonth == 0) {
                                                selectedMonth = 11
                                                selectedYear -= 1
                                            } else {
                                                selectedMonth -= 1
                                            }
                                        }
                                        WalletTimeFilterMode.SPECIFIC_DATE -> {
                                            selectedDateMillis -= 24L * 60 * 60 * 1000
                                        }
                                        WalletTimeFilterMode.TODAY -> {
                                            selectedDateMillis = System.currentTimeMillis() - 24L * 60 * 60 * 1000
                                            timeMode = WalletTimeFilterMode.SPECIFIC_DATE
                                        }
                                        WalletTimeFilterMode.THIS_WEEK -> {
                                            selectedDateMillis -= 7L * 24 * 60 * 60 * 1000
                                        }
                                        else -> {}
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Sebelumnya")
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (timeMode == WalletTimeFilterMode.CUSTOM_RANGE) {
                                            onPickCustomRange()
                                        } else {
                                            onPickDate()
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Pilih Tanggal",
                                    tint = Emerald500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = displayDateTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                )
                            }

                            IconButton(
                                onClick = {
                                    when (timeMode) {
                                        WalletTimeFilterMode.THIS_MONTH -> {
                                            if (selectedMonth == 11) {
                                                selectedMonth = 0
                                                selectedYear += 1
                                            } else {
                                                selectedMonth += 1
                                            }
                                        }
                                        WalletTimeFilterMode.SPECIFIC_DATE -> {
                                            selectedDateMillis += 24L * 60 * 60 * 1000
                                        }
                                        WalletTimeFilterMode.TODAY -> {
                                            selectedDateMillis = System.currentTimeMillis() + 24L * 60 * 60 * 1000
                                            timeMode = WalletTimeFilterMode.SPECIFIC_DATE
                                        }
                                        WalletTimeFilterMode.THIS_WEEK -> {
                                            selectedDateMillis += 7L * 24 * 60 * 60 * 1000
                                        }
                                        else -> {}
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Selanjutnya")
                            }
                        }
                    }
                }
            }

            item {
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
                            .background(if (flowFilter == WalletFlowFilter.ALL) Emerald500 else Color.Transparent)
                            .clickable { flowFilter = WalletFlowFilter.ALL }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Semua (${metrics.totalCount})",
                            fontWeight = FontWeight.Bold,
                            color = if (flowFilter == WalletFlowFilter.ALL) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (flowFilter == WalletFlowFilter.OUTFLOW) Rose500 else Color.Transparent)
                            .clickable { flowFilter = WalletFlowFilter.OUTFLOW }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Dari Wallet (${metrics.outflowCount})",
                            fontWeight = FontWeight.Bold,
                            color = if (flowFilter == WalletFlowFilter.OUTFLOW) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (flowFilter == WalletFlowFilter.INFLOW) Emerald500 else Color.Transparent)
                            .clickable { flowFilter = WalletFlowFilter.INFLOW }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ke Wallet (${metrics.inflowCount})",
                            fontWeight = FontWeight.Bold,
                            color = if (flowFilter == WalletFlowFilter.INFLOW) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Mutasi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${filteredTransactions.size} transaksi ditemukan",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
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
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tidak Ada Transaksi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val flowDesc = when (flowFilter) {
                                WalletFlowFilter.ALL -> "pengeluaran maupun pemasukan"
                                WalletFlowFilter.OUTFLOW -> "pengeluaran atau transfer keluar (dari wallet)"
                                WalletFlowFilter.INFLOW -> "pemasukan atau transfer masuk (ke wallet)"
                            }
                            Text(
                                text = "Belum ada transaksi $flowDesc untuk ${wallet.name} pada periode yang dipilih.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            } else {
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
                        TransactionItem(
                            transaction = tx,
                            shieldStatus = shieldStatus,
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
