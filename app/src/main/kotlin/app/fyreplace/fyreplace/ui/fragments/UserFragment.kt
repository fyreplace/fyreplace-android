package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentUserBinding
import app.fyreplace.fyreplace.grpc.formatDate
import app.fyreplace.fyreplace.ui.FailureHandler
import app.fyreplace.fyreplace.ui.getUsername
import app.fyreplace.fyreplace.ui.loadAvatar
import app.fyreplace.fyreplace.viewmodels.BlockedUsersChangeViewModel
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.UserViewModel
import app.fyreplace.protos.Rank
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UserFragment : DialogFragment(), FailureHandler {
    override val rootView by lazy { bd.root }
    private val cvm by sharedViewModel<CentralViewModel>()
    private val icvm by sharedViewModel<BlockedUsersChangeViewModel>()
    private val vm by viewModel<UserViewModel> { parametersOf(args.profile) }
    private val args by navArgs<UserFragmentArgs>()
    private lateinit var bd: FragmentUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTransitions()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog
        .Builder(requireContext())
        .setView(R.layout.fragment_user)
        .setPositiveButton(R.string.ok, null)
        .create()
        .apply { setOnShowListener { setup() } }

    private fun setup() {
        val container = dialog?.findViewById<View>(R.id.container) ?: return
        bd = FragmentUserBinding.bind(container)

        setupToolbar()
        setupMenu()
        setupContent()
    }

    private fun setupToolbar() {
        bd.toolbar.title = requireContext().getUsername(args.profile)
        bd.toolbar.subtitle = when (args.profile.rank) {
            Rank.RANK_SUPERUSER -> getString(R.string.user_rank_superuser)
            Rank.RANK_STAFF -> getString(R.string.user_rank_staff)
            else -> null
        }
    }

    private fun setupMenu() {
        bd.toolbar.inflateMenu(R.menu.fragment_user)
        bd.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.block -> block()
                R.id.unblock -> unblock()
                R.id.report -> report()
                else -> return@setOnMenuItemClickListener false
            }

            return@setOnMenuItemClickListener true
        }

        if (args.profile.rank > Rank.RANK_CITIZEN) {
            bd.toolbar.menu.findItem(R.id.report).isVisible = false
        } else cvm.currentUser.launchCollect {
            bd.toolbar.menu.findItem(R.id.report).isVisible = it?.profile?.id != args.profile.id
        }

        cvm.currentUser.combine(vm.blocked) { u, b -> (u?.profile?.id != args.profile.id) to b }
            .launchCollect { (isNotCurrentUser, blocked) ->
                bd.toolbar.menu.findItem(R.id.block).isVisible = !blocked && isNotCurrentUser
                bd.toolbar.menu.findItem(R.id.unblock).isVisible = blocked && isNotCurrentUser
            }
    }

    private fun setupContent() {
        Glide.with(this).loadAvatar(args.profile.avatar.url).into(bd.avatar)

        vm.user.filterNotNull().launchCollect { user ->
            bd.dateJoined.isVisible = true
            bd.dateJoined.text = getString(R.string.user_date_joined, user.dateJoined.formatDate())
            bd.bio.isVisible = user.bio.isNotBlank()
            bd.bio.text = user.bio
        }

        launch { vm.retrieve(args.profile.id) }
    }

    private fun block() = showChoiceAlert(R.string.user_block_title, null) {
        launch {
            vm.updateBlock(blocked = true)

            if (args.position != -1) {
                icvm.add(args.position, args.profile)
            }
        }
    }

    private fun unblock() = showChoiceAlert(R.string.user_unblock_title, null) {
        launch {
            vm.updateBlock(blocked = false)

            if (args.position != -1) {
                icvm.delete(args.position)
            }
        }
    }

    private fun report() = showChoiceAlert(R.string.post_report_title, null) {
        launch {
            vm.report()
            showBasicSnackbar(R.string.user_report_success_message)
        }
    }
}
