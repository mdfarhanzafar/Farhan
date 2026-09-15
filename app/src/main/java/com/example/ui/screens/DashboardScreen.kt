package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.Strings

@Composable
fun DashboardScreen(
    viewModel: HisabViewModel,
    onNavigate: (Screen) -> Unit
) {
    val summary by viewModel.financialSummary.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val lang = userProfile?.language ?: "en"
    val hasUnread = notifications.any { !it.isRead }

    var selectedTransactionForReceipt by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedCustomerForReminder by remember { mutableStateOf<CustomerEntity?>(null) }
    var showReminderPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        HisabBrandHeader(
            businessName = userProfile?.businessName ?: "Sharma Kirana Store",
            hasUnreadNotifications = hasUnread,
            onNotificationClick = { onNavigate(Screen.Notifications) },
            onProfileClick = { onNavigate(Screen.Profile) },
            onSearchClick = { onNavigate(Screen.Search) }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Financial Summary Cards (Answering: How much will I get? How much will I give?)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: You Will Get & You Will Give
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: You Will Get (Credit / In)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.Customers) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoneyInGreen.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = Strings.get("you_will_get", lang),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MoneyInGreenLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = MoneyInGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = formatCurrency(summary.totalYouWillGet),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MoneyInGreenDark
                                    )
                                )
                                Text(
                                    text = "${customers.count { it.currentBalance > 0 }} pending customers",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Card 2: You Will Give (Debit / Out)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.Suppliers) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoneyOutRed.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = Strings.get("you_will_give", lang),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MoneyOutRedLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = MoneyOutRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = formatCurrency(summary.totalYouWillGive),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MoneyOutRedDark
                                    )
                                )
                                Text(
                                    text = "To suppliers & dealers",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Row 2: Today's Collection & Today's Expenses
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.Income) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MoneyInGreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Savings, contentDescription = null, tint = MoneyInGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = Strings.get("today_collection", lang),
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Text(
                                        text = formatCurrency(summary.todayCollection),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MoneyInGreenDark)
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.Expenses) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MoneyOutRedLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MoneyOutRed, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = Strings.get("today_expenses", lang),
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Text(
                                        text = formatCurrency(summary.todayExpenses),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MoneyOutRedDark)
                                    )
                                }
                            }
                        }
                    }

                    // Net Balance Strip
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(Screen.Reports) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (summary.netBalance >= 0) HisabNavyPrimary else MoneyOutRedDark
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Strings.get("net_balance", lang),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Medium)
                                )
                            }
                            Text(
                                text = "${if (summary.netBalance >= 0) "+ " else ""}${formatCurrency(summary.netBalance)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }
            }

            // Quick Actions Hub
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            QuickActionItem(
                                title = "Customer",
                                icon = Icons.Default.PersonAdd,
                                color = HisabNavyPrimary,
                                bgColor = Color(0xFFE2E8F0),
                                onClick = { onNavigate(Screen.Customers) }
                            )
                            QuickActionItem(
                                title = "Supplier",
                                icon = Icons.Default.LocalShipping,
                                color = Color(0xFF0284C7),
                                bgColor = Color(0xFFE0F2FE),
                                onClick = { onNavigate(Screen.Suppliers) }
                            )
                            QuickActionItem(
                                title = "Hisab",
                                icon = Icons.Default.AddCircle,
                                color = MoneyInGreen,
                                bgColor = MoneyInGreenLight,
                                onClick = { onNavigate(Screen.AddTransaction()) }
                            )
                            QuickActionItem(
                                title = "Expense",
                                icon = Icons.Default.Payment,
                                color = MoneyOutRed,
                                bgColor = MoneyOutRedLight,
                                onClick = { onNavigate(Screen.Expenses) }
                            )
                            QuickActionItem(
                                title = "Invoice",
                                icon = Icons.Default.Description,
                                color = PendingAmber,
                                bgColor = PendingAmberLight,
                                onClick = { onNavigate(Screen.CreateInvoice) }
                            )
                            QuickActionItem(
                                title = "Reminder",
                                icon = Icons.Default.NotificationsActive,
                                color = Color(0xFF7C3AED),
                                bgColor = Color(0xFFEDE9FE),
                                onClick = {
                                    val topPending = customers.firstOrNull { it.currentBalance > 0 }
                                    if (topPending != null) {
                                        selectedCustomerForReminder = topPending
                                    } else {
                                        showReminderPicker = true
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Mini Analytics Snapshot
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Hisab Snapshot",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            TextButton(onClick = { onNavigate(Screen.Reports) }) {
                                Text("Full Report >", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Outstanding", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(formatCurrency(summary.totalYouWillGet), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MoneyInGreenDark))
                            }
                            Column {
                                Text("Today's Net", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(formatCurrency(summary.todayCollection - summary.todayExpenses), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = HisabNavyPrimary))
                            }
                            Column {
                                Text("Active Khata", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text("${customers.size} Parties", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = { onNavigate(Screen.TransactionsHistory) }) {
                        Text("View All", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Recent Transactions List
            if (recentTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No transactions found",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                "Tap '+ Add' to record your first credit or cash entry.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            } else {
                items(recentTransactions) { tx ->
                    val isGot = tx.type == "YOU_GOT"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTransactionForReceipt = tx },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isGot) MoneyInGreenLight else MoneyOutRedLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isGot) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = if (isGot) MoneyInGreen else MoneyOutRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = tx.partyName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatDate(tx.date)} • ${tx.paymentMethod}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isGot) "+ " else "- "}${formatCurrency(tx.amount)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isGot) MoneyInGreenDark else MoneyOutRedDark
                                    )
                                )
                                Text(
                                    text = if (isGot) "You Got" else "You Gave",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isGot) MoneyInGreen else MoneyOutRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt Dialog
    selectedTransactionForReceipt?.let { tx ->
        TransactionReceiptDialog(
            transaction = tx,
            businessName = userProfile?.businessName ?: "Sharma Kirana Store",
            onDismiss = { selectedTransactionForReceipt = null }
        )
    }

    // Reminder Dialog
    selectedCustomerForReminder?.let { cust ->
        ReminderBottomSheetDialog(
            customer = cust,
            businessName = userProfile?.businessName ?: "Sharma Kirana Store",
            onDismiss = { selectedCustomerForReminder = null }
        )
    }
}
