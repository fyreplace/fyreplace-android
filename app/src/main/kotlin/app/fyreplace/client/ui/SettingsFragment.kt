package app.fyreplace.client.ui

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import app.fyreplace.client.R
import app.fyreplace.client.viewmodels.CentralViewModel
import app.fyreplace.client.viewmodels.SettingsViewModel
import app.fyreplace.client.views.ImagePreference
import io.grpc.Status
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), FailureHandler {
    override val preferences by inject<SharedPreferences>()
    private val cvm by sharedViewModel<CentralViewModel>()
    private val vm by viewModel<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTransitions()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cvm.user.launchCollect { user ->
            findPreference<ImagePreference>("avatar")?.run {
                imageUrl = user?.avatar?.url
                title = user?.username ?: getString(R.string.settings_username)
                summary = user?.run {
                    getString(
                        R.string.settings_date_joined,
                        DATE_FORMATTER.format(Date(dateJoined.seconds * 1000))
                    )
                } ?: getString(R.string.settings_not_joined)
            }

            findPreference<Preference>("email")?.summary =
                user?.email ?: getString(R.string.settings_email)

            for (preference in listOf("register", "login")) {
                findPreference<Preference>(preference)?.isVisible = user == null
            }

            for (preference in listOf("email", "logout", "delete")) {
                findPreference<Preference>(preference)?.isVisible = user != null
            }
        }

        for (pair in setOf(
            findPreference<Preference>("register") to true,
            findPreference<Preference>("login") to false
        )) {
            pair.first?.setOnPreferenceClickListener {
                val directions = SettingsFragmentDirections
                    .toFragmentLogin(isRegistering = pair.second)
                findNavController().navigate(directions)
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("logout")?.setOnPreferenceClickListener {
            logout()
            return@setOnPreferenceClickListener true
        }

        findPreference<Preference>("delete")?.setOnPreferenceClickListener {
            val alert = AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_account_deletion_title)
                .setMessage(R.string.settings_account_deletion_message)
                .setPositiveButton(R.string.settings_delete) { _, _ -> delete() }
                .setNegativeButton(R.string.cancel, null)
                .show()
            val button = alert.getButton(DialogInterface.BUTTON_POSITIVE)
            button.isEnabled = false

            launch {
                alert.setOnDismissListener { cancel() }

                for (i in 3 downTo 1) {
                    button.text = getString(R.string.settings_delete_countdown, i)
                    delay(1000)
                }

                button.setText(R.string.settings_delete)
                button.isEnabled = true
            }

            return@setOnPreferenceClickListener true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SettingsDataStore()
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
    }

    override fun onFailure(failure: Throwable) {
        val error = Status.fromThrowable(failure)
        val (title, message) = when (error.code) {
            Status.Code.ALREADY_EXISTS -> R.string.login_error_existing_email_title to R.string.login_error_existing_email_message
            Status.Code.INVALID_ARGUMENT -> R.string.login_error_invalid_email_title to R.string.login_error_invalid_email_message
            else -> return super.onFailure(failure)
        }

        showBasicAlert(title, message, error = true)
    }

    private fun logout() = launch { vm.logout() }

    private fun delete() = launch {
        vm.delete()
        showBasicAlert(
            R.string.settings_account_deletion_success_title,
            R.string.settings_account_deletion_success_message
        )
    }

    private companion object {
        val DATE_FORMATTER: DateFormat = SimpleDateFormat.getDateInstance()
    }

    private inner class SettingsDataStore : PreferenceDataStore() {
        override fun putString(key: String, value: String?) = launch {
            vm.sendEmailUpdateEmail(value ?: "")
            showBasicAlert(
                R.string.settings_email_change_success_title,
                R.string.settings_email_change_success_message
            )
        }.let {}
    }
}
