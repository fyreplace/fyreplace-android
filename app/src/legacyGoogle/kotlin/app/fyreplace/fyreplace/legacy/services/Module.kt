package app.fyreplace.fyreplace.legacy.services

import com.squareup.moshi.Moshi
import com.squareup.wire.WireJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object ServicesModule {
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder().add(WireJsonAdapterFactory()).build()
}
