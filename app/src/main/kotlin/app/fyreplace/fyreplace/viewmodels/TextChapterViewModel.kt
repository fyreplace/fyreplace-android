package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.fyreplace.events.ChapterWasUpdatedEvent
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.protos.ChapterServiceGrpcKt
import app.fyreplace.protos.chapterLocation
import app.fyreplace.protos.chapterTextUpdate
import com.google.protobuf.ByteString
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class TextChapterViewModel @AssistedInject constructor(
    private val em: EventsManager,
    @Assisted private val postId: ByteString,
    @Assisted private val position: Int,
    @Assisted initialText: String,
    private val chapterStub: ChapterServiceGrpcKt.ChapterServiceCoroutineStub
) :
    TextInputViewModel(initialText) {
    suspend fun updateTextChapter(): Unit = whileLoading {
        chapterStub.updateText(chapterTextUpdate {
            location = chapterLocation {
                postId = this@TextChapterViewModel.postId
                position = this@TextChapterViewModel.position
            }
            text = this@TextChapterViewModel.text.value
        })
        em.post(ChapterWasUpdatedEvent(postId, position, text.value))
    }

    companion object {
        fun provideFactory(
            assistedFactory: TextChapterViewModelFactory,
            postId: ByteString,
            position: Int,
            text: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                assistedFactory.create(postId, position, text) as T
        }
    }
}
