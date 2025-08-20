package app.fyreplace.fyreplace.ui

class Clicker {
    private var onClickHandler = {}

    fun setOnClickHandler(handler: () -> Unit) {
        onClickHandler = handler
    }

    fun onClick() = onClickHandler()
}
