package app.fyreplace.client.ui

import android.animation.LayoutTransition
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import app.fyreplace.client.R
import app.fyreplace.client.databinding.FragmentSettingsBinding
import app.fyreplace.client.viewmodels.SettingsViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {
    private val vm by viewModel<SettingsViewModel>()
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
            Glide.with(this)
                .load(it?.avatar?.url)
                .placeholder(R.drawable.ic_baseline_account_circle)
                .circleCrop()
                .transition(withCrossFade())
                .into(bd.avatar)
            bd.username.text = it?.username ?: getString(R.string.settings_username)
        }

        for (pair in setOf(bd.register to true, bd.login to false)) {
            pair.first.setOnClickListener {
                val directions = SettingsFragmentDirections
                    .toFragmentLogin(isRegistering = pair.second)
                findNavController().navigate(directions)
            }
        }

        bd.logout.setOnClickListener { logout() }

        bd.delete.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_account_deletion_title)
                .setMessage(R.string.settings_account_deletion_message)
                .setPositiveButton(R.string.settings_delete) { _, _ -> delete() }
                .setNeutralButton(R.string.cancel, null)
                .show()
            val button = alert.getButton(DialogInterface.BUTTON_POSITIVE)
            button.isEnabled = false

            launch {
                for (i in 3 downTo 1) {
                    button.text = getString(R.string.settings_delete_countdown, i)
                    delay(1000)
                }

                button.setText(R.string.settings_delete)
                button.isEnabled = true
            }
        }
    }

    private fun logout() = launch {
        bd.buttons.layoutTransition = LayoutTransition()
        vm.logout()
    }

    private fun delete() = launch {
        vm.delete()
        showBasicAlert(
            R.string.settings_account_deletion_success_title,
            R.string.settings_account_deletion_success_message
        )
    }
}
