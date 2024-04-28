package app.fyreplace.fyreplace.extensions

import android.app.Application
import app.fyreplace.fyreplace.App

val Application.current get() = this as App
