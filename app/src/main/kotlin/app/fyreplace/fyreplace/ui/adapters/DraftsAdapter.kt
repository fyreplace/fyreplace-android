package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Post
import com.google.protobuf.ByteString

class DraftsAdapter(itemListener: ItemClickListener<Post>) :
    ItemListAdapter<Post, ArchiveAdapter.ChapterHolder>(itemListener) {
    override fun getItemViewType(position: Int): Int {
        val post = items[position]
        return when (post.chaptersCount) {
            0 -> TYPE_EMPTY
            else -> if (post.getChapters(0).text.isEmpty()) TYPE_IMAGE else TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArchiveAdapter.ChapterHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_EMPTY -> EmptyHolder(inflater.inflate(R.layout.item_draft_empty, parent, false))
            TYPE_TEXT -> TextHolder(inflater.inflate(R.layout.item_draft_text, parent, false))
            TYPE_IMAGE -> ImageHolder(inflater.inflate(R.layout.item_draft_image, parent, false))
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: ArchiveAdapter.ChapterHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val post = items[position]

        if (post.chaptersCount > 0) {
            holder.setup(post.getChapters(0))
            (holder as DraftHolder).setup(post)
        }
    }

    override fun getItemId(item: Post): ByteString = item.id

    companion object {
        const val TYPE_EMPTY = 1
        const val TYPE_TEXT = 2
        const val TYPE_IMAGE = 3
    }

    interface DraftHolder {
        val parts: TextView?

        fun setup(post: Post) {
            val parts = parts ?: return
            parts.text = parts.resources.getQuantityString(
                R.plurals.drafts_item_parts,
                post.chapterCount,
                post.chapterCount
            )
        }
    }

    class EmptyHolder(itemView: View) : ArchiveAdapter.ChapterHolder(itemView) {
        override fun setup(chapter: Chapter) = Unit
    }

    class TextHolder(itemView: View) : ArchiveAdapter.TextHolder(itemView), DraftHolder {
        override val parts: TextView = itemView.findViewById(R.id.parts)
    }

    class ImageHolder(itemView: View) : ArchiveAdapter.ImageHolder(itemView), DraftHolder {
        override val parts: TextView = itemView.findViewById(R.id.parts)
    }
}
