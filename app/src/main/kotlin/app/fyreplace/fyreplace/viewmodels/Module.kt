package app.fyreplace.fyreplace.viewmodels

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { MainViewModel() }
    viewModel { CentralViewModel(get(), get()) }
    viewModel { FeedViewModel() }
    viewModel { NotificationsViewModel() }
    viewModel { ArchiveChangeViewModel() }
    viewModel { ArchiveViewModel(get()) }
    viewModel { DraftsViewModel() }
    viewModel { PostViewModel(it.get(), get()) }
    viewModel { UserViewModel(it.get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { BlockedUsersChangeViewModel() }
    viewModel { BlockedUsersViewModel(get()) }
    viewModel { ImageSelectorViewModel() }
}
