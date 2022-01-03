package app.fyreplace.client.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import app.fyreplace.client.R
import app.fyreplace.client.databinding.FragmentDraftsBinding
import app.fyreplace.client.viewmodels.DraftsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DraftsFragment : BaseFragment(R.layout.fragment_drafts) {
    override val rootView get() = bd.root
    private val vm by viewModel<DraftsViewModel>()
    private lateinit var bd: FragmentDraftsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentDraftsBinding.bind(it)
        bd.lifecycleOwner = viewLifecycleOwner
    }
}
