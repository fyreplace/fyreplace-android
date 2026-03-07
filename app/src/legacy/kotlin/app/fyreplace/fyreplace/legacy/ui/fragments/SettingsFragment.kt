package app.fyreplace.fyreplace.legacy.ui.fragments

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
import app.fyreplace.fyreplace.legacy.extensions.setAppIcon
import app.fyreplace.fyreplace.legacy.extensions.applyTheme
import app.fyreplace.fyreplace.legacy.extensions.date
import app.fyreplace.fyreplace.legacy.extensions.getAppIcon
import app.fyreplace.fyreplace.legacy.extensions.setupTransitions
import app.fyreplace.fyreplace.legacy.ui.FailureHandler
import app.fyreplace.fyreplace.legacy.ui.ImageSelector
import app.fyreplace.fyreplace.legacy.ui.ImageSelectorFactory
import app.fyreplace.fyreplace.legacy.ui.preferences.AppIconPreference
import app.fyreplace.fyreplace.legacy.ui.preferences.AvatarPreference
import app.fyreplace.fyreplace.legacy.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.legacy.viewmodels.SettingsViewModel
import app.fyreplace.fyreplace.legacy.viewmodels.SettingsViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), FailureHandler, ImageSelector.Listener {
    @Inject
    lateinit var vmFactory: SettingsViewModelFactory

    @Inject
    lateinit var imageSelectorFactory: ImageSelectorFactory

    override lateinit var rootView: View
    private val cvm by activityViewModels<CentralViewModel>()
    private val vm by activityViewModels<SettingsViewModel> {
        SettingsViewModel.provideFactory(vmFactory, cvm.currentUser.value?.blockedUsers ?: 0)
    }
    private val imageSelector by lazy { imageSelectorFactory.create(this, this, this, 1024 * 1024) }

    @Suppress("SimplifyBooleanWithConstants")
    private val canChangeEnvironment: Boolean
        get() {
            val environment = preferences?.getString(
                "app.environment",
                getString(R.string.settings_environment_default_value)
            )

            @Suppress("KotlinConstantConditions")
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
            findPreference<AvatarPreference>("avatar")?.run {
                imageUrl = user?.profile?.avatar?.url
                title = user?.profile?.username ?: getString(R.string.settings_username)
                summary = user?.run {
                    getString(R.string.settings_date_joined, dateFormatter.format(dateJoined.date))
                } ?: getString(R.string.settings_has_not_joined)
                isEnabled = user != null
                setOnPreferenceClickListener {
                    imageSelector.showImageChooser(R.string.profile_avatar, canRemove = true)
                    return@setOnPreferenceClickListener true
                }
            }

            findPreference<Preference>("email")?.summary = user?.email

            findPreference<Preference>("bio")?.summary =
                user?.bio?.ifEmpty { getString(R.string.settings_bio_desc) }

            for ((preference, needsUser) in mapOf(
                "category_about" to true,
                "category_environment" to false,
                "register" to false,
                "login" to false,
                "email" to true,
                "bio" to true,
                "blocked_users" to true,
                "app_icon_normal" to true,
                "app_icon_alt" to true,
                "logout" to true,
                "delete" to true
            )) {
                findPreference<Preference>(preference)?.isVisible = (user != null) == needsUser
            }

            if (!canChangeEnvironment) {
                findPreference<Preference>("category_environment")?.isVisible = false
            }
        }

        vm.blockedUsers.launchCollect(viewLifecycleOwner.lifecycleScope) {
            findPreference<Preference>("blocked_users")?.summary =
                resources.getQuantityString(R.plurals.settings_blocked_users_desc, it, it)
        }

        for ((pref, registering) in setOf(
            findPreference<Preference>("register") to true,
            findPreference<Preference>("login") to false
        )) {
            pref?.setOnPreferenceClickListener {
                val directions = SettingsFragmentDirections.toLogin(isRegistering = registering)
                findNavController().navigate(directions)
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("bio")?.setOnPreferenceClickListener {
            val directions = SettingsFragmentDirections.toBio()
            findNavController().navigate(directions)
            return@setOnPreferenceClickListener true
        }

        findPreference<Preference>("blocked_users")?.setOnPreferenceClickListener {
            val directions = SettingsFragmentDirections.toBlockedUsers()
            findNavController().navigate(directions)
            return@setOnPreferenceClickListener true
        }

        val appIconNameToMipmap = mapOf(
            "app_icon_normal" to R.mipmap.ic_launcher,
            "app_icon_alt" to R.mipmap.ic_launcher_alt
        )

        for ((iconName, mipmap) in appIconNameToMipmap.entries) {
            findPreference<AppIconPreference>(iconName)?.run {
                if (requireContext().getAppIcon() == mipmap) {
                    setIcon(R.drawable.ic_baseline_check)
                }

                setOnPreferenceClickListener {
                    for (otherIconName in appIconNameToMipmap.keys.filter { n -> n != iconName }) {
                        findPreference<AppIconPreference>(otherIconName)?.icon = null
                    }

                    it.setIcon(R.drawable.ic_baseline_check)
                    requireContext().setAppIcon(mipmap)
                    return@setOnPreferenceClickListener true
                }
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
            else -> R.string.error_validation_title to R.string.error_validation_message
        }

        else -> super.getFailureTexts(error)
    }

    private fun logout() = launch { vm.logout() }

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
        showBasicAlert(
            R.string.settings_account_deletion_success_title,
            R.string.settings_account_deletion_success_message
        )
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

                "theme" -> {
                    preferences?.edit { putString("settings.theme", value) }
                    preferences?.applyTheme(requireContext())
                }

                "environment" -> {
                    preferences?.edit { putString("app.environment", value) }
                    activity?.run { startActivity(Intent.makeRestartActivityTask(componentName)) }
                }

                else -> super.putString(key, value)
            }
        }.let {}
    }
}
