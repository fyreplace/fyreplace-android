package app.fyreplace.fyreplace.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import com.google.protobuf.ByteString

abstract class ItemListAdapter<Item : Any, VH : ItemHolder> :
    RecyclerView.Adapter<VH>() {
    protected val items = mutableListOf<Item>()
    private var itemListener: ((item: Item, position: Int) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    final override fun setHasStableIds(hasStableIds: Boolean) = super.setHasStableIds(hasStableIds)

    override fun getItemId(position: Int) = getItemId(items[position]).asReadOnlyByteBuffer().long

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.itemView.setOnClickListener { itemListener?.invoke(items[position], position) }

    abstract fun getItemId(item: Item): ByteString

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

    fun update(position: Int, item: Item) {
        items[position] = item
        notifyItemChanged(position)
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
}
