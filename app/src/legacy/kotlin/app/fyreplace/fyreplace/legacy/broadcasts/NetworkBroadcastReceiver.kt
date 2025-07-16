package app.fyreplace.fyreplace.legacy.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.NetworkConnectionWasChangedEvent
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.ManagedChannel
import javax.inject.Inject

@AndroidEntryPoint
class NetworkBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var em: EventsManager

    @Inject
    lateinit var channel: ManagedChannel

    override fun onReceive(context: Context, intent: Intent) {
        channel.enterIdle()
        em.post(NetworkConnectionWasChangedEvent(intent))
    }
}
