package app.fyreplace.fyreplace.ui.adapters.holders

import android.view.View
import app.fyreplace.fyreplace.extensions.firstChapter
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Post

abstract class PreviewHolder(itemView: View) : ItemHolder(itemView) {
    abstract fun setup(chapter: Chapter)

    open fun setup(post: Post) {
        setup(post.author, post.dateCreated)
        setup(post.firstChapter)
    }
}
