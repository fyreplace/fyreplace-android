package app.fyreplace.fyreplace.ui.views

data class TextInputConfig(
    val inputType: Int,
    val maxLength: Int,
    val allowEmpty: Boolean = true,
    val text: String = ""
)
