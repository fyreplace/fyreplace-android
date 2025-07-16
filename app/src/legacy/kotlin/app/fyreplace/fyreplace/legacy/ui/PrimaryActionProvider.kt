package app.fyreplace.fyreplace.legacy.ui

interface PrimaryActionProvider {
    fun getPrimaryActionText(): Int? = null

    fun getPrimaryActionIcon(): Int? = null

    fun getPrimaryActionStyle(): PrimaryActionStyle = PrimaryActionStyle.EXTENDED

    fun onPrimaryAction()
}

enum class PrimaryActionStyle {
    EXTENDED,
    SHRUNK,
    NONE
}
