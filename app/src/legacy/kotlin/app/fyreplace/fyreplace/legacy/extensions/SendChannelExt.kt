package app.fyreplace.fyreplace.legacy.extensions

import kotlinx.coroutines.channels.SendChannel

suspend fun <E> SendChannel<E>.sendAllAndClose(elements: Iterable<E>) {
    for (element in elements) {
        send(element)
    }

    close()
}
