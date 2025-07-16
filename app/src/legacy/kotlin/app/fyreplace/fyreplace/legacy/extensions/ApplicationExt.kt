package app.fyreplace.fyreplace.legacy.extensions

import android.app.Application
import app.fyreplace.fyreplace.App

val Application.current get() = this as App
