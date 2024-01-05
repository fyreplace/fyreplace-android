package app.fyreplace.fyreplace.extensions

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipDescription
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.use
import app.fyreplace.fyreplace.R
import com.google.android.material.color.DynamicColors
import com.google.protobuf.ByteString
import java.util.Date

val Context.mainPreferences: SharedPreferences
    get() = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

fun Context.makeShareUri(type: String, id: ByteString, position: Int? = null): Uri {
    val host = getString(R.string.link_hosts).split(';').first()
    var uriString = "https://$host/$type/${id.base64ShortString}"
    position?.let { uriString += "/$position" }
    return Uri.parse(uriString)
}

fun Context.makeShareIntent(type: String, id: ByteString, position: Int? = null) =
    Intent(Intent.ACTION_SEND).apply {
        this.type = ClipDescription.MIMETYPE_TEXT_PLAIN
        putExtra(Intent.EXTRA_TEXT, makeShareUri(type, id, position).toString())
    }

fun Context.getDynamicColor(@AttrRes attr: Int, @ColorInt default: Int) = DynamicColors
    .wrapContextIfAvailable(this)
    .obtainStyledAttributes(intArrayOf(attr))
    .use { it.getColor(0, default) }

fun Context.createNotification(
    tag: String,
    intent: Intent,
    channel: String?,
    title: String,
    body: String,
    date: Date
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
            .setWhen(date.time)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

    NotificationManagerCompat.from(this).notify(tag, 0, notification)
}

fun Context.deleteNotification(tag: String) = NotificationManagerCompat.from(this).cancel(tag, 0)

fun Context.deleteNotifications(tag: Regex, beforeDate: Date) {
    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    val notificationManagerCompat = NotificationManagerCompat.from(this)

    for (statusNotification in notificationManager.activeNotifications) {
        if (tag.matches(statusNotification.tag) && statusNotification.notification.`when` <= beforeDate.time) {
            notificationManagerCompat.cancel(statusNotification.tag, statusNotification.id)
        }
    }
}

fun Context.deleteNotifications() = NotificationManagerCompat.from(this).cancelAll()
