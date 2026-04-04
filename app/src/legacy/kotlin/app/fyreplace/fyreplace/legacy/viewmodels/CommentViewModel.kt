package app.fyreplace.fyreplace.legacy.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.protos.CommentCreation
import app.fyreplace.protos.CommentServiceClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okio.ByteString

class CommentViewModel @AssistedInject constructor(
    override val preferences: SharedPreferences,
    private val commentService: CommentServiceClient,
    @Assisted private val postId: ByteString,
    @Assisted initialText: String
) :
    TextInputViewModel(initialText) {
    suspend fun create() = commentService.Create().executeFully(
        CommentCreation(
            post_id = postId,
            text = text.value
        )
    )

    companion object {
        fun provideFactory(
            assistedFactory: CommentViewModelFactory,
            postId: ByteString,
            text: String
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                assistedFactory.create(postId, text) as T
        }
    }
}
