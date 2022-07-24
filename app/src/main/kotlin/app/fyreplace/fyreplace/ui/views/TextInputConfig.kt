package app.fyreplace.fyreplace.ui.views

import android.text.InputType

data class TextInputConfig(
    val inputType: Int = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
    val maxLength: Int,
    val allowEmpty: Boolean = true,
    val text: String = ""
)
