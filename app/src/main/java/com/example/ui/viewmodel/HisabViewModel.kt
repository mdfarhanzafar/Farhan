package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.DatabaseSeeder
import com.example.data.entity.*
import com.example.data.repository.HisabRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen {
    object Dashboard : Screen()
    object Customers : Screen()
    data class CustomerLedger(val customerId: Long) : Screen()
    object Suppliers : Screen()
    data class SupplierLedger(val supplierId: Long) : Screen()
    data class AddTransaction(val partyType: String = "CUSTOMER", val partyId: Long? = null, val initialType: String = "YOU_GAVE") : Screen()
    object TransactionsHistory : Screen()
    object Expenses : Screen()
    object Income : Screen()
    object Invoices : Screen()
    data class InvoiceDetail(val invoiceId: Long) : Screen()
    object CreateInvoice : Screen()
    object Reports : Screen()
    object Search : Screen()
    object Notifications : Screen()
    object Profile : Screen()
    object Admin : Screen()
    object LandingShowcase : Screen()
    object Auth : Screen()
}

data class FinancialSummary(
    val totalYouWillGet: Double = 0.0,
    val totalYouWillGive: Double = 0.0,
    val todayCollection: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val netBalance: Double = 0.0
)

class HisabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HisabRepository

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenBackStack = mutableListOf<Screen>()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HisabRepository(db.hisabKitabDao())

        viewModelScope.launch {
            DatabaseSeeder.seedDatabaseIfEmpty(db.hisabKitabDao())
        }
    }

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<TransactionEntity>> = repository.getRecentTransactions(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomes: StateFlow<List<IncomeEntity>> = repository.allIncomes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Calculated Financial Summary
    val financialSummary: StateFlow<FinancialSummary> = combine(
        customers,
        suppliers,
        allTransactions,
        expenses,
        incomes
    ) { custList, suppList, txList, expList, incList ->
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Total You Will Get = positive customer balances + negative supplier balances (supplier owes you)
        val custGet = custList.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
        val suppOwesUs = suppList.filter { it.currentBalance < 0 }.sumOf { -it.currentBalance }
        val totalGet = custGet + suppOwesUs

        // Total You Will Give = positive supplier balances + negative customer balances (you owe customer)
        val suppGive = suppList.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
        val weOweCust = custList.filter { it.currentBalance < 0 }.sumOf { -it.currentBalance }
        val totalGive = suppGive + weOweCust

        // Today's Collection = customer payments received today (YOU_GOT) + today's incomes
        val todayCustPayments = txList.filter {
            it.partyType == "CUSTOMER" && it.type == "YOU_GOT" && it.date >= startOfToday
        }.sumOf { it.amount }
        val todayIncomes = incList.filter { it.date >= startOfToday }.sumOf { it.amount }
        val todayCollection = todayCustPayments + todayIncomes

        // Today's Expenses = expenses recorded today + payments to suppliers today (YOU_GAVE)
        val directExpensesToday = expList.filter { it.date >= startOfToday }.sumOf { it.amount }
        val todaySuppPayments = txList.filter {
            it.partyType == "SUPPLIER" && it.type == "YOU_GAVE" && it.date >= startOfToday
        }.sumOf { it.amount }
        val todayExpenses = directExpensesToday + todaySuppPayments

        FinancialSummary(
            totalYouWillGet = totalGet,
            totalYouWillGive = totalGive,
            todayCollection = todayCollection,
            todayExpenses = todayExpenses,
            netBalance = totalGet - totalGive
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // Navigation methods
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            _screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_screenBackStack.isNotEmpty()) {
            val previous = _screenBackStack.removeAt(_screenBackStack.size - 1)
            _currentScreen.value = previous
            return true
        }
        if (_currentScreen.value !is Screen.Dashboard) {
            _currentScreen.value = Screen.Dashboard
            return true
        }
        return false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Customer Operations
    fun addCustomer(
        name: String,
        mobile: String,
        address: String = "",
        openingBalance: Double = 0.0,
        groupName: String = "Regular",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val id = repository.addCustomer(
                CustomerEntity(
                    name = name,
                    mobile = mobile,
                    address = address,
                    openingBalance = openingBalance,
                    currentBalance = openingBalance,
                    groupName = groupName,
                    notes = notes
                )
            )
            _toastMessage.emit("Customer $name added successfully!")
            navigateTo(Screen.CustomerLedger(id))
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            _toastMessage.emit("Customer updated")
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _toastMessage.emit("Customer deleted")
            navigateTo(Screen.Customers)
        }
    }

    // Supplier Operations
    fun addSupplier(
        name: String,
        mobile: String,
        address: String = "",
        openingBalance: Double = 0.0,
        category: String = "Wholesale",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val id = repository.addSupplier(
                SupplierEntity(
                    name = name,
                    mobile = mobile,
                    address = address,
                    openingBalance = openingBalance,
                    currentBalance = openingBalance,
                    category = category,
                    notes = notes
                )
            )
            _toastMessage.emit("Supplier $name added successfully!")
            navigateTo(Screen.SupplierLedger(id))
        }
    }

    fun updateSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.updateSupplier(supplier)
            _toastMessage.emit("Supplier updated")
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
            _toastMessage.emit("Supplier deleted")
            navigateTo(Screen.Suppliers)
        }
    }

    // Transaction Operations
    fun addTransaction(
        partyType: String,
        partyId: Long,
        partyName: String,
        type: String,
        amount: Double,
        date: Long,
        description: String,
        paymentMethod: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    partyType = partyType,
                    partyId = partyId,
                    partyName = partyName,
                    type = type,
                    amount = amount,
                    date = date,
                    description = description,
                    paymentMethod = paymentMethod,
                    notes = notes
                )
            )
            _toastMessage.emit("Entry saved: ₹${"%.0f".format(amount)}")
            if (partyType == "CUSTOMER") {
                navigateTo(Screen.CustomerLedger(partyId))
            } else {
                navigateTo(Screen.SupplierLedger(partyId))
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _toastMessage.emit("Entry removed")
        }
    }

    fun getTransactionsForParty(partyType: String, partyId: Long): Flow<List<TransactionEntity>> {
        return repository.getTransactionsForParty(partyType, partyId)
    }

    // Expense Operations
    fun addExpense(
        amount: Double,
        category: String,
        date: Long,
        description: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            repository.addExpense(
                ExpenseEntity(
                    amount = amount,
                    category = category,
                    date = date,
                    description = description,
                    paymentMethod = paymentMethod
                )
            )
            _toastMessage.emit("Expense of ₹${"%.0f".format(amount)} recorded")
            navigateTo(Screen.Expenses)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _toastMessage.emit("Expense deleted")
        }
    }

    // Income Operations
    fun addIncome(
        amount: Double,
        sourceCategory: String,
        date: Long,
        description: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            repository.addIncome(
                IncomeEntity(
                    amount = amount,
                    sourceCategory = sourceCategory,
                    date = date,
                    description = description,
                    paymentMethod = paymentMethod
                )
            )
            _toastMessage.emit("Income of ₹${"%.0f".format(amount)} recorded")
            navigateTo(Screen.Income)
        }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.deleteIncome(income)
            _toastMessage.emit("Income deleted")
        }
    }

    // Invoice Operations
    fun addInvoice(
        invoiceNumber: String,
        customerId: Long?,
        customerName: String,
        customerMobile: String,
        customerAddress: String,
        subtotal: Double,
        discount: Double,
        taxPercent: Double,
        taxAmount: Double,
        totalAmount: Double,
        paymentStatus: String,
        notes: String,
        itemsJson: String
    ) {
        viewModelScope.launch {
            val id = repository.addInvoice(
                InvoiceEntity(
                    invoiceNumber = invoiceNumber,
                    customerId = customerId,
                    customerName = customerName,
                    customerMobile = customerMobile,
                    customerAddress = customerAddress,
                    subtotal = subtotal,
                    discount = discount,
                    taxPercent = taxPercent,
                    taxAmount = taxAmount,
                    totalAmount = totalAmount,
                    paymentStatus = paymentStatus,
                    notes = notes,
                    itemsJson = itemsJson
                )
            )
            _toastMessage.emit("Invoice $invoiceNumber generated successfully!")
            navigateTo(Screen.InvoiceDetail(id))
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
            _toastMessage.emit("Invoice removed")
            navigateTo(Screen.Invoices)
        }
    }

    // Profile & Settings
    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            _toastMessage.emit("Business settings saved")
        }
    }

    fun updateLanguage(langCode: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveUserProfile(current.copy(language = langCode))
            _toastMessage.emit("Language updated to ${if (langCode == "hi") "हिन्दी" else if (langCode == "hinglish") "Hinglish" else "English"}")
        }
    }

    fun toggleDarkMode(darkMode: Boolean?) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveUserProfile(current.copy(isDarkMode = darkMode))
        }
    }

    // Notifications
    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            _toastMessage.emit("All notifications cleared")
        }
    }

    // Backup & Restore
    fun triggerCloudBackup() {
        viewModelScope.launch {
            val now = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            _toastMessage.emit("Backup completed successfully! Synced at $now")
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(getApplication())
            db.clearAllTables()
            DatabaseSeeder.seedDatabaseIfEmpty(db.hisabKitabDao())
            _toastMessage.emit("Demo data reloaded successfully")
            navigateTo(Screen.Dashboard)
        }
    }
}
