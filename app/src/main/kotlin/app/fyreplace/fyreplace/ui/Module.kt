package app.fyreplace.fyreplace.ui

import org.koin.dsl.module

val fragmentsModule = module {
    factory { ImageSelector(it.get(), it.get()) }
}
