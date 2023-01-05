package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.ItemFeedPostImageBinding
import app.fyreplace.fyreplace.databinding.ItemFeedPostTextBinding
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.RemoteNotificationWasReceivedEvent
import app.fyreplace.fyreplace.extensions.firstChapter
import app.fyreplace.fyreplace.ui.adapters.holders.PreviewHolder
import app.fyreplace.protos.Post
import com.google.protobuf.ByteString
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

class FeedAdapter(
    private val em: EventsManager,
    private val lifecycleOwner: LifecycleOwner,
    private val canVote: StateFlow<Boolean>,
    private val voteListener: VoteListener,
    itemListener: ItemClickListener<Post>
) :
    ItemListAdapter<Post, PreviewHolder>(itemListener) {
    override fun getItemViewType(position: Int) =
        if (items[position].firstChapter.hasImage()) TYPE_IMAGE else TYPE_TEXT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT -> TextPostHolder(
                inflater.inflate(R.layout.item_feed_post_text, parent, false)
            )
            TYPE_IMAGE -> ImagePostHolder(
                inflater.inflate(R.layout.item_feed_post_image, parent, false)
            )
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.setup(items[position])
    }

    override fun getItemId(item: Post): ByteString = item.id

    fun addOrUpdate(item: Post) {
        val position = items.indexOfFirst { it.id == item.id }

        if (position == -1) {
            add(itemCount, item)
        } else if (items[position] != item) {
            update(position, item)
        }
    }

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
    }

    interface VoteListener {
        fun onPostVoted(view: View, position: Int, spread: Boolean)
    }

    abstract inner class PostHolder(itemView: View) : PreviewHolder(itemView) {
        private val down = itemView.findViewById<ImageButton>(R.id.down)
        private val up = itemView.findViewById<ImageButton>(R.id.up)
        private val votes = itemView.findViewById<TextView>(R.id.votes)
        private val comments = itemView.findViewById<TextView>(R.id.comments)
        private val isVoting get() = down.isActivated || up.isActivated
        private val scope = MainScope()

        override fun setup(post: Post) {
            super.setup(post)
            votes.text = post.voteCount.toString()
            comments.text = post.commentCount.toString()
            scope.launch {
                em.events.filterIsInstance<RemoteNotificationWasReceivedEvent>()
                    .filter { it.command == "comment:creation" && it.postId == post.id }
                    .collect {
                        val newCount = comments.text.toString().toInt() + 1
                        comments.text = newCount.toString()
                    }
            }
        }

        fun onDownClicked(view: View) = vote(view)

        fun onUpClicked(view: View) = vote(view)

        private fun vote(button: View) {
            if (!isVoting) scope.launch {
                button.isActivated = true
                delay(500)
                voteListener.onPostVoted(button, bindingAdapterPosition, button == up)
                button.isActivated = false
            }
        }
    }

    inner class TextPostHolder(itemView: View) : PostHolder(itemView) {
        private val bd = ItemFeedPostTextBinding.bind(itemView)

        init {
            bd.lifecycleOwner = lifecycleOwner
            bd.holder = this
            bd.canVote = canVote
        }
    }

    inner class ImagePostHolder(itemView: View) : PostHolder(itemView) {
        private val bd = ItemFeedPostImageBinding.bind(itemView)

        init {
            bd.lifecycleOwner = lifecycleOwner
            bd.holder = this
            bd.canVote = canVote
        }
    }
}
