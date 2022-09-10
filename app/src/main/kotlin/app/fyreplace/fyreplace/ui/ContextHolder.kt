package app.fyreplace.fyreplace.ui

import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.Button
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.isNotNullOrBlank
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
        lateinit var textInput: TextInputEditText
        lateinit var textWatcher: TextWatcher
        val alert = MaterialAlertDialogBuilder(getContext() ?: return)
            .setTitle(title)
            .setView(R.layout.block_text_input)
            .setPositiveButton(R.string.ok) { _, _ ->
                val text = textInput.text ?: ""
                action(text.trim().toString())
            }
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener { textInput.removeTextChangedListener(textWatcher) }
            .show()

        alert.findViewById<TextInputLayout>(R.id.text_input_layout)?.run {
            isCounterEnabled = true
            counterMaxLength = config.maxLength
        }

        val positiveButton = alert.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.isEnabled = config.allowEmpty
        textInput = alert.findViewById(R.id.text_input)!!
        textWatcher = ButtonToggleWatcher(textInput, positiveButton)
        with(textInput) {
            inputType = config.inputType
            setText(config.text)
            requestFocus()
            addTextChangedListener(textWatcher)

            if (config.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE > 0) {
                minLines = 3
            }
        }

        alert.showSoftInput()
    }

    private class ButtonToggleWatcher(
        private val textInput: TextInputEditText,
        private val button: Button
    ) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            button.isEnabled = textInput.text.isNotNullOrBlank()
        }

        override fun afterTextChanged(s: Editable?) = Unit
    }
}
