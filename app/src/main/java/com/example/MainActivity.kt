package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.HisabViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: HisabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsState()
            val isDarkTheme = userProfile?.isDarkMode ?: false

            MyApplicationTheme(darkTheme = isDarkTheme) {
                HisabKitabApp(viewModel)
            }
        }
    }
}

@Composable
fun HisabKitabApp(viewModel: HisabViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back button presses
    BackHandler {
        val handled = viewModel.navigateBack()
        if (!handled) {
            // Already at root dashboard, let activity handle back
            (context as? ComponentActivity)?.finish()
        }
    }

    // Toast collector
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Check if bottom bar should be visible
    val showBottomBar = remember(currentScreen) {
        when (currentScreen) {
            is Screen.Dashboard,
            is Screen.Customers,
            is Screen.Suppliers,
            is Screen.Invoices,
            is Screen.Profile -> true
            else -> false
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        Scaffold(
            bottomBar = {
                if (showBottomBar && !isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Dashboard,
                            onClick = { viewModel.navigateTo(Screen.Dashboard) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HisabNavyPrimary,
                                selectedTextColor = HisabNavyPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Customers,
                            onClick = { viewModel.navigateTo(Screen.Customers) },
                            icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
                            label = { Text("Customers") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HisabNavyPrimary,
                                selectedTextColor = HisabNavyPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Suppliers,
                            onClick = { viewModel.navigateTo(Screen.Suppliers) },
                            icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Suppliers") },
                            label = { Text("Suppliers") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HisabNavyPrimary,
                                selectedTextColor = HisabNavyPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Invoices,
                            onClick = { viewModel.navigateTo(Screen.Invoices) },
                            icon = { Icon(Icons.Default.Description, contentDescription = "Invoices") },
                            label = { Text("Bills & GST") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HisabNavyPrimary,
                                selectedTextColor = HisabNavyPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Profile,
                            onClick = { viewModel.navigateTo(Screen.Profile) },
                            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                            label = { Text("More") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HisabNavyPrimary,
                                selectedTextColor = HisabNavyPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                // Wide Screen Navigation Rail (for tablets/foldables)
                if (showBottomBar && isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        NavigationRailItem(
                            selected = currentScreen is Screen.Dashboard,
                            onClick = { viewModel.navigateTo(Screen.Dashboard) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") }
                        )
                        NavigationRailItem(
                            selected = currentScreen is Screen.Customers,
                            onClick = { viewModel.navigateTo(Screen.Customers) },
                            icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
                            label = { Text("Customers") }
                        )
                        NavigationRailItem(
                            selected = currentScreen is Screen.Suppliers,
                            onClick = { viewModel.navigateTo(Screen.Suppliers) },
                            icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Suppliers") },
                            label = { Text("Suppliers") }
                        )
                        NavigationRailItem(
                            selected = currentScreen is Screen.Invoices,
                            onClick = { viewModel.navigateTo(Screen.Invoices) },
                            icon = { Icon(Icons.Default.Description, contentDescription = "Invoices") },
                            label = { Text("Bills") }
                        )
                        NavigationRailItem(
                            selected = currentScreen is Screen.Profile,
                            onClick = { viewModel.navigateTo(Screen.Profile) },
                            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                            label = { Text("More") }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (val screen = currentScreen) {
                        is Screen.Dashboard -> DashboardScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Customers -> CustomersScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.CustomerLedger -> CustomerLedgerScreen(customerId = screen.customerId, viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Suppliers -> SuppliersScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.SupplierLedger -> SupplierLedgerScreen(supplierId = screen.supplierId, viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.AddTransaction -> AddTransactionScreen(
                            partyType = screen.partyType,
                            partyId = screen.partyId,
                            initialType = screen.initialType,
                            viewModel = viewModel,
                            onNavigate = viewModel::navigateTo
                        )
                        is Screen.TransactionsHistory -> TransactionsHistoryScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Expenses -> ExpensesScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Income -> IncomeScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Invoices -> InvoicesScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.InvoiceDetail -> InvoicesScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.CreateInvoice -> CreateInvoiceScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Reports -> ReportsScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Search -> SearchScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Notifications -> NotificationsScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Profile -> ProfileScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Admin -> AdminScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.LandingShowcase -> LandingShowcaseScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                        is Screen.Auth -> AuthScreen(viewModel = viewModel, onNavigate = viewModel::navigateTo)
                    }
                }
            }
        }
    }
}
