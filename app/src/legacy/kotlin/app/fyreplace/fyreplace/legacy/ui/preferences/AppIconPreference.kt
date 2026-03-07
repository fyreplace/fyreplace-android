package app.fyreplace.fyreplace.legacy.ui.preferences

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import app.fyreplace.fyreplace.R

class AppIconPreference : Preference {
    private var appIcon: Drawable? = null

    @Suppress("unused")
    constructor(context: Context) : super(context)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setAppIcon(attrs)
    }

    @Suppress("unused")
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        setAppIcon(attrs)
    }

    @Suppress("unused")
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        setAppIcon(attrs)
    }

    init {
        widgetLayoutResource = R.layout.preference_app_icon
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val image = holder.findViewById(R.id.image) as ImageView
        appIcon?.let(image::setImageDrawable)
    }

    private fun setAppIcon(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppIconPreference)

        try {
            appIcon = typedArray.getDrawable(R.styleable.AppIconPreference_iconName)
        } finally {
            typedArray.recycle()
        }
    }
}
