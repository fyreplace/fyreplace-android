package app.fyreplace.fyreplace.extensions

import app.fyreplace.protos.Post

fun Post.makePreview(anonymous: Boolean = false): Post = Post.newBuilder(this)
    .clearChapters()
    .apply {
        this@makePreview.chaptersList.getOrNull(0)?.let { addChapters(it) }

        if (anonymous) {
            clearAuthor()
        }
    }
    .setIsPreview(true)
    .build()
