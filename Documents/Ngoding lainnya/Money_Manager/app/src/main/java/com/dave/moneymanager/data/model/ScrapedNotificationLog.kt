package com.dave.moneymanager.data.model

data class ScrapedNotificationLog(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val amount: Double? = null,
    val detectedType: String? = null,
    val targetBank: String? = null,
    val destination: String? = null
)
