package app.fyreplace.fyreplace.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Profile

class BlockedUsersAdapter(context: Context) : ItemListAdapter<Profile>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_blocked_user, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.setup(items[position], null)
    }

    override fun getItemId(item: Profile): String = item.id
}
