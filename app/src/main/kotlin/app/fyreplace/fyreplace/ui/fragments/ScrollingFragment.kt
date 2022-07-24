package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.extensions.mainActivity
import app.fyreplace.fyreplace.ui.MainActivity
import app.fyreplace.fyreplace.ui.PrimaryActionProvider

abstract class ScrollingFragment(contentLayoutId: Int) : BaseFragment(contentLayoutId) {
    abstract val recyclerView: RecyclerView
    open val hasPrimaryActionDuplicate = false

    protected var primaryActionStyle = MainActivity.PrimaryActionStyle.EXTENDED
    private val scrollListener = ScrollListener()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (this is PrimaryActionProvider) {
            recyclerView.addOnScrollListener(scrollListener)
        }
    }

    override fun onDestroyView() {
        if (this is PrimaryActionProvider) {
            recyclerView.removeOnScrollListener(scrollListener)
        }

        super.onDestroyView()
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val isScrollingUp = dy <= 0
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val lastPosition = layoutManager.findLastVisibleItemPosition()
            val canScrollDown =
                recyclerView.canScrollVertically(1) and (lastPosition < recyclerView.adapter!!.itemCount - 1)
            val newStyle = when {
                !canScrollDown && hasPrimaryActionDuplicate -> MainActivity.PrimaryActionStyle.NONE
                isScrollingUp -> MainActivity.PrimaryActionStyle.EXTENDED
                else -> MainActivity.PrimaryActionStyle.SHRUNK
            }

            if (newStyle != primaryActionStyle) {
                primaryActionStyle = newStyle
                mainActivity.refreshPrimaryAction(newStyle)
            }
        }
    }
}
