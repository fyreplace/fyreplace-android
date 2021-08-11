package app.fyreplace.client.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.fyreplace.client.R
import app.fyreplace.client.databinding.FragmentSettingsBinding
import com.google.android.material.transition.MaterialSharedAxis

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {
    private lateinit var bd: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentSettingsBinding.bind(it)
        bd.lifecycleOwner = viewLifecycleOwner
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for (pair in setOf(bd.register to true, bd.login to false)) {
            pair.first.setOnClickListener {
                val directions = SettingsFragmentDirections
                    .toFragmentLogin(isRegistering = pair.second)
                findNavController().navigate(directions)
            }
        }
    }
}
