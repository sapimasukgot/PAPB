package com.dave.moneymanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.ShieldTransactionStatus
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionCategory
import com.dave.moneymanager.data.model.TransactionEditHistory
import com.dave.moneymanager.ui.theme.Amber500
import com.dave.moneymanager.ui.theme.Amber600
import com.dave.moneymanager.ui.theme.Blue500
import com.dave.moneymanager.ui.theme.Blue600
import com.dave.moneymanager.ui.theme.Cyan500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Emerald600
import com.dave.moneymanager.ui.theme.Indigo500
import com.dave.moneymanager.ui.theme.Ochre500
import com.dave.moneymanager.ui.theme.Purple500
import com.dave.moneymanager.ui.theme.Rose500
import com.dave.moneymanager.ui.theme.Slate600
import com.dave.moneymanager.ui.theme.Slate700
import com.dave.moneymanager.ui.theme.Teal500
import com.dave.moneymanager.ui.theme.Terracotta500
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    shieldStatus: ShieldTransactionStatus = ShieldTransactionStatus.NONE,
    shieldAmount: Double = 0.0,
    netExpenseAmount: Double = transaction.amount,
    editHistories: List<TransactionEditHistory> = emptyList(),
    expenseCategories: List<TransactionCategory> = emptyList(),
    incomeCategories: List<TransactionCategory> = emptyList(),
    onUpdateCategory: ((Transaction, String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showHistoryDialog by remember { mutableStateOf(false) }

    if (showHistoryDialog) {
        TransactionEditHistoryDialog(
            transaction = transaction,
            editHistories = editHistories,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            onUpdateCategory = { newCategory ->
                onUpdateCategory?.invoke(transaction, newCategory)
            },
            onEditTransaction = {
                showHistoryDialog = false
                onEdit?.invoke() ?: onClick?.invoke()
            },
            shieldStatus = shieldStatus,
            onDismiss = { showHistoryDialog = false }
        )
    }

    val isShieldTx = shieldStatus != ShieldTransactionStatus.NONE ||
        transaction.category.equals("Shield", ignoreCase = true) ||
        transaction.note.contains("Ditalangi Shield", ignoreCase = true) ||
        transaction.note.contains("Shielded", ignoreCase = true)

    val (defaultIcon, defaultColor) = getCategoryVisuals(transaction)
    val icon = if (transaction.category.equals("Shield", ignoreCase = true)) Icons.Default.Shield else defaultIcon
    val color = if (transaction.category.equals("Shield", ignoreCase = true)) Emerald500 else defaultColor
    val displayCategory = if (transaction.category.isNotBlank() && !transaction.category.equals("Shield", ignoreCase = true)) {
        transaction.category
    } else if (isShieldTx) {
        "Shield"
    } else {
        transaction.category
    }

    val isIncome = transaction.type == Transaction.TYPE_INCOME
    val isExpense = transaction.type == Transaction.TYPE_EXPENSE
    val hasShieldDeduction = isExpense && shieldAmount > 0
    val displayAmount = if (hasShieldDeduction) netExpenseAmount else transaction.amount
    val isZeroPersonal = isExpense && hasShieldDeduction && displayAmount == 0.0

    val prefix = when {
        isIncome -> "+ "
        isExpense -> "- "
        else -> "⇄ "
    }
    val amountColor = when {
        isIncome -> Emerald500
        isZeroPersonal -> Emerald500
        isExpense -> Rose500
        else -> Blue500
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { onClick?.invoke() ?: onEdit?.invoke() },
                onLongClick = { onLongClick?.invoke() ?: onDelete() }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.09f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = displayCategory,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Row 1: Category Title (left) & Amount (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayCategory,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isZeroPersonal) "Personal: Rp 0" else prefix + formatRupiah(displayAmount),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = amountColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Row 2: Destination / Note
                val effectiveSenderOrDest = when {
                    !transaction.destination.isNullOrBlank() -> transaction.destination.trim()
                    transaction.note.startsWith("From: ", ignoreCase = true) -> transaction.note.substring(6).trim()
                    transaction.note.startsWith("Dari: ", ignoreCase = true) -> transaction.note.substring(6).trim()
                    transaction.note.startsWith("To: ", ignoreCase = true) -> transaction.note.substring(4).trim()
                    transaction.note.startsWith("Tujuan: ", ignoreCase = true) -> transaction.note.substring(8).trim()
                    else -> null
                }

                val rawNote = transaction.note.trim()
                val isSenderOrDestPrefix = rawNote.startsWith("Tujuan: ", ignoreCase = true) ||
                    rawNote.startsWith("Dari: ", ignoreCase = true) ||
                    rawNote.startsWith("To: ", ignoreCase = true) ||
                    rawNote.startsWith("From: ", ignoreCase = true)

                val subtitleText = when {
                    !effectiveSenderOrDest.isNullOrEmpty() && rawNote.isNotEmpty() && !isSenderOrDestPrefix ->
                        "${if (isIncome) "From" else "To"}: $effectiveSenderOrDest • $rawNote"
                    !effectiveSenderOrDest.isNullOrEmpty() ->
                        "${if (isIncome) "From" else "To"}: $effectiveSenderOrDest"
                    rawNote.isNotEmpty() && !isSenderOrDestPrefix ->
                        rawNote
                    else -> null
                }

                if (subtitleText != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (hasShieldDeduction) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Emerald500.copy(alpha = 0.10f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = Emerald600,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val shieldSubtitle = if (displayAmount == 0.0) {
                            "Paid: ${formatRupiah(transaction.amount)} (100% Shield)"
                        } else {
                            "Paid: ${formatRupiah(transaction.amount)} • Shield: ${formatRupiah(shieldAmount)}"
                        }
                        Text(
                            text = shieldSubtitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Emerald600,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Row 3: Wallet Pill + Timestamp + Compact Status Badges (left) & Action Icons (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            val bankLabel = if (transaction.type == Transaction.TYPE_TRANSFER && transaction.targetBankName != null) {
                                "${transaction.bankName} ➔ ${transaction.targetBankName}"
                            } else {
                                transaction.bankName
                            }
                            Text(
                                text = bankLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = formatTime(transaction.timestamp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            softWrap = false
                        )

                        if (transaction.isAutoScraped) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Amber500.copy(alpha = 0.16f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Auto",
                                        tint = Amber500,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = transaction.sourceApp ?: "Auto",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Amber500,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        if (shieldStatus == ShieldTransactionStatus.SETTLED) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Emerald500.copy(alpha = 0.14f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Settled",
                                        tint = Emerald600,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Settled",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Emerald600,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        } else if (shieldStatus == ShieldTransactionStatus.UNSETTLED || isShieldTx) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Amber500.copy(alpha = 0.14f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Pending Shield",
                                        tint = Amber600,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Pending",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Amber600,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        if (editHistories.isNotEmpty()) {
                            val latest = editHistories.first()
                            val diff = latest.diffAmount
                            val diffSign = if (diff > 0) "+" else if (diff < 0) "-" else ""
                            val diffText = if (diff != 0.0) {
                                "$diffSign${formatRupiah(abs(diff))}"
                            } else {
                                "${editHistories.size}x"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Blue500.copy(alpha = 0.14f))
                                    .border(1.dp, Blue500.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .clickable { showHistoryDialog = true }
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Edit History",
                                        tint = Blue600,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = diffText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Blue600,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (onEdit != null || onClick != null) {
                            IconButton(
                                onClick = { onEdit?.invoke() ?: onClick?.invoke() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onLongClick?.invoke() ?: onDelete() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Rose500.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryVisuals(tx: Transaction): Pair<ImageVector, Color> {
    if (tx.type == Transaction.TYPE_TRANSFER) {
        return Pair(Icons.Default.SyncAlt, Blue500)
    }

    val catLower = tx.category.lowercase().trim()
    return when {
        catLower.contains("shield") || catLower.contains("talangan") ->
            Pair(Icons.Default.Shield, Emerald600)
        catLower.contains("debt") || catLower.contains("hutang") || catLower.contains("utang") ->
            Pair(Icons.Default.Handshake, Amber500)
        catLower.contains("church") || catLower.contains("gereja") || catLower.contains("ibadah") ->
            Pair(Icons.Default.Church, Purple500)
        catLower.contains("with her") || catLower.contains("love") || catLower.contains("pacar") ||
            catLower.contains("doi") || catLower.contains("kencan") || catLower.contains("date") ->
            Pair(Icons.Default.Favorite, Color(0xFFBE185D))
        catLower.contains("coffee") || catLower.contains("kopi") ->
            Pair(Icons.Default.LocalCafe, Terracotta500)
        catLower.contains("food") || catLower.contains("beverage") || catLower.contains("makan") ||
            catLower.contains("minum") || catLower.contains("cafe") || catLower.contains("dapur") || catLower.contains("dapoer") ->
            Pair(Icons.Default.Fastfood, Terracotta500)
        catLower.contains("shopping") || catLower.contains("groceries") || catLower.contains("belanja") ||
            catLower.contains("mart") || catLower.contains("shopee") || catLower.contains("tokopedia") ->
            Pair(Icons.Default.ShoppingBag, Ochre500)
        catLower.contains("bensin") || catLower.contains("fuel") || catLower.contains("spbu") ->
            Pair(Icons.Default.LocalGasStation, Ochre500)
        catLower.contains("transport") || catLower.contains("ojek") ||
            catLower.contains("gocar") || catLower.contains("goride") || catLower.contains("grab") ->
            Pair(Icons.Default.DirectionsCar, Blue500)
        catLower.contains("game") || catLower.contains("gaming") || catLower.contains("steam") ->
            Pair(Icons.Default.SportsEsports, Indigo500)
        catLower.contains("music") || catLower.contains("musik") ->
            Pair(Icons.Default.MusicNote, Purple500)
        catLower.contains("gadget") || catLower.contains("tech") || catLower.contains("elektronik") ->
            Pair(Icons.Default.Devices, Cyan500)
        catLower.contains("tabung") || catLower.contains("saving") ->
            Pair(Icons.Default.Savings, Emerald600)
        catLower.contains("bill") || catLower.contains("utilities") || catLower.contains("tagihan") ||
            catLower.contains("pulsa") || catLower.contains("pln") || catLower.contains("listrik") || catLower.contains("pdam") ->
            Pair(Icons.Default.Receipt, Slate600)
        catLower.contains("entertainment") || catLower.contains("hiburan") || catLower.contains("movie") ||
            catLower.contains("bioskop") || catLower.contains("nonton") ->
            Pair(Icons.Default.Movie, Indigo500)
        catLower.contains("health") || catLower.contains("kesehatan") || catLower.contains("obat") ||
            catLower.contains("apotek") || catLower.contains("dokter") || catLower.contains("hospital") || catLower.contains("klinik") ->
            Pair(Icons.Default.LocalHospital, Teal500)
        catLower.contains("education") || catLower.contains("pendidikan") || catLower.contains("kuliah") ||
            catLower.contains("sekolah") || catLower.contains("buku") || catLower.contains("kursus") ->
            Pair(Icons.Default.School, Indigo500)
        catLower.contains("reimburse") || catLower.contains("reimbursement") ||
            catLower.contains("transfer in") || catLower.contains("incoming transfer") ->
            Pair(Icons.Default.PriceCheck, Blue500)
        catLower.contains("salary") || catLower.contains("wages") || catLower.contains("gaji") || catLower.contains("upah") ->
            Pair(Icons.Default.Payments, Emerald600)
        catLower.contains("bonus") || catLower.contains("allowance") || catLower.contains("thr") || catLower.contains("tunjangan") ->
            Pair(Icons.Default.CardGiftcard, Amber500)
        catLower.contains("topup") || catLower.contains("top up") || catLower.contains("deposit") ->
            Pair(Icons.Default.AccountBalanceWallet, Blue500)
        catLower.contains("cashback") || catLower.contains("reward") || catLower.contains("hadiah") ->
            Pair(Icons.Default.LocalAtm, Emerald600)
        catLower.contains("investment") || catLower.contains("investasi") || catLower.contains("saham") ||
            catLower.contains("reksadana") || catLower.contains("crypto") ->
            Pair(Icons.Default.TrendingUp, Cyan500)
        catLower.contains("travel") || catLower.contains("liburan") || catLower.contains("trip") || catLower.contains("hotel") ->
            Pair(Icons.Default.Flight, Cyan500)
        catLower.contains("fitness") || catLower.contains("gym") || catLower.contains("olahraga") || catLower.contains("sport") ->
            Pair(Icons.Default.FitnessCenter, Teal500)
        catLower.contains("pet") || catLower.contains("hewan") || catLower.contains("kucing") || catLower.contains("anjing") ->
            Pair(Icons.Default.Pets, Ochre500)
        catLower.contains("home") || catLower.contains("rumah") || catLower.contains("kost") || catLower.contains("kosan") ->
            Pair(Icons.Default.Home, Slate600)
        catLower.contains("work") || catLower.contains("kerja") || catLower.contains("freelance") ->
            Pair(Icons.Default.Work, Slate600)
        catLower.contains("family") || catLower.contains("keluarga") ->
            Pair(Icons.Default.FamilyRestroom, Purple500)
        catLower.contains("donation") || catLower.contains("charity") || catLower.contains("amal") || catLower.contains("sedekah") || catLower.contains("zakat") ->
            Pair(Icons.Default.VolunteerActivism, Emerald600)
        catLower.contains("other") || catLower.contains("lain") ->
            Pair(Icons.Default.Category, Slate600)
        else ->
            Pair(if (tx.type == Transaction.TYPE_INCOME) Icons.Default.LocalAtm else Icons.Default.Receipt, Slate700)
    }
}

private fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.US)
    format.maximumFractionDigits = 0
    return "Rp " + format.format(amount)
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    return sdf.format(Date(timestamp))
}
