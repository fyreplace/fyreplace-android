package app.fyreplace.fyreplace.extensions

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import app.fyreplace.fyreplace.ui.MainActivity
import com.google.android.material.transition.MaterialSharedAxis

val Fragment.mainActivity get() = activity as MainActivity

fun Fragment.setupTransitions() {
    enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
    returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
}

fun Fragment.browse(@StringRes resId: Int) =
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(resId))))
