package app.fyreplace.fyreplace.ui.preferences

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.views.TextInputConfig

class EmailPreference : TextInputPreference {
    override val dialogTitle = R.string.settings_email
    override val textInputConfig by lazy {
        TextInputConfig(
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            maxLength = context.resources.getInteger(R.integer.email_max_size)
        )
    }

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
