package app.fyreplace.fyreplace.legacy.ui.fragments

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.viewmodels.BioViewModel
import app.fyreplace.fyreplace.legacy.viewmodels.BioViewModelFactory
import app.fyreplace.fyreplace.legacy.viewmodels.CentralViewModel
import com.squareup.wire.GrpcException
import com.squareup.wire.GrpcStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BioFragment : TextInputFragment() {
    @Inject
    lateinit var vmFactory: BioViewModelFactory

    override val destinationId = R.id.fragment_bio
    override val vm by viewModels<BioViewModel> {
        BioViewModel.provideFactory(vmFactory, cvm.currentUser.value?.bio ?: "")
    }
    override val maxLength by lazy { resources.getInteger(R.integer.bio_max_size) }
    override val allowEmpty = true
    private val cvm by activityViewModels<CentralViewModel>()

    override fun getFailureTexts(error: GrpcException) = when (error.grpcStatus) {
        GrpcStatus.INVALID_ARGUMENT -> R.string.settings_error_bio_too_long_title to R.string.settings_error_bio_too_long_message
        else -> super.getFailureTexts(error)
    }

    override suspend fun onDone() {
        vm.update(vm.text.value)
        cvm.retrieveMe()
        super.onDone()
    }
}
