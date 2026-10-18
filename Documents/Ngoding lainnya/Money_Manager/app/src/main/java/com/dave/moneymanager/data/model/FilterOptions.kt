package com.dave.moneymanager.data.model

import java.util.Calendar

enum class TimeFilterMode(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    SPECIFIC_DATE("Select Day"),
    CUSTOM_RANGE("Custom Range")
}

data class FilterOptions(
    val selectedBankId: Long? = null,
    val timeFilterMode: TimeFilterMode = TimeFilterMode.THIS_MONTH,
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val rangeStartMillis: Long = 0,
    val rangeEndMillis: Long = Long.MAX_VALUE,
    val typeFilter: String = "ALL",
    val isManualMonthSelection: Boolean = false
) {
    fun isViewingCurrentMonth(): Boolean {
        val now = Calendar.getInstance()
        return selectedYear == now.get(Calendar.YEAR) && selectedMonth == now.get(Calendar.MONTH)
    }

    fun isViewingCurrentDay(): Boolean {
        val now = Calendar.getInstance()
        val sel = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        return sel.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
               sel.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    }

    companion object {
        fun default(): FilterOptions {
            val cal = Calendar.getInstance()
            return FilterOptions(
                selectedBankId = null,
                timeFilterMode = TimeFilterMode.THIS_MONTH,
                selectedDateMillis = cal.timeInMillis,
                selectedYear = cal.get(Calendar.YEAR),
                selectedMonth = cal.get(Calendar.MONTH),
                rangeStartMillis = 0,
                rangeEndMillis = Long.MAX_VALUE,
                typeFilter = "ALL",
                isManualMonthSelection = false
            )
        }
    }
}
