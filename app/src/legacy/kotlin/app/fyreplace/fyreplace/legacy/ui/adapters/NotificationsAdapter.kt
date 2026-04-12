package app.fyreplace.fyreplace.legacy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.extensions.firstChapter
import app.fyreplace.fyreplace.legacy.extensions.id
import app.fyreplace.fyreplace.legacy.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.legacy.ui.adapters.holders.PreviewHolder
import app.fyreplace.protos.Notification

class NotificationsAdapter(itemListener: ItemClickListener<Notification>) :
    ItemListAdapter<Notification, ItemHolder>(itemListener) {
    override fun getItemViewType(position: Int): Int {
        val notification = items[position]
        return when {
            notification.user != null -> TYPE_USER
            notification.post != null && notification.post.firstChapter.image != null -> TYPE_IMAGE
            else -> TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_USER -> UserHolder(
                inflater.inflate(R.layout.item_notification_user, parent, false)
            )

            TYPE_TEXT -> TextHolder(
                inflater.inflate(R.layout.item_notification_text, parent, false)
            )

            TYPE_IMAGE -> ImageHolder(
                inflater.inflate(R.layout.item_notification_image, parent, false)
            )

            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        (holder as NotificationHolder).setup(items[position])
    }

    override fun getItemId(item: Notification) = item.id

    companion object {
        const val TYPE_USER = 1
        const val TYPE_TEXT = 2
        const val TYPE_IMAGE = 3
    }

    interface NotificationHolder {
        val count: TextView

        fun setup(notification: Notification) {
            count.text = notification.count.toString()

            if (this is ItemHolder) {
                count.setTextColor(if (notification.is_flag) textColorError else textColorNormal)
            }
        }
    }

    class UserHolder(itemView: View) : ItemHolder(itemView), NotificationHolder {
        override val count: TextView = itemView.findViewById(R.id.count)

        override fun setup(notification: Notification) {
            super<NotificationHolder>.setup(notification)
            setup(notification.user, null)
        }
    }

    class TextHolder(itemView: View) : PreviewHolder(itemView), NotificationHolder {
        override val count: TextView = itemView.findViewById(R.id.count)

        override fun setup(notification: Notification) {
            super<NotificationHolder>.setup(notification)

            when {
                notification.post != null -> setup(notification.post)
                notification.comment != null -> setup(notification.comment)
                else -> Unit
            }
        }
    }

    class ImageHolder(itemView: View) : PreviewHolder(itemView), NotificationHolder {
        override val count: TextView = itemView.findViewById(R.id.count)

        override fun setup(notification: Notification) {
            super<NotificationHolder>.setup(notification)
            setup(notification.post)
        }
    }
}
