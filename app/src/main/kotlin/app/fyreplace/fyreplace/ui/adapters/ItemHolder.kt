package app.fyreplace.fyreplace.ui.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.formatDate
import app.fyreplace.fyreplace.extensions.loadAvatar
import app.fyreplace.fyreplace.extensions.setUsername
import app.fyreplace.protos.Profile
import com.bumptech.glide.Glide
import com.google.protobuf.Timestamp

open class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val avatar: ImageView? = itemView.findViewById(R.id.avatar)
    val username: TextView? = itemView.findViewById(R.id.username)
    val date: TextView? = itemView.findViewById(R.id.date)

    open fun setup(profile: Profile, timestamp: Timestamp?) {
        if (avatar != null) {
            Glide.with(itemView.context).loadAvatar(profile).into(avatar)
        }

        username?.setUsername(profile)
        date?.text = timestamp?.formatDate(singleLine = false)
    }
}
