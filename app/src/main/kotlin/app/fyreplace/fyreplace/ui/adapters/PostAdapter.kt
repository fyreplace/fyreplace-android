package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.views.ChaptersView
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Post
import app.fyreplace.protos.Profile
import com.google.protobuf.Timestamp

class PostAdapter(private var post: Post) : ItemRandomAccessListAdapter<Comment, ItemHolder>(1) {
    private var commentListener: CommentListener? = null

    override fun getItemViewType(position: Int) = when {
        position == 0 -> TYPE_CHAPTERS
        items.containsKey(position - 1) -> TYPE_COMMENT
        else -> TYPE_COMMENT_LOADER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_CHAPTERS -> ChaptersHolder(
                inflater.inflate(R.layout.item_chapters, parent, false)
            )
            TYPE_COMMENT -> CommentHolder(
                inflater.inflate(R.layout.item_comment, parent, false)
            )
            TYPE_COMMENT_LOADER -> ItemHolder(
                inflater.inflate(R.layout.item_comment_loader, parent, false)
            )
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        when (holder) {
            is ChaptersHolder -> holder.chapters.setPost(post)
            is CommentHolder -> {
                val comment = items[position - 1] ?: return
                holder.setup(comment.author, comment.dateCreated)
                holder.content.text = comment.text
            }
        }
    }

    fun setCommentListener(listener: CommentListener) {
        commentListener = listener
    }

    fun updatePost(post: Post) {
        this.post = post
        notifyItemChanged(0)
    }

    companion object {
        const val TYPE_CHAPTERS = 1
        const val TYPE_COMMENT = 2
        const val TYPE_COMMENT_LOADER = 3
    }

    interface CommentListener {
        fun onProfileClicked(profile: Profile)
    }

    private class ChaptersHolder(itemView: View) : ItemHolder(itemView) {
        val chapters: ChaptersView = itemView.findViewById(R.id.chapters)
    }

    private inner class CommentHolder(itemView: View) : ItemHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.content)

        override fun setup(profile: Profile, timestamp: Timestamp?) {
            super.setup(profile, timestamp)
            avatar?.setOnClickListener { commentListener?.onProfileClicked(profile) }
            username?.setOnClickListener { commentListener?.onProfileClicked(profile) }
            username?.setTextColor(
                ResourcesCompat.getColor(
                    itemView.resources,
                    if (profile.id == post.author.id) R.color.seed else R.color.md_theme_onBackground,
                    itemView.context.theme
                )
            )
        }
    }
}
