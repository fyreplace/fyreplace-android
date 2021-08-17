package app.fyreplace.client.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import app.fyreplace.client.R
import app.fyreplace.client.databinding.FragmentSettingsBinding
import app.fyreplace.client.viewmodels.CentralViewModel
import app.fyreplace.client.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {
    private val vm by viewModel<SettingsViewModel>()
    private val cvm by sharedViewModel<CentralViewModel>()
    private lateinit var bd: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentSettingsBinding.bind(it)
        bd.lifecycleOwner = viewLifecycleOwner
        bd.cvm = cvm
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cvm.user.launchCollect {
            bd.username.text = it?.username ?: getString(R.string.settings_username)
        }

        for (pair in setOf(bd.register to true, bd.login to false)) {
            pair.first.setOnClickListener {
                val directions = SettingsFragmentDirections
                    .toFragmentLogin(isRegistering = pair.second)
                findNavController().navigate(directions)
            }
        }

        bd.logout.setOnClickListener { launch { vm.logout() } }
    }
}
