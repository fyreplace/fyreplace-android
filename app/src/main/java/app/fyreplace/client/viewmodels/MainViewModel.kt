package app.fyreplace.client.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.*

class MainViewModel : ViewModel() {
    private val mPageChoices = MutableLiveData<List<@StringRes Int>>(emptyList())
    private val mCurrentPage = MutableLiveData(0)
    private val mPagesState = MediatorLiveData<PagesState>()
    val pageState = mPagesState
    val hasPageChoices = mPageChoices.map { it.isNotEmpty() }

    init {
        mPagesState.addSource(mPageChoices.distinctUntilChanged()) {
            mPagesState.value = PagesState(it, 0)
        }

        mPagesState.addSource(mCurrentPage.distinctUntilChanged()) {
            mPagesState.value = PagesState(mPageChoices.value!!, it)
        }
    }

    fun setPageChoices(choices: List<Int>) {
        mPageChoices.value = choices
    }

    fun choosePage(page: Int) {
        mCurrentPage.value = page
    }
}

data class PagesState(val choices: List<Int>, val current: Int = 0)
