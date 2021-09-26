package app.fyreplace.client.ui

import org.koin.dsl.module

val fragmentsModule = module {
    factory { ImageSelector(it[0], it[1]) }
}
