package app.fyreplace.fyreplace.legacy.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.UserWasBannedEvent
import app.fyreplace.fyreplace.legacy.events.UserWasBlockedEvent
import app.fyreplace.fyreplace.legacy.events.UserWasUnblockedEvent
import app.fyreplace.protos.Cursor
import app.fyreplace.protos.Profile
import app.fyreplace.protos.Profiles
import app.fyreplace.protos.UserServiceGrpcKt
import app.fyreplace.protos.block
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import javax.inject.Inject

@HiltViewModel
@SuppressLint("CheckResult")
class BlockedUsersViewModel @Inject constructor(
    em: EventsManager,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
) :
    ItemListViewModel<Profile, Profiles>(em) {
    override val addedItems = em.events.filterIsInstance<UserWasBlockedEvent>()
    override val updatedItems = em.events.filterIsInstance<UserWasBannedEvent>()
    override val removedItems = em.events.filterIsInstance<UserWasUnblockedEvent>()
    override val forward = true
    override val emptyText = MutableStateFlow(R.string.blocked_users_empty).asStateFlow()

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
