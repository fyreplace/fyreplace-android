package app.fyreplace.fyreplace.ui.adapters.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.firstChapter
import app.fyreplace.fyreplace.extensions.resolveStyleAttribute
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

open class PreviewHolder(itemView: View) : ItemHolder(itemView) {
    protected val textAppearanceNormal =
        itemView.context.theme.resolveStyleAttribute(com.google.android.material.R.attr.textAppearanceBodyLarge)
    protected val textAppearanceTitle =
        itemView.context.theme.resolveStyleAttribute(com.google.android.material.R.attr.textAppearanceHeadlineSmall)
    protected val textMaxLines =
        itemView.context.resources.getInteger(R.integer.item_list_text_max_lines)
    protected val cornerSize =
        itemView.context.resources.getDimensionPixelSize(R.dimen.image_corner_radius)
    private val textPreview: TextView? = itemView.findViewById(R.id.text_preview)
    private val imagePreview: ImageView? = itemView.findViewById(R.id.image_preview)

    open fun setup(post: Post) {
        setup(post.author, post.dateCreated)
        setup(post.firstChapter)
    }

    open fun setup(chapter: Chapter) {
        if (textPreview != null) {
            textPreview.text = chapter.text
            textPreview.setLines(if (chapter.isTitle) 1 else textMaxLines)
            textPreview.setTextAppearance(if (chapter.isTitle) textAppearanceTitle else textAppearanceNormal)
        }

        if (imagePreview != null) {
            Glide.with(itemView.context)
                .load(chapter.image.url)
                .transform(CenterCrop(), RoundedCorners(cornerSize))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imagePreview)
        }
    }

    open fun setup(comment: Comment) {
        textPreview?.text = comment.text
    }
}
