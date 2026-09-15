package com.example.data.repository

import com.example.data.dao.HisabKitabDao
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

class HisabRepository(private val dao: HisabKitabDao) {

    // Customers
    val allCustomers: Flow<List<CustomerEntity>> = dao.getAllCustomers()

    suspend fun getCustomerById(id: Long): CustomerEntity? = dao.getCustomerById(id)

    suspend fun addCustomer(customer: CustomerEntity): Long = dao.insertCustomer(customer)

    suspend fun updateCustomer(customer: CustomerEntity) = dao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: CustomerEntity) {
        dao.deleteCustomer(customer)
        dao.deleteTransactionsForParty("CUSTOMER", customer.id)
    }

    // Suppliers
    val allSuppliers: Flow<List<SupplierEntity>> = dao.getAllSuppliers()

    suspend fun getSupplierById(id: Long): SupplierEntity? = dao.getSupplierById(id)

    suspend fun addSupplier(supplier: SupplierEntity): Long = dao.insertSupplier(supplier)

    suspend fun updateSupplier(supplier: SupplierEntity) = dao.updateSupplier(supplier)

    suspend fun deleteSupplier(supplier: SupplierEntity) {
        dao.deleteSupplier(supplier)
        dao.deleteTransactionsForParty("SUPPLIER", supplier.id)
    }

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    fun getTransactionsForParty(partyType: String, partyId: Long): Flow<List<TransactionEntity>> =
        dao.getTransactionsForParty(partyType, partyId)

    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> =
        dao.getRecentTransactions(limit)

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        val id = dao.insertTransaction(transaction)

        // Update party balance automatically
        if (transaction.partyType == "CUSTOMER") {
            val customer = dao.getCustomerById(transaction.partyId)
            if (customer != null) {
                val newBalance = if (transaction.type == "YOU_GAVE") {
                    customer.currentBalance + transaction.amount
                } else {
                    customer.currentBalance - transaction.amount
                }
                dao.updateCustomer(
                    customer.copy(
                        currentBalance = newBalance,
                        lastTransactionDate = transaction.date
                    )
                )
            }
        } else if (transaction.partyType == "SUPPLIER") {
            val supplier = dao.getSupplierById(transaction.partyId)
            if (supplier != null) {
                val newBalance = if (transaction.type == "YOU_GOT") {
                    supplier.currentBalance + transaction.amount
                } else {
                    supplier.currentBalance - transaction.amount
                }
                dao.updateSupplier(
                    supplier.copy(
                        currentBalance = newBalance,
                        lastTransactionDate = transaction.date
                    )
                )
            }
        }

        // Add a notification for big transactions or reminders
        dao.insertNotification(
            NotificationEntity(
                title = if (transaction.type == "YOU_GOT") "Payment Received" else "Credit Added",
                message = "${transaction.partyName}: ₹${"%.0f".format(transaction.amount)} via ${transaction.paymentMethod}",
                type = "TRANSACTION",
                timestamp = System.currentTimeMillis()
            )
        )

        return id
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
        // Reverse balance
        if (transaction.partyType == "CUSTOMER") {
            val customer = dao.getCustomerById(transaction.partyId)
            if (customer != null) {
                val newBalance = if (transaction.type == "YOU_GAVE") {
                    customer.currentBalance - transaction.amount
                } else {
                    customer.currentBalance + transaction.amount
                }
                dao.updateCustomer(customer.copy(currentBalance = newBalance))
            }
        } else if (transaction.partyType == "SUPPLIER") {
            val supplier = dao.getSupplierById(transaction.partyId)
            if (supplier != null) {
                val newBalance = if (transaction.type == "YOU_GOT") {
                    supplier.currentBalance - transaction.amount
                } else {
                    supplier.currentBalance + transaction.amount
                }
                dao.updateSupplier(supplier.copy(currentBalance = newBalance))
            }
        }
    }

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()

    suspend fun addExpense(expense: ExpenseEntity): Long = dao.insertExpense(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)

    // Incomes
    val allIncomes: Flow<List<IncomeEntity>> = dao.getAllIncomes()

    suspend fun addIncome(income: IncomeEntity): Long = dao.insertIncome(income)

    suspend fun deleteIncome(income: IncomeEntity) = dao.deleteIncome(income)

    // Invoices
    val allInvoices: Flow<List<InvoiceEntity>> = dao.getAllInvoices()

    suspend fun getInvoiceById(id: Long): InvoiceEntity? = dao.getInvoiceById(id)

    suspend fun addInvoice(invoice: InvoiceEntity): Long {
        val id = dao.insertInvoice(invoice)
        dao.insertNotification(
            NotificationEntity(
                title = "Invoice ${invoice.invoiceNumber} Created",
                message = "Invoice for ₹${"%.0f".format(invoice.totalAmount)} (${invoice.customerName}) generated.",
                type = "INVOICE",
                timestamp = System.currentTimeMillis()
            )
        )
        return id
    }

    suspend fun updateInvoice(invoice: InvoiceEntity) = dao.updateInvoice(invoice)

    suspend fun deleteInvoice(invoice: InvoiceEntity) = dao.deleteInvoice(invoice)

    // Profile
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()

    suspend fun saveUserProfile(profile: UserProfileEntity) = dao.insertOrUpdateProfile(profile)

    // Notifications
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()

    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)

    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
}
