package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.UserBanEvent
import app.fyreplace.fyreplace.events.UserBlockEvent
import app.fyreplace.fyreplace.events.UserUnblockEvent
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import javax.inject.Inject

@HiltViewModel
@SuppressLint("CheckResult")
class BlockedUsersViewModel @Inject constructor(
    em: EventsManager,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
) :
    ItemListViewModel<Profile, Profiles>(em) {
    override val addedItems = em.events.filterIsInstance<UserBlockEvent>()
    override val updatedItems = em.events.filterIsInstance<UserBanEvent>()
    override val removedItems = em.events.filterIsInstance<UserUnblockEvent>()
    override val forward = true
    override val emptyText = emptyFlow<Int>().asState(R.string.blocked_users_empty)

    override fun getItemId(item: Profile): ByteString = item.id

    override fun listItems() = userStub.listBlocked(pages)

    override fun hasNextCursor(items: Profiles) = items.hasNext()

    override fun getNextCursor(items: Profiles): Cursor = items.next

    override fun getItemList(items: Profiles): List<Profile> = items.profilesList

    suspend fun unblock(userId: ByteString) {
        userStub.updateBlock(block {
            id = userId
            blocked = false
        })
    }
}
