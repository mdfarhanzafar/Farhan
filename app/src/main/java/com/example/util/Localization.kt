package com.example.util

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    HINGLISH("hinglish", "Hinglish", "Hinglish")
}

object Strings {
    private val translations = mapOf(
        "app_tagline" to mapOf(
            "en" to "Apna Hisab, Apne Haath",
            "hi" to "अपना हिसाब, अपने हाथ",
            "hinglish" to "Apna Hisab, Apne Haath"
        ),
        "you_will_get" to mapOf(
            "en" to "You Will Get",
            "hi" to "आपको मिलेंगे",
            "hinglish" to "Aapko Milenge"
        ),
        "you_will_give" to mapOf(
            "en" to "You Will Give",
            "hi" to "आपको देने हैं",
            "hinglish" to "Aapko Dene Hain"
        ),
        "today_collection" to mapOf(
            "en" to "Today's Collection",
            "hi" to "आज की वसूली",
            "hinglish" to "Aaj Ki Collection"
        ),
        "today_expenses" to mapOf(
            "en" to "Today's Expenses",
            "hi" to "आज के खर्चे",
            "hinglish" to "Aaj Ke Kharche"
        ),
        "net_balance" to mapOf(
            "en" to "Net Balance",
            "hi" to "कुल शुद्ध बकाया",
            "hinglish" to "Net Khata Balance"
        ),
        "you_gave" to mapOf(
            "en" to "You Gave",
            "hi" to "मैंने दिए (उधार)",
            "hinglish" to "Maine Diye (Udhaar)"
        ),
        "you_got" to mapOf(
            "en" to "You Got",
            "hi" to "मैंने लिए (जमा)",
            "hinglish" to "Maine Liye (Jama)"
        ),
        "add_customer" to mapOf(
            "en" to "Add Customer",
            "hi" to "ग्राहक जोड़ें",
            "hinglish" to "Naya Customer Jodein"
        ),
        "add_supplier" to mapOf(
            "en" to "Add Supplier",
            "hi" to "सप्लायर जोड़ें",
            "hinglish" to "Naya Supplier Jodein"
        ),
        "add_expense" to mapOf(
            "en" to "Add Expense",
            "hi" to "खर्चा जोड़ें",
            "hinglish" to "Kharcha Likhein"
        ),
        "add_income" to mapOf(
            "en" to "Add Income",
            "hi" to "कमाई जोड़ें",
            "hinglish" to "Income Likhein"
        ),
        "create_invoice" to mapOf(
            "en" to "Create Invoice",
            "hi" to "बिल बनाएं",
            "hinglish" to "Naya Bill Banayein"
        ),
        "send_reminder" to mapOf(
            "en" to "Send Reminder",
            "hi" to "तगादा भेजें",
            "hinglish" to "Payment Reminder Bhejein"
        ),
        "customers" to mapOf(
            "en" to "Customers",
            "hi" to "ग्राहक सूची",
            "hinglish" to "Customers Khata"
        ),
        "suppliers" to mapOf(
            "en" to "Suppliers",
            "hi" to "सप्लायर सूची",
            "hinglish" to "Suppliers Khata"
        ),
        "transactions" to mapOf(
            "en" to "Transactions",
            "hi" to "लेन-देन",
            "hinglish" to "All Transactions"
        ),
        "reports" to mapOf(
            "en" to "Reports",
            "hi" to "रिपोर्ट व हिसाब",
            "hinglish" to "Reports & Analytics"
        ),
        "profile" to mapOf(
            "en" to "Business Profile",
            "hi" to "व्यापार प्रोफाइल",
            "hinglish" to "Dukan Profile"
        ),
        "search" to mapOf(
            "en" to "Search customer, supplier or bill...",
            "hi" to "ग्राहक, सप्लायर या बिल खोजें...",
            "hinglish" to "Search customer, supplier or bill..."
        )
    )

    fun get(key: String, lang: String = "en"): String {
        return translations[key]?.get(lang) ?: translations[key]?.get("en") ?: key
    }
}
