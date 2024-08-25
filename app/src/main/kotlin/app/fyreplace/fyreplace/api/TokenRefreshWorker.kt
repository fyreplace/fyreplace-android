package app.fyreplace.fyreplace.api

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.fyreplace.fyreplace.data.SecretsHandler
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.extensions.update
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TokenRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val storeResolver: StoreResolver,
    private val secretsHandler: SecretsHandler,
    private val apiResolver: ApiResolver
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        try {
            val response = apiResolver.tokens().getNewToken()
            val newToken = response.body() ?: throw Exception("No token received")
            storeResolver.secretsStore.update { setToken(secretsHandler.encrypt(newToken)) }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}
