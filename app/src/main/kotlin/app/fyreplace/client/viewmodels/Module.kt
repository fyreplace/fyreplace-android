package app.fyreplace.client.viewmodels

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { CentralViewModel(get(), get()) }
    viewModel { FeedViewModel() }
    viewModel { NotificationsViewModel() }
    viewModel { ArchiveViewModel() }
    viewModel { DraftsViewModel() }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
}
