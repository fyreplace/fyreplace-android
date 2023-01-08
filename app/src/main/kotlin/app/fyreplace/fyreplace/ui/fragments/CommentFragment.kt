package app.fyreplace.fyreplace.ui.fragments

import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.CommentWasCreatedEvent
import app.fyreplace.fyreplace.events.CommentWasSavedEvent
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.CommentViewModel
import app.fyreplace.fyreplace.viewmodels.CommentViewModelFactory
import app.fyreplace.protos.comment
import com.google.protobuf.timestamp
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status
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

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.INVALID_ARGUMENT -> R.string.post_error_comment_too_long_title to R.string.post_error_comment_too_long_message
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
        em.post(CommentWasCreatedEvent(comment {
            id = commentId.id
            text = vm.text.value
            author = cvm.currentUser.value!!.profile
            dateCreated = timestamp { seconds = System.currentTimeMillis() / 1000 }
        }, args.postId, true))
        isDone = true
        super.onDone()
    }
}
