package app.fyreplace.client.data

import app.fyreplace.client.grpc.ResponsesObserver
import app.fyreplace.protos.*
import io.grpc.stub.StreamObserver

class ArchivePagingSource(private val postStub: PostServiceGrpc.PostServiceStub) :
    ItemListPagingSource<Post, Posts>() {
    override fun startListing(observer: ResponsesObserver<Posts>): StreamObserver<Page> =
        postStub.listArchive(observer)

    override fun makeResult(items: Posts): LoadResult.Page<Cursor, Post> {
        val previous = if (items.hasPrevious()) items.previous else null
        val next = if (items.hasNext()) items.next else null
        return LoadResult.Page(items.postsList, previous, next)
    }
}
