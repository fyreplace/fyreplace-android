package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentNotificationsBinding
import app.fyreplace.fyreplace.viewmodels.NotificationsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationsFragment : BaseFragment(R.layout.fragment_notifications) {
    override val rootView by lazy { bd.root }
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
