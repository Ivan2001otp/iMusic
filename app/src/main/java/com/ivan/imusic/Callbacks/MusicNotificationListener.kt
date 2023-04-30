package com.ivan.imusic.Callbacks

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.provider.SyncStateContract.Constants
import androidx.core.app.ServiceCompat.STOP_FOREGROUND_DETACH
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener
import com.ivan.imusic.Constants.Others.NOTIFICATION_ID
import com.ivan.imusic.Service.MusicNotificationManager
import com.ivan.imusic.Service.MusicService

class MusicNotificationListener
    (private val musicService: MusicService) : PlayerNotificationManager.NotificationListener {

    override fun onNotificationCancelled(
        notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)

        musicService.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(Service.STOP_FOREGROUND_REMOVE)
            }
            isForegroundService=false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)

        musicService.apply {
            if(ongoing && !isForegroundService){

                ContextCompat.startForegroundService(this,
                    Intent(applicationContext,this::class.java)
                )
                isForegroundService = true
                startForeground(NOTIFICATION_ID,notification)

            }
        }
    }
}