package app.fyreplace.client.data

import app.fyreplace.protos.Cursor
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpcKt
import app.fyreplace.protos.Posts

class ArchivePagingSource(postStub: PostServiceGrpcKt.PostServiceCoroutineStub) :
    ItemListPagingSource<Post, Posts>() {
    override val itemsFlow = postStub.listArchive(cursorFlow)

    override fun makeResult(items: Posts): LoadResult.Page<Cursor, Post> {
        val previous = if (items.hasPrevious()) items.previous else null
        val next = if (items.hasNext()) items.next else null
        return LoadResult.Page(items.postsList, previous, next)
    }
}
