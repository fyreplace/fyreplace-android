package app.fyreplace.fyreplace.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MessagingService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
