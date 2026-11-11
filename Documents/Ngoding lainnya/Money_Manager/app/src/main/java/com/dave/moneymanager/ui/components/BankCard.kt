package com.dave.moneymanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.Bank
import com.dave.moneymanager.ui.theme.Emerald500
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BankCard(
    bank: Bank,
    isSelected: Boolean = false,
    isBalanceHidden: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseColor = Color(bank.colorHex)
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            baseColor.copy(alpha = 0.85f)
        )
    )

    Card(
        modifier = modifier
            .width(220.dp)
            .height(138.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = if (isSelected) {
            BorderStroke(2.dp, Color.White)
        } else {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bank.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (bank.type) {
                                Bank.TYPE_EWALLET -> "E-Wallet"
                                Bank.TYPE_CASH -> "Cash"
                                Bank.TYPE_BANK -> "Bank Account"
                                else -> "Account"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isSelected) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.24f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Filtered",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
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
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (bank.accountNumber.isNotEmpty()) {
                    Text(
                        text = formatAccountNumber(bank.accountNumber),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.68f),
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        )
                    )
                }

                Column {
                    Text(
                        text = "Available Balance",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = if (isBalanceHidden) "Rp ••••••••" else formatRupiah(bank.balance),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.US)
    return "Rp " + format.format(amount)
}

private fun formatAccountNumber(num: String): String {
    return if (num.length > 4) {
        "•••• " + num.takeLast(4)
    } else {
        num
    }
}
