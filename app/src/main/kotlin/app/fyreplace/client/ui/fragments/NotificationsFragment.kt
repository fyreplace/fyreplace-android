package app.fyreplace.client.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import app.fyreplace.client.R
import app.fyreplace.client.databinding.FragmentNotificationsBinding
import app.fyreplace.client.viewmodels.NotificationsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationsFragment : BaseFragment(R.layout.fragment_notifications) {
    override val rootView get() = bd.root
    private val vm by viewModel<NotificationsViewModel>()
    private lateinit var bd: FragmentNotificationsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentNotificationsBinding.bind(it)
        bd.lifecycleOwner = viewLifecycleOwner
    }
}
