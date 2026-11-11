package com.dave.moneymanager.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StatSummaryCard(
    totalBalance: Double,
    periodIncome: Double,
    periodExpense: Double,
    periodLabel: String = "This Month",
    isBalanceHidden: Boolean = false,
    onToggleHideBalance: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Net Balance",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onToggleHideBalance,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isBalanceHidden) "Show Balance" else "Hide Balance",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isBalanceHidden) "Rp ••••••••" else formatRupiah(totalBalance),
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Emerald500.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Income",
                            tint = Emerald500,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBalanceHidden) "Rp ••••••" else formatRupiah(periodIncome),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Emerald500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Rose500.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Expenses",
                            tint = Rose500,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBalanceHidden) "Rp ••••••" else formatRupiah(periodExpense),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Rose500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.US)
    return "Rp " + format.format(amount)
}
