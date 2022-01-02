package app.fyreplace.client.ui.adapters

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.client.R
import app.fyreplace.client.grpc.formatDate
import app.fyreplace.client.ui.loadAvatar
import app.fyreplace.client.ui.resolveTextAttribute
import app.fyreplace.client.ui.setUsername
import app.fyreplace.protos.Profile
import com.bumptech.glide.Glide
import com.google.protobuf.Timestamp

abstract class ItemListAdapter<Item : Any, VH : ItemListAdapter.Holder>(
    context: Context,
    diffCallback: DiffUtil.ItemCallback<Item>
) : PagingDataAdapter<Item, VH>(diffCallback) {
    protected val textAppearanceNormal =
        context.theme.resolveTextAttribute(R.attr.textAppearanceBody1)
    protected val textAppearanceTitle =
        context.theme.resolveTextAttribute(R.attr.textAppearanceHeadline5)
    protected val textMaxLines = context.resources.getInteger(R.integer.item_list_text_max_lines)

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

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
    }
}
