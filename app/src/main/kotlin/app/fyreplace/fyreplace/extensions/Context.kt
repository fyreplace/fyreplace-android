package app.fyreplace.fyreplace.extensions

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import app.fyreplace.fyreplace.MainActivity

@Suppress("RecursivePropertyAccessor")
val Context.activity
    get(): ComponentActivity? = when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.activity
        else -> null
    }

val activity
    @Composable
    @ReadOnlyComposable
    get() = LocalContext.current.activity as? MainActivity
