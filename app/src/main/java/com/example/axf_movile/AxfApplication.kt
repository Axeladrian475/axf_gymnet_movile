package com.example.axf_movile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.axf_movile.network.SessionManager

class AxfApplication : Application() {

    companion object {
        const val CHANNEL_ID = "axf_channel"
        const val CHANNEL_NAME = "AXF GymNet"
    }

    override fun onCreate() {
        super.onCreate()
        SessionManager.init(applicationContext)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de AXF GymNet"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
