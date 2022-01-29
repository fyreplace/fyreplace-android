package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.protos.*

@SuppressLint("CheckResult")
class BlockedUsersViewModel(private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub) :
    ItemListViewModel<Profile, Profiles>() {
    override val forward = true

    override fun listItems() = userStub.listBlocked(pages)

    override fun hasNextCursor(items: Profiles) = items.hasNext()

    override fun getNextCursor(items: Profiles): Cursor = items.next

    override fun getItemList(items: Profiles): List<Profile> = items.profilesList

    suspend fun unblock(userId: String) {
        userStub.updateBlock(block {
            id = userId
            blocked = false
        })
    }
}
