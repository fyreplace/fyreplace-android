package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import app.fyreplace.fyreplace.ui.FailureHandler
import app.fyreplace.fyreplace.ui.MainActivity
import kotlinx.coroutines.*

abstract class BaseFragment(contentLayoutId: Int) : Fragment(contentLayoutId), FailureHandler {
    protected val fragmentLifecycleScope =
        CoroutineScope(Dispatchers.Main.immediate) + SupervisorJob()

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTransitions()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        fragmentLifecycleScope.cancel()
        super.onDestroy()
    }

    protected fun setToolbarInfo(title: String, subtitle: String, icon: String) {
        (activity as? MainActivity)?.setToolbarInfo(title, subtitle, icon)
    }
}
