package app.fyreplace.client

import app.fyreplace.client.data.dataModule
import app.fyreplace.client.grpc.grpcModule
import app.fyreplace.client.viewmodels.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@Suppress("unused")
class App : BaseApp() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule, viewModelsModule, grpcModule, dataModule)
        }
    }
}
