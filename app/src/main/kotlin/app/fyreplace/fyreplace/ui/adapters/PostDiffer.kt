package app.fyreplace.fyreplace.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import app.fyreplace.protos.Post

class PostDiffer : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        val oldChapter = oldItem.getChapters(0)
        val newChapter = newItem.getChapters(0)
        return oldChapter.text == newChapter.text &&
                oldChapter.isTitle == newChapter.isTitle &&
                oldChapter.image == newChapter.image
    }
}
