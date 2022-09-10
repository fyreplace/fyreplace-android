package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.ItemChapterButtonsBinding
import app.fyreplace.fyreplace.extensions.translucent
import app.fyreplace.fyreplace.ui.adapters.holders.PreviewHolder
import app.fyreplace.fyreplace.ui.adapters.holders.TextPreviewHolder
import app.fyreplace.protos.Chapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.StateFlow

class DraftAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val canAddChapter: StateFlow<Boolean>,
    private val chapterListener: ChapterListener,
    itemListener: ItemClickListener<Chapter>
) :
    ItemListAdapter<Chapter, PreviewHolder>(itemListener) {
    init {
        setHasStableIds(false)
    }

    override fun getItemCount() = super.getItemCount() + 1

    override fun getItemViewType(position: Int) = when {
        position >= items.size -> TYPE_BUTTONS
        items[position].hasImage() -> TYPE_IMAGE
        else -> TYPE_TEXT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT -> DraftTextChapterHolder(
                inflater.inflate(R.layout.item_chapter_text, parent, false)
            )
            TYPE_IMAGE -> ImageChapterHolder(
                inflater.inflate(R.layout.item_chapter_image, parent, false)
            )
            TYPE_BUTTONS -> ButtonsChapterHolder(
                inflater.inflate(R.layout.item_chapter_buttons, parent, false)
            )
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.setup(if (position < items.size) items[position] else Chapter.getDefaultInstance())
    }

    override fun getItemId(item: Chapter): ByteString = ByteString.EMPTY

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
        const val TYPE_BUTTONS = 3
    }

    class DraftTextChapterHolder(itemView: View) : TextPreviewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text)

        override fun setup(chapter: Chapter) {
            super.setup(chapter)
            val color = ContextCompat.getColor(itemView.context, R.color.md_theme_onSurface)
            text.text = chapter.text.ifEmpty { itemView.context.getString(R.string.draft_empty) }
            text.setTextColor(if (chapter.text.isEmpty()) color.translucent() else color)
        }
    }

    class ImageChapterHolder(itemView: View) : PreviewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.image)

        override fun setup(chapter: Chapter) {
            Glide.with(itemView.context)
                .load(chapter.image.url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(image)
        }
    }

    inner class ButtonsChapterHolder(itemView: View) : PreviewHolder(itemView) {
        private val bd = ItemChapterButtonsBinding.bind(itemView)

        init {
            bd.lifecycleOwner = lifecycleOwner
            bd.holder = this
            bd.canAddChapter = canAddChapter
        }

        override fun setup(chapter: Chapter) = with(itemView) {
            setOnClickListener(null)
            setOnLongClickListener(null)
        }

        @Suppress("UNUSED_PARAMETER")
        fun onTextClicked(view: View) = chapterListener.onInsertChapter(items.size, TYPE_TEXT)

        @Suppress("UNUSED_PARAMETER")
        fun onImageClicked(view: View) = chapterListener.onInsertChapter(items.size, TYPE_IMAGE)
    }

    interface ChapterListener {
        fun onInsertChapter(position: Int, type: Int)
    }
}
