package app.fyreplace.fyreplace.ui.adapters.holders

import android.view.View
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.firstChapter
import app.fyreplace.fyreplace.extensions.resolveStyleAttribute
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Post

abstract class PreviewHolder(itemView: View) : ItemHolder(itemView) {
    protected val textAppearanceNormal =
        itemView.context.theme.resolveStyleAttribute(R.attr.textAppearanceBodyLarge)
    protected val textAppearanceTitle =
        itemView.context.theme.resolveStyleAttribute(R.attr.textAppearanceHeadlineSmall)
    protected val textMaxLines =
        itemView.context.resources.getInteger(R.integer.item_list_text_max_lines)

    abstract fun setup(chapter: Chapter)

    open fun setup(post: Post) {
        setup(post.author, post.dateCreated)
        setup(post.firstChapter)
    }
}
