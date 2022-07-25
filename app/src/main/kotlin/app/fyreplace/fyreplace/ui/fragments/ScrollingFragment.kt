package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.extensions.mainActivity
import app.fyreplace.fyreplace.ui.PrimaryActionProvider
import app.fyreplace.fyreplace.ui.PrimaryActionStyle

abstract class ScrollingFragment(contentLayoutId: Int) :
    BaseFragment(contentLayoutId),
    PrimaryActionProvider {
    abstract val recyclerView: RecyclerView
    open val hasPrimaryActionDuplicate = false

    protected var mPrimaryActionStyle = PrimaryActionStyle.NONE
    private val scrollListener = ScrollListener()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.addOnScrollListener(scrollListener)
    }

    override fun onDestroyView() {
        recyclerView.removeOnScrollListener(scrollListener)
        super.onDestroyView()
    }

    override fun getPrimaryActionStyle() = mPrimaryActionStyle

    private inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val isScrollingUp = dy <= 0
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val lastPosition = layoutManager.findLastVisibleItemPosition()
            val canScrollDown =
                recyclerView.canScrollVertically(1) and (lastPosition < recyclerView.adapter!!.itemCount - 1)
            val newStyle = when {
                !canScrollDown && hasPrimaryActionDuplicate -> PrimaryActionStyle.NONE
                isScrollingUp -> PrimaryActionStyle.EXTENDED
                else -> PrimaryActionStyle.SHRUNK
            }

            if (newStyle != mPrimaryActionStyle) {
                mPrimaryActionStyle = newStyle
                mainActivity.refreshPrimaryAction()
            }
        }
    }
}
