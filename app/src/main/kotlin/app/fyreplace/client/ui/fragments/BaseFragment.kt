package app.fyreplace.client.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import app.fyreplace.client.ui.FailureHandler
import app.fyreplace.client.ui.MainActivity
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

abstract class BaseFragment(contentLayoutId: Int) : Fragment(contentLayoutId), FailureHandler {
    override val preferences by inject<SharedPreferences>()
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
