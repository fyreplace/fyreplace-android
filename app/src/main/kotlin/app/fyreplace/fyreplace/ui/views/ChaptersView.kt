package app.fyreplace.fyreplace.ui.views

import android.content.Context
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import app.fyreplace.fyreplace.R
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
        gravity = Gravity.CENTER_HORIZONTAL
    }

    fun setPost(post: Post) {
        removeAllViews()

        if (post.isPreview) {
            addView(CircularProgressIndicator(context).apply { isIndeterminate = true })
        } else for (chapter in post.chaptersList) {
            addChapter(chapter)
        }
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
        LinkifyCompat.addLinks(text, Linkify.ALL)
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
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(image)

        image.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return image
    }
}
