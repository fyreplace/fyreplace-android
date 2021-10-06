package app.fyreplace.client.ui.fragments

import app.fyreplace.client.R
import app.fyreplace.client.viewmodels.FeedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedFragment : BaseFragment(R.layout.fragment_feed) {
    private val vm by viewModel<FeedViewModel>()
}
