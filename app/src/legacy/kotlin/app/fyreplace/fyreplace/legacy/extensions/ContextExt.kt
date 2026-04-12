package app.fyreplace.fyreplace.legacy.extensions

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipDescription
import android.content.ComponentName
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import app.fyreplace.fyreplace.R
import com.squareup.wire.Instant
import okio.ByteString

private val activityToAppIcon
    get() = mapOf(
        "app.fyreplace.fyreplace.legacy.ui.MainActivity.Normal" to R.mipmap.ic_launcher,
        "app.fyreplace.fyreplace.legacy.ui.MainActivity.Alternative" to R.mipmap.ic_launcher_alt
    )

val Context.mainPreferences: SharedPreferences
    get() = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

fun Context.makeShareUri(type: String, id: ByteString, position: Int? = null): Uri {
    val host = getString(R.string.link_hosts).split(';').first()
    var uriString = "https://$host/$type/${id.base64ShortString}"
    position?.let { uriString += "/$position" }
    return uriString.toUri()
}

fun Context.makeShareIntent(type: String, id: ByteString, position: Int? = null) =
    Intent(Intent.ACTION_SEND).apply {
        this.type = ClipDescription.MIMETYPE_TEXT_PLAIN
        putExtra(Intent.EXTRA_TEXT, makeShareUri(type, id, position).toString())
    }

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun Context.createNotification(
    tag: String,
    intent: Intent,
    channel: String?,
    title: String,
    body: String,
    instant: Instant
) {
    val contentIntent = PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val notification =
        NotificationCompat.Builder(
            this,
            channel ?: getString(R.string.notification_channel_default_id)
        )
            .setSmallIcon(R.drawable.logo_notification)
            .setColor(getColor(R.color.seed))
            .setContentTitle(title)
            .setContentText(body)
            .setWhen(instant.epochSecond)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

    NotificationManagerCompat.from(this).notify(tag, 0, notification)
}

fun Context.deleteNotification(tag: String) = NotificationManagerCompat.from(this).cancel(tag, 0)

fun Context.deleteNotifications(tag: Regex, beforeDate: Instant) {
    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    val notificationManagerCompat = NotificationManagerCompat.from(this)

    for (statusNotification in notificationManager.activeNotifications) {
        if (tag.matches(statusNotification.tag) && statusNotification.notification.`when` <= beforeDate.toEpochMilli()) {
            notificationManagerCompat.cancel(statusNotification.tag, statusNotification.id)
        }
    }
}

fun Context.deleteNotifications() = NotificationManagerCompat.from(this).cancelAll()

fun Context.setAppIcon(@DrawableRes newAppIcon: Int) {
    for ((activity, appIcon) in activityToAppIcon.entries) {
        packageManager.setComponentEnabledSetting(
            ComponentName(this, activity),
            if (appIcon == newAppIcon) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}

@DrawableRes
fun Context.getAppIcon(): Int {
    for ((activity, appIcon) in activityToAppIcon.entries) {
        if (packageManager.getComponentEnabledSetting(
                ComponentName(
                    this,
                    activity
                )
            ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        ) {
            return appIcon
        }
    }

    return R.mipmap.ic_launcher
}
