package app.fyreplace.fyreplace.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.core.view.iterator
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.CommentWasDeletedEvent
import app.fyreplace.fyreplace.events.PostWasDeletedEvent
import app.fyreplace.fyreplace.events.PostWasSubscribedToEvent
import app.fyreplace.fyreplace.events.PostWasUnsubscribedFromEvent
import app.fyreplace.fyreplace.extensions.formatDate
import app.fyreplace.fyreplace.extensions.isAdmin
import app.fyreplace.fyreplace.extensions.mainActivity
import app.fyreplace.fyreplace.extensions.makePreview
import app.fyreplace.fyreplace.extensions.makeShareIntent
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.PrimaryActionStyle
import app.fyreplace.fyreplace.ui.adapters.PostAdapter
import app.fyreplace.fyreplace.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.ItemRandomAccessListViewModel
import app.fyreplace.fyreplace.viewmodels.PostViewModel
import app.fyreplace.fyreplace.viewmodels.PostViewModelFactory
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Comments
import app.fyreplace.protos.Profile
import com.google.android.material.snackbar.Snackbar
import com.google.protobuf.ByteString
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class PostFragment :
    ItemRandomAccessListFragment<Comment, Comments, ItemHolder>(),
    PostAdapter.CommentListener,
    MenuProvider {
    @Inject
    lateinit var vmFactory: PostViewModelFactory

    override val vm by viewModels<PostViewModel> {
        PostViewModel.provideFactory(vmFactory, args.post.v)
    }
    override val recyclerView get() = bd.recyclerView
    override val hasPrimaryActionDuplicate = true
    val args by navArgs<PostFragmentArgs>()
    private val cvm by activityViewModels<CentralViewModel>()
    private var errored = false
    private var isScrolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (vm.selectedComment.value == null) {
            vm.setSelectedComment(args.commentPosition.takeIf { it >= 0 })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val postAdapter = adapter as PostAdapter
        vm.post.launchCollect(
            viewLifecycleOwner.lifecycleScope,
            action = postAdapter::updatePost
        )
        vm.selectedComment.launchCollect(
            viewLifecycleOwner.lifecycleScope,
            action = postAdapter::updateSelectedComment
        )
    }

    override fun onStart() {
        super.onStart()

        if (vm.shouldScrollToComment) {
            vm.setShouldScrollToComment(false)
            showComment(vm.scrollTargetPosition)
        }
    }

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.NOT_FOUND -> R.string.post_error_not_found_title to R.string.post_error_not_found_message
        Status.Code.PERMISSION_DENIED -> R.string.error_permission_title to R.string.error_permission_message
        Status.Code.INVALID_ARGUMENT -> when (error.description) {
            "invalid_uuid" -> R.string.post_error_not_found_title to R.string.post_error_not_found_message
            else -> super.getFailureTexts(error)
        }

        else -> super.getFailureTexts(error)
    }

    override fun onFailure(failure: Throwable) {
        if (errored) {
            return
        }

        super.onFailure(failure)
        val error = Status.fromThrowable(failure)

        if (error.code in setOf(Status.Code.INVALID_ARGUMENT, Status.Code.NOT_FOUND)) {
            errored = true
            findNavController().navigateUp()
        }
    }

    override fun makeAdapter() =
        PostAdapter(this, cvm.isAuthenticated, vm.post.value, this)

    override fun onFetchedItems(position: Int, items: List<Comment>) {
        if (adapter.totalSize == 0 && vm.post.value.commentsRead in 1 until vm.totalSize) {
            vm.setShouldScrollToComment(true)
        }

        super.onFetchedItems(position, items)
    }

    override suspend fun startFetchingData() {
        if (args.post.isPreview || args.post.chapterCount == 0) {
            vm.retrieve(args.post.id)
        }

        super.startFetchingData()
    }

    override fun onCommentDisplayed(
        view: View,
        position: Int,
        comment: Comment?,
        highlighted: Boolean
    ) {
        val scrollTargetPosition = vm.scrollTargetPosition

        if (highlighted) {
            vm.acknowledgeComment(position)
        }

        when {
            !vm.shouldScrollToComment || position >= vm.totalSize -> return
            position == scrollTargetPosition -> {
                if (comment != null) {
                    vm.setShouldScrollToComment(false)
                }

                showComment(scrollTargetPosition)
            }

            position % ItemRandomAccessListViewModel.PAGE_SIZE == 0 ->
                showComment(scrollTargetPosition)
        }
    }

    override fun onCommentProfileClicked(view: View, position: Int, profile: Profile) {
        val directions = PostFragmentDirections.toUser(profile = profile.p)
        findNavController().navigate(directions)
    }

    override fun onCommentOptionsClicked(view: View, position: Int, comment: Comment) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.item_comment)
        val shareIntent = context?.makeShareIntent("p", vm.post.value.id, position)
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

        val user = cvm.currentUser.value
        val canDelete = user?.profile.isAdmin || comment.author.id == user?.profile?.id
        popup.menu.findItem(R.id.report).isVisible = !canDelete
        popup.menu.findItem(R.id.delete).isVisible = canDelete
        popup.show()
    }

    override fun onNewCommentClicked() {
        val directions = PostFragmentDirections.toComment(
            postId = vm.post.value.id,
            text = vm.savedComment
        )
        findNavController().navigate(directions)
    }

    override fun getPrimaryActionText() = R.string.post_primary_action_comment

    override fun getPrimaryActionIcon() = R.drawable.ic_baseline_comment

    override fun getPrimaryActionStyle() =
        if (cvm.isAuthenticated.value) super.getPrimaryActionStyle()
        else PrimaryActionStyle.NONE

    override fun onPrimaryAction() = onNewCommentClicked()

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_post, menu)
        vm.post.launchCollect(viewLifecycleOwner.lifecycleScope) { post ->
            mainActivity.setToolbarInfo(
                if (!post.isAnonymous) post.author else Profile.getDefaultInstance(),
                post.dateCreated.formatDate()
            )
            val shareIntent = context?.makeShareIntent("p", post.id)
            menu.findItem(R.id.share)?.run { intent = Intent.createChooser(shareIntent, title) }
        }

        vm.subscribed.launchCollect(viewLifecycleOwner.lifecycleScope) { subscribed ->
            menu.findItem(R.id.subscribe)?.isVisible = !subscribed
            menu.findItem(R.id.unsubscribe)?.isVisible = subscribed
        }

        cvm.currentUser.combine(vm.post) { u, p ->
            val currentUserOwnsPost = p.hasAuthor() && p.author.id == u?.profile?.id
            return@combine currentUserOwnsPost || u?.profile.isAdmin
        }.launchCollect(viewLifecycleOwner.lifecycleScope) { canDeletePost ->
            menu.findItem(R.id.report)?.isVisible = !canDeletePost
            menu.findItem(R.id.delete)?.isVisible = canDeletePost
        }

        cvm.isAuthenticated.launchCollect(viewLifecycleOwner.lifecycleScope) { authenticated ->
            for (item in menu) {
                item.isEnabled = authenticated || item.itemId == R.id.share
            }
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

    fun tryShowComment(postId: ByteString, position: Int): Boolean {
        if (postId != vm.post.value.id) {
            return false
        }

        vm.setSelectedComment(position)
        showComment(position)
        return true
    }

    fun tryHandleCommentCreation(comment: Comment, postId: ByteString): Boolean {
        if (postId != vm.post.value.id) {
            return false
        }

        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val position = layoutManager.findLastVisibleCommentPosition()

        if (position == vm.totalSize - 1) {
            showComment(position)
        } else {
            Snackbar.make(
                bd.root,
                getString(R.string.post_snackbar_comment_created, comment.author.username),
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.post_snackbar_comment_created_action) { showComment(vm.totalSize - 1) }
                .show()
        }

        return true
    }

    fun showUnreadComments() {
        if (vm.post.value.commentsRead > 0) {
            showComment(vm.post.value.commentsRead)
        }
    }

    private fun updateSubscription(subscribed: Boolean) {
        launch {
            vm.updateSubscription(subscribed)
            val preview = vm.post.value.makePreview()
            vm.em.post(
                if (subscribed) PostWasSubscribedToEvent(preview)
                else PostWasUnsubscribedFromEvent(preview)
            )
        }
    }

    private suspend fun report() {
        vm.report()
        showBasicSnackbar(R.string.post_report_success_message)
    }

    private suspend fun delete() {
        vm.delete()
        vm.em.post(PostWasDeletedEvent(vm.post.value))
        findNavController().navigateUp()
    }

    private suspend fun reportComment(comment: Comment) {
        vm.reportComment(comment.id)
        showBasicSnackbar(R.string.post_comment_report_success_message)
    }

    private suspend fun deleteComment(position: Int, comment: Comment) {
        vm.deleteComment(comment.id)
        vm.makeDeletedComment(position)?.let {
            vm.em.post(CommentWasDeletedEvent(it, vm.post.value.id))
        }
    }

    private fun showComment(position: Int) {
        if (isScrolling) {
            return
        }

        val viewPosition = position + adapter.offset
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        isScrolling = true

        launch {
            delay(300)
            isScrolling = false
            layoutManager.scrollToPositionWithOffset(viewPosition, 0)
        }
    }

    private fun LinearLayoutManager.findLastVisibleCommentPosition() = min(
        findLastVisibleItemPosition() - adapter.offset,
        vm.totalSize - 1
    )
}