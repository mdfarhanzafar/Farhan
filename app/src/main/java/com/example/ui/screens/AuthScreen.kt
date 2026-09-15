package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: HisabViewModel,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val currentProfile by viewModel.userProfile.collectAsState()

    var step by remember { mutableIntStateOf(1) } // 1: Mobile, 2: OTP, 3: Onboarding Profile
    var mobileNumber by remember { mutableStateOf("9876543210") }
    var otpCode by remember { mutableStateOf("123456") }
    var shopName by remember { mutableStateOf(currentProfile?.businessName ?: "Sharma Kirana Store") }
    var ownerName by remember { mutableStateOf(currentProfile?.ownerName ?: "Ramesh Sharma") }
    var securityPin by remember { mutableStateOf("1234") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Authentication & Setup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(HisabNavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Hisab Kitab",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = HisabNavyPrimary)
            )
            Text(
                text = "“Apna Hisab, Apne Haath”",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(28.dp))

            when (step) {
                1 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Mobile Login / Register", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "Enter your 10-digit mobile number to receive a secure OTP.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            OutlinedTextField(
                                value = mobileNumber,
                                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) mobileNumber = it },
                                label = { Text("Mobile Number") },
                                prefix = { Text("+91 ") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    if (mobileNumber.length < 10) {
                                        Toast.makeText(context, "Please enter valid 10-digit mobile", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    Toast.makeText(context, "OTP sent to +91 $mobileNumber", Toast.LENGTH_SHORT).show()
                                    step = 2
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HisabNavyPrimary)
                            ) {
                                Text("Get OTP (6-Digit)")
                            }
                        }
                    }
                }

                2 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Enter Verification Code", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "We have sent an OTP to +91 $mobileNumber (Demo code: 123456)",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                                label = { Text("Enter OTP") },
                                leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.titleLarge.copy(letterSpacing = 4.sp, textAlign = TextAlign.Center),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    if (otpCode.length < 6) {
                                        Toast.makeText(context, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    step = 3
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MoneyInGreen)
                            ) {
                                Text("Verify & Continue")
                            }

                            TextButton(
                                onClick = { step = 1 },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Change Mobile Number")
                            }
                        }
                    }
                }

                3 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Set Up Business Profile", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                            OutlinedTextField(
                                value = shopName,
                                onValueChange = { shopName = it },
                                label = { Text("Shop / Business Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                label = { Text("Proprietor / Your Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = securityPin,
                                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) securityPin = it },
                                label = { Text("4-Digit App Lock PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val current = currentProfile ?: UserProfileEntity()
                                    viewModel.updateProfile(
                                        current.copy(
                                            businessName = shopName.trim(),
                                            ownerName = ownerName.trim(),
                                            mobileNumber = mobileNumber.trim(),
                                            pin = securityPin
                                        )
                                    )
                                    Toast.makeText(context, "Welcome to Hisab Kitab!", Toast.LENGTH_SHORT).show()
                                    onNavigate(Screen.Dashboard)
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HisabNavyPrimary)
                            ) {
                                Text("Enter Dashboard")
                            }
                        }
                    }
                }
            }
        }
    }
}
