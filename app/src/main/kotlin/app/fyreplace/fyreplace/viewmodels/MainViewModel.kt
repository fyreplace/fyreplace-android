package app.fyreplace.fyreplace.viewmodels

import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MainViewModel : BaseViewModel() {
    private val mPageChoices = MutableStateFlow<List<@StringRes Int>>(emptyList())
    private val mCurrentPage = MutableStateFlow(0)
    val hasPageChoices = mPageChoices
        .map { it.isNotEmpty() }
        .asState(false)
    val pageState = mPageChoices
        .combine(mCurrentPage) { choices, page -> PagesState(choices, page) }
        .asState(PagesState(emptyList(), 0))

    fun setPageChoices(choices: List<Int>) {
        mPageChoices.value = choices
    }

    fun choosePage(page: Int) {
        mCurrentPage.value = page
    }
}

data class PagesState(val choices: List<Int>, val current: Int = 0)
