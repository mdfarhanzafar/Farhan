package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.ui.components.formatDate
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    partyType: String,
    partyId: Long?,
    initialType: String,
    viewModel: HisabViewModel,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    var selectedPartyType by remember { mutableStateOf(partyType) } // "CUSTOMER" or "SUPPLIER"
    var selectedPartyId by remember {
        mutableStateOf(partyId ?: if (partyType == "CUSTOMER") customers.firstOrNull()?.id ?: 0L else suppliers.firstOrNull()?.id ?: 0L)
    }
    var transactionType by remember { mutableStateOf(initialType) } // "YOU_GAVE" or "YOU_GOT"
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val partyName = remember(selectedPartyType, selectedPartyId, customers, suppliers) {
        if (selectedPartyType == "CUSTOMER") {
            customers.find { it.id == selectedPartyId }?.name ?: "Customer"
        } else {
            suppliers.find { it.id == selectedPartyId }?.name ?: "Supplier"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (transactionType == "YOU_GAVE") "Add Debit (You Gave)" else "Add Credit (You Got)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Party Type Switch (Customer vs Supplier)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = {
                            selectedPartyType = "CUSTOMER"
                            if (selectedPartyId == 0L || suppliers.any { it.id == selectedPartyId }) {
                                selectedPartyId = customers.firstOrNull()?.id ?: 0L
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPartyType == "CUSTOMER") HisabNavyPrimary else Color.Transparent,
                            contentColor = if (selectedPartyType == "CUSTOMER") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Customer")
                    }

                    Button(
                        onClick = {
                            selectedPartyType = "SUPPLIER"
                            if (selectedPartyId == 0L || customers.any { it.id == selectedPartyId }) {
                                selectedPartyId = suppliers.firstOrNull()?.id ?: 0L
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPartyType == "SUPPLIER") HisabNavyPrimary else Color.Transparent,
                            contentColor = if (selectedPartyType == "SUPPLIER") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Supplier")
                    }
                }
            }

            // Party Selector Dropdown
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (selectedPartyType == "CUSTOMER") "Select Customer:" else "Select Supplier:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var expandedPartyDropdown by remember { mutableStateOf(false) }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedPartyDropdown = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = partyName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = expandedPartyDropdown,
                        onDismissRequest = { expandedPartyDropdown = false }
                    ) {
                        if (selectedPartyType == "CUSTOMER") {
                            customers.forEach { cust ->
                                DropdownMenuItem(
                                    text = { Text("${cust.name} (${cust.mobile})") },
                                    onClick = {
                                        selectedPartyId = cust.id
                                        expandedPartyDropdown = false
                                    }
                                )
                            }
                        } else {
                            suppliers.forEach { supp ->
                                DropdownMenuItem(
                                    text = { Text("${supp.name} (${supp.category})") },
                                    onClick = {
                                        selectedPartyId = supp.id
                                        expandedPartyDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Transaction Type (You Gave vs You Got)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { transactionType = "YOU_GAVE" },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (transactionType == "YOU_GAVE") MoneyOutRedLight else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (transactionType == "YOU_GAVE") MoneyOutRed else CardBorderLight
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (transactionType == "YOU_GAVE") MoneyOutRed else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedPartyType == "CUSTOMER") "You Gave (Maine Diye)" else "You Paid (Payment)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (transactionType == "YOU_GAVE") MoneyOutRedDark else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { transactionType = "YOU_GOT" },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (transactionType == "YOU_GOT") MoneyInGreenLight else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (transactionType == "YOU_GOT") MoneyInGreen else CardBorderLight
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (transactionType == "YOU_GOT") MoneyInGreen else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedPartyType == "CUSTOMER") "You Got (Maine Liye)" else "Stock In (Goods)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (transactionType == "YOU_GOT") MoneyInGreenDark else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Big Numeric Amount Input
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Enter Amount (₹)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' }) {
                                amountStr = input
                            }
                        },
                        placeholder = { Text("0.00", fontSize = 28.sp) },
                        prefix = {
                            Text("₹ ", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = HisabNavyPrimary)
                        },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Chips (+100, +500, +1000, +2000, +5000)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val quickValues = listOf(100, 500, 1000, 2000, 5000)
                        items(quickValues) { v ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    val cur = amountStr.toDoubleOrNull() ?: 0.0
                                    amountStr = "%.0f".format(cur + v)
                                }
                            ) {
                                Text(
                                    text = "+ ₹$v",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Payment Mode Selector
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Payment Method:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val methods = listOf("Cash", "UPI", "Bank Transfer", "Cheque", "Card")
                        items(methods) { method ->
                            FilterChip(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method },
                                label = { Text(method) }
                            )
                        }
                    }
                }
            }

            // Description / Bill Number & Notes
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Bill / Item Details (Optional)") },
                        placeholder = { Text("e.g. Rice 10kg + Oil 2L") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Personal Remarks / Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Date: ${formatDate(selectedDate)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(onClick = { selectedDate = System.currentTimeMillis() }) {
                                Text("Today", fontSize = 12.sp)
                            }
                            TextButton(onClick = { selectedDate = System.currentTimeMillis() - (86400000L) }) {
                                Text("Yesterday", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save Transaction Button
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount <= 0) {
                        Toast.makeText(context, "Please enter a valid amount greater than 0", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedPartyId == 0L) {
                        Toast.makeText(context, "Please select a party", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.addTransaction(
                        partyType = selectedPartyType,
                        partyId = selectedPartyId,
                        partyName = partyName,
                        type = transactionType,
                        amount = amount,
                        date = selectedDate,
                        description = description.trim(),
                        paymentMethod = selectedPaymentMethod,
                        notes = notes.trim()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (transactionType == "YOU_GAVE") MoneyOutRed else MoneyInGreen
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Entry (₹${amountStr.ifEmpty { "0" }})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
