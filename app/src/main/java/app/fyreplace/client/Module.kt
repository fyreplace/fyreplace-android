package app.fyreplace.client

import android.content.Context
import org.koin.dsl.module

val appModule = module {
    single { get<Context>().resources }
}
