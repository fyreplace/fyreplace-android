package app.fyreplace.fyreplace.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.ContentLoadingProgressBar
import androidx.core.widget.TextViewCompat
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Post
import com.bumptech.glide.Glide

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

    fun setPost(post: Post) {
        removeAllViews()

        if (post.isPreview) {
            val loader = ContentLoadingProgressBar(context)
            loader.isIndeterminate = true
            return addView(loader)
        }

        post.chaptersList.forEach(::addChapter)
    }

    private fun addChapter(chapter: Chapter) {
        val view = if (chapter.hasImage()) makeImageView(chapter)
        else makeTextView(chapter)

        view.updateLayoutParams<MarginLayoutParams> {
            topMargin = resources.getDimensionPixelSize(R.dimen.gap_narrow)
            bottomMargin = topMargin
        }

        addView(view)
    }

    private fun makeTextView(chapter: Chapter): View {
        val text = TextView(context)
        val style = if (chapter.isTitle) R.style.TextAppearance_Material3_HeadlineMedium
        else R.style.TextAppearance_Material3_BodyLarge

        text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        text.text = chapter.text
        text.setTextIsSelectable(true)
        TextViewCompat.setTextAppearance(text, style)
        text.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = resources.getDimensionPixelSize(R.dimen.gap)
            marginEnd = marginStart
        }

        return text
    }

    private fun makeImageView(chapter: Chapter): View {
        val image = ImageView(context)
        Glide.with(context)
            .load(chapter.image.url)
            .into(image)

        image.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        doOnLayout {
            val width = measuredWidth
            val height = width * chapter.image.height / chapter.image.width
            image.updateLayoutParams { this.height = height }
        }

        return image
    }
}
