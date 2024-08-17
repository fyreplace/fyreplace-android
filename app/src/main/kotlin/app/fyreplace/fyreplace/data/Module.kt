package app.fyreplace.fyreplace.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Suppress("unused")
@Module
@InstallIn(ViewModelComponent::class)
interface Module {
    @Binds
    fun bindResourceResolver(resolver: ContextResourceResolver): ResourceResolver

    @Binds
    fun bindStoreResolver(resolver: ContextStoreResolver): StoreResolver

    @Binds
    fun bindSecretsHandler(resolver: EncryptedSecretsHandler): SecretsHandler
}
