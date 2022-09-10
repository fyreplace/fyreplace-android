package app.fyreplace.fyreplace.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.firstChapter
import app.fyreplace.fyreplace.extensions.id
import app.fyreplace.fyreplace.ui.adapters.holders.ImagePreviewHolder
import app.fyreplace.fyreplace.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.ui.adapters.holders.TextPreviewHolder
import app.fyreplace.protos.Notification
import com.google.protobuf.ByteString

class NotificationsAdapter(itemListener: ItemClickListener<Notification>) :
    ItemListAdapter<Notification, ItemHolder>(itemListener) {
    override fun getItemViewType(position: Int): Int {
        val notification = items[position]
        return when (notification.targetCase) {
            Notification.TargetCase.USER -> TYPE_USER
            Notification.TargetCase.POST ->
                if (notification.post.firstChapter.hasImage()) TYPE_IMAGE else TYPE_TEXT
            Notification.TargetCase.COMMENT -> TYPE_TEXT
            else -> throw RuntimeException()
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

    override fun getItemId(item: Notification): ByteString = item.id

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
                count.setTextColor(if (notification.isFlag) textColorError else textColorNormal)
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

    class TextHolder(itemView: View) : TextPreviewHolder(itemView), NotificationHolder {
        override val count: TextView = itemView.findViewById(R.id.count)

        override fun setup(notification: Notification) {
            super<NotificationHolder>.setup(notification)

            when (notification.targetCase) {
                Notification.TargetCase.POST -> setup(notification.post)
                Notification.TargetCase.COMMENT -> setup(notification.comment)
                else -> Unit
            }
        }
    }

    class ImageHolder(itemView: View) : ImagePreviewHolder(itemView), NotificationHolder {
        override val count: TextView = itemView.findViewById(R.id.count)

        override fun setup(notification: Notification) {
            super<NotificationHolder>.setup(notification)
            setup(notification.post)
        }
    }
}
