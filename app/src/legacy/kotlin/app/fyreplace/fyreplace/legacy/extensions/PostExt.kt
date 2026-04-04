package app.fyreplace.fyreplace.legacy.extensions

import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Post

val Post.firstChapter
    get() = chapters.firstOrNull() ?: Chapter()

fun Post.makePreview(anonymous: Boolean = false) = copy(
    author = if (anonymous) null else author,
    is_preview = true,
    chapters = listOfNotNull(chapters.firstOrNull())
)
