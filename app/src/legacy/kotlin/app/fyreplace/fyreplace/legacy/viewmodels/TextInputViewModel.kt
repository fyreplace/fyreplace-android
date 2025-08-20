package app.fyreplace.fyreplace.legacy.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow

abstract class TextInputViewModel(initialText: String) : LoadingViewModel() {
    val text = MutableStateFlow(initialText)
}
