package com.dave.moneymanager.data.model

data class Transaction(
    val id: Long = 0,
    val bankId: Long,
    val bankName: String = "",
    val targetBankId: Long? = null,
    val targetBankName: String? = null,
    val type: String,
    val amount: Double,
    val category: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isAutoScraped: Boolean = false,
    val sourceApp: String? = null,
    val destination: String? = null
) {
    companion object {
        const val TYPE_EXPENSE = "EXPENSE"
        const val TYPE_INCOME = "INCOME"
        const val TYPE_TRANSFER = "TRANSFER"
    }
}

data class TransactionEditHistory(
    val id: Long = 0,
    val transactionId: Long,
    val editTimestamp: Long = System.currentTimeMillis(),
    val oldAmount: Double,
    val newAmount: Double,
    val diffAmount: Double,
    val oldCategory: String? = null,
    val newCategory: String? = null,
    val oldBankName: String? = null,
    val newBankName: String? = null,
    val oldNote: String? = null,
    val newNote: String? = null,
    val changeSummary: String = ""
)
