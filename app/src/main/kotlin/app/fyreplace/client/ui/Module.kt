package app.fyreplace.client.ui

import org.koin.dsl.module

val fragmentsModule = module {
    factory { ImageSelector(it.get(), it.get()) }
}
