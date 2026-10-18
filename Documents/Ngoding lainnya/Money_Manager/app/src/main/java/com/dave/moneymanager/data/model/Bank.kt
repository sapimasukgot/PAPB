package com.dave.moneymanager.data.model

data class Bank(
    val id: Long = 0,
    val name: String,
    val accountNumber: String = "",
    val balance: Double = 0.0,
    val type: String = TYPE_BANK,
    val colorHex: Long = 0xFF1E293B,
    val iconName: String = "account_balance",
    val displayOrder: Int = 0
) {
    companion object {
        const val TYPE_BANK = "BANK"
        const val TYPE_EWALLET = "EWALLET"
        const val TYPE_CASH = "CASH"
        const val TYPE_OTHER = "OTHER"
    }
}
