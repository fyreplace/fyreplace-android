package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentLoginBinding
import app.fyreplace.fyreplace.ui.TitleChoosing
import app.fyreplace.fyreplace.ui.hideSoftKeyboard
import app.fyreplace.fyreplace.viewmodels.LoginViewModel
import io.grpc.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment(R.layout.fragment_login), TitleChoosing {
    override val rootView by lazy { bd.root }
    private val vm by viewModel<LoginViewModel>()
    private val args by navArgs<LoginFragmentArgs>()
    private lateinit var bd: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentLoginBinding.bind(it)
        bd.lifecycleOwner = viewLifecycleOwner
        bd.vm = vm
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.setIsRegistering(args.isRegistering)
        bd.button.setOnClickListener { registerOrLogin() }
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
            else -> R.string.error_validation_title to R.string.error_validation_message
        }
        else -> null
    }

    override fun getTitle() =
        if (args.isRegistering) R.string.settings_register else R.string.settings_login

    private fun registerOrLogin() = launch {
        view?.hideSoftKeyboard()

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
