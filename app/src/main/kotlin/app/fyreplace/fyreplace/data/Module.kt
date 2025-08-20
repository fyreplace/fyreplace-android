package app.fyreplace.fyreplace.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface Module {
    @Binds
    @Singleton
    fun bindResourceResolver(resolver: ContextResourceResolver): ResourceResolver

    @Binds
    @Singleton
    fun bindStoreResolver(resolver: ContextStoreResolver): StoreResolver

    @Binds
    @Singleton
    fun bindSecretsHandler(resolver: EncryptedSecretsHandler): SecretsHandler
}
