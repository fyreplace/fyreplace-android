package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.resolveStyleAttribute
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.protobuf.ByteString

class ArchiveAdapter(itemListener: ItemClickListener<Post>) :
    ItemListAdapter<Post, ArchiveAdapter.ChapterHolder>(itemListener) {
    override fun getItemViewType(position: Int) =
        if (items[position].getChapters(0).text.isEmpty()) TYPE_IMAGE
        else TYPE_TEXT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT -> TextHolder(inflater.inflate(R.layout.item_post_text, parent, false))
            TYPE_IMAGE -> ImageHolder(inflater.inflate(R.layout.item_post_image, parent, false))
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: ChapterHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val post = items[position]
        val chapter = post.getChapters(0)
        holder.setup(post.author, post.dateCreated)
        holder.setup(chapter)
    }

    override fun getItemId(item: Post): ByteString = item.id

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
    }

    abstract class ChapterHolder(itemView: View) : ItemHolder(itemView) {
        protected val textAppearanceNormal =
            itemView.context.theme.resolveStyleAttribute(R.attr.textAppearanceBodyLarge)
        protected val textAppearanceTitle =
            itemView.context.theme.resolveStyleAttribute(R.attr.textAppearanceHeadlineSmall)
        protected val textMaxLines =
            itemView.context.resources.getInteger(R.integer.item_list_text_max_lines)

        abstract fun setup(chapter: Chapter)
    }

    open class TextHolder(itemView: View) : ChapterHolder(itemView) {
        private val preview: TextView = itemView.findViewById(R.id.text_preview)

        override fun setup(chapter: Chapter) {
            preview.text = chapter.text
            preview.setLines(if (chapter.isTitle) 1 else textMaxLines)
            TextViewCompat.setTextAppearance(
                preview,
                if (chapter.isTitle) textAppearanceTitle else textAppearanceNormal
            )
        }
    }

    open class ImageHolder(itemView: View) : ChapterHolder(itemView) {
        private val preview: ImageView = itemView.findViewById(R.id.image_preview)
        private val cornerSize =
            itemView.context.resources.getDimensionPixelSize(R.dimen.image_corner_radius)

        override fun setup(chapter: Chapter) {
            Glide.with(itemView.context)
                .load(chapter.image.url)
                .transform(CenterCrop(), RoundedCorners(cornerSize))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(preview)
        }
    }
}
