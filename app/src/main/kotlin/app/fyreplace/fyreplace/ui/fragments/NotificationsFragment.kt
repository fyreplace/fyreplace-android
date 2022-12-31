package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.ActivityWasStoppedEvent
import app.fyreplace.fyreplace.events.NotificationWasCreatedEvent
import app.fyreplace.fyreplace.events.NotificationWasDeletedEvent
import app.fyreplace.fyreplace.events.PostWasSeenEvent
import app.fyreplace.fyreplace.extensions.isAdmin
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.ui.adapters.NotificationsAdapter
import app.fyreplace.fyreplace.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.NotificationsViewModel
import app.fyreplace.protos.Notification
import app.fyreplace.protos.Notifications
import app.fyreplace.protos.post
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterIsInstance

@AndroidEntryPoint
class NotificationsFragment :
    ItemListFragment<Notification, Notifications, ItemHolder>(),
    ItemListAdapter.ItemClickListener<Notification>,
    MenuProvider {
    override val vm by activityViewModels<NotificationsViewModel>()
    private val cvm by activityViewModels<CentralViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.em.events.filterIsInstance<ActivityWasStoppedEvent>()
            .launchCollect(viewLifecycleOwner.lifecycleScope) { resetListing() }
        vm.em.events.filterIsInstance<NotificationWasCreatedEvent>()
            .launchCollect(viewLifecycleOwner.lifecycleScope) { refreshListing() }
    }

    override fun makeAdapter() = NotificationsAdapter(this)

    override fun onItemClick(item: Notification, position: Int) {
        val directions = when (item.targetCase) {
            Notification.TargetCase.USER ->
                NotificationsFragmentDirections.actionUser(profile = item.user.p)
            Notification.TargetCase.POST ->
                NotificationsFragmentDirections.actionPost(post = item.post.p)
                    .also { vm.em.post(PostWasSeenEvent(item.post)) }
            Notification.TargetCase.COMMENT ->
                NotificationsFragmentDirections.actionPost(post = post { id = item.comment.id }.p)
            else -> return
        }
        findNavController().navigate(directions)
    }

    override fun onItemLongClick(item: Notification, position: Int) {
        if (item.isFlag && cvm.currentUser.value?.profile?.isAdmin == true) {
            showSelectionAlert(null, R.array.notifications_item_choices) {
                launch {
                    vm.absolve(item)
                    vm.em.post(NotificationWasDeletedEvent(item))
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_notifications, menu)
        vm.isEmpty.launchCollect(viewLifecycleOwner.lifecycleScope) {
            menu.findItem(R.id.clear)?.isEnabled = !it
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.clear -> showChoiceAlert(R.string.notifications_clear_title, null) {
                launch {
                    vm.clear()
                    refreshListing()
                }
            }
            else -> return false
        }

        return true
    }
}
