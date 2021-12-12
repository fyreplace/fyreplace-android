package app.fyreplace.client.ui.fragments

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import app.fyreplace.client.R
import app.fyreplace.client.ui.FailureHandler
import app.fyreplace.client.ui.ImageSelector
import app.fyreplace.client.viewmodels.CentralViewModel
import app.fyreplace.client.viewmodels.SettingsViewModel
import app.fyreplace.client.views.ImagePreference
import io.grpc.Status
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), FailureHandler, ImageSelector.Listener {
    override val preferences by inject<SharedPreferences>()
    private val cvm by sharedViewModel<CentralViewModel>()
    private val vm by viewModel<SettingsViewModel>()
    private val args by navArgs<SettingsFragmentArgs>()
    private val imageSelector by inject<ImageSelector<SettingsFragment>> {
        parametersOf(this, 1f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.onCreate()
        setupTransitions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postponeEnterTransition()
        return super.onCreateView(inflater, container, savedInstanceState)
            ?.apply { doOnPreDraw { startPostponedEnterTransition() } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleArgs()
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

                if (user != null) {
                    setOnPreferenceClickListener {
                        imageSelector.showImageChooser(
                            R.string.settings_avatar_desc,
                            canRemove = true
                        )
                        return@setOnPreferenceClickListener true
                    }
                } else {
                    onPreferenceClickListener = null
                }
            }

            findPreference<EditTextPreference>("password")?.run {
                setOnBindEditTextListener {
                    it.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    it.setText("")
                }
            }

            findPreference<EditTextPreference>("email")?.run {
                summary = user?.email
                setOnBindEditTextListener {
                    it.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    it.setText("")
                }
            }

            findPreference<EditTextPreference>("bio")?.run {
                summary = user?.bio?.ifEmpty { getString(R.string.settings_bio_desc) }
                setOnBindEditTextListener {
                    val maxSize = resources.getInteger(R.integer.settings_bio_max_size)
                    it.minLines = resources.getInteger(R.integer.settings_bio_min_lines)
                    it.gravity = Gravity.TOP or Gravity.START
                    it.filters = arrayOf(InputFilter.LengthFilter(maxSize))
                    it.setText(user?.bio)
                }
            }

            for (preference in listOf("register", "login")) {
                findPreference<Preference>(preference)?.isVisible = user == null
            }

            for (preference in listOf("password", "email", "bio", "logout", "delete")) {
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
            startDelete()
            return@setOnPreferenceClickListener true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SettingsDataStore()
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
    }

    override suspend fun onImage(image: ByteArray) {
        vm.updateAvatar(image)
        cvm.retrieveMe()
    }

    override suspend fun onImageRemoved() {
        vm.updateAvatar(null)
        cvm.retrieveMe()
    }

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.UNAUTHENTICATED -> when (error.description) {
            "timestamp_exceeded" -> R.string.settings_error_timestamp_exceeded_title to R.string.settings_error_timestamp_exceeded_message
            "invalid_token" -> R.string.settings_error_invalid_token_title to R.string.settings_error_invalid_token_message
            else -> R.string.error_authentication_title to R.string.error_authentication_message
        }
        Status.Code.PERMISSION_DENIED -> when (error.description) {
            "user_not_pending" -> R.string.settings_error_user_not_pending_title to R.string.settings_error_user_not_pending_message
            else -> R.string.error_permission_title to R.string.error_permission_message
        }
        Status.Code.ALREADY_EXISTS -> R.string.login_error_existing_email_title to R.string.login_error_existing_email_message
        Status.Code.INVALID_ARGUMENT -> when (error.description) {
            "invalid_email" -> R.string.login_error_invalid_email_title to R.string.login_error_invalid_email_message
            "invalid_password" -> R.string.login_error_invalid_password_title to R.string.login_error_invalid_password_message
            else -> R.string.settings_error_bio_too_long_title to R.string.settings_error_bio_too_long_message
        }
        else -> null
    }

    private fun handleArgs() {
        when (args.path) {
            "" -> return
            getString(R.string.link_path_account_confirm_account) -> confirmActivation(args.token)
            getString(R.string.link_path_user_confirm_email) -> confirmEmailUpdate(args.token)
            else -> showBasicAlert(
                R.string.settings_error_malformed_url_title,
                R.string.settings_error_malformed_url_message,
                error = true
            )
        }
    }

    private fun confirmActivation(token: String) = launch {
        vm.confirmActivation(token)
        showBasicAlert(
            R.string.settings_account_activated_title,
            R.string.settings_account_activated_message
        )
    }

    private fun confirmEmailUpdate(token: String) = launch {
        vm.confirmEmailUpdate(token)
        cvm.retrieveMe()
        showBasicAlert(
            R.string.settings_user_email_changed_title,
            R.string.settings_user_email_changed_message
        )
    }

    private fun logout() = launch { vm.logout() }

    private fun startDelete() {
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
    }

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
            when (key) {
                "password" -> {
                    vm.updatePassword(value.orEmpty())
                    showBasicAlert(
                        R.string.settings_password_change_success_title,
                        R.string.settings_password_change_success_message
                    )
                }
                "email" -> {
                    vm.sendEmailUpdateEmail(value.orEmpty())
                    showBasicAlert(
                        R.string.settings_email_change_success_title,
                        R.string.settings_email_change_success_message
                    )
                }
                "bio" -> {
                    vm.updateBio(value.orEmpty())
                    cvm.retrieveMe()
                }
                else -> super.putString(key, value)
            }
        }.let {}
    }
}
