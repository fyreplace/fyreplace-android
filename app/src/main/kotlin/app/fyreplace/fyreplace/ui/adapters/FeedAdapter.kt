package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.LifecycleOwner
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.ItemFeedPostImageBinding
import app.fyreplace.fyreplace.databinding.ItemFeedPostTextBinding
import app.fyreplace.fyreplace.extensions.firstChapter
import app.fyreplace.fyreplace.ui.adapters.holders.PreviewHolder
import app.fyreplace.protos.Post
import com.google.protobuf.ByteString
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FeedAdapter(
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

    override fun add(position: Int, item: Post) {
        if (item.id !in items.map { it.id }) {
            super.add(position, item)
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
        private val isVoting get() = down.isActivated || up.isActivated
        private val scope = MainScope()

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
