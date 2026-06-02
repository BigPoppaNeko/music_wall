package com.jfcardenas.musicwall.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.jfcardenas.musicwall.scrobble.NowPlayingBus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NowPlayingListenerService : NotificationListenerService() {

    @Inject lateinit var nowPlayingBus: NowPlayingBus

    override fun onListenerConnected() {
        super.onListenerConnected()
        ScrobbleForegroundService.start(this)
        nowPlayingBus.notifyChanged()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        nowPlayingBus.notifyChanged()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        nowPlayingBus.notifyChanged()
    }
}
