package app.fyreplace.fyreplace.extensions

import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Post

val Post.firstChapter: Chapter
    get() = if (chaptersCount > 0) getChapters(0) else Chapter.getDefaultInstance()

fun Post.makePreview(anonymous: Boolean = false): Post = Post.newBuilder(this)
    .clearChapters()
    .apply {
        this@makePreview.chaptersList.getOrNull(0)?.let(::addChapters)

        if (anonymous) {
            clearAuthor()
        }
    }
    .setIsPreview(true)
    .build()
