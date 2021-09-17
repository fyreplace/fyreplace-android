package app.fyreplace.client.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import app.fyreplace.client.viewmodels.CentralViewModel
import com.google.android.material.transition.MaterialSharedAxis
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class BaseFragment(contentLayoutId: Int) : Fragment(contentLayoutId), FailureHandler {
    override val preferences by inject<SharedPreferences>()
    protected val cvm by sharedViewModel<CentralViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }
}
