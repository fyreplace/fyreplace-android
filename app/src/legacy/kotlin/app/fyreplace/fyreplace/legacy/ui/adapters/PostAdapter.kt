package app.fyreplace.fyreplace.legacy.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.ItemCommentBinding
import app.fyreplace.fyreplace.legacy.extensions.resolveStyleAttribute
import app.fyreplace.fyreplace.legacy.extensions.setComment
import app.fyreplace.fyreplace.legacy.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.legacy.ui.views.ChaptersView
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Post
import app.fyreplace.protos.Profile
import com.squareup.wire.Instant
import kotlinx.coroutines.flow.StateFlow

class PostAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val isAuthenticated: StateFlow<Boolean>,
    private var post: Post,
    private val commentListener: CommentListener
) :
    ItemRandomAccessListAdapter<Comment, ItemHolder>(1) {
    private var selectedComment: Int? = null

    override fun getItemCount() = super.getItemCount()

    override fun getItemViewType(position: Int) = when {
        position == 0 -> TYPE_CHAPTERS
        items.containsKey(position - offset) -> TYPE_COMMENT
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

            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        when (holder) {
            is ChaptersHolder -> holder.setup(post)
            is CommentHolder -> holder.setup(items[position - offset] ?: return)
            is CommentLoaderHolder -> holder.setup()
            else -> Unit
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

    fun updateSelectedComment(position: Int?) {
        selectedComment?.inc()?.let(::notifyItemChanged)
        selectedComment = position
        position?.inc()?.let(::notifyItemChanged)
    }

    companion object {
        const val TYPE_CHAPTERS = 1
        const val TYPE_COMMENT = 2
        const val TYPE_COMMENT_LOADER = 3
    }

    interface CommentListener {
        fun onCommentDisplayed(view: View, position: Int, comment: Comment?, highlighted: Boolean)

        fun onCommentProfileClicked(view: View, position: Int, profile: Profile)

        fun onCommentOptionsClicked(view: View, position: Int, comment: Comment)

        fun onNewCommentClicked()
    }

    inner class ChaptersHolder(itemView: View) : ItemHolder(itemView), View.OnLayoutChangeListener {
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
            itemView.context.theme.resolveStyleAttribute(androidx.appcompat.R.attr.colorPrimary)
        private val textColor =
            itemView.context.theme.resolveStyleAttribute(com.google.android.material.R.attr.colorOnSurface)
        private val selectedContainerColor =
            itemView.context.theme.resolveStyleAttribute(com.google.android.material.R.attr.colorPrimaryContainer)
        private val commentPosition get() = bindingAdapterPosition - offset
        private lateinit var comment: Comment

        init {
            bd.lifecycleOwner = lifecycleOwner
            bd.holder = this
            bd.isAuthenticated = isAuthenticated
        }

        override fun setup(profile: Profile?, timestamp: Instant?) {
            super.setup(profile, timestamp)
            bd.username.setTextColor(if (profile?.id == post.author?.id) primaryColor else textColor)
        }

        fun setup(comment: Comment) {
            setup(comment.author, comment.date_created)
            this.comment = comment
            itemView.setBackgroundColor(
                if (commentPosition == selectedComment) selectedContainerColor
                else Color.TRANSPARENT
            )
            val highlighted = post.is_subscribed && commentPosition >= post.comments_read
            bd.content.setComment(comment, highlighted)
            bd.highlight.isVisible = highlighted
            bd.more.visibility = if (comment.is_deleted) View.INVISIBLE else View.VISIBLE
            commentListener.onCommentDisplayed(itemView, commentPosition, comment, highlighted)
        }

        fun fixTextView() {
            bd.content.isEnabled = false
            bd.content.isEnabled = true
        }

        fun onProfileClicked(view: View) {
            val author = comment.author ?: return

            if (!author.is_deleted) {
                commentListener.onCommentProfileClicked(view, commentPosition, author)
            }
        }

        fun onMoreClicked(view: View) {
            if (comment.author?.is_deleted == false) {
                commentListener.onCommentOptionsClicked(view, commentPosition, comment)
            }
        }
    }

    inner class CommentLoaderHolder(itemView: View) : ItemHolder(itemView) {
        fun setup() =
            commentListener.onCommentDisplayed(
                itemView,
                bindingAdapterPosition - offset,
                null,
                false
            )
    }
}
