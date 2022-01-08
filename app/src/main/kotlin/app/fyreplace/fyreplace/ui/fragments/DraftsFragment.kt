package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentDraftsBinding
import app.fyreplace.fyreplace.viewmodels.DraftsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DraftsFragment : BaseFragment(R.layout.fragment_drafts) {
    override val rootView by lazy { bd.root }
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
