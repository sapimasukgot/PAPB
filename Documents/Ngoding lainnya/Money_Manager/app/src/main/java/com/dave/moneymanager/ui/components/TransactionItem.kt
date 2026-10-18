package com.dave.moneymanager.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.ShieldTransactionStatus
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionCategory
import com.dave.moneymanager.data.model.TransactionEditHistory
import com.dave.moneymanager.ui.theme.Amber500
import com.dave.moneymanager.ui.theme.Blue500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import com.dave.moneymanager.ui.theme.Slate700
import com.dave.moneymanager.ui.theme.Slate800
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.text.style.TextOverflow
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { onClick?.invoke() ?: onEdit?.invoke() },
                onLongClick = { onLongClick?.invoke() ?: onDelete() }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {

                val isShieldTx = shieldStatus != ShieldTransactionStatus.NONE ||
                        transaction.category.equals("Shield", ignoreCase = true) ||
                        transaction.note.contains("Ditalangi Shield", ignoreCase = true)

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

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = displayCategory,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = displayCategory,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (transaction.isAutoScraped) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Amber500.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Auto",
                                        tint = Amber500,
                                        modifier = Modifier.size(11.dp)
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
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Emerald500.copy(alpha = 0.15f))
                                    .border(1.dp, Emerald500.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Shield Lunas",
                                        tint = Emerald500,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Shield Lunas ✓",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Emerald500,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        } else if (shieldStatus == ShieldTransactionStatus.UNSETTLED || isShieldTx) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Amber500.copy(alpha = 0.15f))
                                    .border(1.dp, Amber500.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Shield Belum Lunas",
                                        tint = Amber500,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Shield",
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

                        if (editHistories.isNotEmpty()) {
                            val latest = editHistories.first()
                            val diff = latest.diffAmount
                            val diffSign = if (diff > 0) "+" else if (diff < 0) "-" else ""
                            val diffText = if (diff != 0.0) {
                                "$diffSign${formatRupiah(abs(diff))}"
                            } else "${editHistories.size}x"

                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Blue500.copy(alpha = 0.15f))
                                    .border(1.dp, Blue500.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .clickable { showHistoryDialog = true }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Histori Edit",
                                        tint = Blue500,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Diedit: $diffText",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Blue500,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    val effectiveSenderOrDest = when {
                        !transaction.destination.isNullOrBlank() -> transaction.destination.trim()
                        transaction.note.startsWith("From: ", ignoreCase = true) -> transaction.note.substring(6).trim()
                        transaction.note.startsWith("Dari: ", ignoreCase = true) -> transaction.note.substring(6).trim()
                        transaction.note.startsWith("To: ", ignoreCase = true) -> transaction.note.substring(4).trim()
                        transaction.note.startsWith("Tujuan: ", ignoreCase = true) -> transaction.note.substring(8).trim()
                        else -> null
                    }

                    if (!effectiveSenderOrDest.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        val isInc = transaction.type == Transaction.TYPE_INCOME
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background((if (isInc) Emerald500 else Rose500).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = (if (isInc) "📥 From: " else "🎯 To: ") + effectiveSenderOrDest,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isInc) Emerald500 else Rose500
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val rawNote = transaction.note.trim()
                    val isSenderOrDestPrefix = rawNote.startsWith("Tujuan: ", ignoreCase = true) ||
                            rawNote.startsWith("Dari: ", ignoreCase = true) ||
                            rawNote.startsWith("To: ", ignoreCase = true) ||
                            rawNote.startsWith("From: ", ignoreCase = true)

                    if (rawNote.isNotEmpty() && !isSenderOrDestPrefix) {
                        Text(
                            text = rawNote,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
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
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = formatTime(transaction.timestamp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val isIncome = transaction.type == Transaction.TYPE_INCOME
                    val isExpense = transaction.type == Transaction.TYPE_EXPENSE
                    val hasShieldDeduction = isExpense && shieldAmount > 0
                    val displayAmount = if (hasShieldDeduction) netExpenseAmount else transaction.amount
                    val isZeroPersonal = isExpense && hasShieldDeduction && displayAmount == 0.0

                    val prefix = when {
                        isIncome -> "+ "
                        isExpense && isZeroPersonal -> "- "
                        isExpense -> "- "
                        else -> "⇄ "
                    }
                    val amountColor = when {
                        isIncome -> Emerald500
                        isZeroPersonal -> Emerald500
                        isExpense -> Rose500
                        else -> Blue500
                    }

                    Text(
                        text = if (isZeroPersonal) "- Rp 0 (Pribadi)" else prefix + formatRupiah(displayAmount),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = amountColor,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        softWrap = false
                    )

                    if (hasShieldDeduction) {
                        Spacer(modifier = Modifier.height(1.dp))
                        val shieldSubtitle = if (displayAmount == 0.0) {
                            "Kasir: ${formatRupiah(transaction.amount)} (100% Shield)"
                        } else {
                            "Kasir: ${formatRupiah(transaction.amount)} • Shield: ${formatRupiah(shieldAmount)}"
                        }
                        Text(
                            text = shieldSubtitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    if (shieldStatus == ShieldTransactionStatus.SETTLED) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Lunas",
                                tint = Emerald500,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Lunas ✓",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Emerald500,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else if (shieldStatus == ShieldTransactionStatus.UNSETTLED) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Belum Lunas",
                                tint = Amber500,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Belum Lunas",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Amber500,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                if (onEdit != null || onClick != null) {
                    IconButton(
                        onClick = { onEdit?.invoke() ?: onClick?.invoke() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Blue500.copy(alpha = 0.85f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onLongClick?.invoke() ?: onDelete() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
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
        catLower.contains("shield") ->
            Pair(Icons.Default.Shield, Emerald500)
        catLower.contains("church") || catLower.contains("gereja") || catLower.contains("ibadah") ->
            Pair(Icons.Default.Church, Color(0xFF8B5CF6))
        catLower.contains("with her") || catLower.contains("love") || catLower.contains("pacar") ||
                catLower.contains("doi") || catLower.contains("kencan") || catLower.contains("date") ->
            Pair(Icons.Default.Favorite, Color(0xFFEC4899))
        catLower.contains("food") || catLower.contains("beverage") || catLower.contains("makan") ||
                catLower.contains("minum") || catLower.contains("cafe") || catLower.contains("coffee") ->
            Pair(Icons.Default.Fastfood, Amber500)
        catLower.contains("shopping") || catLower.contains("groceries") || catLower.contains("belanja") ||
                catLower.contains("mart") || catLower.contains("shopee") || catLower.contains("tokopedia") ->
            Pair(Icons.Default.ShoppingBag, Color(0xFFEC4899))
        catLower.contains("transport") || catLower.contains("ojek") || catLower.contains("bensin") ||
                catLower.contains("fuel") || catLower.contains("gocar") || catLower.contains("goride") || catLower.contains("grab") ->
            Pair(Icons.Default.DirectionsCar, Blue500)
        catLower.contains("bill") || catLower.contains("utilities") || catLower.contains("tagihan") ||
                catLower.contains("pulsa") || catLower.contains("pln") || catLower.contains("listrik") || catLower.contains("pdam") ->
            Pair(Icons.Default.Receipt, Color(0xFF8B5CF6))
        catLower.contains("entertainment") || catLower.contains("hiburan") || catLower.contains("movie") ||
                catLower.contains("bioskop") || catLower.contains("game") || catLower.contains("steam") || catLower.contains("nonton") ->
            Pair(Icons.Default.Movie, Color(0xFF06B6D4))
        catLower.contains("health") || catLower.contains("kesehatan") || catLower.contains("obat") ||
                catLower.contains("apotek") || catLower.contains("dokter") || catLower.contains("hospital") || catLower.contains("klinik") ->
            Pair(Icons.Default.LocalHospital, Color(0xFFEF4444))
        catLower.contains("education") || catLower.contains("pendidikan") || catLower.contains("kuliah") ||
                catLower.contains("sekolah") || catLower.contains("buku") || catLower.contains("kursus") ->
            Pair(Icons.Default.School, Color(0xFF10B981))
        catLower.contains("reimburse") || catLower.contains("reimbursement") ||
                catLower.contains("transfer in") || catLower.contains("incoming transfer") ->
            Pair(Icons.Default.PriceCheck, Blue500)
        catLower.contains("salary") || catLower.contains("wages") || catLower.contains("gaji") || catLower.contains("upah") ->
            Pair(Icons.Default.Payments, Emerald500)
        catLower.contains("bonus") || catLower.contains("allowance") || catLower.contains("thr") || catLower.contains("tunjangan") ->
            Pair(Icons.Default.CardGiftcard, Amber500)
        catLower.contains("topup") || catLower.contains("top up") || catLower.contains("deposit") ->
            Pair(Icons.Default.AccountBalanceWallet, Color(0xFF8B5CF6))
        catLower.contains("cashback") || catLower.contains("reward") || catLower.contains("hadiah") ->
            Pair(Icons.Default.LocalAtm, Color(0xFFEC4899))
        catLower.contains("investment") || catLower.contains("investasi") || catLower.contains("saham") ||
                catLower.contains("reksadana") || catLower.contains("crypto") ->
            Pair(Icons.Default.TrendingUp, Color(0xFF06B6D4))
        catLower.contains("travel") || catLower.contains("liburan") || catLower.contains("trip") || catLower.contains("hotel") ->
            Pair(Icons.Default.Flight, Color(0xFF14B8A6))
        catLower.contains("fitness") || catLower.contains("gym") || catLower.contains("olahraga") || catLower.contains("sport") ->
            Pair(Icons.Default.FitnessCenter, Color(0xFFF97316))
        catLower.contains("pet") || catLower.contains("hewan") || catLower.contains("kucing") || catLower.contains("anjing") ->
            Pair(Icons.Default.Pets, Color(0xFFF59E0B))
        catLower.contains("home") || catLower.contains("rumah") || catLower.contains("kost") || catLower.contains("kosan") ->
            Pair(Icons.Default.Home, Color(0xFF6366F1))
        catLower.contains("work") || catLower.contains("kerja") || catLower.contains("freelance") ->
            Pair(Icons.Default.Work, Color(0xFF64748B))
        catLower.contains("family") || catLower.contains("keluarga") ->
            Pair(Icons.Default.FamilyRestroom, Color(0xFF14B8A6))
        catLower.contains("donation") || catLower.contains("charity") || catLower.contains("amal") || catLower.contains("sedekah") || catLower.contains("zakat") ->
            Pair(Icons.Default.VolunteerActivism, Emerald500)
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
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
    return sdf.format(Date(timestamp))
}
