package app.fyreplace.fyreplace.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.ItemCommentBinding
import app.fyreplace.fyreplace.databinding.ItemNewCommentBinding
import app.fyreplace.fyreplace.extensions.resolveStyleAttribute
import app.fyreplace.fyreplace.extensions.setComment
import app.fyreplace.fyreplace.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.ui.views.ChaptersView
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Post
import app.fyreplace.protos.Profile
import com.google.protobuf.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val isAuthenticated: StateFlow<Boolean>,
    private var post: Post,
    private val commentListener: CommentListener
) :
    ItemRandomAccessListAdapter<Comment, ItemHolder>(1) {
    private var selectedComment: Int? = null

    override fun getItemCount() = super.getItemCount() + offset

    override fun getItemViewType(position: Int) = when {
        position == 0 -> TYPE_CHAPTERS
        position > totalSize -> TYPE_NEW_COMMENT
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
            TYPE_NEW_COMMENT -> NewCommentHolder(
                inflater.inflate(R.layout.item_new_comment, parent, false)
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
            lifecycleOwner.lifecycleScope.launch { holder.fixTextView() }
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
        const val TYPE_NEW_COMMENT = 4
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

        fun setup(post: Post) {
            lifecycleOwner.lifecycleScope.launch { chapters.setPost(post) }
        }
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
        private val commentPosition get() = bindingAdapterPosition - offset
        private lateinit var comment: Comment
        private var commentJob: Job? = null

        init {
            bd.lifecycleOwner = lifecycleOwner
            bd.holder = this
            bd.isAuthenticated = isAuthenticated
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
            val highlighted = post.isSubscribed && commentPosition >= post.commentsRead
            commentJob = lifecycleOwner.lifecycleScope.launch {
                bd.content.setComment(comment, highlighted)
            }
            bd.highlight.isVisible = highlighted
            bd.more.visibility = if (comment.isDeleted) View.INVISIBLE else View.VISIBLE
            commentListener.onCommentDisplayed(itemView, commentPosition, comment, highlighted)
        }

        suspend fun fixTextView() {
            commentJob?.join()
            bd.content.isEnabled = false
            bd.content.isEnabled = true
        }

        fun onProfileClicked(view: View) {
            if (!comment.author.isDeleted) {
                commentListener.onCommentProfileClicked(view, commentPosition, comment.author)
            }
        }

        fun onMoreClicked(view: View) {
            if (!comment.author.isDeleted) {
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

    inner class NewCommentHolder(itemView: View) : ItemHolder(itemView) {
        private val bd = ItemNewCommentBinding.bind(itemView)

        init {
            bd.lifecycleOwner = lifecycleOwner
            bd.holder = this
            bd.isAuthenticated = isAuthenticated
        }

        @Suppress("UNUSED_PARAMETER")
        fun onButtonClicked(view: View) = commentListener.onNewCommentClicked()
    }
}
