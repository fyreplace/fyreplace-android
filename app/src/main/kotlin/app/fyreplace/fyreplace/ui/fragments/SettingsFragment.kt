package app.fyreplace.fyreplace.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import app.fyreplace.fyreplace.BuildConfig
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.applySettings
import app.fyreplace.fyreplace.extensions.setupTransitions
import app.fyreplace.fyreplace.ui.FailureHandler
import app.fyreplace.fyreplace.ui.ImageSelector
import app.fyreplace.fyreplace.ui.ImageSelectorFactory
import app.fyreplace.fyreplace.ui.preferences.BioPreference
import app.fyreplace.fyreplace.ui.preferences.ImagePreference
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.SettingsViewModel
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), FailureHandler, ImageSelector.Listener {
    @Inject
    lateinit var imageSelectorFactory: ImageSelectorFactory

    override lateinit var rootView: View
    private val cvm by activityViewModels<CentralViewModel>()
    private val vm by activityViewModels<SettingsViewModel>()
    private val imageSelector by lazy { imageSelectorFactory.create(this, this, this, 1024 * 1024) }
    private val canChangeEnvironment: Boolean
        get() {
            val environment = preferences?.getString(
                "app.environment",
                getString(R.string.settings_environment_default_value)
            )

            return (environment != null && environment != getString(R.string.settings_environment_default_value))
                    || BuildConfig.VERSION_CODE % 10 == 0
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTransitions()
        super.onCreate(savedInstanceState)
        imageSelector.onCreate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        postponeEnterTransition()
        rootView = super.onCreateView(inflater, container, savedInstanceState)
            .apply { doOnPreDraw { startPostponedEnterTransition() } }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cvm.currentUser.launchCollect(viewLifecycleOwner.lifecycleScope) { user ->
            findPreference<ImagePreference>("avatar")?.run {
                imageUrl = user?.profile?.avatar?.url
                title = user?.profile?.username ?: getString(R.string.settings_username)
                summary = user?.run {
                    getString(
                        R.string.settings_date_joined,
                        dateFormatter.format(Date(dateJoined.seconds * 1000))
                    )
                } ?: getString(R.string.settings_has_not_joined)
                isEnabled = user != null
                setOnPreferenceClickListener {
                    imageSelector.showImageChooser(R.string.profile_avatar, canRemove = true)
                    return@setOnPreferenceClickListener true
                }
            }

            findPreference<Preference>("email")?.run { summary = user?.email }

            findPreference<BioPreference>("bio")?.run {
                summary = user?.bio?.ifEmpty { getString(R.string.settings_bio_desc) }
                setInitialText(user?.bio ?: "")
            }

            for ((preference, needsUser) in mapOf(
                "category_information" to true,
                "category_environment" to false,
                "register" to false,
                "login" to false,
                "email" to true,
                "bio" to true,
                "blocked_users" to true,
                "privacy_policy" to true,
                "terms_of_service" to true,
                "logout" to true,
                "delete" to true
            )) {
                findPreference<Preference>(preference)?.isVisible = (user != null) == needsUser
            }

            if (!canChangeEnvironment) {
                findPreference<Preference>("category_environment")?.isVisible = false
            }
        }

        cvm.blockedUsers.launchCollect(viewLifecycleOwner.lifecycleScope) {
            findPreference<Preference>("blocked_users")?.summary =
                resources.getQuantityString(R.plurals.settings_blocked_users_desc, it, it)
        }

        for ((pref, registering) in setOf(
            findPreference<Preference>("register") to true,
            findPreference<Preference>("login") to false
        )) {
            pref?.setOnPreferenceClickListener {
                val directions = SettingsFragmentDirections
                    .toFragmentLogin(isRegistering = registering)
                findNavController().navigate(directions)
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("blocked_users")?.run {
            setOnPreferenceClickListener {
                findNavController().navigate(SettingsFragmentDirections.actionBlockedUsers())
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
        setPreferencesFromResource(R.xml.preferences_settings, rootKey)
    }

    override suspend fun onImage(image: ByteArray) {
        cvm.setAvatar(vm.updateAvatar(image))
        cvm.retrieveMe()
    }

    override suspend fun onImageRemoved() {
        vm.updateAvatar(null)
        cvm.retrieveMe()
    }

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.ALREADY_EXISTS -> R.string.login_error_email_already_exists_title to R.string.login_error_email_already_exists_message
        Status.Code.INVALID_ARGUMENT -> when (error.description) {
            "payload_too_large" -> R.string.image_error_file_size_title to R.string.image_error_file_size_message
            "invalid_email" -> R.string.login_error_invalid_email_title to R.string.login_error_invalid_email_message
            else -> R.string.settings_error_bio_too_long_title to R.string.settings_error_bio_too_long_message
        }
        else -> super.getFailureTexts(error)
    }

    private fun logout() = launch {
        vm.logout()
        clearGlideCache()
    }

    private fun startDelete() {
        val alert = MaterialAlertDialogBuilder(requireContext())
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
        clearGlideCache()
        showBasicAlert(
            R.string.settings_account_deletion_success_title,
            R.string.settings_account_deletion_success_message
        )
    }

    private suspend fun clearGlideCache() {
        withContext(Dispatchers.Main.immediate) { Glide.get(requireContext()).clearMemory() }
        withContext(Dispatchers.IO) { Glide.get(requireContext()).clearDiskCache() }
    }

    private companion object {
        val dateFormatter: DateFormat = SimpleDateFormat.getDateInstance()
    }

    private inner class SettingsDataStore : PreferenceDataStore() {
        override fun getString(key: String?, defValue: String?) = when (key) {
            "theme" -> preferences?.getString(
                "settings.theme",
                getString(R.string.settings_theme_auto_value)
            )
            "environment" -> preferences?.getString(
                "app.environment",
                getString(R.string.settings_environment_default_value)
            )
            else -> super.getString(key, defValue)
        }

        override fun putString(key: String, value: String?) = launch {
            when (key) {
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
                "theme" -> {
                    preferences?.edit { putString("settings.theme", value) }
                    preferences?.applySettings(requireContext())
                }
                "environment" -> {
                    preferences?.edit { putString("app.environment", value) }
                    activity?.run {
                        val intent = Intent.makeRestartActivityTask(intent.component)
                        startActivity(intent)
                    }
                }
                else -> super.putString(key, value)
            }
        }.let {}
    }
}
