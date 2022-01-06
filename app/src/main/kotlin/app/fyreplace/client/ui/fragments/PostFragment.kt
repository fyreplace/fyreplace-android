package app.fyreplace.client.ui.fragments

import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.fyreplace.client.R
import app.fyreplace.client.databinding.FragmentPostBinding
import app.fyreplace.client.grpc.formatDate
import app.fyreplace.client.ui.adapters.PostAdapter
import app.fyreplace.client.viewmodels.CentralViewModel
import app.fyreplace.client.viewmodels.PostViewModel
import kotlinx.coroutines.flow.combine
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PostFragment : BaseFragment(R.layout.fragment_post) {
    override val rootView get() = bd.root
    private val cvm by sharedViewModel<CentralViewModel>()
    private val vm by viewModel<PostViewModel> { parametersOf(args.post) }
    private val args by navArgs<PostFragmentArgs>()
    private lateinit var bd: FragmentPostBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentPostBinding.bind(it)
        bd.lifecycleOwner = viewLifecycleOwner
        bd.recycler.setHasFixedSize(true)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PostAdapter(args.post)
        bd.recycler.adapter = adapter
        vm.post.launchCollect {
            adapter.updatePost(it)
            setToolbarInfo(
                if (it.isAnonymous) "" else it.author.username,
                it.dateCreated.formatDate(),
                if (it.isAnonymous) "" else it.author.avatar.url
            )
        }

        if (args.post.isPreview) {
            launch { vm.retrieve(args.post.id) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_post, menu)

        val postUri = "https://fyreplace.link/posts/${args.post.id}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = ClipDescription.MIMETYPE_TEXT_PLAIN
            putExtra(Intent.EXTRA_TEXT, Uri.parse(postUri))
        }
        menu.findItem(R.id.share).run { intent = Intent.createChooser(shareIntent, title) }

        vm.subscribed.launchCollect { subscribed ->
            menu.findItem(R.id.subscribe).isVisible = !subscribed
            menu.findItem(R.id.unsubscribe).isVisible = subscribed
        }

        cvm.user.combine(vm.post) { user, post -> post.hasAuthor() && post.author.id == user?.id }
            .launchCollect { userOwnsPost ->
                menu.findItem(R.id.report).isVisible = !userOwnsPost
                menu.findItem(R.id.delete).isVisible = userOwnsPost
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (super.onOptionsItemSelected(item)) {
            return true
        }

        when (item.itemId) {
            R.id.subscribe -> launch { vm.updateSubscription(true) }
            R.id.unsubscribe -> launch { vm.updateSubscription(false) }
            R.id.report -> showChoiceAlert(
                R.string.post_report_title,
                R.string.post_report_message
            ) { launch { report() } }
            R.id.delete -> showChoiceAlert(
                R.string.post_delete_title,
                R.string.post_delete_message
            ) { launch { delete() } }
            else -> return false
        }

        return true
    }

    private suspend fun report() {
        vm.report()
        showBasicSnackbar(R.string.post_report_success_message)
    }

    private suspend fun delete() {
        vm.delete()
        args.deletionNotifier.onDelete()
        findNavController().navigateUp()
    }
}
