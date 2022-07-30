package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.fyreplace.R
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import com.google.protobuf.Empty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@HiltViewModel
class DraftsViewModel @Inject constructor(private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub) :
    ItemListViewModel<Post, Posts>() {
    override val emptyText = emptyFlow<Int>().asState(R.string.drafts_empty)

    override fun listItems() = postStub.listDrafts(pages)

    override fun hasNextCursor(items: Posts) = items.hasNext()

    override fun getNextCursor(items: Posts): Cursor = items.next

    override fun getItemList(items: Posts): List<Post> = items.postsList

    suspend fun create() = postStub.create(Empty.getDefaultInstance())

    suspend fun delete(postId: ByteString) = postStub.delete(id { id = postId })
}
