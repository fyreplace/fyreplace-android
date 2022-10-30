package app.fyreplace.fyreplace.extensions

import com.google.protobuf.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val dateTimeFormat = SimpleDateFormat.getDateTimeInstance()
private val shortDateTimeFormat =
    SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
private val dateFormat = SimpleDateFormat.getDateInstance()
private val shortDateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT)
private val timeFormat = SimpleDateFormat.getTimeInstance()
private val shortTimeFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)

val Timestamp.date get() = Date(seconds * 1000 + nanos / 1000 + 1)

fun Timestamp.formatDate(singleLine: Boolean = true, short: Boolean = false): String {
    return when {
        singleLine && short -> shortDateTimeFormat.format(date)
        singleLine -> dateTimeFormat.format(date)
        short -> "${shortDateFormat.format(date)}\n${shortTimeFormat.format(date)}"
        else -> "${dateFormat.format(date)}\n${timeFormat.format(date)}"
    }
}
