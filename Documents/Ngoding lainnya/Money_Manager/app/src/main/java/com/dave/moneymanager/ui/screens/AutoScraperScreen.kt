package com.dave.moneymanager.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dave.moneymanager.data.model.ScrapedNotificationLog
import com.dave.moneymanager.ui.theme.Amber500
import com.dave.moneymanager.ui.theme.ColorBNI
import com.dave.moneymanager.ui.theme.ColorCIMB
import com.dave.moneymanager.ui.theme.ColorGoPay
import com.dave.moneymanager.ui.theme.ColorGoPayClone
import com.dave.moneymanager.ui.theme.ColorOVO
import com.dave.moneymanager.ui.theme.ColorOctoPay
import com.dave.moneymanager.ui.theme.ColorShopeePay
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

    val isServiceLive by com.dave.moneymanager.service.MoneyNotificationListenerService.isServiceRunningFlow.collectAsState()
    var isPermissionGranted by remember { mutableStateOf(checkNotificationPermission(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isPermissionGranted = checkNotificationPermission(context)
                if (isPermissionGranted && !com.dave.moneymanager.service.MoneyNotificationListenerService.isServiceRunning) {
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            val isFullyActive = isPermissionGranted && isServiceLive
            val bannerColor = when {
                !isPermissionGranted -> Amber500
                !isServiceLive -> Amber500
                else -> Emerald500
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = bannerColor.copy(alpha = 0.15f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFullyActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = bannerColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when {
                                    !isPermissionGranted -> "Notification Access Required"
                                    !isServiceLive -> "Service Listener Terputus (Android)"
                                    else -> "Notification Scraper Aktif & Terhubung"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = bannerColor
                            )
                            Text(
                                text = when {
                                    !isPermissionGranted ->
                                        "Aktifkan 'Akses Notifikasi' agar aplikasi dapat mendeteksi transaksi otomatis di background."
                                    !isServiceLive ->
                                        "Android memutuskan koneksi background listener (umum terjadi setelah update dari Android Studio). Tekan 'Sambung Ulang' atau matikan lalu hidupkan kembali saklar di Pengaturan HP."
                                    else ->
                                        "Service terhubung ke Android OS dan siap mendeteksi transaksi GoPay, ShopeePay, OVO, Bank BNI, dan Bank CIMB secara otomatis."
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isPermissionGranted) {
                        Button(
                            onClick = { openNotificationAccessSettings(context) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber500)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Pengaturan Akses Notifikasi", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else if (!isServiceLive) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    com.dave.moneymanager.service.MoneyNotificationListenerService.forceReconnect(context)
                                    android.widget.Toast.makeText(context, "Memicu sambung ulang service...", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Amber500)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("⚡ Paksa Sambung Ulang Service", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { openNotificationAccessSettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Buka Pengaturan Akses Notifikasi (Toggle Saklar)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                com.dave.moneymanager.service.MoneyNotificationListenerService.forceReconnect(context)
                                android.widget.Toast.makeText(context, "Service di-refresh!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh Listener Connection", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 Panduan Agar Notifikasi Tidak Dimatikan Android:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Setel Penggunaan Baterai aplikasi ini ke \"Tidak Dibatasi / Unrestricted\".\n" +
                                "• Untuk HP Xiaomi/Samsung/Oppo/Vivo: Aktifkan izin \"Mulai Otomatis / Autostart\".\n" +
                                "• Jika baru update via Android Studio, selalu cek banner di atas bertuliskan \"Aktif & Terhubung\".",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Supported Applications",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AppSupportBadge(
                        appName = "Bank BNI / wondr by BNI",
                        pkg = "src.bni.mbanking, id.co.bni.wondr",
                        color = ColorBNI
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppSupportBadge(
                        appName = "Bank CIMB Niaga (OCTO Mobile)",
                        pkg = "com.cimbniaga.octomobile",
                        color = ColorCIMB
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppSupportBadge(
                        appName = "OctoPay (E-Money CIMB)",
                        pkg = "com.cimbniaga.octomobile (notif OctoPay / QRIS)",
                        color = ColorOctoPay
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppSupportBadge(
                        appName = "GoPay",
                        pkg = "com.gojek.app, com.gojek.gopay",
                        color = ColorGoPay
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppSupportBadge(
                        appName = "GoPay Clone (Dual Apps)",
                        pkg = "Cloned package / parallel space / named 'GoPay'",
                        color = ColorGoPayClone
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppSupportBadge(
                        appName = "ShopeePay",
                        pkg = "com.shopee.id, com.shopeepay.id",
                        color = ColorShopeePay
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppSupportBadge(
                        appName = "OVO",
                        pkg = "ovo.id, com.ovo, Grab OVO",
                        color = ColorOVO
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scraping Activity Logs (${processedLogs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (processedLogs.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearNotificationLogs() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs")
                    }
                }
            }
        }

        if (processedLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Emerald500.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum Ada Transaksi Notifikasi",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Transaksi yang terdeteksi otomatis dari notifikasi GoPay, ShopeePay, OVO, BNI, dan CIMB akan otomatis dicatat dan ditampilkan di sini.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
private fun AppSupportBadge(appName: String, pkg: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = appName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = pkg,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun ScrapedLogItem(log: ScrapedNotificationLog, formatRupiah: (Double) -> String) {
    val timeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val isProcessed = log.status.startsWith("PROCESSED")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isProcessed) Emerald500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${log.title}: ${log.content}",
                style = MaterialTheme.typography.bodySmall
            )

            if (isProcessed && log.amount != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Account: ${log.targetBank ?: "-"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${log.detectedType}: ${formatRupiah(log.amount)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (log.detectedType == "INCOME") Emerald500 else Rose500
                        )
                    )
                }

                if (!log.destination.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🎯 Destination/Source: ${log.destination}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
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

private fun openNotificationAccessSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallback = Intent(Settings.ACTION_SETTINGS)
        fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(fallback)
    }
}
