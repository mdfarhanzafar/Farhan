package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobile: String,
    val address: String = "",
    val openingBalance: Double = 0.0, // > 0: they owe user (You Will Get), < 0: user owes them (You Will Give)
    val currentBalance: Double = 0.0,
    val groupName: String = "Regular", // Retail, Wholesale, VIP, Regular
    val notes: String = "",
    val profileImageUri: String? = null,
    val lastTransactionDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobile: String,
    val address: String = "",
    val openingBalance: Double = 0.0, // > 0: user owes supplier (You Will Give), < 0: supplier owes user
    val currentBalance: Double = 0.0,
    val category: String = "Wholesale",
    val notes: String = "",
    val lastTransactionDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partyType: String, // "CUSTOMER" or "SUPPLIER"
    val partyId: Long,
    val partyName: String,
    val type: String, // "YOU_GAVE" (Debit) or "YOU_GOT" (Credit)
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val paymentMethod: String = "CASH", // CASH, UPI, BANK_TRANSFER, CARD, OTHER
    val notes: String = "",
    val billNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String, // Rent, Electricity, Salary, Transport, Inventory, Marketing, Food, Maintenance, Internet, Other
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val paymentMethod: String = "CASH",
    val receiptImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val sourceCategory: String, // Sales, Service, Commission, Other
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val paymentMethod: String = "CASH",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String,
    val customerMobile: String = "",
    val customerAddress: String = "",
    val invoiceDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val taxPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentStatus: String = "UNPAID", // PAID, UNPAID, PARTIAL
    val notes: String = "",
    val itemsJson: String = "", // serialized line items
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1,
    val fullName: String = "Rajesh Sharma",
    val ownerName: String = "Rajesh Sharma",
    val mobileNumber: String = "+91 98765 43210",
    val businessName: String = "Sharma Kirana & General Store",
    val businessCategory: String = "Kirana & General Store",
    val businessAddress: String = "Shop 12, Main Market, Delhi",
    val gstin: String = "07AAAAA0000A1Z5",
    val upiId: String = "sharmakirana@upi",
    val currency: String = "₹",
    val defaultTaxPercent: Double = 18.0,
    val invoicePrefix: String = "INV-",
    val language: String = "en", // "en", "hi", "hinglish"
    val isDarkMode: Boolean? = null, // null = system
    val isLoggedIn: Boolean = true,
    val role: String = "OWNER",
    val pin: String = "1234"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "REMINDER", "TRANSACTION", "INVOICE", "SUMMARY", "ALERT"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
