package app.fyreplace.fyreplace.legacy.extensions

import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import app.fyreplace.fyreplace.legacy.ui.MainActivity
import com.google.android.material.transition.MaterialSharedAxis

val Fragment.mainActivity get() = activity as MainActivity

fun Fragment.setupTransitions() {
    enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
    returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
}

fun Fragment.browse(@StringRes resId: Int) =
    startActivity(Intent(Intent.ACTION_VIEW, getString(resId).toUri()))
