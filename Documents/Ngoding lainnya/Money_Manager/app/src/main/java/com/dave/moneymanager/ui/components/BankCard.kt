package com.dave.moneymanager.ui.components

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
import androidx.compose.material.icons.filled.CreditCard
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.Bank
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
            baseColor.copy(alpha = 0.75f),
            Color(0xFF0F172A).copy(alpha = 0.9f)
        )
    )

    Card(
        modifier = modifier
            .width(260.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp)
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
                    Column {
                        Text(
                            text = bank.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = when (bank.type) {
                                Bank.TYPE_EWALLET -> "E-Wallet"
                                Bank.TYPE_CASH -> "Cash"
                                Bank.TYPE_BANK -> "Bank Account"
                                else -> "Financial Account"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (bank.accountNumber.isNotEmpty()) {
                    Text(
                        text = formatAccountNumber(bank.accountNumber),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.2.sp
                        )
                    )
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Column {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    )
                    Text(
                        text = if (isBalanceHidden) "Rp ••••••••" else formatRupiah(bank.balance),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
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
