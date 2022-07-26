package app.fyreplace.fyreplace.ui.fragments

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.adapters.BlockedUsersAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.BlockedUsersChangeViewModel
import app.fyreplace.fyreplace.viewmodels.BlockedUsersViewModel
import app.fyreplace.protos.Profile
import app.fyreplace.protos.Profiles
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedUsersFragment :
    ItemListFragment<Profile, Profiles, BlockedUsersAdapter.Holder>(),
    ItemListAdapter.ItemClickListener<Profile>,
    BlockedUsersAdapter.UnblockListener {
    override val icvm by activityViewModels<BlockedUsersChangeViewModel>()
    override val vm by viewModels<BlockedUsersViewModel>()
    override val emptyText by lazy { getString(R.string.blocked_users_empty) }

    override fun makeAdapter() = BlockedUsersAdapter(this, this)

    override fun onItemClick(item: Profile, position: Int) {
        val directions =
            BlockedUsersFragmentDirections.actionUser(profile = item.p, position = position)
        findNavController().navigate(directions)
    }

    override fun onUnblock(profile: Profile, position: Int) =
        showChoiceAlert(R.string.user_unblock_title, null) {
            launch {
                vm.unblock(profile.id)
                icvm.delete(position)
            }
        }
}
