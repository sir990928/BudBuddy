package com.benegedeniz.budsdynamiceq.media

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.benegedeniz.budsdynamiceq.di.ServiceLocator

/**
 * A NotificationListenerService is required by Android to get broad access to the MediaSessionManager.
 * We do not actually need to process notifications here, we just need this service to be enabled
 * by the user in the system Settings -> Notification access.
 */
class MediaListenerService : NotificationListenerService() {

    companion object {
        var instance: MediaListenerService? = null
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.i("MediaListenerService", "Listener connected. App now has notification access.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.i("MediaListenerService", "Listener disconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        
        val notification = sbn.notification
        val template = notification.extras.getString(Notification.EXTRA_TEMPLATE)
        
        if (template?.contains("MediaStyle") == true || notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION)) {
            
            val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            var artist = notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            
            if (artist.isNullOrBlank()) {
                artist = notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            }
            
            if (!title.isNullOrBlank()) {
                Log.d("MediaListenerService", "Extracted song from notification: $artist - $title")
                ServiceLocator.provideMediaObserver(this).updateTitleFromNotification(title, artist, sbn.packageName)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Unused
    }
}
