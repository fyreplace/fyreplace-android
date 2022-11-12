package app.fyreplace.fyreplace.ui.adapters.holders

import android.view.View
import android.widget.TextView
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Comment

open class TextPreviewHolder(itemView: View) : PreviewHolder(itemView) {
    private val preview: TextView = itemView.findViewById(R.id.text_preview)

    override fun setup(chapter: Chapter) {
        preview.text = chapter.text
        preview.setLines(if (chapter.isTitle) 1 else textMaxLines)
        preview.setTextAppearance(if (chapter.isTitle) textAppearanceTitle else textAppearanceNormal)
    }

    fun setup(comment: Comment) {
        preview.text = comment.text
    }
}
