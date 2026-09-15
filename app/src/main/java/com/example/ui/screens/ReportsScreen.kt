package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: HisabViewModel,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val incomes by viewModel.incomes.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var selectedPeriod by remember { mutableStateOf("This Month") } // Today, This Week, This Month, All Time

    val cutoffTime = remember(selectedPeriod) {
        val cal = Calendar.getInstance()
        when (selectedPeriod) {
            "Today" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            "This Week" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.timeInMillis
            }
            "This Month" -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                cal.timeInMillis
            }
            else -> 0L
        }
    }

    // Filtered data
    val periodTransactions = remember(allTransactions, cutoffTime) {
        allTransactions.filter { it.date >= cutoffTime }
    }
    val periodExpenses = remember(expenses, cutoffTime) {
        expenses.filter { it.date >= cutoffTime }
    }
    val periodIncomes = remember(incomes, cutoffTime) {
        incomes.filter { it.date >= cutoffTime }
    }

    val totalGot = remember(periodTransactions) {
        periodTransactions.filter { it.type == "YOU_GOT" }.sumOf { it.amount }
    }
    val totalGave = remember(periodTransactions) {
        periodTransactions.filter { it.type == "YOU_GAVE" }.sumOf { it.amount }
    }
    val totalExpense = remember(periodExpenses) {
        periodExpenses.sumOf { it.amount }
    }
    val totalDirectIncome = remember(periodIncomes) {
        periodIncomes.sumOf { it.amount }
    }

    val totalCashIn = totalGot + totalDirectIncome
    val totalCashOut = totalGave + totalExpense
    val netCashFlow = totalCashIn - totalCashOut

    // Top pending debtors
    val topDebtors = remember(customers) {
        customers.filter { it.currentBalance > 0 }
            .sortedByDescending { it.currentBalance }
            .take(5)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Dashboard) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val reportText = """
                            *Hisab Kitab Business Report - ${userProfile?.businessName ?: "Sharma Kirana"}*
                            Period: $selectedPeriod
                            ----------------------------
                            Total Money Received: ${formatCurrency(totalCashIn)}
                            • Customer Collections: ${formatCurrency(totalGot)}
                            • Direct Incomes: ${formatCurrency(totalDirectIncome)}
                            
                            Total Money Spent: ${formatCurrency(totalCashOut)}
                            • Credit Given: ${formatCurrency(totalGave)}
                            • Operating Expenses: ${formatCurrency(totalExpense)}
                            
                            *Net Cash Balance: ${formatCurrency(netCashFlow)}*
                            ----------------------------
                            Active Customers: ${customers.size}
                            Outstanding Market Credit: ${formatCurrency(customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance })}
                            
                            Verified by Hisab Kitab • Apna Hisab, Apne Haath
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Business Report"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period selector
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Today", "This Week", "This Month", "All Time").forEach { p ->
                        item {
                            FilterChip(
                                selected = selectedPeriod == p,
                                onClick = { selectedPeriod = p },
                                label = { Text(p) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HisabNavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Net Cash Flow Hero
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HisabNavyPrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Net Cash Flow ($selectedPeriod)",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFCBD5E1))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${if (netCashFlow >= 0) "+ " else "- "}${formatCurrency(kotlin.math.abs(netCashFlow))}",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netCashFlow >= 0) Color(0xFF34D399) else Color(0xFFFB7185)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Inflow", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA7F3D0)))
                                Text(formatCurrency(totalCashIn), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(Color(0xFF334155))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Outflow", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFECDD3)))
                                Text(formatCurrency(totalCashOut), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            }
                        }
                    }
                }
            }

            // Inflow vs Outflow Visual Comparison
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Financial Distribution", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))

                        val totalVol = (totalCashIn + totalCashOut).coerceAtLeast(1.0)
                        val inRatio = (totalCashIn / totalVol).toFloat().coerceIn(0.05f, 0.95f)
                        val outRatio = 1f - inRatio

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(inRatio)
                                    .fillMaxHeight()
                                    .background(MoneyInGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(outRatio)
                                    .fillMaxHeight()
                                    .background(MoneyOutRed)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MoneyInGreen))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Inflow: ${(inRatio * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MoneyOutRed))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Outflow: ${(outRatio * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Top Pending Debtors Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Top Outstanding Balances", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(10.dp))

                        if (topDebtors.isEmpty()) {
                            Text("All customer accounts are currently settled!", style = MaterialTheme.typography.bodySmall.copy(color = MoneyInGreenDark))
                        } else {
                            topDebtors.forEach { d ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(d.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(d.mobile, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    }
                                    Text(
                                        formatCurrency(d.currentBalance),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold, color = MoneyInGreenDark)
                                    )
                                }
                                HorizontalDivider(color = CardBorderLight.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Export & Share Report Actions
            item {
                Button(
                    onClick = {
                        val reportText = """
                            *Hisab Kitab Full Statement*
                            Business: ${userProfile?.businessName ?: "Sharma Kirana Store"}
                            Total Customers: ${customers.size}
                            Total Suppliers: ${suppliers.size}
                            Total Transactions Logged: ${allTransactions.size}
                            Net Market Udhaar: ${formatCurrency(customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance })}
                            Net Supplier Dues: ${formatCurrency(suppliers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance })}
                            
                            Apna Hisab, Apne Haath
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export Statement"))
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HisabNavyPrimary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Statement (CSV / Text)")
                }
            }
        }
    }
}
