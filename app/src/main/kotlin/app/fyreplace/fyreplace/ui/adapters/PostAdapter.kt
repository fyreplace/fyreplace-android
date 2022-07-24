package app.fyreplace.fyreplace.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.resolveStyleAttribute
import app.fyreplace.fyreplace.extensions.setComment
import app.fyreplace.fyreplace.ui.views.ChaptersView
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Post
import app.fyreplace.protos.Profile
import com.google.protobuf.Timestamp

class PostAdapter(private var post: Post, private val selectedComment: Int?) :
    ItemRandomAccessListAdapter<Comment, ItemHolder>(1) {
    private var commentListener: CommentListener? = null

    override fun getItemCount() = super.getItemCount() + 1

    override fun getItemViewType(position: Int) = when {
        position == 0 -> TYPE_CHAPTERS
        position > totalSize -> TYPE_NEW_COMMENT
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
            TYPE_COMMENT_LOADER -> CommentLoaderHolder(
                inflater.inflate(R.layout.item_comment_loader, parent, false)
            )
            TYPE_NEW_COMMENT -> NewCommentHolder(
                inflater.inflate(R.layout.item_new_comment, parent, false)
            )
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        when (holder) {
            is ChaptersHolder -> holder.setup(post)
            is CommentHolder -> holder.setup(items[position - 1] ?: return)
            is CommentLoaderHolder -> holder.setup()
            is NewCommentHolder -> holder.setup()
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
        const val TYPE_NEW_COMMENT = 4
    }

    interface CommentListener {
        fun onCommentDisplayed(view: View, position: Int, comment: Comment?)

        fun onCommentProfileClicked(view: View, position: Int, profile: Profile)

        fun onCommentOptionsClicked(view: View, position: Int, comment: Comment)

        fun onNewComment()
    }

    private class ChaptersHolder(itemView: View) : ItemHolder(itemView) {
        private val chapters: ChaptersView = itemView.findViewById(R.id.chapters)

        fun setup(post: Post) = chapters.setPost(post)
    }

    private inner class CommentHolder(itemView: View) : ItemHolder(itemView) {
        private val primaryColor =
            itemView.context.theme.resolveStyleAttribute(R.attr.colorPrimary)
        private val textColor =
            itemView.context.theme.resolveStyleAttribute(R.attr.colorOnSurface)
        private val selectedContainerColor =
            itemView.context.theme.resolveStyleAttribute(R.attr.colorPrimaryContainer)
        private val content: TextView = itemView.findViewById(R.id.content)
        private val more: View = itemView.findViewById(R.id.more)
        private val commentPosition get() = bindingAdapterPosition - 1

        override fun setup(profile: Profile, timestamp: Timestamp?) {
            super.setup(profile, timestamp)
            val onProfileClicked = { view: View ->
                commentListener?.onCommentProfileClicked(view, commentPosition, profile) ?: Unit
            }
            avatar?.setOnClickListener(onProfileClicked)
            username?.setOnClickListener(onProfileClicked)
            username?.setTextColor(if (profile.id == post.author.id) primaryColor else textColor)
        }

        fun setup(comment: Comment) {
            setup(comment.author, comment.dateCreated)
            itemView.setBackgroundColor(
                if (commentPosition == selectedComment) selectedContainerColor
                else Color.TRANSPARENT
            )
            content.setComment(comment)
            more.visibility = if (comment.isDeleted) View.INVISIBLE else View.VISIBLE
            more.setOnClickListener {
                commentListener?.onCommentOptionsClicked(it, commentPosition, comment)
            }

            commentListener?.onCommentDisplayed(itemView, commentPosition, comment)
        }
    }

    private inner class CommentLoaderHolder(itemView: View) : ItemHolder(itemView) {
        fun setup() {
            commentListener?.onCommentDisplayed(itemView, bindingAdapterPosition - 1, null)
        }
    }

    private inner class NewCommentHolder(itemView: View) : ItemHolder(itemView) {
        private val button: Button = itemView.findViewById(R.id.button)

        fun setup() = button.setOnClickListener { commentListener?.onNewComment() }
    }
}
