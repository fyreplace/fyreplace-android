package app.fyreplace.fyreplace.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.formatDate
import app.fyreplace.fyreplace.extensions.makeShareIntent
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.MainActivity
import app.fyreplace.fyreplace.ui.adapters.ItemHolder
import app.fyreplace.fyreplace.ui.adapters.PostAdapter
import app.fyreplace.fyreplace.viewmodels.*
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Comments
import app.fyreplace.protos.Profile
import app.fyreplace.protos.Rank
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@AndroidEntryPoint
class PostFragment :
    ItemRandomAccessListFragment<Comment, Comments, ItemHolder>(),
    MenuProvider,
    PostAdapter.CommentListener {
    @Inject
    lateinit var vmFactory: PostViewModelFactory

    override val vm by viewModels<PostViewModel> {
        PostViewModel.provideFactory(vmFactory, args.post.v)
    }
    private val cvm by activityViewModels<CentralViewModel>()
    private val icvm by activityViewModels<ArchiveChangeViewModel>()
    private val args by navArgs<PostFragmentArgs>()
    private var errored = false
    private val commentPosition get() = args.commentPosition.takeIf { it >= 0 }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val postAdapter = adapter as PostAdapter
        vm.post.launchCollect(viewLifecycleOwner.lifecycleScope, postAdapter::updatePost)

        if (commentPosition == null) {
            vm.setScrolledToComment()
        }

        if (args.post.isPreview || args.post.chapterCount == 0) {
            launch { vm.retrieve(args.post.id) }
        }
    }

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.INVALID_ARGUMENT, Status.Code.NOT_FOUND -> R.string.post_error_not_found_title to R.string.post_error_not_found_message
        else -> super.getFailureTexts(error)
    }

    override fun onFailure(failure: Throwable) {
        if (errored) {
            return
        }

        errored = true
        super.onFailure(failure)
        val error = Status.fromThrowable(failure)

        if (error.code in setOf(Status.Code.INVALID_ARGUMENT, Status.Code.NOT_FOUND)) {
            findNavController().navigateUp()
        }
    }

    override fun makeAdapter() = PostAdapter(vm.post.value, commentPosition).apply {
        setCommentListener(this@PostFragment)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_post, menu)
        vm.post.launchCollect(viewLifecycleOwner.lifecycleScope) { post ->
            (activity as MainActivity).setToolbarInfo(
                if (!post.isAnonymous) post.author else Profile.getDefaultInstance(),
                post.dateCreated.formatDate()
            )
            val shareIntent = context?.makeShareIntent("p", post.id)
            menu.findItem(R.id.share).run { intent = Intent.createChooser(shareIntent, title) }
        }

        vm.subscribed.launchCollect(viewLifecycleOwner.lifecycleScope) { subscribed ->
            menu.findItem(R.id.subscribe).isVisible = !subscribed
            menu.findItem(R.id.unsubscribe).isVisible = subscribed
        }

        cvm.currentUser.combine(vm.post) { u, p ->
            val currentUserOwnsPost = p.hasAuthor() && p.author.id == u?.profile?.id
            val currentUserIsAdmin = (u?.profile?.rank ?: Rank.RANK_CITIZEN) > Rank.RANK_CITIZEN
            return@combine currentUserOwnsPost || currentUserIsAdmin
        }.launchCollect(viewLifecycleOwner.lifecycleScope) { canDeletePost ->
            menu.findItem(R.id.report).isVisible = !canDeletePost
            menu.findItem(R.id.delete).isVisible = canDeletePost
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.subscribe -> updateSubscription(true)
            R.id.unsubscribe -> updateSubscription(false)
            R.id.report -> showChoiceAlert(
                R.string.post_report_title,
                null
            ) { launch { report() } }
            R.id.delete -> showChoiceAlert(
                R.string.post_delete_title,
                R.string.post_delete_message
            ) { launch { delete() } }
            else -> return false
        }

        return true
    }

    override fun onCommentDisplayed(view: View, position: Int, comment: Comment?) {
        val viewPosition = (commentPosition ?: return) + 1
        val layoutManager = bd.recycler.layoutManager as LinearLayoutManager

        if (!vm.shouldScrollToComment || viewPosition >= adapter.itemCount) {
            return
        }

        fun scheduleScroll() {
            launch {
                delay(300)
                layoutManager.scrollToPositionWithOffset(viewPosition, 0)
            }
        }

        if (position == commentPosition && comment != null) {
            vm.setScrolledToComment()
            scheduleScroll()
        } else if (position == commentPosition || position % ItemRandomAccessListViewModel.PAGE_SIZE == 0) {
            scheduleScroll()
        }
    }

    override fun onCommentProfileClicked(view: View, position: Int, profile: Profile) {
        val directions = PostFragmentDirections.actionUser(profile = profile.p)
        findNavController().navigate(directions)
    }

    override fun onCommentOptionsClicked(view: View, position: Int, comment: Comment) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.item_comment)
        val shareIntent = context?.makeShareIntent("p", comment.id, position)
        popup.menu.findItem(R.id.share).run { intent = Intent.createChooser(shareIntent, title) }
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.report -> showChoiceAlert(
                    R.string.post_comment_report_title,
                    null
                ) { launch { reportComment(comment) } }
                R.id.delete -> showChoiceAlert(
                    R.string.post_comment_delete_title,
                    R.string.post_comment_delete_message
                ) { launch { deleteComment(position, comment) } }
                else -> return@setOnMenuItemClickListener false
            }

            return@setOnMenuItemClickListener true
        }

        cvm.currentUser.launchCollect { user ->
            val currentUserIsAdmin = (user?.profile?.rank ?: Rank.RANK_CITIZEN) > Rank.RANK_CITIZEN
            val canDelete = currentUserIsAdmin || comment.author.id == user?.profile?.id
            popup.menu.findItem(R.id.report).isVisible = !canDelete
            popup.menu.findItem(R.id.delete).isVisible = canDelete
        }

        popup.show()
    }

    private fun updateSubscription(subscribed: Boolean) {
        launch {
            vm.updateSubscription(subscribed)

            when {
                args.position == -1 -> return@launch
                subscribed -> icvm.add(args.position, vm.post.value)
                else -> icvm.delete(args.position)
            }
        }
    }

    private suspend fun report() {
        vm.report()
        showBasicSnackbar(R.string.post_report_success_message)
    }

    private suspend fun delete() {
        vm.delete()

        if (args.position != -1) {
            icvm.delete(args.position)
        }

        findNavController().navigateUp()
    }

    private suspend fun reportComment(comment: Comment) {
        vm.reportComment(comment.id)
        showBasicSnackbar(R.string.post_comment_report_success_message)
    }

    private suspend fun deleteComment(position: Int, comment: Comment) {
        vm.deleteComment(position, comment.id)
        vm.makeDeletedComment(position)?.let { adapter.update(position, it) }
    }
}
