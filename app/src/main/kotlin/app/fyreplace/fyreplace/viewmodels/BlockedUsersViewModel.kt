package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.protos.Cursor
import app.fyreplace.protos.Profile
import app.fyreplace.protos.Profiles
import app.fyreplace.protos.UserServiceGrpcKt

class BlockedUsersViewModel(private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub) :
    ItemListViewModel<Profile, Profiles>() {
    override val forward = true

    override fun listItems() = userStub.listBlocked(pages)

    override fun hasNextCursor(items: Profiles) = items.hasNext()

    override fun getNextCursor(items: Profiles): Cursor = items.next

    override fun getItemList(items: Profiles): List<Profile> = items.profilesList
}
