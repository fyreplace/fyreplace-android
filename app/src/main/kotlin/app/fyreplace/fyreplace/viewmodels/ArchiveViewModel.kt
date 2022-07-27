package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Cursor
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpcKt
import app.fyreplace.protos.Posts
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub) :
    ItemListViewModel<Post, Posts>() {
    private var mSelectedPage = R.id.all_posts
    val selectedPage get() = mSelectedPage

    override fun listItems() =
        if (selectedPage == R.id.all_posts) postStub.listArchive(pages)
        else postStub.listOwnPosts(pages)

    override fun hasNextCursor(items: Posts) = items.hasNext()

    override fun getNextCursor(items: Posts): Cursor = items.next

    override fun getItemList(items: Posts): List<Post> = items.postsList

    fun selectPage(index: Int) {
        mSelectedPage = index
    }
}
