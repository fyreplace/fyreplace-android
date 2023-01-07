package app.fyreplace.fyreplace.ui.fragments

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.viewmodels.BioViewModel
import app.fyreplace.fyreplace.viewmodels.BioViewModelFactory
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status
import javax.inject.Inject

@AndroidEntryPoint
class BioFragment : TextInputFragment() {
    @Inject
    lateinit var vmFactory: BioViewModelFactory

    override val vm by viewModels<BioViewModel> {
        BioViewModel.provideFactory(vmFactory, cvm.currentUser.value?.bio ?: "")
    }
    override val maxLength by lazy { resources.getInteger(R.integer.bio_max_size) }
    override val allowEmpty = true
    private val cvm by activityViewModels<CentralViewModel>()

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.INVALID_ARGUMENT -> R.string.settings_error_bio_too_long_title to R.string.settings_error_bio_too_long_message
        else -> super.getFailureTexts(error)
    }

    override suspend fun onDone() {
        vm.update(vm.text.value)
        cvm.retrieveMe()
        super.onDone()
    }
}
