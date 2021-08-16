package app.fyreplace.client.ui

import app.fyreplace.client.R
import app.fyreplace.client.viewmodels.ArchiveViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArchiveFragment : BaseFragment(R.layout.fragment_archive) {
    private val vm by viewModel<ArchiveViewModel>()
}
