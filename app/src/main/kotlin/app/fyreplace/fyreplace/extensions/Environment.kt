package app.fyreplace.fyreplace.extensions

import app.fyreplace.fyreplace.BuildConfig
import app.fyreplace.fyreplace.protos.Environment

val Environment.orDefault: Environment
    get() = this.takeUnless { it in setOf(Environment.UNSPECIFIED, Environment.UNRECOGNIZED) }
        ?: BuildConfig.ENVIRONMENT_DEFAULT
