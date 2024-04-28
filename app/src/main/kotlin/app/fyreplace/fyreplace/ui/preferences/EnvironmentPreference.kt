package app.fyreplace.fyreplace.ui.preferences

import android.content.Context
import android.util.AttributeSet
import app.fyreplace.fyreplace.R

class EnvironmentPreference : SingleChoicePreference {
    override val title = R.string.settings_environment
    override val choiceNames = R.array.settings_environment
    override val choiceValues = R.array.settings_environment_values
    override val defaultValue = R.string.settings_environment_default_value

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
}
