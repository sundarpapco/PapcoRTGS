package com.papco.sundar.papcortgs

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log

class PapcoRTGSApp:Application() {

    companion object{
        const val CHANNEL_ID_INTIMATION="smsChannelID"
    }

    override fun onCreate() {
        super.onCreate()

        //Create the Notification channel so that we can display notifications in the app
        createNotificationChannel()
    }

    private fun createNotificationChannel() {

        val channel = NotificationChannel(
            CHANNEL_ID_INTIMATION,
            applicationContext.getString(R.string.channel_name_intimation),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description=applicationContext.getString(R.string.notification_channel_description)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}