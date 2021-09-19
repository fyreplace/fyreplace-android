package app.fyreplace.client.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject

abstract class BaseFragment(contentLayoutId: Int) : Fragment(contentLayoutId), FailureHandler {
    override val preferences by inject<SharedPreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTransitions()
    }
}
