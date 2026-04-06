package app.fyreplace.fyreplace.legacy.ui.fragments

import android.content.ComponentCallbacks2
import android.os.Bundle
import android.view.View
import androidx.core.view.MenuProvider
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import app.fyreplace.fyreplace.legacy.extensions.insets
import app.fyreplace.fyreplace.legacy.extensions.setupTransitions
import app.fyreplace.fyreplace.legacy.extensions.updateBottomPaddingWithSystemInset
import app.fyreplace.fyreplace.legacy.ui.FailureHandler
import app.fyreplace.fyreplace.legacy.ui.MainActivity
import app.fyreplace.fyreplace.legacy.viewmodels.BaseViewModel

abstract class BaseFragment(contentLayoutId: Int) :
    Fragment(contentLayoutId),
    ComponentCallbacks2,
    FailureHandler {
    protected abstract val destinationId: Int
    protected abstract val bd: ViewDataBinding
    protected abstract val vm: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTransitions()
        super.onCreate(savedInstanceState)
        requireContext().unregisterComponentCallbacks(vm)
        requireContext().registerComponentCallbacks(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (destinationId !in MainActivity.TOP_LEVEL_DESTINATIONS) {
            bd.executePendingBindings()
            bd.root.updateBottomPaddingWithSystemInset(bd.root.insets)
        }

        if (this is MenuProvider) {
            requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        }
    }

    override fun onDestroy() {
        requireContext().registerComponentCallbacks(vm)
        requireContext().unregisterComponentCallbacks(this)
        super.onDestroy()
    }

    override fun onTrimMemory(level: Int) = Unit
}
