package app.fyreplace.fyreplace.legacy.extensions

import com.squareup.wire.Instant
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

private val dateTimeFormat = SimpleDateFormat.getDateTimeInstance()
private val shortDateTimeFormat =
    SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
private val dateFormat = SimpleDateFormat.getDateInstance()
private val shortDateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT)
private val timeFormat = SimpleDateFormat.getTimeInstance()
private val shortTimeFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)

fun Instant.formatDate(singleLine: Boolean = true, short: Boolean = false): String {
    val date = Date.from(this)
    return when {
        singleLine && short -> shortDateTimeFormat.format(date)
        singleLine -> dateTimeFormat.format(date)
        short -> "${shortDateFormat.format(date)}\n${shortTimeFormat.format(date)}"
        else -> "${dateFormat.format(date)}\n${timeFormat.format(date)}"
    }
}
