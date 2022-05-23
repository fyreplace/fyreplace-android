package app.fyreplace.fyreplace.viewmodels

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { ImageSelectorViewModel() }
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { CentralViewModel(get(), get()) }
    viewModel { FeedViewModel() }
    viewModel { NotificationsViewModel() }
    viewModel { ArchiveChangeViewModel() }
    viewModel { ArchiveViewModel(get()) }
    viewModel { DraftsChangeViewModel() }
    viewModel { DraftsViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { PostViewModel(it.get(), get(), get()) }
    viewModel { DraftViewModel(it.get(), get(), get()) }
    viewModel { UserViewModel(it.get(), get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { BlockedUsersChangeViewModel() }
    viewModel { BlockedUsersViewModel(get()) }
}
