package app.fyreplace.fyreplace.ui.fragments

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
import androidx.core.content.edit
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.*
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.FailureHandler
import app.fyreplace.fyreplace.ui.ImageSelector
import app.fyreplace.fyreplace.ui.applySettings
import app.fyreplace.fyreplace.viewmodels.BlockedUsersChangeViewModel
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.SettingsViewModel
import app.fyreplace.fyreplace.views.ImagePreference
import com.bumptech.glide.Glide
import io.grpc.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class SettingsFragment : PreferenceFragmentCompat(), FailureHandler, ImageSelector.Listener {
    override lateinit var rootView: View
    override val preferences by inject<SharedPreferences>()
    private val cvm by sharedViewModel<CentralViewModel>()
    private val icvm by sharedViewModel<BlockedUsersChangeViewModel>()
    private val vm by viewModel<SettingsViewModel>()
    private val args by navArgs<SettingsFragmentArgs>()
    private val imageSelector by inject<ImageSelector<SettingsFragment>> { parametersOf(this, 1f) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.onCreate()
        setupTransitions()

        icvm.addedItems.launchCollect { cvm.addBlockedUser() }
        icvm.removedPositions.launchCollect { cvm.removeBlockedUser() }
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
        handleArgs()
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

            findPreference<ListPreference>("theme")?.run {
                val themeValues = resources.getStringArray(R.array.settings_theme_values)
                val themeNames = resources.getStringArray(R.array.settings_theme)
                val themeValue = preferences.getString(
                    "settings.theme",
                    getString(R.string.settings_theme_auto_value)
                )
                val themeIndex = max(themeValues.indexOf(themeValue), 0)
                value = themeValues[themeIndex]
                summary = themeNames[themeIndex]
            }

            for ((preference, needsUser) in mapOf(
                "register" to false,
                "login" to false,
                "password" to true,
                "email" to true,
                "bio" to true,
                "blocked_users" to true,
                "logout" to true,
                "delete" to true
            )) {
                findPreference<Preference>(preference)?.isVisible = (user != null) == needsUser
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
        Status.Code.ALREADY_EXISTS -> R.string.login_error_email_already_exists_title to R.string.login_error_email_already_exists_message
        Status.Code.INVALID_ARGUMENT -> when (error.description) {
            "invalid_email" -> R.string.login_error_invalid_email_title to R.string.login_error_invalid_email_message
            else -> R.string.settings_error_bio_too_long_title to R.string.settings_error_bio_too_long_message
        }
        else -> null
    }

    private fun handleArgs() {
        when (args.path) {
            "" -> return
            getString(R.string.link_path_account_confirm_activation) -> confirmActivation(args.token)
            getString(R.string.link_path_account_confirm_connection) -> confirmConnection(args.token)
            getString(R.string.link_path_user_confirm_email_update) -> confirmEmailUpdate(args.token)
            else -> showBasicAlert(
                R.string.settings_error_malformed_url_title,
                R.string.settings_error_malformed_url_message,
                error = true
            )
        }
    }

    private fun confirmActivation(token: String) = launch {
        vm.confirmActivation(token)
        showBasicSnackbar(R.string.settings_account_activated_message)
    }

    private fun confirmConnection(token: String) = launch {
        vm.confirmConnection(token)
    }

    private fun confirmEmailUpdate(token: String) = launch {
        vm.confirmEmailUpdate(token)
        cvm.retrieveMe()
        showBasicSnackbar(R.string.settings_user_email_changed_message)
    }

    private fun logout() = launch {
        vm.logout()
        clearGlideCache()
    }

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
                    preferences.edit { putString("settings.theme", value) }
                    val themeIndex = resources
                        .getStringArray(R.array.settings_theme_values)
                        .indexOf(value)
                    findPreference<ListPreference>("theme")?.summary =
                        resources.getStringArray(R.array.settings_theme)[themeIndex]
                    preferences.applySettings(requireContext())
                }
                else -> super.putString(key, value)
            }
        }.let {}
    }
}
