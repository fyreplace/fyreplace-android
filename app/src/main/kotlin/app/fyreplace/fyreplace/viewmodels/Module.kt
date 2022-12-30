package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.protos.Post
import app.fyreplace.protos.Profile
import dagger.assisted.AssistedFactory

@AssistedFactory
@Suppress("unused")
interface SettingsViewModelFactory {
    fun create(blockedUsers: Int): SettingsViewModel
}

@AssistedFactory
@Suppress("unused")
interface PostViewModelFactory {
    fun create(post: Post): PostViewModel
}

@AssistedFactory
@Suppress("unused")
interface DraftViewModelFactory {
    fun create(post: Post): DraftViewModel
}

@AssistedFactory
@Suppress("unused")
interface UserViewModelFactory {
    fun create(profile: Profile): UserViewModel
}
