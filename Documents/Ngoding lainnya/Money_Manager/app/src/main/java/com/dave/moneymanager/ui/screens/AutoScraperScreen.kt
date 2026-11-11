package com.dave.moneymanager.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dave.moneymanager.data.model.ScrapedNotificationLog
import com.dave.moneymanager.ui.theme.Blue500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import com.dave.moneymanager.ui.viewmodel.MoneyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AutoScraperScreen(viewModel: MoneyViewModel) {
    val context = LocalContext.current
    val notificationLogs by viewModel.notificationLogs.collectAsState()
    val processedLogs = remember(notificationLogs) {
        notificationLogs.filter { it.status.startsWith("PROCESSED") }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (checkNotificationPermission(context) && !com.dave.moneymanager.service.MoneyNotificationListenerService.isServiceRunning) {
                    com.dave.moneymanager.service.MoneyNotificationListenerService.forceReconnect(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Captured Notifications (${processedLogs.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Automatically recorded from banking & e-wallet alerts",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }

                if (processedLogs.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearNotificationLogs() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Logs",
                            tint = Rose500
                        )
                    }
                }
            }
        }

        if (processedLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Emerald500.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Captured Notifications Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Transactions detected automatically from GoPay, ShopeePay, OVO, BNI, CIMB, and myBCA notifications will appear here.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(processedLogs) { log ->
                ScrapedLogItem(log = log, formatRupiah = { viewModel.formatRupiah(it) })
            }
        }
    }
}

@Composable
private fun ScrapedLogItem(log: ScrapedNotificationLog, formatRupiah: (Double) -> String) {
    val timeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val isProcessed = log.status.startsWith("PROCESSED")
    val isTransfer = log.detectedType == "TRANSFER"
    val isIncome = log.detectedType == "INCOME"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isProcessed) Emerald500.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${log.appName} • ${log.status}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isProcessed) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = timeFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${log.title}: ${log.content}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            )

            if (isProcessed && log.amount != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account: ${log.targetBank ?: "-"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${log.detectedType}: ${formatRupiah(log.amount)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isTransfer -> Blue500
                                isIncome -> Emerald500
                                else -> Rose500
                            }
                        )
                    )
                }

                if (!log.destination.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Merchant / Party: ${log.destination}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald500
                        )
                    )
                }
            }
        }
    }
}

private fun checkNotificationPermission(context: Context): Boolean {
    val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
    return enabledListeners.contains(context.packageName)
}
