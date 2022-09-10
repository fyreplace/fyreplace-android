package app.fyreplace.fyreplace.ui.adapters.holders

import android.view.View
import android.widget.ImageView
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Chapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

open class ImagePreviewHolder(itemView: View) : PreviewHolder(itemView) {
    private val preview: ImageView = itemView.findViewById(R.id.image_preview)
    private val cornerSize =
        itemView.context.resources.getDimensionPixelSize(R.dimen.image_corner_radius)

    override fun setup(chapter: Chapter) {
        Glide.with(itemView.context)
            .load(chapter.image.url)
            .transform(CenterCrop(), RoundedCorners(cornerSize))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(preview)
    }
}
