package app.fyreplace.fyreplace.ui

interface PrimaryActionProvider {
    fun getPrimaryActionText(): Int? = null

    fun getPrimaryActionIcon(): Int? = null

    fun onPrimaryAction()
}
