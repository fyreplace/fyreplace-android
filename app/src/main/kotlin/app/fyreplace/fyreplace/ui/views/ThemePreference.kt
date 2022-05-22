package app.fyreplace.fyreplace.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.ContextHolder

class ThemePreference :
    Preference,
    Preference.OnPreferenceClickListener,
    ContextHolder {
    private val themeValues by lazy { context.resources.getStringArray(R.array.settings_theme_values) }
    private val themeNames by lazy { context.resources.getStringArray(R.array.settings_theme) }
    private val value: String
        get() = getPersistedString(context.getString(R.string.settings_theme_auto_value))
    private val currentIndex
        get() = themeValues.indexOf(value)

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

    override fun onSetInitialValue(defaultValue: Any?) {
        updateSummary()
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        showSingleChoiceAlert(R.string.settings_theme, R.array.settings_theme, currentIndex) {
            persistString(themeValues[it])
            updateSummary()
        }

        return true
    }

    private fun updateSummary() {
        summary = themeNames[currentIndex]
    }
}
