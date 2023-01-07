package app.fyreplace.fyreplace.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow

abstract class TextInputViewModel(initialText: String) : LoadingViewModel() {
    val text = MutableStateFlow(initialText)
}
