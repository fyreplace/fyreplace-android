package app.fyreplace.fyreplace.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentLoginBinding
import app.fyreplace.fyreplace.extensions.browse
import app.fyreplace.fyreplace.extensions.hideSoftInput
import app.fyreplace.fyreplace.ui.TitleProvider
import app.fyreplace.fyreplace.viewmodels.LoginViewModel
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Status

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login), TitleProvider {
    override val rootView get() = if (::bd.isInitialized) bd.root else null
    override val vm by viewModels<LoginViewModel>()
    private val args by navArgs<LoginFragmentArgs>()
    private lateinit var bd: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentLoginBinding.bind(it).apply {
            lifecycleOwner = viewLifecycleOwner
            ui = this@LoginFragment
            vm = this@LoginFragment.vm
            val seed = ResourcesCompat.getColor(resources, R.color.seed, it.context.theme)
            logo.imageTintList =
                ColorStateList.valueOf(MaterialColors.harmonizeWithPrimary(it.context, seed))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.setIsRegistering(args.isRegistering)
    }

    override fun getFailureTexts(error: Status) = when (error.code) {
        Status.Code.NOT_FOUND -> R.string.login_error_email_not_found_title to R.string.login_error_email_not_found_message
        Status.Code.ALREADY_EXISTS -> when (error.description) {
            "email_taken" -> R.string.login_error_email_already_exists_title to R.string.login_error_email_already_exists_message
            else -> R.string.login_error_existing_username_title to R.string.login_error_existing_username_message
        }
        Status.Code.PERMISSION_DENIED -> when (error.description) {
            "caller_pending" -> R.string.login_error_caller_pending_title to R.string.login_error_caller_pending_message
            "caller_deleted" -> R.string.login_error_caller_deleted_title to R.string.login_error_caller_deleted_message
            "caller_banned" -> R.string.login_error_caller_banned_title to R.string.login_error_caller_banned_message
            "caller_banned_permanently" -> R.string.login_error_caller_banned_permanently_title to R.string.login_error_caller_banned_permanently_message
            "username_reserved" -> R.string.login_error_username_reserved_title to R.string.login_error_username_reserved_message
            else -> R.string.error_permission_title to R.string.error_permission_message
        }
        Status.Code.INVALID_ARGUMENT -> when (error.description) {
            "invalid_email" -> R.string.login_error_invalid_email_title to R.string.login_error_invalid_email_message
            "invalid_username" -> R.string.login_error_invalid_username_title to R.string.login_error_invalid_username_message
            "invalid_password" -> R.string.login_error_invalid_password_title to R.string.login_error_invalid_password_message
            else -> R.string.error_validation_title to R.string.error_validation_message
        }
        else -> super.getFailureTexts(error)
    }

    override fun onFailure(failure: Throwable) {
        val error = Status.fromThrowable(failure)

        if (error.code == Status.Code.CANCELLED) {
            val passwordLayout = layoutInflater.inflate(R.layout.login_password_input, null, false)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.login_password)
                .setView(passwordLayout)
                .setPositiveButton(R.string.ok) { _, _ ->
                    launch {
                        vm.login(passwordLayout.findViewById<EditText>(R.id.password).text.toString())
                        findNavController().navigateUp()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        } else {
            super.onFailure(failure)
        }
    }

    override fun getTitle() =
        if (args.isRegistering) R.string.settings_register else R.string.settings_login

    @Suppress("UNUSED_PARAMETER")
    fun onRegisterOrLoginClicked(view: View) {
        registerOrLogin()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onPrivacyPolicyClicked(view: View) = browse(R.string.legal_privacy_policy_url)

    @Suppress("UNUSED_PARAMETER")
    fun onTermsOfServiceClicked(view: View) = browse(R.string.legal_terms_of_service_url)

    private fun registerOrLogin() = launch {
        view?.hideSoftInput()

        if (args.isRegistering) {
            vm.register()
            showBasicAlert(R.string.login_register_title, R.string.login_register_message)
        } else {
            vm.login()
            showBasicAlert(R.string.login_login_title, R.string.login_login_message)
        }

        findNavController().navigateUp()
    }
}
