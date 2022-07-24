package app.fyreplace.fyreplace.ui.views

import android.content.Context
import android.util.AttributeSet
import app.fyreplace.fyreplace.R

class BioPreference : TextInputPreference {
    override val dialogTitle = R.string.settings_bio
    override val textInputConfig
        get() = TextInputConfig(
            maxLength = context.resources.getInteger(R.integer.bio_max_size),
            text = initialText
        )
    private var initialText = ""

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

    fun setInitialText(text: String) {
        initialText = text
    }
}
