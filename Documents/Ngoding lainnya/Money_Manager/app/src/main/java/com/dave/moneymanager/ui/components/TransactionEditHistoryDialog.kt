package com.dave.moneymanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.ShieldTransactionStatus
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionCategory
import com.dave.moneymanager.data.model.TransactionEditHistory
import com.dave.moneymanager.ui.theme.Blue500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Emerald600
import com.dave.moneymanager.ui.theme.Rose500
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionEditHistoryDialog(
    transaction: Transaction,
    editHistories: List<TransactionEditHistory>,
    expenseCategories: List<TransactionCategory> = emptyList(),
    incomeCategories: List<TransactionCategory> = emptyList(),
    onUpdateCategory: ((newCategory: String) -> Unit)? = null,
    onEditTransaction: (() -> Unit)? = null,
    shieldStatus: ShieldTransactionStatus = ShieldTransactionStatus.NONE,
    onDismiss: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.US)
    var currentCategory by remember(transaction.category) { mutableStateOf(transaction.category) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    if (showCategoryPicker) {
        val categories = if (transaction.type == Transaction.TYPE_EXPENSE) {
            expenseCategories.ifEmpty { TransactionCategory.defaultExpenseCategories }
        } else {
            incomeCategories.ifEmpty { TransactionCategory.defaultIncomeCategories }
        }
        val typeLabel = if (transaction.type == Transaction.TYPE_EXPENSE) "Expense" else "Income"

        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Change $typeLabel Category",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Select a new category for this transaction:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = currentCategory.equals(cat.name, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) Color(cat.colorHex) else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        currentCategory = cat.name
                                        onUpdateCategory?.invoke(cat.name)
                                        showCategoryPicker = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Blue500.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Blue500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Edit History",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$currentCategory • Rp ${formatter.format(transaction.amount)}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                val showCategoryAction = onUpdateCategory != null && transaction.type != Transaction.TYPE_TRANSFER
                val showShieldAction = transaction.type == Transaction.TYPE_EXPENSE && onEditTransaction != null
                if (showCategoryAction || showShieldAction) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showCategoryAction) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Blue500.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Blue500.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable { showCategoryPicker = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Change Category",
                                        tint = Blue500,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Change Category",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Blue500,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        if (showShieldAction) {
                            val isShield = shieldStatus != ShieldTransactionStatus.NONE ||
                                    transaction.note.contains("Ditalangi Shield", ignoreCase = true) ||
                                    transaction.note.contains("Shield", ignoreCase = true) ||
                                    transaction.category.equals("Shield", ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Emerald500.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    onDismiss()
                                    onEditTransaction()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = if (isShield) "Active Shield" else "Convert to Shield",
                                        tint = Emerald600,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isShield) "Shield Active" else "+ Add Shield",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Emerald600,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        text = {
            if (editHistories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No edit history recorded for this transaction yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(editHistories) { edit ->
                        val hasDiff = edit.diffAmount != 0.0
                        val isPositive = edit.diffAmount > 0
                        val diffColor = if (isPositive) Emerald500 else Rose500

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sdf.format(Date(edit.editTimestamp)),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )

                                    if (hasDiff) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(diffColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                    contentDescription = null,
                                                    tint = diffColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                val sign = if (isPositive) "+" else "-"
                                                Text(
                                                    text = "$sign Rp ${formatter.format(Math.abs(edit.diffAmount))}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = diffColor,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                if (hasDiff) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Before: Rp ${formatter.format(edit.oldAmount)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        )
                                        Text(
                                            text = "After: Rp ${formatter.format(edit.newAmount)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                val oldCat = edit.oldCategory
                                val newCat = edit.newCategory
                                val hasCatDiff = !oldCat.isNullOrBlank() &&
                                        !newCat.isNullOrBlank() &&
                                        !oldCat.equals(newCat, ignoreCase = true)
                                if (hasCatDiff) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Blue500.copy(alpha = 0.08f))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Category,
                                            contentDescription = null,
                                            tint = Blue500,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Category: ",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(
                                            text = oldCat,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(
                                            text = " ➔ ",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Blue500,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(
                                            text = newCat,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Blue500,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                if (edit.changeSummary.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = edit.changeSummary,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        dismissButton = if (onEditTransaction != null) {
            {
                val isExp = transaction.type == Transaction.TYPE_EXPENSE
                TextButton(onClick = {
                    onDismiss()
                    onEditTransaction()
                }) {
                    Icon(
                        imageVector = if (isExp) Icons.Default.Shield else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isExp) Emerald500 else Blue500
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExp) "Edit / Split Bill" else "Edit Transaction",
                        color = if (isExp) Emerald500 else Blue500,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        } else null,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}
