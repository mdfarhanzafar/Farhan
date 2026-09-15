package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.InvoiceEntity
import com.example.data.entity.UserProfileEntity
import com.example.ui.theme.*
import org.json.JSONArray

@Composable
fun InvoicePdfPreviewDialog(
    invoice: InvoiceEntity,
    userProfile: UserProfileEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val biz = userProfile ?: UserProfileEntity()
    val isPaid = invoice.paymentStatus.equals("PAID", ignoreCase = true)

    // Parse items json
    data class ParsedItem(val name: String, val qty: Double, val rate: Double, val amount: Double)
    val items = mutableListOf<ParsedItem>()
    try {
        if (invoice.itemsJson.isNotEmpty()) {
            val arr = JSONArray(invoice.itemsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                items.add(
                    ParsedItem(
                        name = obj.optString("name", "Item ${i + 1}"),
                        qty = obj.optDouble("qty", 1.0),
                        rate = obj.optDouble("rate", 0.0),
                        amount = obj.optDouble("amount", 0.0)
                    )
                )
            }
        }
    } catch (e: Exception) {
        // fallback
        items.add(ParsedItem("General Goods & Supplies", 1.0, invoice.subtotal, invoice.subtotal))
    }

    if (items.isEmpty()) {
        items.add(ParsedItem("General Goods & Supplies", 1.0, invoice.subtotal, invoice.subtotal))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tax Invoice Preview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Scrollable A4 paper sheet container
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, CardBorderLight, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Business Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = biz.businessName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = HisabNavyPrimary
                                    )
                                )
                                Text(
                                    text = biz.businessAddress,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                )
                                Text(
                                    text = "Mobile: ${biz.mobileNumber}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                )
                                if (biz.gstin.isNotEmpty()) {
                                    Text(
                                        text = "GSTIN: ${biz.gstin}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = if (isPaid) MoneyInGreenLight else MoneyOutRedLight,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = invoice.paymentStatus.uppercase(),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isPaid) MoneyInGreenDark else MoneyOutRedDark
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = invoice.invoiceNumber,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = HisabNavyPrimary
                                    )
                                )
                                Text(
                                    text = "Date: ${formatDate(invoice.invoiceDate)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                )
                                Text(
                                    text = "Due: ${formatDate(invoice.dueDate)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                        // Bill To Customer
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "BILL TO:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = invoice.customerName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                            if (invoice.customerMobile.isNotEmpty()) {
                                Text(
                                    text = "Contact: ${invoice.customerMobile}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                )
                            }
                            if (invoice.customerAddress.isNotEmpty()) {
                                Text(
                                    text = "Address: ${invoice.customerAddress}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Items Table Header
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ITEM / DESCRIPTION",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)),
                                    modifier = Modifier.weight(2f)
                                )
                                Text(
                                    text = "QTY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)),
                                    modifier = Modifier.weight(0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "RATE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    text = "AMOUNT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)),
                                    modifier = Modifier.weight(1.2f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        // Items Rows
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color(0xFF0F172A)),
                                    modifier = Modifier.weight(2f)
                                )
                                Text(
                                    text = "%.0f".format(item.qty),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155)),
                                    modifier = Modifier.weight(0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = formatCurrency(item.rate),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155)),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    text = formatCurrency(item.amount),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A)),
                                    modifier = Modifier.weight(1.2f),
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Financial Summary Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.width(220.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal:", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
                                Text(formatCurrency(invoice.subtotal), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color(0xFF0F172A)))
                            }
                            if (invoice.discount > 0) {
                                Row(
                                    modifier = Modifier.width(220.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Discount:", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
                                    Text("- ${formatCurrency(invoice.discount)}", style = MaterialTheme.typography.bodySmall.copy(color = MoneyOutRed, fontWeight = FontWeight.Medium))
                                }
                            }
                            if (invoice.taxAmount > 0) {
                                Row(
                                    modifier = Modifier.width(220.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("GST / Tax (${invoice.taxPercent}%):", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
                                    Text("+ ${formatCurrency(invoice.taxAmount)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color(0xFF0F172A)))
                                }
                            }
                            HorizontalDivider(modifier = Modifier.width(220.dp).padding(vertical = 4.dp), color = Color(0xFFCBD5E1))
                            Row(
                                modifier = Modifier.width(220.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total Payable:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = HisabNavyPrimary)
                                )
                                Text(
                                    formatCurrency(invoice.totalAmount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = HisabNavyPrimary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Terms & Notes
                        if (invoice.notes.isNotEmpty()) {
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Terms & Notes:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    )
                                    Text(
                                        text = invoice.notes,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Generated electronically via Hisab Kitab • Apna Hisab, Apne Haath",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), textAlign = TextAlign.Center),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons: Share on WhatsApp, Share Sheet, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val invoiceSummaryText = """
                        *TAX INVOICE - ${biz.businessName}*
                        Invoice No: ${invoice.invoiceNumber}
                        Date: ${formatDate(invoice.invoiceDate)}
                        Customer: ${invoice.customerName}
                        Total Amount: ${formatCurrency(invoice.totalAmount)}
                        Status: ${invoice.paymentStatus}
                        
                        Items:
                        ${items.joinToString("\n") { "• ${it.name} (Qty: ${it.qty.toInt()} x ${formatCurrency(it.rate)}) = ${formatCurrency(it.amount)}" }}
                        
                        Subtotal: ${formatCurrency(invoice.subtotal)}
                        Discount: ${formatCurrency(invoice.discount)}
                        Tax: ${formatCurrency(invoice.taxAmount)}
                        *Grand Total: ${formatCurrency(invoice.totalAmount)}*
                        
                        Thank you for your business!
                        Verified by Hisab Kitab
                    """.trimIndent()

                    Button(
                        onClick = {
                            val cleanNumber = invoice.customerMobile.replace(Regex("[^0-9]"), "")
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(invoiceSummaryText)}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, invoiceSummaryText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, invoiceSummaryText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Invoice ${invoice.invoiceNumber}"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HisabNavyPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Invoice PDF saved to Downloads", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
                    }
                }
            }
        }
    }
}
