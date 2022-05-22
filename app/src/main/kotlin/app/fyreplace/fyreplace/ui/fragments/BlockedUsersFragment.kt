package app.fyreplace.fyreplace.ui.fragments

import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.adapters.BlockedUsersAdapter
import app.fyreplace.fyreplace.viewmodels.BlockedUsersChangeViewModel
import app.fyreplace.fyreplace.viewmodels.BlockedUsersViewModel
import app.fyreplace.protos.Profile
import app.fyreplace.protos.Profiles
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BlockedUsersFragment : ItemListFragment<Profile, Profiles, BlockedUsersAdapter.Holder>() {
    override val icvm by sharedViewModel<BlockedUsersChangeViewModel>()
    override val vm by viewModel<BlockedUsersViewModel>()
    override val emptyText by lazy { getString(R.string.blocked_users_empty) }

    override fun makeAdapter() = BlockedUsersAdapter().apply {
        setOnClickListener { profile, position ->
            val directions =
                BlockedUsersFragmentDirections.actionUser(profile = profile, position = position)
            findNavController().navigate(directions)
        }

        setUnblockListener { profile, position ->
            showChoiceAlert(R.string.user_unblock_title, null) {
                launch {
                    vm.unblock(profile.id)
                    icvm.delete(position)
                }
            }
        }
    }
}
