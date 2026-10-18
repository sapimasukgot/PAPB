package com.dave.moneymanager.data.model

data class TransactionCategory(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: Long,
    val isExpense: Boolean
) {
    companion object {
        val defaultExpenseCategories = listOf(
            TransactionCategory(1, "Food & Dining", "food", 0xFFEF4444L, true),
            TransactionCategory(2, "Transportation", "transport", 0xFF3B82F6L, true),
            TransactionCategory(3, "Shopping", "shopping", 0xFFF97316L, true),
            TransactionCategory(4, "Bills & Utilities", "bills", 0xFFF59E0BL, true),
            TransactionCategory(5, "Entertainment", "entertainment", 0xFF8B5CF6L, true),
            TransactionCategory(6, "Health", "health", 0xFF10B981L, true),
            TransactionCategory(7, "Education", "education", 0xFF6366F1L, true),
            TransactionCategory(8, "Church", "church", 0xFF8B5CF6L, true),
            TransactionCategory(9, "With Her", "love", 0xFFEC4899L, true),
            TransactionCategory(10, "Travel", "travel", 0xFF06B6D4L, true),
            TransactionCategory(11, "Fitness", "fitness", 0xFF84CC16L, true),
            TransactionCategory(12, "Family", "family", 0xFFD946EFL, true),
            TransactionCategory(13, "Charity / Donations", "donation", 0xFFF43F5EL, true),
            TransactionCategory(14, "Pets", "pets", 0xFFD97706L, true),
            TransactionCategory(15, "Home", "home", 0xFF0D9488L, true),
            TransactionCategory(16, "Shield", "shield", 0xFF10B981L, true),
            TransactionCategory(17, "Other Expense", "other", 0xFF6B7280L, true)
        )

        val defaultIncomeCategories = listOf(
            TransactionCategory(18, "Reimburse", "reimburse", 0xFF3B82F6L, false),
            TransactionCategory(19, "Salary", "salary", 0xFF10B981L, false),
            TransactionCategory(20, "Bonus", "bonus", 0xFFF59E0BL, false),
            TransactionCategory(21, "Top Up", "topup", 0xFFF97316L, false),
            TransactionCategory(22, "Cashback", "cashback", 0xFF8B5CF6L, false),
            TransactionCategory(23, "Investment", "investment", 0xFF06B6D4L, false),
            TransactionCategory(24, "Other Income", "other", 0xFF6B7280L, false)
        )

        val expenseCategories = defaultExpenseCategories
        val incomeCategories = defaultIncomeCategories

        fun isFoodCategory(catName: String?): Boolean {
            if (catName.isNullOrBlank()) return false
            val lower = catName.lowercase(java.util.Locale.ROOT).trim()
            return lower.contains("food") ||
                    lower.contains("dining") ||
                    lower.contains("beverage") ||
                    lower.contains("makan") ||
                    lower.contains("minum") ||
                    lower.contains("kuliner") ||
                    lower.contains("snack") ||
                    lower.contains("resto") ||
                    lower.contains("restaurant") ||
                    lower.contains("cafe") ||
                    lower.contains("kafe") ||
                    lower.contains("warung") ||
                    lower.contains("kopi") ||
                    lower.contains("coffee")
        }

        fun isSameDay(time1: Long, time2: Long): Boolean {
            val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = time1 }
            val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = time2 }
            return cal1.get(java.util.Calendar.ERA) == cal2.get(java.util.Calendar.ERA) &&
                    cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                    cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
        }
    }
}

fun isFoodCategory(catName: String?): Boolean = TransactionCategory.isFoodCategory(catName)
fun isSameDay(time1: Long, time2: Long): Boolean = TransactionCategory.isSameDay(time1, time2)
