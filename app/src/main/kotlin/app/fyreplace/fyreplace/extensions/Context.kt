package app.fyreplace.fyreplace.extensions

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Suppress("RecursivePropertyAccessor")
val Context.activity
    get(): ComponentActivity? = when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.activity
        else -> null
    }

val activity @Composable get() = LocalContext.current.activity
