package app.fyreplace.fyreplace.legacy.data

import android.content.Context
import android.content.res.Resources
import app.fyreplace.fyreplace.legacy.extensions.mainPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context) = context.mainPreferences

    @Provides
    fun provideResources(@ApplicationContext context: Context): Resources = context.resources
}
