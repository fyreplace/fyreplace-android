package app.fyreplace.fyreplace.legacy.extensions

fun <T> Collection<T>.mutateAsList(block: MutableList<T>.() -> Unit) = toMutableList().apply(block)
