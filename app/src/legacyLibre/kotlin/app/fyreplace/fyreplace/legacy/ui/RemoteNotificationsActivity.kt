package app.fyreplace.fyreplace.legacy.ui

import androidx.appcompat.app.AppCompatActivity
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.protos.Comment
import okio.ByteString
import javax.inject.Inject

abstract class RemoteNotificationsActivity(contentLayoutId: Int) :
    AppCompatActivity(contentLayoutId),
    FailureHandler {
    @Inject
    lateinit var em: EventsManager

    abstract fun tryHandleCommentCreation(comment: Comment, postId: ByteString): Boolean

    protected fun startReceivingRemoteNotifications() = Unit

    protected fun stopReceivingRemoteNotifications() = Unit

    protected fun requestNotificationPermission() = Unit
}
