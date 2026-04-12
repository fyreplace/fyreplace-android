package app.fyreplace.fyreplace.legacy.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.legacy.extensions.mainActivity
import app.fyreplace.fyreplace.legacy.ui.PrimaryActionProvider

abstract class ScrollingListFragment(contentLayoutId: Int) : BaseFragment(contentLayoutId) {
    abstract val recyclerView: RecyclerView

    private val scrollListener =
        if (this is PrimaryActionProvider) ScrollListener(this)
        else null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scrollListener?.let(recyclerView::addOnScrollListener)
    }

    override fun onDestroyView() {
        scrollListener?.let(recyclerView::removeOnScrollListener)
        super.onDestroyView()
    }

    private class ScrollListener<T>(private val fragment: T) : RecyclerView.OnScrollListener()
            where T : Fragment,
                  T : PrimaryActionProvider {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val isScrollingUp = dy <= 0
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val lastPosition = layoutManager.findLastVisibleItemPosition()
            val canScrollDown =
                recyclerView.canScrollVertically(1) and (lastPosition < recyclerView.adapter!!.itemCount - 1)
            val extended = !canScrollDown || isScrollingUp

            if (extended != fragment.primaryActionExtended) {
                fragment.primaryActionExtended = extended
                fragment.mainActivity.refreshPrimaryAction()
            }
        }
    }
}
