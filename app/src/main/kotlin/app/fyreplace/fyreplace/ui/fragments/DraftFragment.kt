package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.Button
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentDraftBinding
import app.fyreplace.fyreplace.ui.ImageSelector
import app.fyreplace.fyreplace.ui.ImageSelectorFactory
import app.fyreplace.fyreplace.ui.MainActivity
import app.fyreplace.fyreplace.ui.adapters.DraftAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.ui.views.TextInputConfig
import app.fyreplace.fyreplace.viewmodels.ArchiveChangeViewModel
import app.fyreplace.fyreplace.viewmodels.DraftViewModel
import app.fyreplace.fyreplace.viewmodels.DraftViewModelFactory
import app.fyreplace.fyreplace.viewmodels.DraftsChangeViewModel
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.chapter
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status
import javax.inject.Inject

@AndroidEntryPoint
class DraftFragment :
    BaseFragment(R.layout.fragment_draft),
    MenuProvider,
    ImageSelector.Listener,
    ItemListAdapter.ItemClickListener<Chapter>,
    DraftAdapter.ChapterListener {
    @Inject
    lateinit var vmFactory: DraftViewModelFactory

    @Inject
    lateinit var imageSelectorFactory: ImageSelectorFactory

    override val rootView by lazy { if (this::bd.isInitialized) bd.root else null }
    private val vm by viewModels<DraftViewModel> {
        DraftViewModel.provideFactory(vmFactory, args.post.v)
    }
    private val icvm by activityViewModels<DraftsChangeViewModel>()
    private val pcvm by activityViewModels<ArchiveChangeViewModel>()
    private lateinit var bd: FragmentDraftBinding
    private lateinit var adapter: DraftAdapter
    private val args by navArgs<DraftFragmentArgs>()
    private val imageSelector by lazy { imageSelectorFactory.create(this, this, this, 0.5f) }
    private var currentChapterPosition = -1
    private val chapterTextMaxSize by lazy { requireContext().resources.getInteger(R.integer.chapter_text_max_size) }

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
        bd.recycler.setHasFixedSize(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = DraftAdapter(viewLifecycleOwner, vm.canAddChapter)
        bd.recycler.adapter = adapter
        launch {
            vm.retrieve(args.post.id)
            vm.post.launchCollect {
                icvm.update(args.position, it)
                (activity as MainActivity).setToolbarInfo(
                    getString(R.string.draft_length, it.chapterCount)
                )
            }
            adapter.setOnClickListener(this@DraftFragment)
            adapter.setChapterListener(this@DraftFragment)
            adapter.addAll(vm.post.value.chaptersList)
        }
    }

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.INVALID_ARGUMENT -> when (error.description) {
            "chapter_empty" -> R.string.draft_error_chapter_empty_title to R.string.draft_error_chapter_empty_message
            "post_empty" -> R.string.draft_error_post_empty_title to R.string.draft_error_post_empty_message
            else -> R.string.draft_error_chapter_too_long_title to R.string.draft_error_chapter_too_long_message
        }
        else -> super.getFailureTexts(error)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_draft, menu)
        val publish = menu.findItem(R.id.publish)
        val publishButton = publish.actionView.findViewById<Button>(R.id.button)
        publishButton.setOnClickListener { onMenuItemSelected(publish) }
        vm.canPublish.launchCollect(lifecycleScope, publishButton::setEnabled)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.publish -> showSelectionAlert(
                R.string.draft_publish_title,
                R.array.draft_publish_choices
            ) { launch { publish(it != 0) } }

            R.id.delete -> showChoiceAlert(
                R.string.draft_delete_title,
                R.string.draft_delete_message
            ) { launch { delete() } }

            else -> return false
        }

        return true
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
            updateTextChapter(item, new = false)
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
                DraftAdapter.TYPE_TEXT -> updateTextChapter(
                    Chapter.getDefaultInstance(),
                    new = true
                )
                DraftAdapter.TYPE_IMAGE -> updateImageChapter(new = true)
            }
        }
    }

    private suspend fun publish(anonymously: Boolean) {
        vm.publish(anonymously)
        icvm.delete(args.position)
        pcvm.add(args.position, vm.makePreview())
        findNavController().navigateUp()
    }

    private suspend fun delete() {
        vm.delete()
        icvm.delete(args.position)
        findNavController().navigateUp()
    }

    private fun deleteChapter(position: Int) {
        launch {
            vm.deleteChapter(position)
            adapter.remove(position)
        }
    }

    private fun updateTextChapter(chapter: Chapter, new: Boolean) {
        val title = if (new) R.string.draft_add_text else R.string.draft_update_text
        showTextInputAlert(
            title,
            TextInputConfig(INPUT_TYPE, chapterTextMaxSize, chapter.text)
        ) {
            launch {
                vm.updateChapterText(currentChapterPosition, it)
                adapter.update(currentChapterPosition, chapter { text = it })
            }
        }
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

    private companion object {
        const val INPUT_TYPE =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    }
}
