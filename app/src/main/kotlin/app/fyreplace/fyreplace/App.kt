package app.fyreplace.fyreplace

import android.content.SharedPreferences
import app.fyreplace.fyreplace.data.dataModule
import app.fyreplace.fyreplace.grpc.grpcModule
import app.fyreplace.fyreplace.extensions.applySettings
import app.fyreplace.fyreplace.ui.fragmentsModule
import app.fyreplace.fyreplace.viewmodels.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@Suppress("unused")
class App : BaseApp() {
    override fun onCreate() {
        super.onCreate()
        val koinApp = startKoin {
            androidContext(this@App)
            modules(appModule, fragmentsModule, viewModelsModule, grpcModule, dataModule)
        }
        koinApp.koin.get<SharedPreferences>().applySettings(koinApp.koin.get())
    }
}
