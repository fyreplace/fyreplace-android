package app.fyreplace.fyreplace.ui.adapters

import androidx.recyclerview.widget.RecyclerView

abstract class ItemRandomAccessListAdapter<Item : Any, VH : ItemHolder>(private val offset: Int) :
    RecyclerView.Adapter<VH>() {
    protected val items = mutableMapOf<Int, Item>()
    private var totalSize = 0

    override fun getItemCount() = totalSize + offset

    fun setTotalSize(size: Int) {
        val diff = totalSize - size
        totalSize = size

        if (diff < 0) {
            notifyItemRangeRemoved(totalSize + offset, -diff)
        } else {
            notifyItemRangeInserted(totalSize + offset, diff)
        }
    }

    fun resetTo(items: Map<Int, Item>) {
        val oldTotalSize = totalSize
        totalSize = items.size
        this.items.clear()
        notifyItemRangeRemoved(offset, oldTotalSize)
        this.items.putAll(items)
        notifyItemRangeInserted(offset, totalSize)
    }

    fun insert(item: Item) {
        items[totalSize] = item
        totalSize++
        notifyItemInserted(totalSize + offset - 1)
    }

    fun update(position: Int, items: List<Item>) = items.forEachIndexed { i, item ->
        update(position + i, item)
    }

    fun update(position: Int, item: Item) {
        items[position] = item
        notifyItemChanged(position + offset)
    }
}
