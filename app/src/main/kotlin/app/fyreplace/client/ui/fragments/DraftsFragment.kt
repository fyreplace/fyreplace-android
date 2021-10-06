package app.fyreplace.client.ui.fragments

import app.fyreplace.client.R
import app.fyreplace.client.viewmodels.DraftsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DraftsFragment : BaseFragment(R.layout.fragment_drafts) {
    private val vm by viewModel<DraftsViewModel>()
}
