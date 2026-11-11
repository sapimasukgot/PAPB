package com.dave.moneymanager

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import com.dave.moneymanager.service.MoneyNotificationListenerService
import com.dave.moneymanager.ui.components.BottomNavBar
import com.dave.moneymanager.ui.components.BudgetAlertDialog
import com.dave.moneymanager.ui.components.NavigationScreen
import com.dave.moneymanager.ui.screens.AutoScraperScreen
import com.dave.moneymanager.ui.screens.BankManagementScreen
import com.dave.moneymanager.ui.screens.CategoriesScreen
import com.dave.moneymanager.ui.screens.DashboardScreen
import com.dave.moneymanager.ui.screens.ShieldScreen
import com.dave.moneymanager.ui.theme.Amber500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.MoneyManagerTheme
import com.dave.moneymanager.ui.viewmodel.MoneyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MoneyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ensureNotificationListenerConnected()

        setContent {
            MoneyManagerTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ensureNotificationListenerConnected()
        viewModel.refreshDateFilterIfNeeded()
    }

    private fun ensureNotificationListenerConnected() {
        try {
            val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(this)
            if (!enabledListeners.contains(packageName)) {
                return
            }

            if (!MoneyNotificationListenerService.isServiceRunning) {
                MoneyNotificationListenerService.forceReconnect(this)

                val handler = android.os.Handler(mainLooper)
                handler.postDelayed({
                    if (!MoneyNotificationListenerService.isServiceRunning) {
                        MoneyNotificationListenerService.forceReconnect(this)
                    }
                }, 1500)

                handler.postDelayed({
                    if (!MoneyNotificationListenerService.isServiceRunning) {
                        MoneyNotificationListenerService.forceReconnect(this)
                        android.util.Log.w(
                            "MainActivity",
                            "NotificationListener still disconnected after 3 reconnect attempts. " +
                                "User may need to toggle notification access in Settings."
                        )
                    }
                }, 4000)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error ensuring listener connected: ${e.message}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MoneyViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(NavigationScreen.DASHBOARD) }
    var categoriesInitialTab by remember { mutableIntStateOf(0) }
    val budgetAlert by viewModel.budgetAlert.collectAsState()
    val isServiceLive by MoneyNotificationListenerService.isServiceRunningFlow.collectAsState()
    var isPermissionGranted by remember {
        mutableStateOf(
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        )
    }

    var currentDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentDateMillis = System.currentTimeMillis()
                isPermissionGranted = NotificationManagerCompat
                    .getEnabledListenerPackages(context)
                    .contains(context.packageName)
                viewModel.refreshDateFilterIfNeeded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val todayFormatted = remember(currentDateMillis) {
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.US)
        sdf.format(Date(currentDateMillis))
    }

    var lastBackPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler(enabled = currentScreen != NavigationScreen.DASHBOARD) {
        currentScreen = NavigationScreen.DASHBOARD
    }

    BackHandler(enabled = currentScreen == NavigationScreen.DASHBOARD) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressedTime < 2000) {
            (context as? ComponentActivity)?.finish()
        } else {
            lastBackPressedTime = now
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

    if (budgetAlert != null) {
        BudgetAlertDialog(
            alert = budgetAlert!!,
            onDismiss = { viewModel.dismissBudgetAlert() },
            onNavigateToBudget = {
                categoriesInitialTab = 1
                currentScreen = NavigationScreen.CATEGORIES
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentScreen.headerTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    letterSpacing = (-0.01).sp
                                )
                            )
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    },
                    actions = {
                        val isListenerReady = isPermissionGranted && isServiceLive
                        val statusColor = if (isListenerReady) MaterialTheme.colorScheme.primary else Amber500
                        val statusLabel = when {
                            !isPermissionGranted -> "Enable Auto"
                            !isServiceLive -> "Reconnect"
                            else -> "Auto-Write On"
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(statusColor.copy(alpha = 0.08f))
                                .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                .clickable {
                                    if (!isPermissionGranted) {
                                        try {
                                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            val fallback = Intent(Settings.ACTION_SETTINGS)
                                            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(fallback)
                                        }
                                    } else {
                                        MoneyNotificationListenerService.forceReconnect(context)
                                        Toast.makeText(
                                            context,
                                            "Auto-Write listener refreshed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                )
            }
        },
        bottomBar = {
            BottomNavBar(
                currentScreen = currentScreen,
                onScreenSelected = { screen ->
                    currentScreen = screen
                    if (screen == NavigationScreen.CATEGORIES) {
                        categoriesInitialTab = 0
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                when (screen) {
                    NavigationScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToScreen = { targetScreen ->
                            if (targetScreen == NavigationScreen.CATEGORIES) {
                                categoriesInitialTab = 1
                            }
                            currentScreen = targetScreen
                        }
                    )
                    NavigationScreen.CATEGORIES -> CategoriesScreen(
                        viewModel = viewModel,
                        initialTab = categoriesInitialTab
                    )
                    NavigationScreen.SHIELD -> ShieldScreen(
                        viewModel = viewModel
                    )
                    NavigationScreen.BANKS -> BankManagementScreen(
                        viewModel = viewModel
                    )
                    NavigationScreen.AUTO_WRITE -> AutoScraperScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
