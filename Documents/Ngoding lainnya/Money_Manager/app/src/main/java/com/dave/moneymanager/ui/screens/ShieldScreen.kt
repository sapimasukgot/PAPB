package com.dave.moneymanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.moneymanager.data.model.Bank
import com.dave.moneymanager.data.model.MyDebtEntry
import com.dave.moneymanager.data.model.ShieldContact
import com.dave.moneymanager.data.model.ShieldEntry
import com.dave.moneymanager.data.model.ShieldWalletDeduction
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionCategory
import com.dave.moneymanager.data.model.ShieldHistoryItem
import com.dave.moneymanager.ui.components.CalculatorDialog
import com.dave.moneymanager.ui.components.SearchableContactDialog
import com.dave.moneymanager.ui.theme.Amber500
import com.dave.moneymanager.ui.theme.Blue500
import com.dave.moneymanager.ui.theme.Emerald500
import com.dave.moneymanager.ui.theme.Rose500
import com.dave.moneymanager.ui.viewmodel.MoneyViewModel
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun saveShieldAvatar(context: Context, uri: Uri, friendName: String): String? {
    return try {
        val dir = File(context.filesDir, "shield_avatars")
        if (!dir.exists()) dir.mkdirs()
        val safeName = friendName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val file = File(dir, "avatar_${safeName}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ShieldAvatarView(
    avatarUri: String?,
    friendName: String,
    modifier: Modifier = Modifier,
    isSettled: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    showCameraBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val bitmap = remember(avatarUri) {
        avatarUri?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (isSettled) Emerald500.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.2f)
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = friendName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val avatarChar = friendName.firstOrNull()?.uppercase() ?: "?"
            Text(
                text = avatarChar,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.42f).sp,
                color = if (isSettled) Emerald500 else Amber500
            )
        }

        if (showCameraBadge) {
            Box(
                modifier = Modifier
                    .size((size.value * 0.42f).dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    tint = Color.White,
                    modifier = Modifier.size((size.value * 0.24f).dp)
                )
            }
        }
    }
}

private enum class ShieldScreenMode(val title: String) {
    RECEIVABLES("Receivables"),
    MY_DEBT("My Debt")
}

private enum class ShieldTabFilter(val title: String) {
    UNPAID("Unpaid"),
    SETTLED("Settled"),
    ALL("All")
}

@Composable
fun ShieldScreen(
    viewModel: MoneyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var screenMode by remember { mutableStateOf(ShieldScreenMode.RECEIVABLES) }

    val shields by viewModel.shields.collectAsState()
    val banks by viewModel.banks.collectAsState()
    val totalUnsettled by viewModel.totalUnsettledShieldAmount.collectAsState()
    val totalSettled by viewModel.totalSettledShieldAmount.collectAsState()
    val unsettledCount by viewModel.unsettledShieldCount.collectAsState()

    var currentTab by remember { mutableStateOf(ShieldTabFilter.UNPAID) }
    var searchQuery by remember { mutableStateOf("") }

    val shieldHistories by viewModel.shieldHistories.collectAsState()
    val shieldContacts by viewModel.shieldContacts.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()

    val myDebts by viewModel.myDebts.collectAsState()
    val totalUnsettledMyDebt by viewModel.totalUnsettledMyDebtAmount.collectAsState()
    val totalSettledMyDebt by viewModel.totalSettledMyDebtAmount.collectAsState()
    val unsettledMyDebtCount by viewModel.unsettledMyDebtCount.collectAsState()

    var myDebtTab by remember { mutableStateOf(ShieldTabFilter.UNPAID) }
    var myDebtSearchQuery by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var shieldToEdit by remember { mutableStateOf<ShieldEntry?>(null) }
    var shieldToSettle by remember { mutableStateOf<ShieldEntry?>(null) }
    var shieldToDelete by remember { mutableStateOf<ShieldEntry?>(null) }
    var shieldToRemind by remember { mutableStateOf<ShieldEntry?>(null) }
    var shieldForAvatarOptions by remember { mutableStateOf<ShieldEntry?>(null) }
    var shieldForPhotoPick by remember { mutableStateOf<ShieldEntry?>(null) }
    var fullPhotoPreviewUri by remember { mutableStateOf<String?>(null) }

    var showAddMyDebtDialog by remember { mutableStateOf(false) }
    var myDebtToEdit by remember { mutableStateOf<MyDebtEntry?>(null) }
    var myDebtToDelete by remember { mutableStateOf<MyDebtEntry?>(null) }
    var myDebtToSettle by remember { mutableStateOf<MyDebtEntry?>(null) }
    var myDebtForAvatarOptions by remember { mutableStateOf<MyDebtEntry?>(null) }
    var myDebtForPhotoPick by remember { mutableStateOf<MyDebtEntry?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val target = shieldForPhotoPick
        if (uri != null && target != null) {
            val savedPath = saveShieldAvatar(context, uri, target.friendName)
            if (savedPath != null) {
                viewModel.updateShieldAvatar(target.id, target.friendName, savedPath)
                Toast.makeText(context, "Profile photo for ${target.friendName} updated!", Toast.LENGTH_SHORT).show()
            }
        }
        shieldForPhotoPick = null
    }

    val myDebtPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val target = myDebtForPhotoPick
        if (uri != null && target != null) {
            val savedPath = saveShieldAvatar(context, uri, target.personName)
            if (savedPath != null) {
                viewModel.updateMyDebtAvatar(target.id, target.personName, savedPath)
                Toast.makeText(context, "Profile photo for ${target.personName} updated!", Toast.LENGTH_SHORT).show()
            }
        }
        myDebtForPhotoPick = null
    }

    val filteredList = remember(shields, currentTab, searchQuery) {
        shields.filter { item ->
            val matchesTab = when (currentTab) {
                ShieldTabFilter.UNPAID -> !item.isSettled
                ShieldTabFilter.SETTLED -> item.isSettled
                ShieldTabFilter.ALL -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.friendName.contains(searchQuery, ignoreCase = true) ||
                        item.note.contains(searchQuery, ignoreCase = true)
            }
            matchesTab && matchesSearch
        }
    }

    val filteredMyDebts = remember(myDebts, myDebtTab, myDebtSearchQuery) {
        myDebts.filter { item ->
            val matchesTab = when (myDebtTab) {
                ShieldTabFilter.UNPAID -> !item.isSettled
                ShieldTabFilter.SETTLED -> item.isSettled
                ShieldTabFilter.ALL -> true
            }
            val matchesSearch = if (myDebtSearchQuery.isBlank()) true else {
                item.personName.contains(myDebtSearchQuery, ignoreCase = true) ||
                        item.note.contains(myDebtSearchQuery, ignoreCase = true)
            }
            matchesTab && matchesSearch
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Emerald500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Shield & Debt Tracker",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "Track friend receivables & manage your debts",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    ShieldScreenMode.values().forEach { mode ->
                        val isSelected = screenMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) (if (mode == ShieldScreenMode.RECEIVABLES) Emerald500 else Rose500) else Color.Transparent)
                                .clickable { screenMode = mode }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (mode == ShieldScreenMode.RECEIVABLES) Icons.Default.Shield else Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mode.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            if (screenMode == ShieldScreenMode.RECEIVABLES) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isUnpaidSelected = currentTab == ShieldTabFilter.UNPAID

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currentTab = if (currentTab == ShieldTabFilter.UNPAID) ShieldTabFilter.ALL else ShieldTabFilter.UNPAID
                                },
                            shape = RoundedCornerShape(16.dp),
                            border = if (isUnpaidSelected) BorderStroke(1.5.dp, Rose500) else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Rose500.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassTop,
                                            contentDescription = null,
                                            tint = Rose500,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Unpaid",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnpaidSelected) Rose500 else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = viewModel.formatRupiah(totalUnsettled),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Rose500,
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$unsettledCount pending people",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        val isSettledSelected = currentTab == ShieldTabFilter.SETTLED

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currentTab = if (currentTab == ShieldTabFilter.SETTLED) ShieldTabFilter.ALL else ShieldTabFilter.SETTLED
                                },
                            shape = RoundedCornerShape(16.dp),
                            border = if (isSettledSelected) BorderStroke(1.5.dp, Emerald500) else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Emerald500.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Emerald500,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Settled",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSettledSelected) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = viewModel.formatRupiah(totalSettled),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Emerald500,
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Paid back",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search friend name or notes...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp)
                        ) {
                            ShieldTabFilter.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Emerald500 else Color.Transparent)
                                        .clickable { currentTab = tab }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Emerald500.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Emerald500,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No matching results" else "No friend receivables yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Try another search term" else "Enable the Shield option when recording shared expenses.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { item ->
                        val itemHistories = remember(shieldHistories, item) {
                            shieldHistories.filter {
                                it.shieldId == item.id || it.friendName.trim().equals(item.friendName.trim(), ignoreCase = true)
                            }
                        }
                        ShieldItemCard(
                            entry = item,
                            histories = itemHistories,
                            allTransactions = allTransactions,
                            onChangeAvatar = { shieldForAvatarOptions = item },
                            onEdit = { shieldToEdit = item },
                            onSettle = { shieldToSettle = item },
                            onUnsettle = { viewModel.unsettleShield(item.id) },
                            onRemind = { shieldToRemind = item },
                            onDelete = { shieldToDelete = item }
                        )
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isUnpaidSelected = myDebtTab == ShieldTabFilter.UNPAID

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    myDebtTab = if (myDebtTab == ShieldTabFilter.UNPAID) ShieldTabFilter.ALL else ShieldTabFilter.UNPAID
                                },
                            shape = RoundedCornerShape(18.dp),
                            border = if (isUnpaidSelected) BorderStroke(2.dp, Rose500) else null,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnpaidSelected) Rose500.copy(alpha = 0.22f) else Rose500.copy(alpha = 0.12f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isUnpaidSelected) 4.dp else 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Rose500),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassTop,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Unpaid",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Rose500
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = viewModel.formatRupiah(totalUnsettledMyDebt),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Rose500,
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$unsettledMyDebtCount active debts",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        val isSettledSelected = myDebtTab == ShieldTabFilter.SETTLED

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    myDebtTab = if (myDebtTab == ShieldTabFilter.SETTLED) ShieldTabFilter.ALL else ShieldTabFilter.SETTLED
                                },
                            shape = RoundedCornerShape(18.dp),
                            border = if (isSettledSelected) BorderStroke(2.dp, Emerald500) else null,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSettledSelected) Emerald500.copy(alpha = 0.22f) else Emerald500.copy(alpha = 0.12f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSettledSelected) 4.dp else 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Emerald500),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Settled",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald500
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = viewModel.formatRupiah(totalSettledMyDebt),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Emerald500,
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Debts paid off",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = myDebtSearchQuery,
                            onValueChange = { myDebtSearchQuery = it },
                            placeholder = { Text("Search lender name or notes...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingIcon = {
                                if (myDebtSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { myDebtSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp)
                        ) {
                            ShieldTabFilter.values().forEach { tab ->
                                val isSelected = myDebtTab == tab
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Rose500 else Color.Transparent)
                                        .clickable { myDebtTab = tab }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredMyDebts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Rose500.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Rose500,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (myDebtSearchQuery.isNotEmpty()) "No matching results" else "No debt records yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (myDebtSearchQuery.isNotEmpty()) "Try another search term" else "Tap 'Add Debt' below to record a debt you owe.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    items(filteredMyDebts, key = { it.id }) { item ->
                        MyDebtItemCard(
                            entry = item,
                            formattedAmount = viewModel.formatRupiah(item.amount),
                            onChangeAvatar = { myDebtForAvatarOptions = item },
                            onEdit = { myDebtToEdit = item },
                            onSettle = {
                                if (item.settledBankId != null) {
                                    viewModel.settleMyDebt(
                                        debtId = item.id,
                                        bankId = item.settledBankId,
                                        bankName = item.settledBankName,
                                        category = item.category,
                                        deductWallet = true
                                    )
                                    Toast.makeText(
                                        context,
                                        "Debt to ${item.personName} settled! Deducted from ${item.settledBankName ?: "Wallet"} • ${item.category}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    myDebtToSettle = item
                                }
                            },
                            onUnsettle = {
                                viewModel.unsettleMyDebt(item.id)
                                Toast.makeText(
                                    context,
                                    "Settlement undone & balance restored",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onDelete = { myDebtToDelete = item }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        FloatingActionButton(
            onClick = {
                if (screenMode == ShieldScreenMode.RECEIVABLES) {
                    showAddDialog = true
                } else {
                    showAddMyDebtDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = if (screenMode == ShieldScreenMode.RECEIVABLES) Emerald500 else Rose500,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (screenMode == ShieldScreenMode.RECEIVABLES) "Add Shield" else "Add Debt",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showAddDialog) {
        ManualAddShieldDialog(
            existingShields = shields,
            contacts = shieldContacts,
            banks = banks,
            onDismiss = { showAddDialog = false },
            onSave = { friendName, amount, note, timestamp, avatarUri, walletDeductions ->
                if (walletDeductions.isNotEmpty()) {
                    viewModel.addShieldWithWallets(
                        friendName = friendName,
                        amount = amount,
                        note = note,
                        timestamp = timestamp,
                        avatarUri = avatarUri,
                        walletDeductions = walletDeductions
                    )
                    val fmt = NumberFormat.getNumberInstance(Locale.US)
                    val walletSummary = walletDeductions.joinToString(", ") { "${it.bankName}: Rp ${fmt.format(it.amount)}" }
                    Toast.makeText(context, "Shield for $friendName recorded (deducted from $walletSummary)!", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.addShield(
                        friendName = friendName,
                        amount = amount,
                        note = note,
                        timestamp = timestamp,
                        avatarUri = avatarUri
                    )
                    Toast.makeText(context, "Shield for $friendName recorded!", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
            }
        )
    }

    if (shieldToEdit != null) {
        val target = shieldToEdit!!
        EditShieldDialog(
            entry = target,
            banks = banks,
            onDismiss = { shieldToEdit = null },
            onViewFullPhoto = { fullPhotoPreviewUri = it },
            onSave = { updatedEntry, walletDeductions ->
                if (walletDeductions.isNotEmpty()) {
                    viewModel.updateShieldWithWallets(updatedEntry, walletDeductions)
                    val fmt = NumberFormat.getNumberInstance(Locale.US)
                    val walletSummary = walletDeductions.joinToString(", ") { "${it.bankName}: Rp ${fmt.format(it.amount)}" }
                    Toast.makeText(context, "Shield for ${updatedEntry.friendName} updated! Deducted from $walletSummary", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.updateShield(updatedEntry)
                    Toast.makeText(context, "Shield for ${updatedEntry.friendName} updated!", Toast.LENGTH_SHORT).show()
                }
                shieldToEdit = null
            },
            onPartialPayment = { deductionAmount, isTransfer, bankId, bankName, note, updatedName, updatedNote ->
                viewModel.recordPartialPayment(
                    shieldId = target.id,
                    amount = deductionAmount,
                    isTransfer = isTransfer,
                    bankId = bankId,
                    bankName = bankName,
                    note = note,
                    updatedFriendName = updatedName,
                    updatedNote = updatedNote
                )
                shieldToEdit = null
                val methodStr = if (isTransfer) "transferred to $bankName (recorded as income)" else "offset via item/service"
                val fmt = NumberFormat.getNumberInstance(Locale.US)
                Toast.makeText(context, "${target.friendName}'s balance reduced by Rp ${fmt.format(deductionAmount)} ($methodStr)!", Toast.LENGTH_LONG).show()
            }
        )
    }

    if (shieldToSettle != null) {
        val target = shieldToSettle!!
        SettleConfirmationDialog(
            entry = target,
            banks = banks,
            onDismiss = { shieldToSettle = null },
            onConfirm = { bankId, bankName, addToBalance ->
                viewModel.settleShield(
                    shieldId = target.id,
                    bankId = bankId,
                    bankName = bankName,
                    recordIncome = addToBalance
                )
                shieldToSettle = null
                Toast.makeText(context, "${target.friendName}'s Shield settled!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (shieldToDelete != null) {
        val target = shieldToDelete!!
        AlertDialog(
            onDismissRequest = { shieldToDelete = null },
            title = { Text("Delete Shield Record?") },
            text = { Text("Delete ${target.friendName}'s receivable of ${viewModel.formatRupiah(target.amount)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteShield(target.id)
                        shieldToDelete = null
                        Toast.makeText(context, "Shield record deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { shieldToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (shieldToRemind != null) {
        val target = shieldToRemind!!
        RemindFriendDialog(
            entry = target,
            onDismiss = { shieldToRemind = null },
            onCopy = { message ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Shield Reminder", message)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Reminder copied to clipboard!", Toast.LENGTH_SHORT).show()
                shieldToRemind = null
            },
            onShare = { message ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, message)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Remind ${target.friendName} via")
                context.startActivity(shareIntent)
                shieldToRemind = null
            }
        )
    }

    if (shieldForAvatarOptions != null) {
        val target = shieldForAvatarOptions!!
        AlertDialog(
            onDismissRequest = { shieldForAvatarOptions = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Emerald500,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Profile Photo: ${target.friendName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ShieldAvatarView(
                        avatarUri = target.avatarUri,
                        friendName = target.friendName,
                        isSettled = target.isSettled,
                        size = 80.dp,
                        showCameraBadge = false,
                        onClick = if (target.avatarUri != null) {
                            { fullPhotoPreviewUri = target.avatarUri }
                        } else null
                    )
                    if (target.avatarUri != null) {
                        Text(
                            text = "Tap photo to view full screen",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Emerald500,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Text(
                        text = if (target.avatarUri != null)
                            "Change or remove profile photo for ${target.friendName}."
                        else
                            "Choose a profile photo from your gallery for ${target.friendName}.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentTarget = target
                        shieldForAvatarOptions = null
                        shieldForPhotoPick = currentTarget
                        photoPickerLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (target.avatarUri != null) "Change Photo" else "Choose Photo")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (target.avatarUri != null) {
                        TextButton(
                            onClick = {
                                viewModel.updateShieldAvatar(target.id, target.friendName, null)
                                Toast.makeText(context, "Profile photo removed", Toast.LENGTH_SHORT).show()
                                shieldForAvatarOptions = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Rose500)
                        ) {
                            Text("Remove")
                        }
                    }
                    TextButton(onClick = { shieldForAvatarOptions = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    if (fullPhotoPreviewUri != null) {
        Dialog(
            onDismissRequest = {  },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f)),
                contentAlignment = Alignment.Center
            ) {
                val file = remember(fullPhotoPreviewUri) { File(fullPhotoPreviewUri!!) }
                val bitmap = remember(file) {
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "Photo not found",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                IconButton(
                    onClick = { fullPhotoPreviewUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    if (showAddMyDebtDialog) {
        AddEditMyDebtDialog(
            contacts = shieldContacts,
            existingShields = shields,
            banks = banks,
            expenseCategories = expenseCategories,
            onDismiss = { showAddMyDebtDialog = false },
            onSave = { creditorName, amount, note, timestamp, avatarUri, bankId, bankName, category ->
                viewModel.addMyDebt(
                    personName = creditorName,
                    amount = amount,
                    note = note,
                    timestamp = timestamp,
                    avatarUri = avatarUri,
                    bankId = bankId,
                    bankName = bankName,
                    category = category
                )
                showAddMyDebtDialog = false
                Toast.makeText(context, "Debt to $creditorName recorded!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (myDebtToEdit != null) {
        val target = myDebtToEdit!!
        AddEditMyDebtDialog(
            entry = target,
            contacts = shieldContacts,
            existingShields = shields,
            banks = banks,
            expenseCategories = expenseCategories,
            onDismiss = { myDebtToEdit = null },
            onSave = { personName, amount, note, timestamp, avatarUri, bankId, bankName, category ->
                viewModel.updateMyDebt(
                    target.copy(
                        personName = personName,
                        amount = amount,
                        note = note,
                        timestamp = timestamp,
                        settledBankId = bankId,
                        settledBankName = bankName,
                        category = category,
                        avatarUri = avatarUri
                    )
                )
                myDebtToEdit = null
                Toast.makeText(context, "Debt to $personName updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (myDebtToSettle != null) {
        val target = myDebtToSettle!!
        SettleMyDebtDialog(
            entry = target,
            banks = banks,
            expenseCategories = expenseCategories,
            formatRupiah = { viewModel.formatRupiah(it) },
            onDismiss = { myDebtToSettle = null },
            onConfirm = { bankId, bankName, category, deductWallet ->
                viewModel.settleMyDebt(
                    debtId = target.id,
                    bankId = bankId,
                    bankName = bankName,
                    category = category,
                    deductWallet = deductWallet
                )
                myDebtToSettle = null
                val walletMsg = if (deductWallet && bankName != null) " (Deducted from $bankName • $category)" else ""
                Toast.makeText(context, "Debt to ${target.personName} settled!$walletMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (myDebtToDelete != null) {
        val target = myDebtToDelete!!
        AlertDialog(
            onDismissRequest = { myDebtToDelete = null },
            title = { Text("Delete Debt Record?") },
            text = { Text("Delete your debt to ${target.personName} of ${viewModel.formatRupiah(target.amount)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMyDebt(target.id)
                        myDebtToDelete = null
                        Toast.makeText(context, "Debt record deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { myDebtToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (myDebtForAvatarOptions != null) {
        val target = myDebtForAvatarOptions!!
        AlertDialog(
            onDismissRequest = { myDebtForAvatarOptions = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Rose500,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Profile Photo: ${target.personName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ShieldAvatarView(
                        avatarUri = target.avatarUri,
                        friendName = target.personName,
                        isSettled = target.isSettled,
                        size = 80.dp,
                        onClick = {
                            if (target.avatarUri != null) {
                                fullPhotoPreviewUri = target.avatarUri
                                myDebtForAvatarOptions = null
                            }
                        }
                    )
                    Text(
                        text = "Choose a photo from your gallery to easily identify this person.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        myDebtForPhotoPick = target
                        myDebtPhotoPickerLauncher.launch("image/*")
                        myDebtForAvatarOptions = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (target.avatarUri == null) "Choose Photo" else "Change Photo")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (target.avatarUri != null) {
                        TextButton(
                            onClick = {
                                viewModel.updateMyDebtAvatar(target.id, target.personName, null)
                                Toast.makeText(context, "Profile photo removed", Toast.LENGTH_SHORT).show()
                                myDebtForAvatarOptions = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Rose500)
                        ) {
                            Text("Remove")
                        }
                    }
                    TextButton(onClick = { myDebtForAvatarOptions = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun MyDebtItemCard(
    entry: MyDebtEntry,
    formattedAmount: String,
    onChangeAvatar: () -> Unit,
    onEdit: () -> Unit,
    onSettle: () -> Unit,
    onUnsettle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US) }
    val formattedDate = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }
    val isSettled = entry.isSettled

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSettled) Emerald500.copy(alpha = 0.35f) else Rose500.copy(alpha = 0.3f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShieldAvatarView(
                    avatarUri = entry.avatarUri,
                    friendName = entry.personName,
                    isSettled = isSettled,
                    showCameraBadge = true,
                    size = 46.dp,
                    onClick = onChangeAvatar
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.personName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formattedAmount,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSettled) Emerald500 else Rose500,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSettled) Emerald500.copy(alpha = 0.12f) else Rose500.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isSettled) "Settled" else "Unpaid",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSettled) Emerald500 else Rose500,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.category.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Blue500.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Blue500,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = entry.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Blue500,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (!entry.settledBankName.isNullOrBlank()) {
                    val walletTint = if (isSettled) Emerald500 else Amber500
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = walletTint.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = walletTint,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSettled) "Paid via ${entry.settledBankName}" else "Pay via ${entry.settledBankName}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = walletTint,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (entry.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = entry.note,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isSettled) {
                    Button(
                        onClick = onSettle,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Settled", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onUnsettle,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Undo Settled", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Amber500)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Debt",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Debt",
                        tint = Rose500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditMyDebtDialog(
    entry: MyDebtEntry? = null,
    contacts: List<ShieldContact>,
    existingShields: List<ShieldEntry> = emptyList(),
    banks: List<Bank> = emptyList(),
    expenseCategories: List<TransactionCategory> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (creditorName: String, amount: Double, note: String, timestamp: Long, avatarUri: String?, bankId: Long?, bankName: String?, category: String) -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    var creditorName by remember { mutableStateOf(entry?.personName ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (entry != null) {
                if (entry.amount % 1.0 == 0.0) entry.amount.toLong().toString() else entry.amount.toString()
            } else ""
        )
    }
    var note by remember { mutableStateOf(entry?.note ?: "") }
    val timestamp = remember { entry?.timestamp ?: System.currentTimeMillis() }
    var customAvatarUri by remember { mutableStateOf(entry?.avatarUri) }

    val availableCategories = remember(expenseCategories) {
        if (expenseCategories.isNotEmpty()) {
            expenseCategories.map { it.name }
        } else {
            TransactionCategory.defaultExpenseCategories.map { it.name }
        }
    }
    var selectedCategory by remember(entry, availableCategories) {
        mutableStateOf(
            entry?.category?.takeIf { it.isNotBlank() }
                ?: availableCategories.firstOrNull { it.equals("Food & Dining", ignoreCase = true) }
                ?: availableCategories.firstOrNull()
                ?: "Food & Dining"
        )
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var selectedBank by remember(entry, banks) {
        mutableStateOf(
            banks.find { it.id == entry?.settledBankId }
                ?: banks.find { it.name.equals(entry?.settledBankName, ignoreCase = true) }
                ?: banks.firstOrNull()
        )
    }
    var walletDropdownExpanded by remember { mutableStateOf(false) }

    var showContactPicker by remember { mutableStateOf(false) }
    var showCalc by remember { mutableStateOf(false) }

    val matchedContact = remember(creditorName, contacts) {
        val trimmed = creditorName.trim()
        if (trimmed.isEmpty()) null
        else contacts.find { it.name.trim().equals(trimmed, ignoreCase = true) }
    }
    val effectiveAvatarUri = customAvatarUri ?: matchedContact?.avatarUri

    if (showContactPicker) {
        SearchableContactDialog(
            contacts = contacts,
            existingShields = existingShields,
            onDismiss = { showContactPicker = false },
            onSelectContact = { name, avatar ->
                creditorName = name
                customAvatarUri = avatar
                showContactPicker = false
            }
        )
    }

    if (showCalc) {
        CalculatorDialog(
            initialAmount = amountText,
            onDismiss = { showCalc = false },
            onApply = { res ->
                amountText = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
                showCalc = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = Rose500
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (entry == null) "Record New Debt" else "Edit Debt Entry",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (contacts.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showContactPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Blue500.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Blue500.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                tint = Blue500,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Select / Import Contact",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Blue500
                                )
                                Text(
                                    text = "Choose from saved Shield contacts",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = creditorName,
                    onValueChange = { creditorName = it },
                    label = { Text("Creditor Name") },
                    placeholder = { Text("e.g., Budi, Mom, Alex") },
                    singleLine = true,
                    leadingIcon = {
                        ShieldAvatarView(
                            avatarUri = effectiveAvatarUri,
                            friendName = creditorName.ifBlank { "?" },
                            size = 28.dp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Debt Amount (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = {
                        IconButton(onClick = { showCalc = true }) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculator",
                                tint = Rose500
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Category") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Blue500
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        availableCategories.forEach { catName ->
                            DropdownMenuItem(
                                text = { Text(catName, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedCategory = catName
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (banks.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = walletDropdownExpanded,
                            onExpandedChange = { walletDropdownExpanded = !walletDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedBank?.name ?: "Select Wallet / Account",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pay Using Wallet / Account") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Amber500
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = walletDropdownExpanded,
                                onDismissRequest = { walletDropdownExpanded = false }
                            ) {
                                banks.forEach { bank ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(bank.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "Balance: Rp ${formatter.format(bank.balance)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedBank = bank
                                            walletDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Wallet balance will only be deducted when you press 'Mark Settled'.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Purpose (Optional)") },
                    placeholder = { Text("e.g., Dinner split, monthly rent") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (creditorName.isNotBlank() && amt > 0) {
                        onSave(
                            creditorName.trim(),
                            amt,
                            note.trim(),
                            timestamp,
                            effectiveAvatarUri,
                            selectedBank?.id,
                            selectedBank?.name,
                            selectedCategory
                        )
                    }
                },
                enabled = creditorName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Rose500)
            ) {
                Text(
                    text = if (entry == null) "Save Debt" else "Save Changes",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettleMyDebtDialog(
    entry: MyDebtEntry,
    banks: List<Bank>,
    expenseCategories: List<TransactionCategory>,
    formatRupiah: (Double) -> String,
    onDismiss: () -> Unit,
    onConfirm: (bankId: Long?, bankName: String?, category: String, deductWallet: Boolean) -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    var deductWallet by remember { mutableStateOf(true) }

    val availableCategories = remember(expenseCategories) {
        if (expenseCategories.isNotEmpty()) {
            expenseCategories.map { it.name }
        } else {
            TransactionCategory.defaultExpenseCategories.map { it.name }
        }
    }
    var selectedCategory by remember(entry, availableCategories) {
        mutableStateOf(
            entry.category.takeIf { it.isNotBlank() }
                ?: availableCategories.firstOrNull { it.equals("Food & Dining", ignoreCase = true) }
                ?: availableCategories.firstOrNull()
                ?: "Food & Dining"
        )
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var selectedBank by remember(entry, banks) {
        mutableStateOf(
            banks.find { it.id == entry.settledBankId }
                ?: banks.find { it.name.equals(entry.settledBankName, ignoreCase = true) }
                ?: banks.firstOrNull()
        )
    }
    var walletDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Settle Debt", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Mark debt to ${entry.personName} (${formatRupiah(entry.amount)}) as settled?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Deduct Wallet & Record Expense?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Automatically deduct balance and record in your selected category.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        )
                    }
                    Switch(
                        checked = deductWallet,
                        onCheckedChange = { deductWallet = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Emerald500)
                    )
                }

                if (deductWallet) {
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Expense Category") },
                            leadingIcon = {
                                Icon(Icons.Default.Category, contentDescription = null, tint = Blue500)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            availableCategories.forEach { catName ->
                                DropdownMenuItem(
                                    text = { Text(catName, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        selectedCategory = catName
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (banks.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = walletDropdownExpanded,
                            onExpandedChange = { walletDropdownExpanded = !walletDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedBank?.name ?: "Select Wallet / Account",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pay Using Wallet") },
                                leadingIcon = {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Amber500)
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = walletDropdownExpanded,
                                onDismissRequest = { walletDropdownExpanded = false }
                            ) {
                                banks.forEach { bank ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(bank.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "Balance: Rp ${formatter.format(bank.balance)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedBank = bank
                                            walletDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        if (deductWallet) selectedBank?.id else null,
                        if (deductWallet) selectedBank?.name else null,
                        selectedCategory,
                        deductWallet
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text("Mark Settled", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ShieldItemCard(
    entry: ShieldEntry,
    histories: List<ShieldHistoryItem> = emptyList(),
    allTransactions: List<Transaction> = emptyList(),
    onChangeAvatar: () -> Unit = {},
    onEdit: () -> Unit,
    onSettle: () -> Unit,
    onUnsettle: () -> Unit,
    onRemind: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val formattedDate = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val formattedAmount = remember(entry.amount) { "Rp " + formatter.format(entry.amount) }
    var isHistoryExpanded by remember { mutableStateOf(false) }

    val categoryLabel = remember(entry, histories, allTransactions) {
        var linkedTx = if (entry.transactionId != null) {
            allTransactions.find { it.id == entry.transactionId }
        } else null

        if (linkedTx == null) {
            val histTxId = histories.firstOrNull { it.transactionId != null }?.transactionId
            if (histTxId != null) {
                linkedTx = allTransactions.find { it.id == histTxId }
            }
        }

        val rawCategory = linkedTx?.category?.takeIf { it.isNotBlank() && !it.equals("Shield", ignoreCase = true) }

        when {
            rawCategory != null -> {
                if (rawCategory.startsWith("Pembelian", ignoreCase = true) || rawCategory.startsWith("Beli", ignoreCase = true) || rawCategory.startsWith("Purchase", ignoreCase = true)) {
                    rawCategory
                } else if (rawCategory.equals("Makanan", ignoreCase = true) || rawCategory.equals("Food & Dining", ignoreCase = true)) {
                    "Food & Dining"
                } else {
                    rawCategory
                }
            }
            entry.note.isNotBlank() && !entry.note.startsWith("Kasir:", ignoreCase = true) && !entry.note.startsWith("Cashier:", ignoreCase = true) && !entry.note.startsWith("Total:", ignoreCase = true) -> {
                val noteLower = entry.note.lowercase(Locale.ROOT)
                if (noteLower.contains("makan") || noteLower.contains("food")) {
                    "Food & Dining"
                } else if (entry.note.startsWith("Pembelian", ignoreCase = true) || entry.note.startsWith("Purchase", ignoreCase = true)) {
                    entry.note
                } else if (entry.note.length <= 25 && !entry.note.contains(":") && !entry.note.contains("\n")) {
                    entry.note
                } else {
                    "Shared Expense"
                }
            }
            else -> "Food & Dining"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
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
                verticalAlignment = Alignment.Top
            ) {

                Row(modifier = Modifier.weight(1f)) {
                    ShieldAvatarView(
                        avatarUri = entry.avatarUri,
                        friendName = entry.friendName,
                        isSettled = entry.isSettled,
                        size = 46.dp,
                        showCameraBadge = true,
                        onClick = onChangeAvatar
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.friendName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (entry.note.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = entry.note,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    fontSize = 12.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$formattedDate • $categoryLabel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formattedAmount,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (entry.isSettled) Emerald500 else Rose500,
                            fontSize = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (entry.isSettled) Emerald500.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (entry.isSettled) "Settled" else "Unpaid",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (entry.isSettled) Emerald500 else Amber500,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            if (histories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isHistoryExpanded = !isHistoryExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val hasDeduction = histories.any { it.amount < 0 }
                            Text(
                                text = if (hasDeduction) "Transaction History (${histories.size})" else "Activity History (${histories.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                            )
                        }
                        Icon(
                            imageVector = if (isHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isHistoryExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (isHistoryExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val timeFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US) }
                            histories.forEach { hItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val isDeduction = hItem.amount < 0
                                            val absAmount = kotlin.math.abs(hItem.amount)
                                            Text(
                                                text = if (isDeduction) "- Rp ${formatter.format(absAmount)}" else "+ Rp ${formatter.format(hItem.amount)}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isDeduction) Rose500 else Emerald500
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            if (isDeduction) {
                                                val isTransfer = hItem.note.contains("Ditransfer", ignoreCase = true) || hItem.note.contains("Transferred", ignoreCase = true)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if (isTransfer) Blue500.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.15f))
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (isTransfer) "Transfer" else "Offset",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isTransfer) Blue500 else Amber500
                                                        )
                                                    )
                                                }
                                            } else {
                                                if (hItem.transactionId != null) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Blue500.copy(alpha = 0.15f))
                                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Bill Split",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Blue500
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Amber500.copy(alpha = 0.15f))
                                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Manual",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Amber500
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (hItem.note.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = hItem.note,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                )
                                            )
                                        }
                                    }
                                    Text(
                                        text = timeFormat.format(Date(hItem.timestamp)),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!entry.isSettled) {

                        Button(
                            onClick = onSettle,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Settled", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }

                        OutlinedButton(
                            onClick = onRemind,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remind", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                        }
                    } else {

                        OutlinedButton(
                            onClick = onUnsettle,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Undo Settled", fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false)
                        }

                        if (entry.settledBankName != null) {
                            Text(
                                text = "Received in: ${entry.settledBankName}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Rose500,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettleConfirmationDialog(
    entry: ShieldEntry,
    banks: List<Bank>,
    onDismiss: () -> Unit,
    onConfirm: (bankId: Long?, bankName: String?, addToBalance: Boolean) -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    var addToBalance by remember { mutableStateOf(true) }
    var selectedBank by remember { mutableStateOf(banks.firstOrNull()) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Settlement", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Mark ${entry.friendName}'s receivable of Rp ${formatter.format(entry.amount)} as settled?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Add to Wallet Balance?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Automatically record as income and add to your wallet balance.",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                        )
                    }
                    Switch(
                        checked = addToBalance,
                        onCheckedChange = { addToBalance = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Emerald500)
                    )
                }

                if (addToBalance && banks.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedBank?.name ?: "Select Wallet / Account",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Receiving Wallet") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            banks.forEach { bank ->
                                DropdownMenuItem(
                                    text = { Text(bank.name, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        selectedBank = bank
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        if (addToBalance) selectedBank?.id else null,
                        if (addToBalance) selectedBank?.name else null,
                        addToBalance
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text("Mark Settled", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RemindFriendDialog(
    entry: ShieldEntry,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val message = remember(entry) {
        val itemDesc = if (entry.note.isNotEmpty()) " (${entry.note})" else ""
        "Hi ${entry.friendName}! Friendly reminder about our split bill$itemDesc of Rp ${formatter.format(entry.amount)}. Let me know whenever you get a chance to transfer, thanks!"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Emerald500)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Remind ${entry.friendName}", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Friendly reminder message preview:",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onShare(message) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text("Send / Share", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onCopy(message) }) {
                Text("Copy Text")
            }
        }
    )
}

data class WalletAllocationDraft(
    val id: Long = System.nanoTime(),
    val bank: Bank? = null,
    val amountText: String = ""
)

enum class ShieldDeductAmountMode {
    DIFFERENCE,
    FULL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShieldWalletAllocationSection(
    banks: List<Bank>,
    currentShieldAmount: Double,
    newShieldAmount: Double,
    isDeductEnabled: Boolean,
    onDeductEnabledChange: (Boolean) -> Unit,
    deductMode: ShieldDeductAmountMode,
    onDeductModeChange: (ShieldDeductAmountMode) -> Unit,
    allocations: List<WalletAllocationDraft>,
    onAllocationsChange: (List<WalletAllocationDraft>) -> Unit,
    onOpenCalculator: (initial: String, onApply: (String) -> Unit) -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val canShowDifference = currentShieldAmount > 0 && newShieldAmount > currentShieldAmount
    val targetAmount = when {
        canShowDifference && deductMode == ShieldDeductAmountMode.DIFFERENCE -> newShieldAmount - currentShieldAmount
        else -> newShieldAmount
    }
    val totalAllocated = allocations.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
    val diff = targetAmount - totalAllocated
    val isExactMatch = kotlin.math.abs(diff) < 0.01

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeductEnabled) Emerald500.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = if (isDeductEnabled) 1.5.dp else 1.dp,
            color = if (isDeductEnabled) Emerald500.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDeductEnabled) Emerald500.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = if (isDeductEnabled) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Deduct from Wallet / Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Deduct wallet balance for this Shield entry",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }
                Switch(
                    checked = isDeductEnabled,
                    onCheckedChange = onDeductEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Emerald500
                    )
                )
            }

            if (isDeductEnabled) {
                if (banks.isEmpty()) {
                    Text(
                        text = "No wallets/accounts registered yet.",
                        color = Rose500,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {

                    if (canShowDifference) {
                        val diffAmount = newShieldAmount - currentShieldAmount
                        Text(
                            text = "Select Amount to Deduct:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onDeductModeChange(ShieldDeductAmountMode.DIFFERENCE) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (deductMode == ShieldDeductAmountMode.DIFFERENCE) Emerald500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    width = if (deductMode == ShieldDeductAmountMode.DIFFERENCE) 1.5.dp else 1.dp,
                                    color = if (deductMode == ShieldDeductAmountMode.DIFFERENCE) Emerald500 else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Difference Only (+)",
                                        fontSize = 11.sp,
                                        fontWeight = if (deductMode == ShieldDeductAmountMode.DIFFERENCE) FontWeight.Bold else FontWeight.Medium,
                                        color = if (deductMode == ShieldDeductAmountMode.DIFFERENCE) Emerald500 else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Rp ${formatter.format(diffAmount)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (deductMode == ShieldDeductAmountMode.DIFFERENCE) Emerald500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onDeductModeChange(ShieldDeductAmountMode.FULL) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (deductMode == ShieldDeductAmountMode.FULL) Emerald500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    width = if (deductMode == ShieldDeductAmountMode.FULL) 1.5.dp else 1.dp,
                                    color = if (deductMode == ShieldDeductAmountMode.FULL) Emerald500 else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Full Total",
                                        fontSize = 11.sp,
                                        fontWeight = if (deductMode == ShieldDeductAmountMode.FULL) FontWeight.Bold else FontWeight.Medium,
                                        color = if (deductMode == ShieldDeductAmountMode.FULL) Emerald500 else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Rp ${formatter.format(newShieldAmount)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (deductMode == ShieldDeductAmountMode.FULL) Emerald500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target Wallet Deduction:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "Rp ${formatter.format(targetAmount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Emerald500
                            )
                        }
                    }

                    Text(
                        text = if (allocations.size > 1) "Per-Wallet Allocation (${allocations.size} wallets):" else "Select Wallet:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    allocations.forEachIndexed { index, alloc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    var dropdownExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        ExposedDropdownMenuBox(
                                            expanded = dropdownExpanded,
                                            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                                        ) {
                                            OutlinedTextField(
                                                value = alloc.bank?.let { "${it.name} (Bal: Rp ${formatter.format(it.balance)})" } ?: "Select Wallet",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Wallet #${index + 1}") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            ExposedDropdownMenu(
                                                expanded = dropdownExpanded,
                                                onDismissRequest = { dropdownExpanded = false }
                                            ) {
                                                banks.forEach { b ->
                                                    val isAlreadyUsedInOtherRow = allocations.any { it.id != alloc.id && it.bank?.id == b.id }
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = b.name,
                                                                    fontWeight = if (b.id == alloc.bank?.id) FontWeight.Bold else FontWeight.Normal,
                                                                    color = if (isAlreadyUsedInOtherRow) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                                                                )
                                                                Text(
                                                                    text = if (isAlreadyUsedInOtherRow) "(Already selected)" else "Rp ${formatter.format(b.balance)}",
                                                                    fontSize = 11.sp,
                                                                    color = if (isAlreadyUsedInOtherRow) Rose500.copy(alpha = 0.7f) else Emerald500
                                                                )
                                                            }
                                                        },
                                                        onClick = {
                                                            val updated = allocations.toMutableList()
                                                            updated[index] = alloc.copy(bank = b)
                                                            onAllocationsChange(updated)
                                                            dropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (allocations.size > 1) {
                                        IconButton(
                                            onClick = {
                                                val updated = allocations.toMutableList()
                                                updated.removeAt(index)
                                                onAllocationsChange(updated)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Wallet",
                                                tint = Rose500,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = alloc.amountText,
                                    onValueChange = { newTxt ->
                                        if (newTxt.all { it.isDigit() }) {
                                            val updated = allocations.toMutableList()
                                            updated[index] = alloc.copy(amountText = newTxt)
                                            onAllocationsChange(updated)
                                        }
                                    },
                                    label = { Text("Amount from ${alloc.bank?.name ?: "Wallet"} (Rp)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    trailingIcon = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            if (allocations.size > 1) {
                                                val otherSum = allocations.filterIndexed { i, _ -> i != index }
                                                    .sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
                                                val rem = (targetAmount - otherSum).coerceAtLeast(0.0)
                                                Surface(
                                                    modifier = Modifier
                                                        .clickable {
                                                            val updated = allocations.toMutableList()
                                                            updated[index] = alloc.copy(amountText = rem.toLong().toString())
                                                            onAllocationsChange(updated)
                                                        },
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Blue500.copy(alpha = 0.12f)
                                                ) {
                                                    Text(
                                                        text = "Rest",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Blue500,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    onOpenCalculator(alloc.amountText.ifBlank { "0" }) { res ->
                                                        val num = res.toDoubleOrNull() ?: 0.0
                                                        val updated = allocations.toMutableList()
                                                        updated[index] = alloc.copy(
                                                            amountText = if (num % 1.0 == 0.0) num.toLong().toString() else res
                                                        )
                                                        onAllocationsChange(updated)
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Calculate,
                                                    contentDescription = "Calculator",
                                                    tint = Emerald500,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (allocations.size < banks.size) {
                            OutlinedButton(
                                onClick = {
                                    val usedBankIds = allocations.mapNotNull { it.bank?.id }.toSet()
                                    val nextBank = banks.firstOrNull { it.id !in usedBankIds } ?: banks.firstOrNull()
                                    val alreadyAllocated = allocations.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
                                    val remaining = (targetAmount - alreadyAllocated).coerceAtLeast(0.0)
                                    val newDraft = WalletAllocationDraft(
                                        bank = nextBank,
                                        amountText = if (remaining > 0) remaining.toLong().toString() else "0"
                                    )
                                    onAllocationsChange(allocations + newDraft)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Use > 1 Wallet", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (allocations.size > 1) {
                            OutlinedButton(
                                onClick = {
                                    val count = allocations.size
                                    if (count > 0 && targetAmount > 0) {
                                        val perWallet = (targetAmount / count).toLong()
                                        val remainder = targetAmount.toLong() - (perWallet * count)
                                        val updated = allocations.mapIndexed { i, item ->
                                            val amt = if (i == 0) perWallet + remainder else perWallet
                                            item.copy(amountText = amt.toString())
                                        }
                                        onAllocationsChange(updated)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 10.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Blue500)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Split Evenly", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Blue500)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isExactMatch -> Emerald500.copy(alpha = 0.12f)
                            diff > 0 -> Amber500.copy(alpha = 0.12f)
                            else -> Rose500.copy(alpha = 0.12f)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isExactMatch -> Emerald500.copy(alpha = 0.4f)
                                diff > 0 -> Amber500.copy(alpha = 0.4f)
                                else -> Rose500.copy(alpha = 0.4f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when {
                                    isExactMatch -> "✓ Exact Allocation (${allocations.size} wallet)"
                                    diff > 0 -> "⚠️ Short by Rp ${formatter.format(diff)}"
                                    else -> "⚠️ Exceeds by Rp ${formatter.format(-diff)}"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isExactMatch -> Emerald500
                                    diff > 0 -> Amber500
                                    else -> Rose500
                                }
                            )
                            Text(
                                text = "Rp ${formatter.format(totalAllocated)} / Rp ${formatter.format(targetAmount)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    isExactMatch -> Emerald500
                                    diff > 0 -> Amber500
                                    else -> Rose500
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualAddShieldDialog(
    existingShields: List<ShieldEntry> = emptyList(),
    contacts: List<ShieldContact> = emptyList(),
    banks: List<Bank> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        friendName: String,
        amount: Double,
        note: String,
        timestamp: Long,
        avatarUri: String?,
        walletDeductions: List<ShieldWalletDeduction>
    ) -> Unit
) {
    val context = LocalContext.current
    var friendName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showCalc by remember { mutableStateOf(false) }
    var customAvatarUri by remember { mutableStateOf<String?>(null) }
    var showContactPicker by remember { mutableStateOf(false) }

    var deductFromWallet by remember { mutableStateOf(false) }
    var deductMode by remember { mutableStateOf(ShieldDeductAmountMode.FULL) }
    var walletAllocations by remember {
        mutableStateOf(
            listOf(
                WalletAllocationDraft(
                    bank = banks.firstOrNull(),
                    amountText = ""
                )
            )
        )
    }
    var calcForAllocationCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }
    var calcForAllocationInitial by remember { mutableStateOf("0") }

    val currentNewAmount = amountText.toDoubleOrNull() ?: 0.0

    LaunchedEffect(currentNewAmount) {
        if (walletAllocations.size == 1 && currentNewAmount > 0) {
            val single = walletAllocations.first()
            walletAllocations = listOf(
                single.copy(
                    amountText = if (currentNewAmount % 1.0 == 0.0) currentNewAmount.toLong().toString() else currentNewAmount.toString()
                )
            )
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveShieldAvatar(context, uri, friendName.ifBlank { "contact" })
            if (savedPath != null) {
                customAvatarUri = savedPath
            }
        }
    }

    val matchedContact = remember(friendName, contacts) {
        val trimmed = friendName.trim()
        if (trimmed.isEmpty()) null
        else contacts.find { it.name.trim().equals(trimmed, ignoreCase = true) }
    }

    val matchingExisting = remember(friendName, existingShields) {
        val trimmed = friendName.trim()
        if (trimmed.isEmpty()) null
        else existingShields.find { !it.isSettled && it.friendName.trim().equals(trimmed, ignoreCase = true) }
    }

    val effectiveAvatarUri = customAvatarUri ?: matchedContact?.avatarUri ?: matchingExisting?.avatarUri

    if (showContactPicker) {
        SearchableContactDialog(
            contacts = contacts,
            existingShields = existingShields,
            onDismiss = { showContactPicker = false },
            onSelectContact = { name, avatar ->
                friendName = name
                customAvatarUri = avatar
                showContactPicker = false
            }
        )
    }

    if (showCalc) {
        CalculatorDialog(
            initialAmount = amountText,
            onDismiss = { showCalc = false },
            onApply = { res ->
                amountText = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
                showCalc = false
            }
        )
    }

    if (calcForAllocationCallback != null) {
        CalculatorDialog(
            initialAmount = calcForAllocationInitial,
            onDismiss = { calcForAllocationCallback = null },
            onApply = { res ->
                calcForAllocationCallback?.invoke(res.toString())
                calcForAllocationCallback = null
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Emerald500)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record New Shield", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                if (contacts.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showContactPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Emerald500.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PersonSearch,
                                    contentDescription = null,
                                    tint = Emerald500,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Select from Shield Contacts (${contacts.size} saved)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald500
                                    )
                                )
                            }
                            Text("Search ➔", style = MaterialTheme.typography.labelSmall.copy(color = Emerald500, fontWeight = FontWeight.Bold))
                        }
                    }
                }

                OutlinedTextField(
                    value = friendName,
                    onValueChange = {
                        friendName = it
                        customAvatarUri = null
                    },
                    label = { Text("Friend Name") },
                    placeholder = { Text("e.g. Budi, Sarah") },
                    singleLine = true,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (friendName.isNotBlank()) {
                                IconButton(onClick = {
                                    friendName = ""
                                    customAvatarUri = null
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                            if (contacts.isNotEmpty()) {
                                IconButton(onClick = { showContactPicker = true }) {
                                    Icon(
                                        imageVector = Icons.Default.PersonSearch,
                                        contentDescription = "Search Contact",
                                        tint = Emerald500
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (friendName.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (matchedContact != null) Emerald500.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (matchedContact != null) Emerald500.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShieldAvatarView(
                                avatarUri = effectiveAvatarUri,
                                friendName = friendName,
                                size = 46.dp,
                                showCameraBadge = true,
                                onClick = { photoPickerLauncher.launch("image/*") }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = friendName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (matchedContact != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Emerald500.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "Saved Contact",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald500
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                if (matchedContact != null) {
                                    if (effectiveAvatarUri != null) {
                                        Text(
                                            text = "✓ Profile photo linked automatically from previous records",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Emerald500, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                        )
                                    } else {
                                        Text(
                                            text = "Tap avatar to add a profile photo",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "New contact • Name and photo will be saved for future use",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (matchingExisting != null) {
                    val formatter = NumberFormat.getNumberInstance(Locale.US)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Amber500.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Amber500, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${matchingExisting.friendName} already has an active balance (Rp ${formatter.format(matchingExisting.amount)})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Amber500)
                                )
                                Text(
                                    text = "Amount will be added to this friend's existing card & history.",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) amountText = it },
                    label = { Text("Amount Owed (Rp)") },
                    placeholder = { Text("0") },
                    trailingIcon = {
                        IconButton(onClick = { showCalc = true }) {
                            Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = Emerald500)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Items / Order / Merchant (Optional)") },
                    placeholder = { Text("e.g. Padang Rice + Iced Tea") },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (banks.isNotEmpty()) {
                    ShieldWalletAllocationSection(
                        banks = banks,
                        currentShieldAmount = 0.0,
                        newShieldAmount = currentNewAmount,
                        isDeductEnabled = deductFromWallet,
                        onDeductEnabledChange = { enabled ->
                            deductFromWallet = enabled
                            if (enabled && walletAllocations.size == 1 && currentNewAmount > 0) {
                                val single = walletAllocations.first()
                                walletAllocations = listOf(
                                    single.copy(
                                        amountText = if (currentNewAmount % 1.0 == 0.0) currentNewAmount.toLong().toString() else currentNewAmount.toString()
                                    )
                                )
                            }
                        },
                        deductMode = deductMode,
                        onDeductModeChange = { deductMode = it },
                        allocations = walletAllocations,
                        onAllocationsChange = { walletAllocations = it },
                        onOpenCalculator = { initial, onApply ->
                            calcForAllocationInitial = initial
                            calcForAllocationCallback = onApply
                        }
                    )
                }
            }
        },
        confirmButton = {
            val totalAllocated = walletAllocations.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
            val isDeductValid = !deductFromWallet || (
                banks.isNotEmpty() &&
                walletAllocations.isNotEmpty() &&
                walletAllocations.all { it.bank != null && (it.amountText.toDoubleOrNull() ?: 0.0) > 0 } &&
                walletAllocations.mapNotNull { it.bank?.id }.distinct().size == walletAllocations.size &&
                kotlin.math.abs(currentNewAmount - totalAllocated) < 0.01
            )
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (friendName.isNotBlank() && amt > 0 && isDeductValid) {
                        val deductions = if (deductFromWallet) {
                            walletAllocations.map {
                                ShieldWalletDeduction(
                                    bankId = it.bank!!.id,
                                    bankName = it.bank!!.name,
                                    amount = it.amountText.toDoubleOrNull() ?: 0.0
                                )
                            }
                        } else emptyList()

                        onSave(friendName.trim(), amt, note.trim(), System.currentTimeMillis(), effectiveAvatarUri, deductions)
                    }
                },
                enabled = friendName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0 && isDeductValid,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text(
                    text = if (matchingExisting != null) "Add to Balance" else "Save",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditShieldDialog(
    entry: ShieldEntry,
    banks: List<Bank>,
    onDismiss: () -> Unit,
    onViewFullPhoto: (String) -> Unit = {},
    onSave: (updatedEntry: ShieldEntry, walletDeductions: List<ShieldWalletDeduction>) -> Unit,
    onPartialPayment: (
        amount: Double,
        isTransfer: Boolean,
        bankId: Long?,
        bankName: String?,
        note: String,
        updatedFriendName: String?,
        updatedNote: String?
    ) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    var friendName by remember { mutableStateOf(entry.friendName) }
    var amountText by remember {
        mutableStateOf(
            if (entry.amount % 1.0 == 0.0) entry.amount.toLong().toString() else entry.amount.toString()
        )
    }
    var note by remember { mutableStateOf(entry.note) }
    var currentAvatarUri by remember { mutableStateOf(entry.avatarUri) }
    val editAvatarContext = LocalContext.current
    val editAvatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = saveShieldAvatar(editAvatarContext, uri, friendName.ifBlank { entry.friendName })
            if (saved != null) {
                currentAvatarUri = saved
            }
        }
    }

    var deductFromWallet by remember { mutableStateOf(false) }
    var deductMode by remember { mutableStateOf(ShieldDeductAmountMode.DIFFERENCE) }
    var walletAllocations by remember {
        mutableStateOf(
            listOf(
                WalletAllocationDraft(
                    bank = banks.firstOrNull(),
                    amountText = if (entry.amount % 1.0 == 0.0) entry.amount.toLong().toString() else entry.amount.toString()
                )
            )
        )
    }
    var calcForAllocationCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }
    var calcForAllocationInitial by remember { mutableStateOf("0") }

    val currentNewAmount = amountText.toDoubleOrNull() ?: 0.0
    val canShowDifference = entry.amount > 0 && currentNewAmount > entry.amount
    val effectiveTargetDeductAmount = when {
        canShowDifference && deductMode == ShieldDeductAmountMode.DIFFERENCE -> currentNewAmount - entry.amount
        else -> currentNewAmount
    }

    LaunchedEffect(effectiveTargetDeductAmount, deductMode) {
        if (walletAllocations.size == 1 && effectiveTargetDeductAmount > 0) {
            val single = walletAllocations.first()
            walletAllocations = listOf(
                single.copy(
                    amountText = if (effectiveTargetDeductAmount % 1.0 == 0.0) effectiveTargetDeductAmount.toLong().toString() else effectiveTargetDeductAmount.toString()
                )
            )
        }
    }

    var deductionText by remember { mutableStateOf("") }
    var isTransferMethod by remember { mutableStateOf(true) }
    var selectedBank by remember { mutableStateOf(banks.firstOrNull()) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var deductionNote by remember { mutableStateOf("") }

    var showEditCalc by remember { mutableStateOf(false) }
    var showDeductionCalc by remember { mutableStateOf(false) }

    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val deductionAmount = deductionText.toDoubleOrNull() ?: 0.0
    val isDeductionValid = deductionAmount > 0 && deductionAmount <= entry.amount
    val isTransferValid = !isTransferMethod || selectedBank != null || banks.isEmpty()
    val remainingAfterDeduction = (entry.amount - deductionAmount).coerceAtLeast(0.0)
    val isFullyPaidAfterDeduction = deductionAmount >= entry.amount && deductionAmount > 0

    if (showEditCalc) {
        CalculatorDialog(
            initialAmount = amountText,
            onDismiss = { showEditCalc = false },
            onApply = { res ->
                amountText = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
                showEditCalc = false
            }
        )
    }

    if (showDeductionCalc) {
        CalculatorDialog(
            initialAmount = deductionText.ifBlank { "0" },
            onDismiss = { showDeductionCalc = false },
            onApply = { res ->
                deductionText = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
                showDeductionCalc = false
            }
        )
    }

    if (calcForAllocationCallback != null) {
        CalculatorDialog(
            initialAmount = calcForAllocationInitial,
            onDismiss = { calcForAllocationCallback = null },
            onApply = { res ->
                calcForAllocationCallback?.invoke(res.toString())
                calcForAllocationCallback = null
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (selectedTab == 0) Icons.Default.Edit else Icons.Default.Payments,
                    contentDescription = null,
                    tint = Emerald500
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedTab == 0) "Edit Shield Record" else "Partial Payment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (!entry.isSettled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 0) Emerald500 else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Edit Details",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 1) Emerald500 else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Partial Payment",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                if (selectedTab == 0) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShieldAvatarView(
                            avatarUri = currentAvatarUri,
                            friendName = friendName.ifBlank { entry.friendName },
                            isSettled = entry.isSettled,
                            size = 54.dp,
                            showCameraBadge = false,
                            onClick = if (currentAvatarUri != null) {
                                { onViewFullPhoto(currentAvatarUri!!) }
                            } else null
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Profile Photo (Tap photo to preview)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { editAvatarPicker.launch("image/*") },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (currentAvatarUri != null) "Change Photo" else "Choose Photo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                if (currentAvatarUri != null) {
                                    IconButton(
                                        onClick = { currentAvatarUri = null },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove Photo",
                                            tint = Rose500,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = friendName,
                        onValueChange = { friendName = it },
                        label = { Text("Friend Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) amountText = it },
                        label = { Text("Amount Owed (Rp)") },
                        trailingIcon = {
                            IconButton(onClick = { showEditCalc = true }) {
                                Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = Emerald500)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Items / Order / Note") },
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (banks.isNotEmpty()) {
                        ShieldWalletAllocationSection(
                            banks = banks,
                            currentShieldAmount = entry.amount,
                            newShieldAmount = currentNewAmount,
                            isDeductEnabled = deductFromWallet,
                            onDeductEnabledChange = { enabled ->
                                deductFromWallet = enabled
                                if (enabled && walletAllocations.size == 1 && effectiveTargetDeductAmount > 0) {
                                    val single = walletAllocations.first()
                                    walletAllocations = listOf(
                                        single.copy(
                                            amountText = if (effectiveTargetDeductAmount % 1.0 == 0.0) effectiveTargetDeductAmount.toLong().toString() else effectiveTargetDeductAmount.toString()
                                        )
                                    )
                                }
                            },
                            deductMode = deductMode,
                            onDeductModeChange = { deductMode = it },
                            allocations = walletAllocations,
                            onAllocationsChange = { walletAllocations = it },
                            onOpenCalculator = { initial, onApply ->
                                calcForAllocationInitial = initial
                                calcForAllocationCallback = onApply
                            }
                        )
                    }

                    if (!entry.isSettled) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTab = 1 },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Emerald500.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Emerald500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Friend paying in installments?",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Emerald500)
                                    )
                                    Text(
                                        text = "Switch to Partial Payment tab to reduce balance & record transfer or offset.",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }
                } else {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current Active Balance",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                                Text(
                                    text = "Rp ${formatter.format(entry.amount)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Rose500
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Friend",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                                Text(
                                    text = friendName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = deductionText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) deductionText = it },
                        label = { Text("Payment Amount (Rp)") },
                        trailingIcon = {
                            IconButton(onClick = { showDeductionCalc = true }) {
                                Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = Emerald500)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val percentages = listOf(0.25 to "25%", 0.50 to "50%", 0.75 to "75%", 1.0 to "Full")
                        percentages.forEach { (pct, label) ->
                            val calcAmt = (entry.amount * pct).toLong()
                            val isSelected = deductionText == calcAmt.toString()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Emerald500.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Emerald500 else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { deductionText = calcAmt.toString() }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Emerald500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Repayment Method:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTransferMethod = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTransferMethod) Emerald500.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isTransferMethod) 1.5.dp else 1.dp,
                            color = if (isTransferMethod) Emerald500 else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isTransferMethod,
                                onClick = { isTransferMethod = true },
                                colors = RadioButtonDefaults.colors(selectedColor = Emerald500)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Transferred (Record as Income)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Funds deposited into your wallet & added to balance automatically.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }

                    if (isTransferMethod && banks.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = bankDropdownExpanded,
                            onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedBank?.name ?: "Select Receiving Wallet",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Receiving Wallet *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = bankDropdownExpanded,
                                onDismissRequest = { bankDropdownExpanded = false }
                            ) {
                                banks.forEach { bank ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(bank.name, fontWeight = FontWeight.Medium)
                                                Text(
                                                    "Rp ${formatter.format(bank.balance)}",
                                                    fontSize = 12.sp,
                                                    color = Emerald500
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedBank = bank
                                            bankDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTransferMethod = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!isTransferMethod) Emerald500.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (!isTransferMethod) 1.5.dp else 1.dp,
                            color = if (!isTransferMethod) Emerald500 else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !isTransferMethod,
                                onClick = { isTransferMethod = false },
                                colors = RadioButtonDefaults.colors(selectedColor = Emerald500)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "They Paid for Something Else (Offset)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Reduce receivable balance directly without changing wallet balance.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = deductionNote,
                        onValueChange = { deductionNote = it },
                        label = {
                            Text(
                                if (!isTransferMethod) "Note (e.g., Covered coffee, parking, etc.)"
                                else "Additional Note (Optional)"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (deductionAmount > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    deductionAmount > entry.amount -> Rose500.copy(alpha = 0.12f)
                                    isFullyPaidAfterDeduction -> Emerald500.copy(alpha = 0.12f)
                                    else -> Amber500.copy(alpha = 0.12f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when {
                                        deductionAmount > entry.amount -> "⚠️ Exceeds total balance!"
                                        isFullyPaidAfterDeduction -> "Status: Settled in Full! ✓"
                                        else -> "New Remaining Balance:"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            deductionAmount > entry.amount -> Rose500
                                            isFullyPaidAfterDeduction -> Emerald500
                                            else -> Amber500
                                        }
                                    )
                                )
                                Text(
                                    text = when {
                                        deductionAmount > entry.amount -> "Max Rp ${formatter.format(entry.amount)}"
                                        isFullyPaidAfterDeduction -> "Rp 0"
                                        else -> "Rp ${formatter.format(remainingAfterDeduction)}"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when {
                                            deductionAmount > entry.amount -> Rose500
                                            isFullyPaidAfterDeduction -> Emerald500
                                            else -> Amber500
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0) {
                val totalAllocated = walletAllocations.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
                val isDeductValid = !deductFromWallet || (
                    banks.isNotEmpty() &&
                    walletAllocations.isNotEmpty() &&
                    walletAllocations.all { it.bank != null && (it.amountText.toDoubleOrNull() ?: 0.0) > 0 } &&
                    walletAllocations.mapNotNull { it.bank?.id }.distinct().size == walletAllocations.size &&
                    kotlin.math.abs(effectiveTargetDeductAmount - totalAllocated) < 0.01
                )
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (friendName.isNotBlank() && amt > 0 && isDeductValid) {
                            val deductions = if (deductFromWallet) {
                                walletAllocations.map {
                                    ShieldWalletDeduction(
                                        bankId = it.bank!!.id,
                                        bankName = it.bank!!.name,
                                        amount = it.amountText.toDoubleOrNull() ?: 0.0
                                    )
                                }
                            } else emptyList()

                            onSave(
                                entry.copy(
                                    friendName = friendName.trim(),
                                    amount = amt,
                                    note = note.trim(),
                                    avatarUri = currentAvatarUri
                                ),
                                deductions
                            )
                        }
                    },
                    enabled = friendName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0 && isDeductValid,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (isDeductionValid && isTransferValid) {
                            onPartialPayment(
                                deductionAmount,
                                isTransferMethod,
                                if (isTransferMethod) selectedBank?.id else null,
                                if (isTransferMethod) selectedBank?.name else null,
                                deductionNote.trim(),
                                friendName.trim(),
                                note.trim()
                            )
                        }
                    },
                    enabled = isDeductionValid && isTransferValid,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Text(
                        text = if (isFullyPaidAfterDeduction) "Settle Balance" else "Deduct Balance",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
