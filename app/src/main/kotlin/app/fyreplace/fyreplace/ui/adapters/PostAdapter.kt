package app.fyreplace.fyreplace.ui.adapters

import android.graphics.Color
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.util.LinkifyCompat
import androidx.lifecycle.LifecycleOwner
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.ItemCommentBinding
import app.fyreplace.fyreplace.databinding.ItemNewCommentBinding
import app.fyreplace.fyreplace.extensions.resolveStyleAttribute
import app.fyreplace.fyreplace.extensions.setComment
import app.fyreplace.fyreplace.ui.views.ChaptersView
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Post
import app.fyreplace.protos.Profile
import com.google.protobuf.Timestamp

class PostAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private var post: Post,
    private val commentListener: CommentListener
) :
    ItemRandomAccessListAdapter<Comment, ItemHolder>(1) {
    private var selectedComment: Int? = null

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
        }
    }

    override fun onViewAttachedToWindow(holder: ItemHolder) {
        super.onViewAttachedToWindow(holder)

        if (holder is CommentHolder) {
            holder.fixTextView()
        }
    }

    fun updatePost(post: Post) {
        this.post = post
        notifyItemChanged(0)
    }

    fun updateSelectedComment(position: Int) {
        selectedComment = position
        notifyItemChanged(position + 1)
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

    class ChaptersHolder(itemView: View) : ItemHolder(itemView), View.OnLayoutChangeListener {
        private val chapters: ChaptersView = itemView.findViewById(R.id.chapters)

        init {
            itemView.addOnLayoutChangeListener(this)
        }

        override fun onLayoutChange(
            v: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            itemView.minimumHeight = v.height
        }

        fun setup(post: Post) = chapters.setPost(post)
    }

    inner class CommentHolder(itemView: View) : ItemHolder(itemView) {
        override val shortDate = true
        private val bd = ItemCommentBinding.bind(itemView)
        private val primaryColor =
            itemView.context.theme.resolveStyleAttribute(R.attr.colorPrimary)
        private val textColor =
            itemView.context.theme.resolveStyleAttribute(R.attr.colorOnSurface)
        private val selectedContainerColor =
            itemView.context.theme.resolveStyleAttribute(R.attr.colorPrimaryContainer)
        private val commentPosition get() = bindingAdapterPosition - 1
        private lateinit var comment: Comment

        init {
            bd.lifecycleOwner = lifecycleOwner
            bd.holder = this
        }

        override fun setup(profile: Profile, timestamp: Timestamp?) {
            super.setup(profile, timestamp)
            bd.username.setTextColor(if (profile.id == post.author.id) primaryColor else textColor)
        }

        fun setup(comment: Comment) {
            setup(comment.author, comment.dateCreated)
            this.comment = comment
            itemView.setBackgroundColor(
                if (commentPosition == selectedComment) selectedContainerColor
                else Color.TRANSPARENT
            )
            bd.content.setComment(comment)
            bd.more.visibility = if (comment.isDeleted) View.INVISIBLE else View.VISIBLE
            commentListener.onCommentDisplayed(itemView, commentPosition, comment)
        }

        fun fixTextView() {
            bd.content.isEnabled = false
            bd.content.isEnabled = true
            bd.content.setTextIsSelectable(!comment.isDeleted)
            LinkifyCompat.addLinks(bd.content, Linkify.ALL)
        }

        fun onProfileClicked(view: View) =
            commentListener.onCommentProfileClicked(view, commentPosition, comment.author)

        fun onMoreClicked(view: View) =
            commentListener.onCommentOptionsClicked(view, commentPosition, comment)
    }

    inner class CommentLoaderHolder(itemView: View) : ItemHolder(itemView) {
        fun setup() =
            commentListener.onCommentDisplayed(itemView, bindingAdapterPosition - 1, null)
    }

    inner class NewCommentHolder(itemView: View) : ItemHolder(itemView) {
        private val bd = ItemNewCommentBinding.bind(itemView)

        init {
            bd.lifecycleOwner = lifecycleOwner
            bd.holder = this
        }

        @Suppress("UNUSED_PARAMETER")
        fun onButtonClicked(view: View) = commentListener.onNewComment()
    }
}
