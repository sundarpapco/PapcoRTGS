package com.papco.sundar.papcortgs.extentions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

fun Context.weHaveNotificationPermission(): Boolean {
    NotificationManagerCompat.from(applicationContext).apply {
        return if (Build.VERSION.SDK_INT <= 32) true
        else
            ActivityCompat.checkSelfPermission(
                this@weHaveNotificationPermission, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.isInternetConnected(): Boolean =
    (this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
    getNetworkCapabilities(activeNetwork)?.run {
        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    } ?: false
}