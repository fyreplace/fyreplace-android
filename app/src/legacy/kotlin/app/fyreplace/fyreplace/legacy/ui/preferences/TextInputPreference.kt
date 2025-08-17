package app.fyreplace.fyreplace.legacy.ui.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import app.fyreplace.fyreplace.legacy.ui.ContextHolder
import app.fyreplace.fyreplace.legacy.ui.views.TextInputConfig

@Suppress("LeakingThis")
abstract class TextInputPreference :
    Preference,
    Preference.OnPreferenceClickListener,
    ContextHolder {
    protected abstract val dialogTitle: Int
    protected abstract val textInputConfig: TextInputConfig

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        showTextInputAlert(dialogTitle, textInputConfig, ::persistString)
        return true
    }
}
