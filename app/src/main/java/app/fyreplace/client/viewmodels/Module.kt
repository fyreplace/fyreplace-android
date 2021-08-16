package app.fyreplace.client.viewmodels

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { MainViewModel(get(), get()) }
    viewModel { FeedViewModel() }
    viewModel { NotificationsViewModel() }
    viewModel { ArchiveViewModel() }
    viewModel { DraftsViewModel() }
    viewModel { SettingsViewModel() }
    viewModel { LoginViewModel(get(), get()) }
}
