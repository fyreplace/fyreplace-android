package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import app.fyreplace.fyreplace.extensions.setupTransitions
import app.fyreplace.fyreplace.ui.FailureHandler

abstract class BaseFragment(contentLayoutId: Int) : Fragment(contentLayoutId), FailureHandler {
    override fun onCreate(savedInstanceState: Bundle?) {
        setupTransitions()
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (this is MenuProvider) {
            requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        }
    }
}
