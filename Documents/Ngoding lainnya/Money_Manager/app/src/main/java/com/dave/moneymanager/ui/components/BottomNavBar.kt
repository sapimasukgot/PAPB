package com.dave.moneymanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NavigationScreen(val title: String, val headerTitle: String, val icon: ImageVector) {
    DASHBOARD("Home", "Money Manager", Icons.Default.Home),
    CATEGORIES("Budgets", "Categories & Budget", Icons.Default.PieChart),
    SHIELD("Shield", "Shield & Debts", Icons.Default.Shield),
    BANKS("Accounts", "Accounts & Wallets", Icons.Default.AccountBalanceWallet),
    AUTO_WRITE("Auto-Write", "Auto-Write Logs", Icons.Default.NotificationsActive)
}

@Composable
fun BottomNavBar(
    currentScreen: NavigationScreen,
    onScreenSelected: (NavigationScreen) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            NavigationScreen.values().forEach { screen ->
                val isSelected = currentScreen == screen
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onScreenSelected(screen) },
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title
                        )
                    },
                    label = {
                        Text(
                            text = screen.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}
