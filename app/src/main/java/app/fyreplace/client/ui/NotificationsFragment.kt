package app.fyreplace.client.ui

import app.fyreplace.client.R
import app.fyreplace.client.viewmodels.NotificationsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationsFragment : BaseFragment(R.layout.fragment_notifications) {
    private val vm by viewModel<NotificationsViewModel>()
}
