package com.dave.moneymanager.data.model

data class ShieldEntry(
    val id: Long = 0,
    val transactionId: Long? = null,
    val friendName: String,
    val amount: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSettled: Boolean = false,
    val settledAt: Long? = null,
    val settledBankId: Long? = null,
    val settledBankName: String? = null,
    val avatarUri: String? = null
)

data class ShieldFriendInput(
    val name: String,
    val amount: Double,
    val note: String = "",
    val isSettled: Boolean = false,
    val paidAmount: Double = 0.0,
    val isTransfer: Boolean = false,
    val settleBankId: Long? = null,
    val settleBankName: String? = null
)

data class ShieldHistoryItem(
    val id: Long = 0,
    val shieldId: Long,
    val transactionId: Long? = null,
    val friendName: String,
    val amount: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class ShieldTransactionStatus {
    NONE,
    UNSETTLED,
    SETTLED
}

data class ShieldContact(
    val id: Long = 0,
    val name: String,
    val avatarUri: String? = null
)

data class ShieldWalletDeduction(
    val bankId: Long,
    val bankName: String,
    val amount: Double
)

