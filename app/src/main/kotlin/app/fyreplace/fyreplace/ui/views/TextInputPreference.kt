package app.fyreplace.fyreplace.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import app.fyreplace.fyreplace.ui.ContextHolder

@Suppress("LeakingThis")
abstract class TextInputPreference :
    Preference,
    Preference.OnPreferenceClickListener,
    ContextHolder {
    protected abstract val dialogTitle: Int
    protected abstract val textInputConfig: TextInputConfig

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
        onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        showTextInputAlert(dialogTitle, textInputConfig, ::persistString)
        return true
    }
}
