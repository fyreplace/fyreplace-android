package app.fyreplace.fyreplace.ui.fragments

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.viewmodels.TextChapterViewModel
import app.fyreplace.fyreplace.viewmodels.TextChapterViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status
import javax.inject.Inject

@AndroidEntryPoint
class TextChapterFragment : TextInputFragment() {
    @Inject
    lateinit var vmFactory: TextChapterViewModelFactory

    override val vm by viewModels<TextChapterViewModel> {
        TextChapterViewModel.provideFactory(
            vmFactory,
            args.postId,
            args.position,
            args.text
        )
    }
    override val maxLength by lazy { resources.getInteger(R.integer.chapter_text_max_size) }
    override val allowEmpty = true
    private val args by navArgs<TextChapterFragmentArgs>()

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.INVALID_ARGUMENT -> R.string.draft_error_chapter_too_long_title to R.string.draft_error_chapter_too_long_message
        else -> super.getFailureTexts(error)
    }

    override suspend fun onDone() {
        vm.updateTextChapter()
        super.onDone()
    }
}
