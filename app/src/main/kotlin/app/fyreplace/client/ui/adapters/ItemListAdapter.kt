package app.fyreplace.client.ui.adapters

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.client.R
import app.fyreplace.client.grpc.formatDate
import app.fyreplace.client.ui.loadAvatar
import app.fyreplace.client.ui.resolveTextAttribute
import app.fyreplace.client.ui.setUsername
import app.fyreplace.protos.Profile
import com.bumptech.glide.Glide
import com.google.protobuf.Timestamp
import java.util.*

abstract class ItemListAdapter<Item : Any, VH : ItemListAdapter.Holder>(context: Context) :
    RecyclerView.Adapter<VH>() {
    protected val textAppearanceNormal =
        context.theme.resolveTextAttribute(R.attr.textAppearanceBody1)
    protected val textAppearanceTitle =
        context.theme.resolveTextAttribute(R.attr.textAppearanceHeadline5)
    protected val textMaxLines = context.resources.getInteger(R.integer.item_list_text_max_lines)
    protected val items = mutableListOf<Item>()
    private var itemListener: ((item: Item, position: Int) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    final override fun setHasStableIds(hasStableIds: Boolean) = super.setHasStableIds(hasStableIds)

    override fun getItemId(position: Int) =
        UUID.fromString(getItemId(items[position])).mostSignificantBits

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.setOnClickListener { itemListener?.invoke(items[position], position) }
    }

    abstract fun getItemId(item: Item): String

    fun setOnClickListener(listener: (item: Item, position: Int) -> Unit) {
        itemListener = listener
    }

    fun add(items: List<Item>) {
        val start = this.items.size
        this.items += items
        notifyItemRangeInserted(start, items.size)
    }

    fun remove(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clear() {
        val count = items.size
        items.clear()
        notifyItemRangeRemoved(0, count)
    }

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
    }

    abstract class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.avatar)
        val username: TextView = itemView.findViewById(R.id.username)
        val date: TextView = itemView.findViewById(R.id.date)

        fun setup(profile: Profile, timestamp: Timestamp) {
            Glide.with(itemView.context).loadAvatar(profile).into(avatar)
            username.setUsername(profile.username)
            date.text = timestamp.formatDate(singleLine = false)
        }
    }
}
