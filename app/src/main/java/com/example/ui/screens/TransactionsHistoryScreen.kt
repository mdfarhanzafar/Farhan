package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.TransactionEntity
import com.example.ui.components.TransactionReceiptDialog
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsHistoryScreen(
    viewModel: HisabViewModel,
    onNavigate: (Screen) -> Unit
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var filterType by remember { mutableStateOf("All") } // All, You Got, You Gave, Customers, Suppliers
    var selectedTransactionForReceipt by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTransactions = remember(allTransactions, filterType) {
        when (filterType) {
            "You Got" -> allTransactions.filter { it.type == "YOU_GOT" }
            "You Gave" -> allTransactions.filter { it.type == "YOU_GAVE" }
            "Customers" -> allTransactions.filter { it.partyType == "CUSTOMER" }
            "Suppliers" -> allTransactions.filter { it.partyType == "SUPPLIER" }
            else -> allTransactions
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Transactions (${allTransactions.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "You Got", "You Gave", "Customers", "Suppliers").forEach { f ->
                    item {
                        FilterChip(
                            selected = filterType == f,
                            onClick = { filterType = f },
                            label = { Text(f) }
                        )
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions found", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        val isGot = tx.type == "YOU_GOT"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTransactionForReceipt = tx },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                        Text(tx.partyName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("${formatDate(tx.date)} • ${tx.paymentMethod} • ${tx.partyType}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
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
                                        style = MaterialTheme.typography.labelSmall.copy(color = if (isGot) MoneyInGreen else MoneyOutRed, fontWeight = FontWeight.Bold)
                                    )
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
}
