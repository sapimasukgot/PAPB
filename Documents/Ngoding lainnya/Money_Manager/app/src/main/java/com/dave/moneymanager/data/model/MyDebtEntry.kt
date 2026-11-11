package com.dave.moneymanager.data.model

data class MyDebtEntry(
    val id: Long = 0,
    val personName: String,
    val amount: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSettled: Boolean = false,
    val settledAt: Long? = null,
    val settledBankId: Long? = null,
    val settledBankName: String? = null,
    val category: String = "Food & Dining",
    val settledTransactionId: Long? = null,
    val avatarUri: String? = null
)
