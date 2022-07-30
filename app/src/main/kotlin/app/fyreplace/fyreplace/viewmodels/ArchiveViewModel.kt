package app.fyreplace.fyreplace.viewmodels

import androidx.annotation.IdRes
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Cursor
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpcKt
import app.fyreplace.protos.Posts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub) :
    ItemListViewModel<Post, Posts>() {
    private var mSelectedPage = MutableStateFlow(R.id.all_posts)
    val selectedPage = mSelectedPage.asStateFlow()
    override val emptyText = selectedPage
        .map { if (it == R.id.all_posts) R.string.archive_all_empty else R.string.archive_own_empty }
        .asState(R.string.archive_all_empty)

    override fun listItems() =
        if (selectedPage.value == R.id.all_posts) postStub.listArchive(pages)
        else postStub.listOwnPosts(pages)

    override fun hasNextCursor(items: Posts) = items.hasNext()

    override fun getNextCursor(items: Posts): Cursor = items.next

    override fun getItemList(items: Posts): List<Post> = items.postsList

    fun selectPage(@IdRes index: Int) {
        mSelectedPage.value = index
    }
}
