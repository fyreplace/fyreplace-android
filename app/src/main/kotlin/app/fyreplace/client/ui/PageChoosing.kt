package app.fyreplace.client.ui

interface PageChoosing {
    val pageChoices: List<Int>

    fun choosePage(page: Int)
}
