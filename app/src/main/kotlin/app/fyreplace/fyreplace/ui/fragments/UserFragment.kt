package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentUserBinding
import app.fyreplace.fyreplace.extensions.formatDate
import app.fyreplace.fyreplace.extensions.loadAvatar
import app.fyreplace.fyreplace.extensions.setupTransitions
import app.fyreplace.fyreplace.ui.FailureHandler
import app.fyreplace.fyreplace.viewmodels.BlockedUsersChangeViewModel
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.Sentence
import app.fyreplace.fyreplace.viewmodels.UserViewModel
import app.fyreplace.protos.Rank
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UserFragment : DialogFragment(), FailureHandler {
    override val rootView by lazy { if (this::bd.isInitialized) bd.root else null }
    private val cvm by sharedViewModel<CentralViewModel>()
    private val icvm by sharedViewModel<BlockedUsersChangeViewModel>()
    private val vm by viewModel<UserViewModel> { parametersOf(args.profile.v) }
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
        bd.toolbar.title = args.profile.username
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
                R.id.ban -> ban()
                else -> return@setOnMenuItemClickListener false
            }

            return@setOnMenuItemClickListener true
        }

        cvm.currentUser.combine(vm.blocked) { u, b -> (u?.profile?.id != args.profile.id) to b }
            .launchCollect { (isNotCurrentUser, blocked) ->
                bd.toolbar.menu.findItem(R.id.block).isVisible = !blocked && isNotCurrentUser
                bd.toolbar.menu.findItem(R.id.unblock).isVisible = blocked && isNotCurrentUser
            }

        cvm.currentUser.launchCollect {
            val isNotCurrentUser = it?.profile?.id != args.profile.id
            val currentRank = it?.profile?.rank ?: Rank.RANK_UNSPECIFIED
            bd.toolbar.menu.findItem(R.id.report).isVisible =
                args.profile.rank == currentRank && isNotCurrentUser
        }

        cvm.currentUser.combine(vm.banned) { currentUser, banned ->
            val isNotCurrentUser = currentUser?.profile?.id != args.profile.id
            val currentRank = currentUser?.profile?.rank ?: Rank.RANK_UNSPECIFIED
            return@combine args.profile.rank < currentRank && isNotCurrentUser && !banned
        }.launchCollect { canBan -> bd.toolbar.menu.findItem(R.id.ban).isVisible = canBan }
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
            icvm.add(args.position, args.profile.v)
        }
    }

    private fun unblock() = showChoiceAlert(R.string.user_unblock_title, null) {
        launch {
            vm.updateBlock(blocked = false)
            icvm.delete(args.position)
        }
    }

    private fun report() = showChoiceAlert(R.string.post_report_title, null) {
        launch {
            vm.report()
            showBasicSnackbar(R.string.user_report_success_message)
        }
    }

    private fun ban() = showSelectionAlert(
        R.string.user_ban_title,
        R.array.user_ban_choices
    ) { choice ->
        when (choice) {
            0 -> launch {
                vm.ban(Sentence.WEEK)
                finishBan()
            }
            1 -> launch {
                vm.ban(Sentence.MONTH)
                finishBan()
            }
            else -> showChoiceAlert(R.string.user_ban_permanently_title, null) {
                launch {
                    vm.ban(Sentence.PERMANENTLY)
                    finishBan()
                }
            }
        }
    }

    private suspend fun finishBan() {
        showBasicSnackbar(R.string.user_ban_success_message)
        bd.toolbar.menu.findItem(R.id.ban).isVisible = false
        icvm.update(args.position, args.profile.v.toBuilder().setIsBanned(true).build())
    }
}
