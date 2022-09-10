package app.fyreplace.fyreplace.ui.adapters.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.formatDate
import app.fyreplace.fyreplace.extensions.resolveStyleAttribute
import app.fyreplace.fyreplace.extensions.setAvatar
import app.fyreplace.fyreplace.extensions.setUsername
import app.fyreplace.protos.Profile
import com.google.protobuf.Timestamp

open class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textColorNormal =
        itemView.context.theme.resolveStyleAttribute(R.attr.colorOnBackground)
    val textColorError =
        itemView.context.theme.resolveStyleAttribute(R.attr.colorError)

    protected val avatar: ImageView? = itemView.findViewById(R.id.avatar)
    protected val username: TextView? = itemView.findViewById(R.id.username)
    protected val date: TextView? = itemView.findViewById(R.id.date)
    protected open val shortDate = false

    open fun setup(profile: Profile, timestamp: Timestamp?) {
        avatar?.setAvatar(profile)
        username?.setUsername(profile)
        date?.text = timestamp?.formatDate(singleLine = shortDate, short = shortDate)
    }
}
