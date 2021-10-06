package app.fyreplace.client.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import app.fyreplace.client.R
import app.fyreplace.protos.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class ArchiveAdapter(context: Context) :
    ItemListAdapter<Post, ItemListAdapter.Holder>(context, PostDiffer()) {
    override fun getItemViewType(position: Int) =
        when (getItem(position)?.getChapters(0)?.text?.length) {
            null, 0 -> TYPE_IMAGE
            else -> TYPE_TEXT
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT -> TextHolder(inflater.inflate(R.layout.item_post_text, parent, false))
            TYPE_IMAGE -> ImageHolder(inflater.inflate(R.layout.item_post_image, parent, false))
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val post = getItem(position) ?: return
        val chapter = post.getChapters(0)
        val context = holder.itemView.context
        holder.setup(post.author, post.dateCreated)
        val cornerSize = context.resources.getDimensionPixelSize(R.dimen.corner_size)

        when (holder) {
            is TextHolder -> {
                holder.preview.text = chapter.text
                holder.preview.setLines(if (chapter.isTitle) 1 else textMaxLines)
                TextViewCompat.setTextAppearance(
                    holder.preview,
                    if (chapter.isTitle) textAppearanceTitle else textAppearanceNormal
                )
            }
            is ImageHolder -> Glide.with(context)
                .load(chapter.image.url)
                .transform(CenterCrop(), RoundedCorners(cornerSize))
                .into(holder.preview)
        }
    }

    class TextHolder(itemView: View) : ItemListAdapter.Holder(itemView) {
        val preview: TextView = itemView.findViewById(R.id.text_preview)
    }

    class ImageHolder(itemView: View) : ItemListAdapter.Holder(itemView) {
        val preview: ImageView = itemView.findViewById(R.id.image_preview)
    }
}
