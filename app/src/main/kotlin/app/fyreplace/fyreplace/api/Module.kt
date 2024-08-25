package app.fyreplace.fyreplace.api

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
    fun bindApiResolver(resolver: RemoteApiResolver): ApiResolver
}
