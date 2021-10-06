package app.fyreplace.client.viewmodels

import app.fyreplace.client.data.ArchivePagingSource
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpc
import app.fyreplace.protos.Posts

class ArchiveViewModel(private val postStub: PostServiceGrpc.PostServiceStub) :
    ItemListViewModel<Post, Posts>() {
    override fun makeSource() = ArchivePagingSource(postStub)
}
