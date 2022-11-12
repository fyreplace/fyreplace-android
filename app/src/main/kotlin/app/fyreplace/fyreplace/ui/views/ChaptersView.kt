package app.fyreplace.fyreplace.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.updateLayoutParams
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.setLinkifiedText
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.progressindicator.CircularProgressIndicator

@Suppress("unused")
class ChaptersView : LinearLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        orientation = VERTICAL
    }

    suspend fun setPost(post: Post) {
        removeAllViews()

        if (post.isPreview) {
            val loader = CircularProgressIndicator(context).apply { isIndeterminate = true }
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL
            loader.layoutParams = layoutParams
            addView(loader)
        } else for (chapter in post.chaptersList) {
            addChapter(chapter)
        }
    }

    private suspend fun addChapter(chapter: Chapter) {
        val view = if (chapter.hasImage()) makeImageView(chapter)
        else makeTextView(chapter)

        view.updateLayoutParams<MarginLayoutParams> {
            topMargin = resources.getDimensionPixelSize(R.dimen.gap_narrow)
            bottomMargin = topMargin
        }

        addView(view)
    }

    private suspend fun makeTextView(chapter: Chapter): View {
        val text = AppCompatTextView(context)
        val style = if (chapter.isTitle) R.style.TextAppearance_Material3_HeadlineMedium
        else R.style.TextAppearance_Material3_BodyLarge

        text.setTextAppearance(style)
        text.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = resources.getDimensionPixelSize(R.dimen.gap)
            marginEnd = marginStart
        }

        return text.apply { setLinkifiedText(chapter.text) }
    }

    private fun makeImageView(chapter: Chapter): View {
        val image = ChapterImageView(context)
        image.adjustViewBounds = true
        image.setChapterImage(chapter.image)
        Glide.with(context)
            .load(chapter.image.url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(image)

        image.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return image
    }
}
