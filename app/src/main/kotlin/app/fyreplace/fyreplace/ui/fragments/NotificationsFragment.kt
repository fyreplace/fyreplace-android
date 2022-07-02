package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentNotificationsBinding
import app.fyreplace.fyreplace.viewmodels.NotificationsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : BaseFragment(R.layout.fragment_notifications) {
    override val rootView by lazy { if (this::bd.isInitialized) bd.root else null }
    private val vm by viewModels<NotificationsViewModel>()
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
