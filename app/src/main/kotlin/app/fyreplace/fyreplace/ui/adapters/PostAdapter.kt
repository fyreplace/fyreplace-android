package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.ContentLoadingProgressBar
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Post
import com.bumptech.glide.Glide

class PostAdapter(private var post: Post) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    override fun getItemCount() = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_post_chapters, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        holder.container.removeAllViews()

        if (post.isPreview) {
            val loader = ContentLoadingProgressBar(context)
            loader.isIndeterminate = true
            return holder.container.addView(loader)
        }

        post.chaptersList.forEach(holder::addChapter)
    }

    fun updatePost(post: Post) {
        this.post = post
        notifyItemChanged(0)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.container)

        fun addChapter(chapter: Chapter) {
            val view = if (chapter.hasImage()) makeImageView(chapter)
            else makeTextView(chapter)

            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = itemView.resources.getDimensionPixelSize(R.dimen.gap_narrow)
                bottomMargin = topMargin
            }

            container.addView(view)
        }

        private fun makeTextView(chapter: Chapter): View {
            val text = TextView(itemView.context)
            val style = if (chapter.isTitle) R.style.TextAppearance_AppCompat_Title
            else R.style.TextAppearance_AppCompat_Body1

            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            text.text = chapter.text
            text.setTextIsSelectable(true)
            TextViewCompat.setTextAppearance(text, style)
            text.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = itemView.resources.getDimensionPixelSize(R.dimen.gap)
                marginEnd = marginStart
            }

            return text
        }

        private fun makeImageView(chapter: Chapter): View {
            val image = ImageView(itemView.context)
            Glide.with(itemView.context)
                .load(chapter.image.url)
                .into(image)

            image.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            container.doOnLayout {
                val width = container.measuredWidth
                val height = width * chapter.image.height / chapter.image.width
                image.updateLayoutParams { this.height = height }
            }

            return image
        }
    }
}
