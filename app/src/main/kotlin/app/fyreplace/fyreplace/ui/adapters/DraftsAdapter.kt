package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.firstChapter
import app.fyreplace.fyreplace.ui.adapters.holders.PreviewHolder
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.Post
import com.google.protobuf.ByteString

class DraftsAdapter(itemListener: ItemClickListener<Post>) :
    ItemListAdapter<Post, PreviewHolder>(itemListener) {
    override fun getItemViewType(position: Int): Int {
        val post = items[position]
        return when (post.chaptersCount) {
            0 -> TYPE_EMPTY
            else -> if (post.firstChapter.hasImage()) TYPE_IMAGE else TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_EMPTY -> EmptyHolder(
                inflater.inflate(R.layout.item_draft_empty, parent, false)
            )
            TYPE_TEXT -> DraftHolder(
                inflater.inflate(R.layout.item_draft_text, parent, false)
            )
            TYPE_IMAGE -> DraftHolder(
                inflater.inflate(R.layout.item_draft_image, parent, false)
            )
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        (holder as DraftHolder).setup(items[position])
    }

    override fun getItemId(item: Post): ByteString = item.id

    companion object {
        const val TYPE_EMPTY = 1
        const val TYPE_TEXT = 2
        const val TYPE_IMAGE = 3
    }

    open class DraftHolder(itemView: View) : PreviewHolder(itemView) {
        private val parts: TextView = itemView.findViewById(R.id.parts)

        override fun setup(post: Post) {
            super.setup(post)

            parts.text = parts.resources?.getQuantityString(
                R.plurals.drafts_item_parts,
                post.chapterCount,
                post.chapterCount
            )
        }
    }

    class EmptyHolder(itemView: View) : DraftHolder(itemView) {
        override fun setup(chapter: Chapter) = Unit
    }
}
