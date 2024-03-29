package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.UserWasUnblockedEvent
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.adapters.BlockedUsersAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.BlockedUsersViewModel
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.protos.Profile
import app.fyreplace.protos.Profiles
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedUsersFragment :
    ItemListFragment<Profile, Profiles, BlockedUsersAdapter.Holder>(),
    ItemListAdapter.ItemClickListener<Profile>,
    BlockedUsersAdapter.UnblockListener {
    override val vm by viewModels<BlockedUsersViewModel>()
    private val cvm by activityViewModels<CentralViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.addedPositions.launchCollect { cvm.addBlockedUser() }
        vm.removedPositions.launchCollect { cvm.removeBlockedUser() }
    }

    override fun makeAdapter() = BlockedUsersAdapter(this, this)

    override fun onItemClick(item: Profile, position: Int) {
        val directions =
            BlockedUsersFragmentDirections.actionUser(profile = item.p)
        findNavController().navigate(directions)
    }

    override fun onUnblock(profile: Profile) =
        showChoiceAlert(R.string.user_unblock_title, null) {
            launch {
                vm.unblock(profile.id)
                vm.em.post(UserWasUnblockedEvent(profile))
            }
        }
}
