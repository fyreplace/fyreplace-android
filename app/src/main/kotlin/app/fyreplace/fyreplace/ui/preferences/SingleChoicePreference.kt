package app.fyreplace.fyreplace.ui.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import app.fyreplace.fyreplace.ui.ContextHolder

abstract class SingleChoicePreference :
    Preference,
    Preference.OnPreferenceClickListener,
    ContextHolder {
    abstract val title: Int
    abstract val choiceNames: Int
    abstract val choiceValues: Int
    abstract val defaultValue: Int
    private val choiceNamesList by lazy { context.resources.getStringArray(choiceNames) }
    private val choiceValuesList by lazy { context.resources.getStringArray(choiceValues) }
    private val value: String
        get() = getPersistedString(context.getString(defaultValue))
    private val currentIndex
        get() = choiceValuesList.indexOf(value)

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

    override fun onSetInitialValue(defaultValue: Any?) {
        onPreferenceClickListener = this
        updateSummary()
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        showSingleChoiceAlert(title, choiceNames, currentIndex) {
            persistString(choiceValuesList[it])
            updateSummary()
        }

        return true
    }

    private fun updateSummary() {
        summary = choiceNamesList[currentIndex]
    }
}
