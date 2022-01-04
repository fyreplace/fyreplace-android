package app.fyreplace.client

import android.content.SharedPreferences
import app.fyreplace.client.data.dataModule
import app.fyreplace.client.grpc.grpcModule
import app.fyreplace.client.ui.applySettings
import app.fyreplace.client.ui.fragmentsModule
import app.fyreplace.client.viewmodels.viewModelsModule
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
