package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentDraftBinding
import app.fyreplace.fyreplace.events.*
import app.fyreplace.fyreplace.extensions.mainActivity
import app.fyreplace.fyreplace.extensions.makePreview
import app.fyreplace.fyreplace.ui.ImageSelector
import app.fyreplace.fyreplace.ui.ImageSelectorFactory
import app.fyreplace.fyreplace.ui.PrimaryActionProvider
import app.fyreplace.fyreplace.ui.adapters.DraftAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.DraftViewModel
import app.fyreplace.fyreplace.viewmodels.DraftViewModelFactory
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.chapter
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import javax.inject.Inject

@AndroidEntryPoint
class DraftFragment :
    BaseFragment(R.layout.fragment_draft),
    ImageSelector.Listener,
    ItemListAdapter.ItemClickListener<Chapter>,
    DraftAdapter.ChapterListener,
    MenuProvider,
    PrimaryActionProvider {
    @Inject
    lateinit var em: EventsManager

    @Inject
    lateinit var vmFactory: DraftViewModelFactory

    @Inject
    lateinit var imageSelectorFactory: ImageSelectorFactory

    override val rootView get() = if (::bd.isInitialized) bd.root else null
    override val vm by viewModels<DraftViewModel> {
        DraftViewModel.provideFactory(vmFactory, args.post.v)
    }
    val args by navArgs<DraftFragmentArgs>()
    private lateinit var bd: FragmentDraftBinding
    private lateinit var adapter: DraftAdapter
    private val imageSelector by lazy { imageSelectorFactory.create(this, this, this, 512 * 1024) }
    private var currentChapterPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.onCreate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentDraftBinding.bind(it)
        bd.lifecycleOwner = viewLifecycleOwner
        bd.recyclerView.setHasFixedSize(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = DraftAdapter(viewLifecycleOwner, vm.canAddChapter, this, this)
        bd.recyclerView.adapter = adapter
        launch {
            vm.retrieve(args.post.id)
            vm.post.launchCollect(viewLifecycleOwner.lifecycleScope) {
                em.post(DraftWasUpdatedEvent(it))
                mainActivity.setToolbarInfo(getString(R.string.draft_length, it.chapterCount))
            }
            adapter.addAll(vm.post.value.chaptersList)
            em.events.filterIsInstance<ChapterWasUpdatedEvent>()
                .filter { it.postId == vm.post.value.id }
                .launchCollect(viewLifecycleOwner.lifecycleScope) {
                    adapter.update(it.position, chapter { text = it.text })
                }
        }
    }

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.INVALID_ARGUMENT -> when (error.description) {
            "payload_too_large" -> R.string.image_error_file_size_title to R.string.image_error_file_size_message
            "chapter_empty" -> R.string.draft_error_chapter_empty_title to R.string.draft_error_chapter_empty_message
            "post_empty" -> R.string.draft_error_post_empty_title to R.string.draft_error_post_empty_message
            else -> R.string.error_validation_title to R.string.error_validation_message
        }
        else -> super.getFailureTexts(error)
    }

    override suspend fun onImage(image: ByteArray) {
        val uploadedImage = vm.updateChapterImage(currentChapterPosition, image)
        adapter.update(currentChapterPosition, chapter { this.image = uploadedImage })
    }

    override suspend fun onImageRemoved() = deleteChapter(currentChapterPosition)

    override suspend fun onImageSelectionCancelled() {
        if (!vm.post.value.chaptersList[currentChapterPosition].hasImage()) {
            deleteChapter(currentChapterPosition)
        }
    }

    override fun onItemClick(item: Chapter, position: Int) {
        currentChapterPosition = position

        if (item.hasImage()) {
            updateImageChapter(new = false)
        } else {
            updateTextChapter(item)
        }
    }

    override fun onItemLongClick(item: Chapter, position: Int) {
        currentChapterPosition = position
        val choices = resources.getStringArray(R.array.draft_item_choices).toMutableList()
        var firstAction = { moveChapter(position, position - 1) }
        val secondAction = { moveChapter(position, position + 1) }

        if (position == vm.post.value.chapterCount - 1) {
            choices.removeAt(1)
        }

        if (position == 0) {
            choices.removeAt(0)
            firstAction = secondAction
        }

        showSelectionAlert(null, choices.toTypedArray()) { choice ->
            when (choice) {
                choices.size - 1 -> deleteChapter(position)
                choices.size - 2 -> onItemClick(item, position)
                0 -> firstAction()
                1 -> secondAction()
            }
        }
    }

    override fun onInsertChapter(position: Int, type: Int) {
        if (type == DraftAdapter.TYPE_BUTTONS) {
            return
        }

        launch {
            currentChapterPosition = position
            vm.createChapter()
            adapter.add(position, Chapter.getDefaultInstance())

            when (type) {
                DraftAdapter.TYPE_TEXT -> updateTextChapter(Chapter.getDefaultInstance())
                DraftAdapter.TYPE_IMAGE -> updateImageChapter(new = true)
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_draft, menu)
        vm.canPublish.launchCollect(viewLifecycleOwner.lifecycleScope) {
            mainActivity.refreshPrimaryAction()
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.delete -> showChoiceAlert(
                R.string.draft_delete_title,
                R.string.draft_delete_message
            ) { launch { delete() } }

            else -> return false
        }

        return true
    }

    override fun getPrimaryActionText() =
        if (vm.canPublish.value) R.string.draft_primary_action_publish else null

    override fun getPrimaryActionIcon() =
        if (vm.canPublish.value) R.drawable.ic_baseline_check else null

    override fun onPrimaryAction() = showSelectionAlert(
        R.string.draft_publish_title,
        R.array.draft_publish_choices
    ) { launch { publish(it != 0) } }

    private suspend fun publish(anonymously: Boolean) {
        vm.publish(anonymously)
        em.post(DraftWasPublishedEvent(vm.post.value.makePreview(anonymously)))
        findNavController().navigateUp()
    }

    private suspend fun delete() {
        vm.delete()
        em.post(DraftWasDeletedEvent(vm.post.value))
        findNavController().navigateUp()
    }

    private fun deleteChapter(position: Int) {
        launch {
            vm.deleteChapter(position)
            adapter.remove(position)
        }
    }

    private fun updateTextChapter(chapter: Chapter) {
        val directions = DraftFragmentDirections.toTextChapter(
            postId = vm.post.value.id,
            position = currentChapterPosition,
            text = chapter.text
        )
        findNavController().navigate(directions)
    }

    private fun updateImageChapter(new: Boolean) {
        val title = if (new) R.string.draft_add_image else R.string.draft_update_image
        imageSelector.showImageChooser(title, canRemove = !new)
    }

    private fun moveChapter(fromPosition: Int, toPosition: Int) {
        launch {
            vm.moveChapter(fromPosition, toPosition)
            adapter.add(toPosition, adapter.remove(fromPosition))
        }
    }
}
