package app.fyreplace.fyreplace.events

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
abstract class EventsModule {
    @Singleton
    @Binds
    abstract fun provideEventsManager(eventsManagerImpl: EventsManagerImpl): EventsManager
}
