package app.fyreplace.fyreplace.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.fyreplace.fyreplace.R
import com.google.android.material.color.MaterialColors

class MaterialSwipeLayout : SwipeRefreshLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        val surface = MaterialColors.getColor(
            context,
            R.attr.colorSurface,
            MaterialSwipeLayout::class.qualifiedName
        )
        val primary = MaterialColors.getColor(
            context,
            R.attr.colorPrimary,
            MaterialSwipeLayout::class.qualifiedName
        )
        setProgressBackgroundColorSchemeColor(surface)
        setColorSchemeColors(primary)
    }
}
