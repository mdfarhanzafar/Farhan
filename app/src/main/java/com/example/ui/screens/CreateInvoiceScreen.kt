package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatCurrency
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

data class InvoiceLineItemState(
    var name: String = "",
    var qtyStr: String = "1",
    var rateStr: String = ""
) {
    val qty: Double get() = qtyStr.toDoubleOrNull() ?: 1.0
    val rate: Double get() = rateStr.toDoubleOrNull() ?: 0.0
    val amount: Double get() = qty * rate
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    viewModel: HisabViewModel,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val invoices by viewModel.invoices.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var customerMobile by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }

    val nextInvNumber = remember(invoices) {
        "INV-2026-%03d".format(invoices.size + 101)
    }
    var invoiceNumber by remember { mutableStateOf(nextInvNumber) }

    val items = remember {
        mutableStateListOf(
            InvoiceLineItemState("Toor Dal 5kg", "2", "680"),
            InvoiceLineItemState("Aashirvaad Atta 10kg", "1", "440")
        )
    }

    var discountStr by remember { mutableStateOf("0") }
    var selectedTaxPercent by remember { mutableDoubleStateOf(5.0) } // 0%, 5%, 12%, 18%
    var paymentStatus by remember { mutableStateOf("PAID") } // PAID, UNPAID
    var notes by remember { mutableStateOf("Goods once sold will not be taken back. Payment due within 7 days.") }

    val subtotal = items.sumOf { it.amount }
    val discount = discountStr.toDoubleOrNull() ?: 0.0
    val taxableAmount = kotlin.math.max(0.0, subtotal - discount)
    val taxAmount = taxableAmount * (selectedTaxPercent / 100.0)
    val totalAmount = taxableAmount + taxAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Invoice", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
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
            // Invoice Metadata Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Invoice Information", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = invoiceNumber,
                            onValueChange = { invoiceNumber = it },
                            label = { Text("Invoice #") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = paymentStatus == "PAID",
                                onClick = { paymentStatus = if (paymentStatus == "PAID") "UNPAID" else "PAID" },
                                label = { Text(if (paymentStatus == "PAID") "PAID" else "UNPAID") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MoneyInGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Customer Selector / Input Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Bill To (Customer Details)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    // Quick Select from existing customers
                    if (customers.isNotEmpty()) {
                        Text("Quick Select from Khata:", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(customers) { c ->
                                FilterChip(
                                    selected = selectedCustomerId == c.id,
                                    onClick = {
                                        selectedCustomerId = c.id
                                        customerName = c.name
                                        customerMobile = c.mobile
                                        customerAddress = c.address
                                    },
                                    label = { Text(c.name) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customerMobile,
                        onValueChange = { customerMobile = it },
                        label = { Text("Customer Mobile (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text("Customer Address (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Line Items Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Invoice Items (${items.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        TextButton(onClick = { items.add(InvoiceLineItemState()) }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    items.forEachIndexed { index, item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = item.name,
                                        onValueChange = { item.name = it },
                                        placeholder = { Text("Item Name / Description") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    if (items.size > 1) {
                                        IconButton(onClick = { items.removeAt(index) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Color.Gray)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = item.qtyStr,
                                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) item.qtyStr = it },
                                        label = { Text("Qty") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = item.rateStr,
                                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) item.rateStr = it },
                                        label = { Text("Rate (₹)") },
                                        modifier = Modifier.weight(1.2f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true
                                    )
                                    Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                                        Text("Amount", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                        Text(formatCurrency(item.amount), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HisabNavyPrimary))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Calculation and Taxes Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Taxes & Totals", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
                        Text(formatCurrency(subtotal), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = discountStr,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) discountStr = it },
                            label = { Text("Discount (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GST Rate:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(0.0, 5.0, 12.0, 18.0).forEach { rate ->
                                    FilterChip(
                                        selected = selectedTaxPercent == rate,
                                        onClick = { selectedTaxPercent = rate },
                                        label = { Text("${rate.toInt()}%", fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }

                    if (taxAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GST (${selectedTaxPercent.toInt()}%):", style = MaterialTheme.typography.bodyMedium)
                            Text("+ ${formatCurrency(taxAmount)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    HorizontalDivider(color = CardBorderLight)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total Payable:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                        Text(formatCurrency(totalAmount), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = HisabNavyPrimary))
                    }
                }
            }

            // Notes / Terms
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Terms & Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            // Submit Button
            Button(
                onClick = {
                    if (customerName.isBlank()) {
                        Toast.makeText(context, "Please enter customer name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val validItems = items.filter { it.name.isNotBlank() && it.amount > 0 }
                    if (validItems.isEmpty()) {
                        Toast.makeText(context, "Please add at least one line item with rate > 0", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val jsonArray = JSONArray()
                    validItems.forEach { item ->
                        val obj = JSONObject()
                        obj.put("name", item.name.trim())
                        obj.put("qty", item.qty)
                        obj.put("rate", item.rate)
                        obj.put("amount", item.amount)
                        jsonArray.put(obj)
                    }

                    val now = System.currentTimeMillis()
                    val dueDate = now + (7L * 86400000L) // 7 days

                    viewModel.addInvoice(
                        invoiceNumber = invoiceNumber.trim(),
                        customerId = selectedCustomerId,
                        customerName = customerName.trim(),
                        customerMobile = customerMobile.trim(),
                        customerAddress = customerAddress.trim(),
                        subtotal = subtotal,
                        discount = discount,
                        taxPercent = selectedTaxPercent,
                        taxAmount = taxAmount,
                        totalAmount = totalAmount,
                        paymentStatus = paymentStatus,
                        notes = notes.trim(),
                        itemsJson = jsonArray.toString()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HisabNavyPrimary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate & View Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
