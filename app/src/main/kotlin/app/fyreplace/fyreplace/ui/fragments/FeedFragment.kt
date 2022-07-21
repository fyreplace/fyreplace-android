package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentFeedBinding
import app.fyreplace.fyreplace.viewmodels.FeedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : BaseFragment(R.layout.fragment_feed) {
    override val rootView by lazy { if (::bd.isInitialized) bd.root else null }
    private val vm by viewModels<FeedViewModel>()
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
