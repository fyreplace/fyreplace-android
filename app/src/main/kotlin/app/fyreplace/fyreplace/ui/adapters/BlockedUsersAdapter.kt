package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.adapters.holders.ItemHolder
import app.fyreplace.protos.Profile
import com.google.protobuf.ByteString

class BlockedUsersAdapter(
    private val unblockListener: UnblockListener,
    itemListener: ItemClickListener<Profile>
) :
    ItemListAdapter<Profile, BlockedUsersAdapter.Holder>(itemListener) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_blocked_user, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.setup(items[position], null)
        holder.unblock.setOnClickListener {
            unblockListener.onUnblock(items[holder.bindingAdapterPosition])
        }
    }

    override fun getItemId(item: Profile): ByteString = item.id

    class Holder(itemView: View) : ItemHolder(itemView) {
        val unblock: Button = itemView.findViewById(R.id.unblock)
    }

    interface UnblockListener {
        fun onUnblock(profile: Profile)
    }
}
