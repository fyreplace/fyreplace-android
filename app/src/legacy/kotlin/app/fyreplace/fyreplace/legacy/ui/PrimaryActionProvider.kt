package app.fyreplace.fyreplace.legacy.ui

interface PrimaryActionProvider {
    val primaryActionText: Int? get() = null

    val primaryActionIcon: Int? get() = null

    var primaryActionExtended: Boolean

    fun onPrimaryAction()
}
