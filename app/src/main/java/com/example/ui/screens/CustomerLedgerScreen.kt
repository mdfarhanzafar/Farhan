package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.components.ReminderBottomSheetDialog
import com.example.ui.components.TransactionReceiptDialog
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(
    customerId: Long,
    viewModel: HisabViewModel,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val customer = customers.find { it.id == customerId }
    val userProfile by viewModel.userProfile.collectAsState()

    val transactionsFlow = remember(customerId) {
        viewModel.getTransactionsForParty("CUSTOMER", customerId)
    }
    val transactions by transactionsFlow.collectAsState(initial = emptyList())

    var showReminderDialog by remember { mutableStateOf(false) }
    var selectedTransactionForReceipt by remember { mutableStateOf<TransactionEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isYouWillGet = customer.currentBalance > 0
    val isYouWillGive = customer.currentBalance < 0
    val isSettled = customer.currentBalance == 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = customer.mobile,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Customers) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${customer.mobile}")
                        }
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = HisabNavyPrimary)
                    }
                    IconButton(onClick = {
                        val clean = customer.mobile.replace(Regex("[^0-9]"), "")
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?phone=$clean")
                        }
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                    }
                    var expandedMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Send Reminder") },
                            onClick = {
                                expandedMenu = false
                                showReminderDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Customer") },
                            onClick = {
                                expandedMenu = false
                                showDeleteConfirmDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MoneyOutRed) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Prominent You Gave (Debit) & You Got (Credit) Buttons
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            onNavigate(Screen.AddTransaction(partyType = "CUSTOMER", partyId = customer.id, initialType = "YOU_GAVE"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MoneyOutRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("You Gave (Debit)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            onNavigate(Screen.AddTransaction(partyType = "CUSTOMER", partyId = customer.id, initialType = "YOU_GOT"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MoneyInGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("You Got (Credit)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Net Balance Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isYouWillGet -> MoneyInGreenLight
                            isYouWillGive -> MoneyOutRedLight
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isYouWillGet -> MoneyInGreen.copy(alpha = 0.5f)
                            isYouWillGive -> MoneyOutRed.copy(alpha = 0.5f)
                            else -> CardBorderLight
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when {
                                isYouWillGet -> "You Will Receive (Aapko Milenge)"
                                isYouWillGive -> "You Will Pay (Aapko Dene Hain)"
                                else -> "Account Settled"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isYouWillGet -> MoneyInGreenDark
                                    isYouWillGive -> MoneyOutRedDark
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSettled) "₹0" else formatCurrency(abs(customer.currentBalance)),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    isYouWillGet -> MoneyInGreenDark
                                    isYouWillGive -> MoneyOutRedDark
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        )
                        if (customer.address.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📍 ${customer.address}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showReminderDialog = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Reminder", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    val summaryText = """
                                        *Hisab Kitab Account Statement*
                                        Customer: ${customer.name}
                                        Current Balance: ${if (isYouWillGet) "You Will Get ${formatCurrency(customer.currentBalance)}" else "You Will Give ${formatCurrency(abs(customer.currentBalance))}"}
                                        Total Transactions: ${transactions.size}
                                        
                                        Powered by Hisab Kitab - Apna Hisab, Apne Haath
                                    """.trimIndent()
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, summaryText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Ledger Statement"))
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Statement", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Timeline Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions Timeline (${transactions.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Transaction Items
            if (transactions.isEmpty()) {
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
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No transactions yet", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "Use the buttons below to record 'You Gave' (Sale) or 'You Got' (Payment).",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            )
                        }
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    val isGot = tx.type == "YOU_GOT"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTransactionForReceipt = tx },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isGot) MoneyInGreenLight else MoneyOutRedLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isGot) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = if (isGot) MoneyInGreen else MoneyOutRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (tx.description.isNotEmpty()) tx.description else (if (isGot) "Payment Received" else "Credit Given"),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${formatDate(tx.date)} • Mode: ${tx.paymentMethod}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    if (tx.notes.isNotEmpty()) {
                                        Text(
                                            text = "Note: ${tx.notes}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }
                            }

                            // Right: Amount + delete
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (isGot) "+ " else "- "}${formatCurrency(tx.amount)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isGot) MoneyInGreenDark else MoneyOutRedDark
                                        )
                                    )
                                    Text(
                                        text = if (isGot) "You Got (Credit)" else "You Gave (Debit)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isGot) MoneyInGreen else MoneyOutRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        viewModel.deleteTransaction(tx)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
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
    if (showReminderDialog) {
        ReminderBottomSheetDialog(
            customer = customer,
            businessName = userProfile?.businessName ?: "Sharma Kirana Store",
            onDismiss = { showReminderDialog = false }
        )
    }

    // Delete Confirm Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Customer?") },
            text = { Text("Are you sure you want to delete ${customer.name}? All ledger transaction history will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(customer)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyOutRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
