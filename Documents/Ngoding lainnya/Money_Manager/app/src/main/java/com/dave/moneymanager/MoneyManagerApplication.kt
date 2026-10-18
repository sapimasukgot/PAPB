package com.dave.moneymanager

import android.app.Application
import com.dave.moneymanager.data.local.MoneyRepository
import com.dave.moneymanager.service.KeepAliveService

class MoneyManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MoneyRepository.getInstance(this)

        KeepAliveService.createNotificationChannel(this)

        KeepAliveService.start(this)
    }
}
