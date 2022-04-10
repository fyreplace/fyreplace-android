package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.protos.Cursor
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpcKt
import app.fyreplace.protos.Posts

class DraftsViewModel(private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub) :
    ItemListViewModel<Post, Posts>() {
    override fun listItems() = postStub.listDrafts(pages)

    override fun hasNextCursor(items: Posts) = items.hasNext()

    override fun getNextCursor(items: Posts): Cursor = items.next

    override fun getItemList(items: Posts): List<Post> = items.postsList
}
