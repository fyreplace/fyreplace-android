package app.fyreplace.fyreplace.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface Module {
    @Binds
    fun bindResourceResolver(resolver: ResourceResolverImpl): ResourceResolver

    @Binds
    fun bindConnectionStoreResolver(resolver: StoreResolverImpl): StoreResolver
}
