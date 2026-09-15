package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.entity.SupplierEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.components.TransactionReceiptDialog
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierLedgerScreen(
    supplierId: Long,
    viewModel: HisabViewModel,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val suppliers by viewModel.suppliers.collectAsState()
    val supplier = suppliers.find { it.id == supplierId }
    val userProfile by viewModel.userProfile.collectAsState()

    val transactionsFlow = remember(supplierId) {
        viewModel.getTransactionsForParty("SUPPLIER", supplierId)
    }
    val transactions by transactionsFlow.collectAsState(initial = emptyList())

    var selectedTransactionForReceipt by remember { mutableStateOf<TransactionEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (supplier == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isPayable = supplier.currentBalance > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(supplier.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("${supplier.category} • ${supplier.mobile}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Suppliers) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${supplier.mobile}") }
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = HisabNavyPrimary)
                    }
                    IconButton(onClick = {
                        val clean = supplier.mobile.replace(Regex("[^0-9]"), "")
                        val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("https://api.whatsapp.com/send?phone=$clean") }
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MoneyOutRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
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
                            onNavigate(Screen.AddTransaction(partyType = "SUPPLIER", partyId = supplier.id, initialType = "YOU_GAVE"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MoneyInGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("You Paid (Payment)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            onNavigate(Screen.AddTransaction(partyType = "SUPPLIER", partyId = supplier.id, initialType = "YOU_GOT"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MoneyOutRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stock In (Goods)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            // Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPayable) MoneyOutRedLight else MoneyInGreenLight
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isPayable) "You Will Give (Payable to Supplier)" else "Advance Given to Supplier",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isPayable) MoneyOutRedDark else MoneyInGreenDark
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatCurrency(abs(supplier.currentBalance)),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPayable) MoneyOutRedDark else MoneyInGreenDark
                            )
                        )
                        if (supplier.address.isNotEmpty()) {
                            Text(
                                text = "📍 ${supplier.address}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }

            // Timeline header
            item {
                Text(
                    text = "Supplier Transactions Timeline (${transactions.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No supplier entries yet", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Record supplier payments or received stock using the buttons below.", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center))
                        }
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    val isPayment = tx.type == "YOU_GAVE"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTransactionForReceipt = tx },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isPayment) MoneyInGreenLight else MoneyOutRedLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPayment) Icons.Default.Payment else Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = if (isPayment) MoneyInGreen else MoneyOutRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (tx.description.isNotEmpty()) tx.description else (if (isPayment) "Payment to Supplier" else "Stock In"),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${formatDate(tx.date)} • Mode: ${tx.paymentMethod}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatCurrency(tx.amount),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isPayment) MoneyInGreenDark else MoneyOutRedDark
                                        )
                                    )
                                    Text(
                                        text = if (isPayment) "You Paid" else "Stock Added",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isPayment) MoneyInGreen else MoneyOutRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { viewModel.deleteTransaction(tx) },
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

    selectedTransactionForReceipt?.let { tx ->
        TransactionReceiptDialog(
            transaction = tx,
            businessName = userProfile?.businessName ?: "Sharma Kirana Store",
            onDismiss = { selectedTransactionForReceipt = null }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Supplier?") },
            text = { Text("Are you sure you want to delete ${supplier.name}? All transaction history with this supplier will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSupplier(supplier)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyOutRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
