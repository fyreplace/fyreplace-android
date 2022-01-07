package app.fyreplace.fyreplace.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.loadAvatar
import com.bumptech.glide.Glide

class ImagePreference : Preference {
    var imageUrl: String? = null
        set(value) {
            field = value
            updateImage()
        }

    private var image: ImageView? = null

    @Suppress("unused")
    constructor(context: Context) : super(context)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    @Suppress("unused")
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    @Suppress("unused")
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        widgetLayoutResource = R.layout.preference_image
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        image = holder.findViewById(R.id.image) as ImageView
        updateImage()
    }

    private fun updateImage() {
        image?.let { Glide.with(context).loadAvatar(imageUrl).into(it) }
    }
}
