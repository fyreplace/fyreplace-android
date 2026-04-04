package app.fyreplace.fyreplace.legacy.ui.fragments

import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.events.CommentWasCreatedEvent
import app.fyreplace.fyreplace.legacy.events.CommentWasSavedEvent
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.legacy.viewmodels.CommentViewModel
import app.fyreplace.fyreplace.legacy.viewmodels.CommentViewModelFactory
import app.fyreplace.protos.Comment
import com.squareup.wire.GrpcException
import com.squareup.wire.GrpcStatus
import com.squareup.wire.Instant
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommentFragment : TextInputFragment() {
    @Inject
    lateinit var em: EventsManager

    @Inject
    lateinit var vmFactory: CommentViewModelFactory

    override val vm by viewModels<CommentViewModel> {
        CommentViewModel.provideFactory(vmFactory, args.postId, args.text)
    }
    override val maxLength by lazy { resources.getInteger(R.integer.comment_max_size) }
    override val allowEmpty = false
    private val cvm by activityViewModels<CentralViewModel>()
    private val args by navArgs<CommentFragmentArgs>()
    private var isDone = false

    override fun onStop() {
        if (!isDone) {
            em.post(CommentWasSavedEvent(vm.text.value))
        }

        super.onStop()
    }

    override fun getFailureTexts(error: GrpcException) = when (error.grpcStatus) {
        GrpcStatus.PERMISSION_DENIED -> when (error.grpcMessage) {
            "caller_blocked" -> R.string.comment_error_blocked_title to R.string.comment_error_blocked_message
            else -> R.string.comment_error_too_long_title to R.string.comment_error_too_long_message
        }

        GrpcStatus.INVALID_ARGUMENT ->
            if (vm.text.value.length > maxLength) R.string.comment_error_too_long_title to R.string.comment_error_too_long_message
            else R.string.error_validation_title to R.string.error_validation_message

        else -> super.getFailureTexts(error)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)
        val done = menu.findItem(R.id.done) ?: return
        done.setTitle(R.string.comment_menu_action_post)
        done.icon = null
    }

    override suspend fun onDone() {
        val commentId = vm.create()
        em.post(
            CommentWasCreatedEvent(
                Comment(
                    id = commentId.id,
                    text = vm.text.value,
                    author = cvm.currentUser.value!!.profile,
                    date_created = Instant.now()
                ), args.postId, true
            )
        )
        isDone = true
        super.onDone()
    }
}
