package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.fyreplace.extensions.imageChunkFlow
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.*

@SuppressLint("CheckResult")
class DraftViewModel(
    initialPost: Post,
    private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub,
    private val chapterStub: ChapterServiceGrpcKt.ChapterServiceCoroutineStub
) : LoadingViewModel() {
    private val mPost = MutableStateFlow(initialPost)
    val post = mPost.asStateFlow()
    val canAddChapter = post
        .combine(isLoading) { post, isLoading -> post.chapterCount < 10 && !isLoading }
        .asState(false)
    val canPublish = post.map { it.chapterCount > 0 }.asState(false)

    suspend fun retrieve(postId: ByteString) {
        mPost.value = postStub.retrieve(id { id = postId })
    }

    suspend fun delete() {
        postStub.delete(id { id = post.value.id })
    }

    suspend fun publish(anonymously: Boolean) {
        postStub.publish(publication {
            id = post.value.id
            anonymous = anonymously
        })
    }

    suspend fun createChapter(): Unit = whileLoading {
        chapterStub.create(chapterLocation {
            postId = post.value.id
            position = post.value.chaptersCount
        })
        mPost.value = Post.newBuilder(post.value)
            .addChapters(Chapter.getDefaultInstance())
            .setChapterCount(post.value.chaptersCount + 1)
            .build()
    }

    suspend fun deleteChapter(position: Int): Unit = whileLoading {
        chapterStub.delete(chapterLocation {
            postId = post.value.id
            this.position = position
        })
        mPost.value = Post.newBuilder(post.value)
            .removeChapters(position)
            .setChapterCount(post.value.chaptersCount - 1)
            .build()
    }

    suspend fun updateChapterText(position: Int, text: String): Unit = whileLoading {
        chapterStub.updateText(chapterTextUpdate {
            location = chapterLocation {
                postId = post.value.id
                this.position = position
            }
            this.text = text
        })
        mPost.value = post.value.copy { chapters[position] = chapter { this.text = text } }
    }

    suspend fun updateChapterImage(position: Int, image: ByteArray) = whileLoading {
        val firstUpdate = chapterImageUpdate {
            location = chapterLocation {
                postId = post.value.id
                this.position = position
            }
        }
        val chunksFlow = image.imageChunkFlow.map { chapterImageUpdate { chunk = it } }
        val response = chapterStub.updateImage(chunksFlow.onStart { emit(firstUpdate) })
        mPost.value = post.value.copy { chapters[position] = chapter { this.image = response } }
        return@whileLoading response
    }

    suspend fun moveChapter(fromPosition: Int, toPosition: Int) = whileLoading {
        chapterStub.move(chapterRelocation {
            postId = post.value.id
            this.fromPosition = fromPosition
            this.toPosition = toPosition
        })
        mPost.value = Post.newBuilder(post.value)
            .removeChapters(fromPosition)
            .addChapters(toPosition, post.value.chaptersList[fromPosition])
            .build()
    }

    fun makePreview(): Post = Post.newBuilder(post.value)
        .clearChapters()
        .apply { post.value.chaptersList.getOrNull(0)?.let { addChapters(it) } }
        .setIsPreview(true)
        .build()
}
