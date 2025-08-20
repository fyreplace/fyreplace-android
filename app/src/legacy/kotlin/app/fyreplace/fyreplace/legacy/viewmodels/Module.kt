package app.fyreplace.fyreplace.legacy.viewmodels

import app.fyreplace.protos.Post
import app.fyreplace.protos.Profile
import com.google.protobuf.ByteString
import dagger.assisted.AssistedFactory

@AssistedFactory
interface SettingsViewModelFactory {
    fun create(blockedUsers: Int): SettingsViewModel
}

@AssistedFactory
interface PostViewModelFactory {
    fun create(post: Post): PostViewModel
}

@AssistedFactory
interface CommentViewModelFactory {
    fun create(postId: ByteString, text: String): CommentViewModel
}

@AssistedFactory
interface DraftViewModelFactory {
    fun create(post: Post): DraftViewModel
}

@AssistedFactory
interface TextChapterViewModelFactory {
    fun create(postId: ByteString, position: Int, text: String): TextChapterViewModel
}

@AssistedFactory
interface UserViewModelFactory {
    fun create(profile: Profile): UserViewModel
}

@AssistedFactory
interface BioViewModelFactory {
    fun create(bio: String): BioViewModel
}
