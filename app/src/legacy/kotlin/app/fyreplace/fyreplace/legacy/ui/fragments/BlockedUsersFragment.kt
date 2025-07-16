package app.fyreplace.fyreplace.legacy.ui.fragments

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.events.UserWasUnblockedEvent
import app.fyreplace.fyreplace.legacy.grpc.p
import app.fyreplace.fyreplace.legacy.ui.adapters.BlockedUsersAdapter
import app.fyreplace.fyreplace.legacy.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.legacy.viewmodels.BlockedUsersViewModel
import app.fyreplace.protos.Profile
import app.fyreplace.protos.Profiles
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedUsersFragment :
    ItemListFragment<Profile, Profiles, BlockedUsersAdapter.Holder>(),
    ItemListAdapter.ItemClickListener<Profile>,
    BlockedUsersAdapter.UnblockListener {
    override val vm by viewModels<BlockedUsersViewModel>()

    override fun makeAdapter() = BlockedUsersAdapter(this, this)

    override fun onItemClick(item: Profile, position: Int) {
        val directions =
            BlockedUsersFragmentDirections.toUser(profile = item.p)
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
