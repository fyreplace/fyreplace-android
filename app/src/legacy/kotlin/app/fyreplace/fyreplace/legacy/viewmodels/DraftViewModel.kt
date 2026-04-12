package app.fyreplace.fyreplace.legacy.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.legacy.events.ChapterWasUpdatedEvent
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.extensions.imageChunks
import app.fyreplace.fyreplace.legacy.extensions.mutateAsList
import app.fyreplace.fyreplace.legacy.extensions.sendAllAndClose
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.ChapterImageUpdate
import app.fyreplace.protos.ChapterLocation
import app.fyreplace.protos.ChapterRelocation
import app.fyreplace.protos.ChapterServiceClient
import app.fyreplace.protos.Id
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceClient
import app.fyreplace.protos.Publication
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okio.ByteString

@SuppressLint("CheckResult")
class DraftViewModel @AssistedInject constructor(
    override val preferences: SharedPreferences,
    em: EventsManager,
    private val postService: PostServiceClient,
    private val chapterService: ChapterServiceClient,
    @Assisted initialPost: Post
) : LoadingViewModel() {
    private val mPost = MutableStateFlow(initialPost)
    val post = mPost.asStateFlow()
    val canAddChapter = post
        .combine(isLoading) { post, isLoading -> post.chapter_count < 10 && !isLoading }
        .asState(false)
    val canPublish = post.map { it.chapter_count > 0 }.asState(false)

    init {
        viewModelScope.launch {
            em.events.filterIsInstance<ChapterWasUpdatedEvent>()
                .filter { it.postId == post.value.id }
                .collect {
                    mPost.value = post.value.copy(
                        chapters = post.value.chapters.mutateAsList {
                            this[it.position] = Chapter(text = it.text)
                        }
                    )
                }
        }
    }

    suspend fun retrieve(postId: ByteString) {
        mPost.value = postService.Retrieve().executeFully(Id(id = postId))
    }

    suspend fun delete() = postService.Delete().executeFully(Id(id = post.value.id))

    suspend fun publish(anonymously: Boolean) = postService.Publish().executeFully(
        Publication(
            id = post.value.id,
            anonymous = anonymously
        )
    )

    suspend fun createChapter() = whileLoading {
        chapterService.Create().executeFully(
            ChapterLocation(
                post_id = post.value.id,
                position = post.value.chapter_count
            )
        )
        mPost.value = post.value.copy(
            chapters = post.value.chapters + Chapter(),
            chapter_count = post.value.chapter_count + 1
        )
    }

    suspend fun deleteChapter(position: Int) = whileLoading {
        chapterService.Delete().executeFully(
            ChapterLocation(
                post_id = post.value.id,
                position = position
            )
        )
        mPost.value = post.value.copy(
            chapters = post.value.chapters.mutateAsList { removeAt(position) },
            chapter_count = post.value.chapter_count - 1
        )
    }

    suspend fun updateChapterImage(position: Int, image: ByteArray) = whileLoading {
        val (sender, receiver) = chapterService.UpdateImage().executeFully()
        val firstUpdate = ChapterImageUpdate(
            location = ChapterLocation(
                post_id = post.value.id,
                position = position
            )
        )

        sender.send(firstUpdate)
        sender.sendAllAndClose(image.imageChunks.map { ChapterImageUpdate(chunk = it) })
        val image = receiver.receive()
        mPost.value = post.value.copy(
            chapters = post.value.chapters.mutateAsList { this[position] = Chapter(image = image) }
        )
        return@whileLoading image
    }

    suspend fun moveChapter(fromPosition: Int, toPosition: Int) = whileLoading {
        chapterService.Move().executeFully(
            ChapterRelocation(
                post_id = post.value.id,
                from_position = fromPosition,
                to_position = toPosition
            )
        )

        mPost.value = post.value.copy(chapters = post.value.chapters.mutateAsList {
            add(toPosition, removeAt(fromPosition))
        })
    }

    companion object {
        fun provideFactory(assistedFactory: DraftViewModelFactory, post: Post) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    assistedFactory.create(post) as T
            }
    }
}
