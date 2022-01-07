package app.fyreplace.fyreplace.ui

import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes

@StyleRes
fun Resources.Theme.resolveTextAttribute(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    resolveAttribute(attr, typedValue, true)
    return typedValue.data
}
