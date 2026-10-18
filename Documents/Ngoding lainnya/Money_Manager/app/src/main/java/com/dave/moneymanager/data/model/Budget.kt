package com.dave.moneymanager.data.model

data class Budget(
    val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val monthYear: String
)

data class CategoryBudgetStatus(
    val budgetId: Long,
    val category: String,
    val monthlyLimit: Double,
    val currentSpent: Double,
    val remainingBudget: Double,
    val percentageUsed: Float,
    val isExceeded: Boolean,
    val colorHex: Long,
    val iconName: String
)

data class BudgetAlertData(
    val category: String,
    val monthlyLimit: Double,
    val currentSpent: Double,
    val remainingBudget: Double,
    val overBudgetAmount: Double
)
