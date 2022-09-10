package app.fyreplace.fyreplace.ui.adapters.holders

import android.view.View
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.resolveStyleAttribute
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Comment

open class TextPreviewHolder(itemView: View) : PreviewHolder(itemView) {
    private val textAppearanceNormal =
        itemView.context.theme.resolveStyleAttribute(R.attr.textAppearanceBodyLarge)
    private val textAppearanceTitle =
        itemView.context.theme.resolveStyleAttribute(R.attr.textAppearanceHeadlineSmall)
    private val textMaxLines =
        itemView.context.resources.getInteger(R.integer.item_list_text_max_lines)
    private val preview: TextView = itemView.findViewById(R.id.text_preview)

    override fun setup(chapter: Chapter) {
        preview.text = chapter.text
        preview.setLines(if (chapter.isTitle) 1 else textMaxLines)
        TextViewCompat.setTextAppearance(
            preview,
            if (chapter.isTitle) textAppearanceTitle else textAppearanceNormal
        )
    }

    fun setup(comment: Comment) {
        preview.text = comment.text
    }
}
