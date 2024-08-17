package app.fyreplace.fyreplace.extensions

import androidx.datastore.core.DataStore
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.GeneratedMessageLite.Builder

suspend fun <T : GeneratedMessageLite<T, B>, B : Builder<T, B>> DataStore<T>.update(block: suspend B.() -> Unit) {
    updateData { it.toBuilder().apply { block() }.build() }
}
