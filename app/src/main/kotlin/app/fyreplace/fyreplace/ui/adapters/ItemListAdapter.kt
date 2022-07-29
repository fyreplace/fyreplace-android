package app.fyreplace.fyreplace.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import com.google.protobuf.ByteString

abstract class ItemListAdapter<Item, VH : ItemHolder>(
    private val itemListener: ItemClickListener<Item> = object : ItemClickListener<Item> {}
) :
    RecyclerView.Adapter<VH>() {
    protected val items = mutableListOf<Item>()

    init {
        setHasStableIds(true)
    }

    final override fun setHasStableIds(hasStableIds: Boolean) = super.setHasStableIds(hasStableIds)

    override fun getItemId(position: Int) = getItemId(items[position]).asReadOnlyByteBuffer().long

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.setOnClickListener {
            itemListener.onItemClick(
                items[holder.bindingAdapterPosition],
                holder.bindingAdapterPosition
            )
        }
        holder.itemView.setOnLongClickListener {
            itemListener.onItemLongClick(
                items[holder.bindingAdapterPosition],
                holder.bindingAdapterPosition
            )
            return@setOnLongClickListener true
        }
    }

    abstract fun getItemId(item: Item): ByteString

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

    fun remove(position: Int): Item {
        val item = items.removeAt(position)
        notifyItemRemoved(position)
        return item
    }

    fun removeAll() {
        val count = items.size
        items.clear()
        notifyItemRangeRemoved(0, count)
    }

    interface ItemClickListener<Item> {
        fun onItemClick(item: Item, position: Int) = Unit

        fun onItemLongClick(item: Item, position: Int) = Unit
    }
}
