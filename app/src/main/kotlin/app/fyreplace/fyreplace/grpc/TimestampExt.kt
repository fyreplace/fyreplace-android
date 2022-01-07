package app.fyreplace.fyreplace.grpc

import com.google.protobuf.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val dateTimeFormat: DateFormat = SimpleDateFormat.getDateTimeInstance()
private val dateFormat: DateFormat = SimpleDateFormat.getDateInstance()
private val timeFormat: DateFormat = SimpleDateFormat.getTimeInstance()

fun Timestamp.formatDate(singleLine: Boolean = true): String {
    val date = Date(seconds * 1000)
    return if (singleLine) dateTimeFormat.format(date)
    else "${dateFormat.format(date)}\n${timeFormat.format(date)}"
}
