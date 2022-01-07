package app.fyreplace.fyreplace.ui.fragments

import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis

fun Fragment.setupTransitions() {
    enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
    returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
}
