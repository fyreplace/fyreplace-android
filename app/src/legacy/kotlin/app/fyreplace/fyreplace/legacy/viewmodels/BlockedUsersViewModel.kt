package app.fyreplace.fyreplace.legacy.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.UserWasBannedEvent
import app.fyreplace.fyreplace.legacy.events.UserWasBlockedEvent
import app.fyreplace.fyreplace.legacy.events.UserWasUnblockedEvent
import app.fyreplace.protos.Block
import app.fyreplace.protos.Profile
import app.fyreplace.protos.Profiles
import app.fyreplace.protos.UserServiceClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import okio.ByteString
import javax.inject.Inject

@HiltViewModel
@SuppressLint("CheckResult")
class BlockedUsersViewModel @Inject constructor(
    override val preferences: SharedPreferences,
    em: EventsManager,
    private val userService: UserServiceClient
) : ItemListViewModel<Profile, Profiles>(em) {
    override val addedItems = em.events.filterIsInstance<UserWasBlockedEvent>()
    override val updatedItems = em.events.filterIsInstance<UserWasBannedEvent>()
    override val removedItems = em.events.filterIsInstance<UserWasUnblockedEvent>()
    override val forward = true
    override val emptyText = MutableStateFlow(R.string.blocked_users_empty).asStateFlow()

    override fun getItemId(item: Profile) = item.id

    override fun listItems() = userService.ListBlocked()

    override fun getNextCursor(items: Profiles) = items.next

    override fun getItemList(items: Profiles) = items.profiles

    suspend fun unblock(userId: ByteString) = userService.UpdateBlock().executeFully(
        Block(
            id = userId,
            blocked = false
        )
    )
}
