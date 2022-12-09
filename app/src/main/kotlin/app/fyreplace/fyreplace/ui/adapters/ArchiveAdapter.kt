package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.firstChapter
import app.fyreplace.fyreplace.ui.adapters.holders.PreviewHolder
import app.fyreplace.protos.Post
import com.google.protobuf.ByteString

class ArchiveAdapter(itemListener: ItemClickListener<Post>) :
    ItemListAdapter<Post, PreviewHolder>(itemListener) {
    override fun getItemViewType(position: Int) =
        if (items[position].firstChapter.text.isEmpty()) TYPE_IMAGE
        else TYPE_TEXT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT -> PreviewHolder(
                inflater.inflate(R.layout.item_post_text, parent, false)
            )
            TYPE_IMAGE -> PreviewHolder(
                inflater.inflate(R.layout.item_post_image, parent, false)
            )
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.setup(items[position])
    }

    override fun getItemId(item: Post): ByteString = item.id

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
    }
}
