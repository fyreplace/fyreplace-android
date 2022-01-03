package app.fyreplace.client.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import app.fyreplace.client.R
import app.fyreplace.client.databinding.FragmentFeedBinding
import app.fyreplace.client.viewmodels.FeedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedFragment : BaseFragment(R.layout.fragment_feed) {
    override val rootView get() = bd.root
    private val vm by viewModel<FeedViewModel>()
    private lateinit var bd: FragmentFeedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentFeedBinding.bind(it)
        bd.lifecycleOwner = viewLifecycleOwner
    }
}
