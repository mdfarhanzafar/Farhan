package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingShowcaseScreen(
    viewModel: HisabViewModel,
    onNavigate: (Screen) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Hisab Kitab", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Hero Brand Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = HisabNavyPrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E3E62)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Hisab Kitab",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "“Apna Hisab, Apne Haath”",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "The modern, trustworthy digital ledger and business accounting platform crafted for Indian retail stores, kirana shops, wholesalers, and small enterprises.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCBD5E1), textAlign = TextAlign.Center)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onNavigate(Screen.Dashboard) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Open My Dashboard", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Key Pillars
            item {
                Text("Why Indian Businesses Choose Hisab Kitab", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MoneyInGreen)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("3x Faster Recovery", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Automated polite WhatsApp payment reminders recover dues in days.", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF0284C7))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("100% Offline First", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Never stops working even without network, with encrypted cloud sync.", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                }
            }

            // Pricing Plans
            item {
                Text("Transparent Plans for Every Business", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MoneyInGreen)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Business Pro", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = HisabNavyPrimary))
                            Surface(color = MoneyInGreenLight, shape = RoundedCornerShape(6.dp)) {
                                Text("MOST POPULAR", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall.copy(color = MoneyInGreenDark, fontWeight = FontWeight.Bold))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("₹0", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold))
                            Text(" / Lifetime Free for Small Shops", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("• Unlimited Customers & Suppliers", style = MaterialTheme.typography.bodySmall)
                        Text("• WhatsApp & SMS Payment Reminders", style = MaterialTheme.typography.bodySmall)
                        Text("• Professional GST Invoices & PDF Share", style = MaterialTheme.typography.bodySmall)
                        Text("• Daily Expenses & Revenue Tracking", style = MaterialTheme.typography.bodySmall)
                        Text("• Free Secure Cloud Backup", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Testimonials
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Merchant Testimonial", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "“Pehle register mein hisab likhte the, kabhi kagaz fat jaata tha toh pareshani hoti thi. Hisab Kitab se ek click pe WhatsApp reminder chala jaata hai aur sabhi customer time pe paise de jaate hain.”",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("— Ramesh Sharma, Sharma General Store, Meerut", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = HisabNavyPrimary))
                    }
                }
            }
        }
    }
}
