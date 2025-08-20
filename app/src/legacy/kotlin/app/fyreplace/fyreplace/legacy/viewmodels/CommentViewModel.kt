package app.fyreplace.fyreplace.legacy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.protos.CommentServiceGrpcKt
import app.fyreplace.protos.commentCreation
import com.google.protobuf.ByteString
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class CommentViewModel @AssistedInject constructor(
    @Assisted private val postId: ByteString,
    @Assisted initialText: String,
    private val commentStub: CommentServiceGrpcKt.CommentServiceCoroutineStub
) :
    TextInputViewModel(initialText) {
    suspend fun create() = commentStub.create(commentCreation {
        postId = this@CommentViewModel.postId
        text = this@CommentViewModel.text.value
    })

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
