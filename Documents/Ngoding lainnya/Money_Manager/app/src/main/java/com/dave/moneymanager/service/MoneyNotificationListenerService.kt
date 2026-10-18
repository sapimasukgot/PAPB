package com.dave.moneymanager.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dave.moneymanager.data.local.MoneyRepository
import com.dave.moneymanager.data.model.ScrapedNotificationLog
import com.dave.moneymanager.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoneyNotificationListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val TAG = "MoneyNotifListener"
        private val _isServiceRunningFlow = MutableStateFlow(false)
        val isServiceRunningFlow = _isServiceRunningFlow.asStateFlow()

        private val recentNotificationTimestamps = java.util.concurrent.ConcurrentHashMap<String, Long>()

        var isServiceRunning: Boolean
            get() = _isServiceRunningFlow.value
            private set(value) {
                _isServiceRunningFlow.value = value
            }

        fun forceReconnect(context: Context) {
            val comp = ComponentName(context, MoneyNotificationListenerService::class.java)
            val pm = context.packageManager
            try {

                pm.setComponentEnabledSetting(
                    comp,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    comp,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.i(TAG, "Notification listener component state toggled successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling component setting: ${e.message}")
            }

            try {
                requestRebind(comp)
                Log.i(TAG, "requestRebind called successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "requestRebind failed: ${e.message}")
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceRunning = true
        Log.i(TAG, "Money Notification Listener connected to Android OS!")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceRunning = false
        Log.w(TAG, "Money Notification Listener disconnected by Android OS! Attempting auto-rebind...")
        try {
            forceReconnect(this)
        } catch (_: Exception) {}
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        if (packageName == applicationContext.packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = (extras.getCharSequence(Notification.EXTRA_TITLE_BIG)
            ?: extras.getCharSequence(Notification.EXTRA_TITLE))?.toString()?.trim() ?: ""

        if (!NotificationParser.isFinancialApp(packageName, title)) {
            return
        }

        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim() ?: ""
        val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()?.trim() ?: ""
        val infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()?.trim() ?: ""
        val tickerText = notification.tickerText?.toString()?.trim() ?: ""

        @Suppress("DEPRECATION")
        val messagingText = try {
            val msgArray = extras.getParcelableArray("android.messages")
            msgArray?.mapNotNull { parcel ->
                (parcel as? android.os.Bundle)?.getCharSequence("text")?.toString()?.trim()
            }?.joinToString(". ") ?: ""
        } catch (_: Exception) { "" }

        val conversationTitle = extras.getCharSequence("android.conversationTitle")?.toString()?.trim() ?: ""

        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        val linesJoined = textLines?.filterNotNull()?.joinToString(" ") { it.toString().trim() } ?: ""

        val contentParts = mutableListOf<String>()
        if (text.isNotEmpty()) contentParts.add(text)
        if (bigText.isNotEmpty() && !contentParts.contains(bigText)) contentParts.add(bigText)
        if (linesJoined.isNotEmpty() && !contentParts.contains(linesJoined)) contentParts.add(linesJoined)
        if (messagingText.isNotEmpty() && contentParts.none { it.contains(messagingText) }) contentParts.add(messagingText)
        if (contentParts.isEmpty() && conversationTitle.isNotEmpty()) contentParts.add(conversationTitle)
        if (contentParts.isEmpty() && subText.isNotEmpty()) contentParts.add(subText)
        if (contentParts.isEmpty() && infoText.isNotEmpty()) contentParts.add(infoText)
        if (contentParts.isEmpty() && summaryText.isNotEmpty()) contentParts.add(summaryText)
        if (contentParts.isEmpty() && tickerText.isNotEmpty()) contentParts.add(tickerText)

        val content = contentParts.joinToString(". ")

        if (title.isEmpty() && content.isEmpty()) {
            return
        }

        val dedupeKey = "${title.trim().lowercase()}|${content.trim().lowercase()}"
        val now = System.currentTimeMillis()
        recentNotificationTimestamps.entries.removeIf { now - it.value > 30_000L }
        val lastSeen = recentNotificationTimestamps[dedupeKey]
        if (lastSeen != null && now - lastSeen < 15_000L) {
            Log.w(TAG, "Duplicate notification received within 15s from $packageName, dropped: [$title] $content")
            return
        }
        recentNotificationTimestamps[dedupeKey] = now

        val isClonedUser = try {
            val userStr = sbn.user?.toString() ?: ""
            val isNotUserZero = !userStr.contains("UserHandle{0}") && !userStr.contains("UserHandle[0]") && !userStr.endsWith("0}")
            isNotUserZero || userStr.contains("999") || userStr.contains("95") || userStr.contains("10")
        } catch (_: Exception) {
            false
        }

        Log.d(TAG, "Captured financial notification from $packageName (isClonedUser=$isClonedUser): [$title] $content")

        scope.launch {
            handleNotification(packageName, title, content, sbn.postTime, isClonedUser, true)
        }
    }

    private suspend fun handleNotification(
        packageName: String,
        title: String,
        content: String,
        postTime: Long,
        isClonedUser: Boolean = false,
        isTarget: Boolean = true
    ) {
        val repository = MoneyRepository.getInstance(applicationContext)

        if (!isTarget) {
            return
        }

        val result = repository.processScrapedNotification(
            packageName = packageName,
            title = title,
            content = content,
            postTime = postTime,
            isClonedUser = isClonedUser
        )
        Log.i(TAG, "Notification result: ${result.message}")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        job.cancel()
    }
}
