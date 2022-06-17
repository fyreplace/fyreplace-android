package app.fyreplace.fyreplace.ui

import android.content.Context
import android.text.InputType
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.showSoftInput
import app.fyreplace.fyreplace.ui.views.TextInputConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

interface ContextHolder {
    fun getContext(): Context?

    fun showBasicAlert(@StringRes title: Int, @StringRes message: Int?, error: Boolean = false) {
        MaterialAlertDialogBuilder(getContext() ?: return)
            .setTitle(title)
            .apply { message?.let { setMessage(it) } ?: setMessage(null) }
            .setPositiveButton(R.string.ok, null)
            .run { if (error) setIcon(R.drawable.ic_baseline_error) else this }
            .show()
    }

    fun showChoiceAlert(@StringRes title: Int, @StringRes message: Int?, action: () -> Unit) {
        MaterialAlertDialogBuilder(getContext() ?: return)
            .setTitle(title)
            .apply { message?.let { setMessage(it) } ?: setMessage(null) }
            .setPositiveButton(R.string.yes) { _, _ -> action() }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    fun showSelectionAlert(
        @StringRes title: Int?,
        @ArrayRes choices: Int,
        action: (selected: Int) -> Unit
    ) {
        val choicesList = getContext()?.resources?.getStringArray(choices) ?: return
        showSelectionAlert(title, choicesList, action)
    }

    fun showSelectionAlert(
        @StringRes title: Int?,
        choices: Array<String>,
        action: (selected: Int) -> Unit
    ) {
        MaterialAlertDialogBuilder(getContext() ?: return)
            .setItems(choices) { _, choice -> action(choice) }
            .apply { title?.let { setTitle(it) } }
            .show()
    }

    fun showSingleChoiceAlert(
        @StringRes title: Int,
        @ArrayRes choices: Int,
        selected: Int,
        action: (selected: Int) -> Unit
    ) {
        MaterialAlertDialogBuilder(getContext() ?: return)
            .setTitle(title)
            .setSingleChoiceItems(choices, selected) { dialog, choice ->
                action(choice)
                dialog.dismiss()
            }
            .show()
    }

    fun showTextInputAlert(
        @StringRes title: Int,
        config: TextInputConfig,
        action: (text: String) -> Unit
    ) {
        val inputLayout: TextInputLayout?
        var textInput: TextInputEditText? = null
        val alert = MaterialAlertDialogBuilder(getContext() ?: return)
            .setTitle(title)
            .setView(R.layout.alert_text_input)
            .setPositiveButton(R.string.ok) { _, _ -> action(textInput?.text.toString()) }
            .setNegativeButton(R.string.cancel, null)
            .show()
        inputLayout = alert.findViewById(R.id.text_input_layout)
        inputLayout?.run {
            isCounterEnabled = true
            counterMaxLength = config.maxLength
        }
        textInput = alert.findViewById(R.id.text_input)
        textInput?.run {
            inputType = config.inputType
            setText(config.text)
            requestFocus()

            if (config.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE > 0) {
                minLines = 3
            }
        }

        alert.showSoftInput()
    }
}
