package app.fyreplace.fyreplace.ui.fragments

import androidx.fragment.app.Fragment
import app.fyreplace.fyreplace.ui.MainActivity
import app.fyreplace.protos.Profile
import com.google.android.material.transition.MaterialSharedAxis

fun Fragment.setupTransitions() {
    enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
    returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
}

fun Fragment.setToolbarInfo(profile: Profile, subtitle: String) {
    (activity as? MainActivity)?.setToolbarInfo(profile, subtitle)
}
