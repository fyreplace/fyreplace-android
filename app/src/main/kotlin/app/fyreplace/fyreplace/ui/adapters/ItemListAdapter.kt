package app.fyreplace.fyreplace.ui.adapters

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.grpc.formatDate
import app.fyreplace.fyreplace.ui.loadAvatar
import app.fyreplace.fyreplace.ui.resolveTextAttribute
import app.fyreplace.fyreplace.ui.setUsername
import app.fyreplace.protos.Profile
import com.bumptech.glide.Glide
import com.google.protobuf.Timestamp
import java.util.*

abstract class ItemListAdapter<Item : Any, VH : ItemListAdapter.Holder>(context: Context) :
    RecyclerView.Adapter<VH>() {
    protected val textAppearanceNormal =
        context.theme.resolveTextAttribute(R.attr.textAppearanceBodyLarge)
    protected val textAppearanceTitle =
        context.theme.resolveTextAttribute(R.attr.textAppearanceHeadlineSmall)
    protected val textMaxLines =
        context.resources.getInteger(R.integer.item_list_text_max_lines)
    protected val items = mutableListOf<Item>()
    private var itemListener: ((item: Item, position: Int) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    final override fun setHasStableIds(hasStableIds: Boolean) = super.setHasStableIds(hasStableIds)

    override fun getItemId(position: Int) =
        UUID.fromString(getItemId(items[position])).mostSignificantBits

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.itemView.setOnClickListener { itemListener?.invoke(items[position], position) }

    abstract fun getItemId(item: Item): String

    fun setOnClickListener(listener: (item: Item, position: Int) -> Unit) {
        itemListener = listener
    }

    fun add(position: Int, item: Item) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    fun addAll(items: List<Item>) {
        val start = this.items.size
        this.items += items
        notifyItemRangeInserted(start, items.size)
    }

    fun remove(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeAll() {
        val count = items.size
        items.clear()
        notifyItemRangeRemoved(0, count)
    }

    open class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView? = itemView.findViewById(R.id.avatar)
        val username: TextView? = itemView.findViewById(R.id.username)
        val date: TextView? = itemView.findViewById(R.id.date)

        fun setup(profile: Profile, timestamp: Timestamp?) {
            if (avatar != null) {
                Glide.with(itemView.context).loadAvatar(profile).into(avatar)
            }

            username?.setUsername(profile)
            date?.text = timestamp?.formatDate(singleLine = false)
        }
    }
}
