package app.fyreplace.fyreplace.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Profile
import com.google.protobuf.ByteString

class BlockedUsersAdapter(context: Context) :
    ItemListAdapter<Profile, BlockedUsersAdapter.Holder>(context) {
    private var unblockListener: ((profile: Profile, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_blocked_user, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.setup(items[position], null)
        holder.unblock.setOnClickListener { unblockListener?.invoke(items[position], position) }
    }

    override fun getItemId(item: Profile): ByteString = item.id

    fun setUnblockListener(listener: ((profile: Profile, position: Int) -> Unit)) {
        unblockListener = listener
    }

    class Holder(itemView: View) : ItemListAdapter.Holder(itemView) {
        val unblock: Button = itemView.findViewById(R.id.unblock)
    }
}
