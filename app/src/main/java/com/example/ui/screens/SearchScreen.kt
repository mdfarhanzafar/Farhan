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
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: HisabViewModel,
    onNavigate: (Screen) -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") } // All, Customers, Suppliers, Transactions, Invoices

    val matchedCustomers = remember(customers, query) {
        if (query.isBlank()) emptyList()
        else customers.filter { it.name.contains(query, ignoreCase = true) || it.mobile.contains(query) }
    }

    val matchedSuppliers = remember(suppliers, query) {
        if (query.isBlank()) emptyList()
        else suppliers.filter { it.name.contains(query, ignoreCase = true) || it.mobile.contains(query) }
    }

    val matchedTransactions = remember(transactions, query) {
        if (query.isBlank()) emptyList()
        else transactions.filter { it.partyName.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }

    val matchedInvoices = remember(invoices, query) {
        if (query.isBlank()) emptyList()
        else invoices.filter { it.invoiceNumber.contains(query, ignoreCase = true) || it.customerName.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search khata, bills, party...", fontSize = 14.sp) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                },
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Customers", "Suppliers", "Transactions", "Invoices").forEach { tab ->
                    item {
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab) }
                        )
                    }
                }
            }

            if (query.isBlank()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Search Hisab Kitab", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Type a name, mobile number, or invoice code.", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Customers
                    if (selectedTab == "All" || selectedTab == "Customers") {
                        if (matchedCustomers.isNotEmpty()) {
                            item {
                                Text("Customers (${matchedCustomers.size})", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = HisabNavyPrimary))
                            }
                            items(matchedCustomers) { c ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(Screen.CustomerLedger(c.id)) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = HisabNavyPrimary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(c.name, fontWeight = FontWeight.Bold)
                                            Text(c.mobile, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                        }
                                        Text(formatCurrency(c.currentBalance), fontWeight = FontWeight.Bold, color = if (c.currentBalance > 0) MoneyInGreenDark else MoneyOutRedDark)
                                    }
                                }
                            }
                        }
                    }

                    // Suppliers
                    if (selectedTab == "All" || selectedTab == "Suppliers") {
                        if (matchedSuppliers.isNotEmpty()) {
                            item {
                                Text("Suppliers (${matchedSuppliers.size})", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = HisabNavyPrimary))
                            }
                            items(matchedSuppliers) { s ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(Screen.SupplierLedger(s.id)) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0284C7))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(s.name, fontWeight = FontWeight.Bold)
                                            Text("${s.category} • ${s.mobile}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                        }
                                        Text(formatCurrency(s.currentBalance), fontWeight = FontWeight.Bold, color = MoneyOutRedDark)
                                    }
                                }
                            }
                        }
                    }

                    // Invoices
                    if (selectedTab == "All" || selectedTab == "Invoices") {
                        if (matchedInvoices.isNotEmpty()) {
                            item {
                                Text("Invoices (${matchedInvoices.size})", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = HisabNavyPrimary))
                            }
                            items(matchedInvoices) { inv ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(Screen.Invoices) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = PendingAmber)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(inv.invoiceNumber, fontWeight = FontWeight.Bold)
                                            Text("${inv.customerName} • ${formatDate(inv.invoiceDate)}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                        }
                                        Text(formatCurrency(inv.totalAmount), fontWeight = FontWeight.Bold, color = HisabNavyPrimary)
                                    }
                                }
                            }
                        }
                    }

                    // Transactions
                    if (selectedTab == "All" || selectedTab == "Transactions") {
                        if (matchedTransactions.isNotEmpty()) {
                            item {
                                Text("Transactions (${matchedTransactions.size})", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = HisabNavyPrimary))
                            }
                            items(matchedTransactions) { tx ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        val isGot = tx.type == "YOU_GOT"
                                        Icon(
                                            if (isGot) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = if (isGot) MoneyInGreen else MoneyOutRed
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tx.partyName, fontWeight = FontWeight.Bold)
                                            Text("${tx.description} • ${formatDate(tx.date)}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                        }
                                        Text(formatCurrency(tx.amount), fontWeight = FontWeight.Bold, color = if (isGot) MoneyInGreenDark else MoneyOutRedDark)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
