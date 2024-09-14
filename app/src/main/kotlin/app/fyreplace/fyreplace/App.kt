package app.fyreplace.fyreplace

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    @Suppress("UnstableApiUsage")
    override fun onCreate() {
        super.onCreate()
        val dsn = getString(R.string.sentry_dsn)

        if (resources.getBoolean(R.bool.sentry_enabled) && dsn.isNotEmpty()) {
            SentryAndroid.init(this) {
                it.dsn = dsn
                it.environment = getString(R.string.sentry_environment)
                it.release = getString(R.string.sentry_release)
                it.isAttachViewHierarchy = true
                it.isEnableUserInteractionTracing = true
                it.isEnableUserInteractionBreadcrumbs = true

                if (BuildConfig.DEBUG) {
                    it.tracesSampleRate = 1.0
                    it.profilesSampleRate = 1.0
                    it.isEnableSpotlight = true
                }
            }
        }
    }
}
