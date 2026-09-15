package com.example.data.database

import com.example.data.dao.HisabKitabDao
import com.example.data.entity.*
import kotlinx.coroutines.flow.first

object DatabaseSeeder {
    suspend fun seedDatabaseIfEmpty(dao: HisabKitabDao) {
        val existingCustomers = dao.getAllCustomers().first()
        if (existingCustomers.isNotEmpty()) return

        // 1. User Profile
        dao.insertOrUpdateProfile(
            UserProfileEntity(
                id = 1,
                fullName = "Rajesh Sharma",
                mobileNumber = "+91 98765 43210",
                businessName = "Sharma Kirana & General Store",
                businessCategory = "Kirana & General Store",
                businessAddress = "Shop 12, Main Market, Delhi - 110092",
                gstin = "07AAAAA0000A1Z5",
                currency = "₹",
                defaultTaxPercent = 18.0,
                invoicePrefix = "INV-2026-",
                language = "en",
                isLoggedIn = true,
                role = "OWNER"
            )
        )

        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L

        // 2. Customers
        val rahulId = dao.insertCustomer(
            CustomerEntity(
                name = "Rahul Kumar",
                mobile = "+91 98112 34567",
                address = "H-42, Laxmi Nagar, Delhi",
                openingBalance = 0.0,
                currentBalance = 4500.0,
                groupName = "Regular",
                notes = "Pays usually on 1st of every month via UPI",
                lastTransactionDate = now - (oneDay * 1)
            )
        )

        val amitId = dao.insertCustomer(
            CustomerEntity(
                name = "Amit Traders",
                mobile = "+91 98223 45678",
                address = "Shop 4, Chandni Chowk, Delhi",
                openingBalance = 0.0,
                currentBalance = 12800.0,
                groupName = "Wholesale",
                notes = "Bulk buyer for pulses and grains",
                lastTransactionDate = now - (oneDay * 2)
            )
        )

        val sanaId = dao.insertCustomer(
            CustomerEntity(
                name = "Sana Enterprises",
                mobile = "+91 98334 56789",
                address = "Plot 19, Karol Bagh, Delhi",
                openingBalance = 0.0,
                currentBalance = -3200.0, // We owe them advance!
                groupName = "VIP",
                notes = "Advance payment given for upcoming festival supply",
                lastTransactionDate = now - (oneDay * 3)
            )
        )

        val rakeshId = dao.insertCustomer(
            CustomerEntity(
                name = "Rakesh Store",
                mobile = "+91 98445 67890",
                address = "Atta Market, Sector 18, Noida",
                openingBalance = 0.0,
                currentBalance = 6750.0,
                groupName = "Retail",
                notes = "Monthly credit customer",
                lastTransactionDate = now - (oneDay * 4)
            )
        )

        val imranId = dao.insertCustomer(
            CustomerEntity(
                name = "Imran General Store",
                mobile = "+91 98556 78901",
                address = "Railway Road, Old City, Ghaziabad",
                openingBalance = 0.0,
                currentBalance = 1500.0,
                groupName = "Regular",
                notes = "Small balance pending",
                lastTransactionDate = now - (oneDay * 5)
            )
        )

        // 3. Suppliers
        val sharmaSuppId = dao.insertSupplier(
            SupplierEntity(
                name = "Sharma FMCG Distributors",
                mobile = "+91 98667 89012",
                address = "Godown 7, Kashmere Gate, Delhi",
                openingBalance = 0.0,
                currentBalance = 18500.0,
                category = "FMCG Wholesale",
                notes = "Main dealer for soaps, toiletries, and tea",
                lastTransactionDate = now - (oneDay * 2)
            )
        )

        val poojaSuppId = dao.insertSupplier(
            SupplierEntity(
                name = "Pooja Packaging & Bags",
                mobile = "+91 98778 90123",
                address = "Okhla Industrial Area Phase 2, Delhi",
                openingBalance = 0.0,
                currentBalance = 4200.0,
                category = "Packaging Material",
                notes = "Carry bags and boxes supplier",
                lastTransactionDate = now - (oneDay * 6)
            )
        )

        val mahaveerSuppId = dao.insertSupplier(
            SupplierEntity(
                name = "Mahaveer Grains & Oils",
                mobile = "+91 98889 01234",
                address = "Naya Bazar Wholesale Market, Delhi",
                openingBalance = 0.0,
                currentBalance = 9600.0,
                category = "Grains & Pulses",
                notes = "Supplies wheat, rice, mustard oil",
                lastTransactionDate = now - (oneDay * 3)
            )
        )

        // 4. Sample Transactions
        // Rahul Kumar transactions
        dao.insertTransaction(
            TransactionEntity(
                partyType = "CUSTOMER",
                partyId = rahulId,
                partyName = "Rahul Kumar",
                type = "YOU_GAVE",
                amount = 2000.0,
                date = now - (oneDay * 3),
                description = "Sale of Groceries & Pulses",
                paymentMethod = "CASH",
                notes = "Bill #240"
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                partyType = "CUSTOMER",
                partyId = rahulId,
                partyName = "Rahul Kumar",
                type = "YOU_GOT",
                amount = 1000.0,
                date = now - (oneDay * 2),
                description = "Payment Received via GPay UPI",
                paymentMethod = "UPI",
                notes = "UPI Ref: 42918820"
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                partyType = "CUSTOMER",
                partyId = rahulId,
                partyName = "Rahul Kumar",
                type = "YOU_GAVE",
                amount = 3500.0,
                date = now - (oneDay * 1),
                description = "Delivered cooking oil (5L) & basmati rice",
                paymentMethod = "CASH",
                notes = "Pending payment promise by weekend"
            )
        )

        // Amit Traders transactions
        dao.insertTransaction(
            TransactionEntity(
                partyType = "CUSTOMER",
                partyId = amitId,
                partyName = "Amit Traders",
                type = "YOU_GAVE",
                amount = 15000.0,
                date = now - (oneDay * 4),
                description = "Bulk wheat bags supply (10 bags)",
                paymentMethod = "BANK_TRANSFER",
                notes = "Invoice INV-2026-002"
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                partyType = "CUSTOMER",
                partyId = amitId,
                partyName = "Amit Traders",
                type = "YOU_GOT",
                amount = 2200.0,
                date = now - (oneDay * 2),
                description = "NEFT part payment received",
                paymentMethod = "BANK_TRANSFER",
                notes = "Bank Ref: SBIN88390"
            )
        )

        // Sana Enterprises
        dao.insertTransaction(
            TransactionEntity(
                partyType = "CUSTOMER",
                partyId = sanaId,
                partyName = "Sana Enterprises",
                type = "YOU_GOT",
                amount = 3200.0,
                date = now - (oneDay * 3),
                description = "Advance received for upcoming festive order",
                paymentMethod = "UPI",
                notes = "Will adjust in next delivery"
            )
        )

        // Supplier Transactions
        dao.insertTransaction(
            TransactionEntity(
                partyType = "SUPPLIER",
                partyId = sharmaSuppId,
                partyName = "Sharma FMCG Distributors",
                type = "YOU_GOT", // Supplier gave us goods = We got inventory
                amount = 25000.0,
                date = now - (oneDay * 5),
                description = "FMCG inventory batch delivery",
                paymentMethod = "BANK_TRANSFER"
            )
        )
        dao.insertTransaction(
            TransactionEntity(
                partyType = "SUPPLIER",
                partyId = sharmaSuppId,
                partyName = "Sharma FMCG Distributors",
                type = "YOU_GAVE", // We paid supplier
                amount = 6500.0,
                date = now - (oneDay * 2),
                description = "Cheque clearing payment",
                paymentMethod = "BANK_TRANSFER"
            )
        )

        // 5. Expenses
        dao.insertExpense(
            ExpenseEntity(
                amount = 15000.0,
                category = "Rent",
                date = now - (oneDay * 4),
                description = "Shop Monthly Rent for Main Market",
                paymentMethod = "BANK_TRANSFER"
            )
        )
        dao.insertExpense(
            ExpenseEntity(
                amount = 3450.0,
                category = "Electricity",
                date = now - (oneDay * 3),
                description = "Commercial BSES Power Bill",
                paymentMethod = "UPI"
            )
        )
        dao.insertExpense(
            ExpenseEntity(
                amount = 8000.0,
                category = "Salary",
                date = now - (oneDay * 2),
                description = "Shop assistant staff monthly salary",
                paymentMethod = "CASH"
            )
        )
        dao.insertExpense(
            ExpenseEntity(
                amount = 650.0,
                category = "Transport",
                date = now,
                description = "Goods delivery auto tempo fare from godown",
                paymentMethod = "CASH"
            )
        )
        dao.insertExpense(
            ExpenseEntity(
                amount = 180.0,
                category = "Food",
                date = now,
                description = "Daily staff tea and samosas",
                paymentMethod = "UPI"
            )
        )

        // 6. Incomes
        dao.insertIncome(
            IncomeEntity(
                amount = 14250.0,
                sourceCategory = "Sales",
                date = now,
                description = "Today's retail counter cash collection",
                paymentMethod = "CASH"
            )
        )
        dao.insertIncome(
            IncomeEntity(
                amount = 450.0,
                sourceCategory = "Service",
                date = now,
                description = "Express home delivery service fee",
                paymentMethod = "UPI"
            )
        )
        dao.insertIncome(
            IncomeEntity(
                amount = 1200.0,
                sourceCategory = "Commission",
                date = now - (oneDay * 1),
                description = "Mobile recharge & bill payment commission",
                paymentMethod = "UPI"
            )
        )

        // 7. Invoices
        val items1 = """
            [
              {"name": "Fortune Sunlite Sunflower Oil 5L", "qty": 4, "rate": 650.0, "amount": 2600.0},
              {"name": "India Gate Basmati Rice 25kg", "qty": 2, "rate": 1800.0, "amount": 3600.0}
            ]
        """.trimIndent()
        dao.insertInvoice(
            InvoiceEntity(
                invoiceNumber = "INV-2026-001",
                customerId = rahulId,
                customerName = "Rahul Kumar",
                customerMobile = "+91 98112 34567",
                customerAddress = "H-42, Laxmi Nagar, Delhi",
                invoiceDate = now - (oneDay * 3),
                dueDate = now + (oneDay * 4),
                subtotal = 6200.0,
                discount = 200.0,
                taxPercent = 5.0,
                taxAmount = 300.0,
                totalAmount = 6300.0,
                paymentStatus = "PAID",
                notes = "Thank you for shopping with Sharma Kirana Store!",
                itemsJson = items1
            )
        )

        val items2 = """
            [
              {"name": "Aashirvaad Shudh Chakki Atta 50kg", "qty": 10, "rate": 2000.0, "amount": 20000.0},
              {"name": "Madhur Pure Crystal Sugar 50kg", "qty": 5, "rate": 3000.0, "amount": 15000.0}
            ]
        """.trimIndent()
        dao.insertInvoice(
            InvoiceEntity(
                invoiceNumber = "INV-2026-002",
                customerId = amitId,
                customerName = "Amit Traders",
                customerMobile = "+91 98223 45678",
                customerAddress = "Shop 4, Chandni Chowk, Delhi",
                invoiceDate = now - (oneDay * 4),
                dueDate = now + (oneDay * 3),
                subtotal = 35000.0,
                discount = 1000.0,
                taxPercent = 5.0,
                taxAmount = 1700.0,
                totalAmount = 35700.0,
                paymentStatus = "UNPAID",
                notes = "Payment due within 7 days. Interest @1.5% applicable on late dues.",
                itemsJson = items2
            )
        )

        // 8. Notifications
        dao.insertNotification(
            NotificationEntity(
                title = "Welcome to Hisab Kitab!",
                message = "Apna Hisab, Apne Haath. All your customer khata, invoices and daily expenses in one place.",
                type = "ALERT",
                timestamp = now - (oneDay * 3),
                isRead = true
            )
        )
        dao.insertNotification(
            NotificationEntity(
                title = "Payment Due: Rahul Kumar",
                message = "Pending balance of ₹4,500. Tap to send a polite WhatsApp reminder.",
                type = "REMINDER",
                timestamp = now - (oneDay * 1),
                isRead = false
            )
        )
        dao.insertNotification(
            NotificationEntity(
                title = "Invoice #INV-2026-002 Created",
                message = "Invoice for ₹35,700 generated for Amit Traders. Status: Unpaid.",
                type = "INVOICE",
                timestamp = now - (oneDay * 2),
                isRead = false
            )
        )
        dao.insertNotification(
            NotificationEntity(
                title = "Daily Hisab Summary",
                message = "Today's collection: ₹14,250. Today's expenses: ₹830. Net: +₹13,420.",
                type = "SUMMARY",
                timestamp = now,
                isRead = false
            )
        )
    }
}
