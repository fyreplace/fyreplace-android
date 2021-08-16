package app.fyreplace.client.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.fyreplace.client.R
import app.fyreplace.client.databinding.FragmentLoginBinding
import app.fyreplace.client.viewmodels.LoginViewModel
import io.grpc.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment(R.layout.fragment_login), TitleChoosing {
    private val vm by viewModel<LoginViewModel>()
    private lateinit var bd: FragmentLoginBinding
    private val args by navArgs<LoginFragmentArgs>()

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
        bd.button.setOnClickListener {
            launch {
                if (args.isRegistering) {
                    vm.register()
                    showBasicAlert(R.string.login_register_title, R.string.login_register_message)
                } else {
                    vm.login()
                }

                findNavController().navigateUp()
            }
        }
    }

    override fun onFailure(failure: Throwable) {
        val error = Status.fromThrowable(failure)
        val (title, message) = when (error.code) {
            Status.Code.ALREADY_EXISTS -> when (error.description) {
                "email_taken" -> R.string.login_error_existing_email_title to R.string.login_error_existing_email_message
                else -> R.string.login_error_existing_username_title to R.string.login_error_existing_username_message
            }
            Status.Code.PERMISSION_DENIED -> when (error.description) {
                "caller_pending" -> R.string.login_error_caller_pending_title to R.string.login_error_caller_pending_message
                "caller_deleted" -> R.string.login_error_caller_deleted_title to R.string.login_error_caller_deleted_message
                "caller_banned" -> R.string.login_error_caller_banned_title to R.string.login_error_caller_banned_message
                else -> R.string.error_permission_title to R.string.error_permission_message
            }
            Status.Code.INVALID_ARGUMENT -> when (error.description) {
                "invalid_credentials" -> R.string.login_error_invalid_credentials_title to R.string.login_error_invalid_credentials_message
                "invalid_email" -> R.string.login_error_invalid_email_title to R.string.login_error_invalid_email_message
                "invalid_username" -> R.string.login_error_invalid_username_title to R.string.login_error_invalid_username_message
                "invalid_password" -> R.string.login_error_invalid_password_title to R.string.login_error_invalid_password_message
                else -> R.string.error_validation_title to R.string.error_validation_message
            }
            else -> return super.onFailure(failure)
        }

        showBasicAlert(title, message, error = true)
    }

    override fun getTitle() =
        if (args.isRegistering) R.string.login_register else R.string.login_login
}
