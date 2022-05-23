package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentDraftBinding
import app.fyreplace.fyreplace.ui.ImageSelector
import app.fyreplace.fyreplace.ui.adapters.DraftAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.ui.views.TextInputConfig
import app.fyreplace.fyreplace.viewmodels.DraftViewModel
import app.fyreplace.fyreplace.viewmodels.DraftsChangeViewModel
import app.fyreplace.protos.Chapter
import app.fyreplace.protos.chapter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DraftFragment :
    BaseFragment(R.layout.fragment_draft),
    ImageSelector.Listener,
    ItemListAdapter.ItemClickListener<Chapter>,
    DraftAdapter.ChapterListener {
    override val rootView by lazy { bd.root }
    private val vm by viewModel<DraftViewModel> { parametersOf(args.post) }
    private val icvm by sharedViewModel<DraftsChangeViewModel>()
    private lateinit var bd: FragmentDraftBinding
    private lateinit var adapter: DraftAdapter
    private val args by navArgs<DraftFragmentArgs>()
    private val imageSelector by inject<ImageSelector<DraftFragment>> { parametersOf(this, 0.5f) }
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
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = DraftAdapter(viewLifecycleOwner, vm.canAddChapter)
        bd.recycler.adapter = adapter
        launch {
            vm.retrieve(args.post.id)
            vm.post.launchCollect { icvm.update(args.position, it) }
            adapter.setOnClickListener(this@DraftFragment)
            adapter.setChapterChangeListener(this@DraftFragment)
            adapter.addAll(vm.post.value.chaptersList)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_draft, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (super.onOptionsItemSelected(item)) {
            return true
        }

        when (item.itemId) {
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

        showSelectionAlert(choices.toTypedArray()) { choice ->
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
