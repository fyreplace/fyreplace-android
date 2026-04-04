package app.fyreplace.fyreplace.legacy.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.fyreplace.legacy.events.ChapterWasUpdatedEvent
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.protos.ChapterLocation
import app.fyreplace.protos.ChapterServiceClient
import app.fyreplace.protos.ChapterTextUpdate
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okio.ByteString

class TextChapterViewModel @AssistedInject constructor(
    override val preferences: SharedPreferences,
    private val em: EventsManager,
    private val chapterService: ChapterServiceClient,
    @Assisted private val postId: ByteString,
    @Assisted private val position: Int,
    @Assisted initialText: String
) :
    TextInputViewModel(initialText) {
    suspend fun updateTextChapter() = whileLoading {
        chapterService.UpdateText().executeFully(
            ChapterTextUpdate(
                location = ChapterLocation(post_id = postId, position = position),
                text = text.value
            )
        )
        em.post(ChapterWasUpdatedEvent(postId, position, text.value))
    }

    companion object {
        fun provideFactory(
            assistedFactory: TextChapterViewModelFactory,
            postId: ByteString,
            position: Int,
            text: String
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                assistedFactory.create(postId, position, text) as T
        }
    }
}
