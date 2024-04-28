package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.protos.Post
import app.fyreplace.protos.Profile
import com.google.protobuf.ByteString
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
interface CommentViewModelFactory {
    fun create(postId: ByteString, text: String): CommentViewModel
}

@AssistedFactory
@Suppress("unused")
interface DraftViewModelFactory {
    fun create(post: Post): DraftViewModel
}

@AssistedFactory
@Suppress("unused")
interface TextChapterViewModelFactory {
    fun create(postId: ByteString, position: Int, text: String): TextChapterViewModel
}

@AssistedFactory
@Suppress("unused")
interface UserViewModelFactory {
    fun create(profile: Profile): UserViewModel
}

@AssistedFactory
@Suppress("unused")
interface BioViewModelFactory {
    fun create(bio: String): BioViewModel
}
