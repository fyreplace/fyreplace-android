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
import app.fyreplace.protos.Chapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.StateFlow

class DraftAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val canAddChapter: StateFlow<Boolean>
) :
    ItemListAdapter<Chapter, DraftAdapter.ChapterHolder>() {
    private var chapterListener: ChapterListener? = null

    init {
        setHasStableIds(false)
    }

    override fun getItemCount() = super.getItemCount() + 1

    override fun getItemViewType(position: Int) = when {
        position >= items.size -> TYPE_BUTTONS
        items[position].hasImage() -> TYPE_IMAGE
        else -> TYPE_TEXT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT -> TextChapterHolder(
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

    override fun onBindViewHolder(holder: ChapterHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (position >= items.size) {
            holder.itemView.setOnClickListener(null)
            holder.itemView.setOnLongClickListener(null)
            holder.setup(Chapter.getDefaultInstance())
        } else {
            holder.setup(items[position])
        }
    }

    override fun getItemId(item: Chapter): ByteString = ByteString.EMPTY

    fun setChapterListener(listener: ChapterListener) {
        chapterListener = listener
    }

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
        const val TYPE_BUTTONS = 3
    }

    abstract class ChapterHolder(itemView: View) : ItemHolder(itemView) {
        abstract fun setup(chapter: Chapter)
    }

    class TextChapterHolder(itemView: View) : ChapterHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text)

        override fun setup(chapter: Chapter) {
            val color = ContextCompat.getColor(itemView.context, R.color.md_theme_onSurface)
            text.text = chapter.text.ifEmpty { itemView.context.getString(R.string.draft_empty) }
            text.setTextColor(if (chapter.text.isEmpty()) color.translucent() else color)
        }
    }

    class ImageChapterHolder(itemView: View) : ChapterHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)

        override fun setup(chapter: Chapter) {
            Glide.with(itemView.context)
                .load(chapter.image.url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(image)
        }
    }

    inner class ButtonsChapterHolder(itemView: View) : ChapterHolder(itemView) {
        private val bd = ItemChapterButtonsBinding.bind(itemView)

        init {
            bd.lifecycleOwner = lifecycleOwner
            bd.canAddChapter = canAddChapter
        }

        override fun setup(chapter: Chapter) {
            bd.text.setOnClickListener { chapterListener?.onInsertChapter(items.size, TYPE_TEXT) }
            bd.image.setOnClickListener { chapterListener?.onInsertChapter(items.size, TYPE_IMAGE) }
        }
    }

    interface ChapterListener {
        fun onInsertChapter(position: Int, type: Int)
    }
}
