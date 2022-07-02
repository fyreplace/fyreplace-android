package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.protos.Cursor
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpcKt
import app.fyreplace.protos.Posts
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub) :
    ItemListViewModel<Post, Posts>() {
    override fun listItems() = postStub.listArchive(pages)

    override fun hasNextCursor(items: Posts) = items.hasNext()

    override fun getNextCursor(items: Posts): Cursor = items.next

    override fun getItemList(items: Posts): List<Post> = items.postsList
}
