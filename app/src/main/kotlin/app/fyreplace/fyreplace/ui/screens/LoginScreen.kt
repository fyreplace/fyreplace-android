package app.fyreplace.fyreplace.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.ContextResourceResolver
import app.fyreplace.fyreplace.extensions.activity
import app.fyreplace.fyreplace.fakes.FakeApiResolver
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeSecretsHandler
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.account.EnvironmentSelector
import app.fyreplace.fyreplace.ui.views.account.Logo
import app.fyreplace.fyreplace.ui.views.account.RandomCodeInput
import app.fyreplace.fyreplace.ui.views.account.SubmitOrCancel
import app.fyreplace.fyreplace.viewmodels.screens.EnvironmentViewModel
import app.fyreplace.fyreplace.viewmodels.screens.LoginViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@ExperimentalSharedTransitionApi
@Composable
fun SharedTransitionScope.LoginScreen(
    visibilityScope: AnimatedVisibilityScope,
    deepLinkRandomCode: String? = null,
    viewModel: LoginViewModel = hiltViewModel(),
    environmentViewModel: EnvironmentViewModel = hiltViewModel(requireNotNull(activity))
) {
    val environment by environmentViewModel.environment.collectAsStateWithLifecycle()
    val identifier by viewModel.identifier.collectAsStateWithLifecycle()
    val randomCode by viewModel.randomCode.collectAsStateWithLifecycle()
    val isWaitingForRandomCode by viewModel.isWaitingForRandomCode.collectAsStateWithLifecycle()
    val canSubmit by viewModel.canSubmit.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    val identifierFocus = remember(::FocusRequester)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.spacing_huge))
            .imePadding()
    ) {
        Logo(
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "image"),
                visibilityScope
            )
        )

        Box(
            modifier = Modifier
                .padding(bottom = dimensionResource(R.dimen.spacing_medium))
                .sharedElement(
                    rememberSharedContentState(key = "environment-selector"),
                    visibilityScope
                )
        ) {
            EnvironmentSelector(
                environment = environment,
                onEnvironmentChange = environmentViewModel::updateEnvironment,
                enabled = !isWaitingForRandomCode
            )
        }

        val fieldModifier = Modifier
            .widthIn(
                dimensionResource(R.dimen.form_min_width),
                dimensionResource(R.dimen.form_max_width)
            )
            .fillMaxWidth()
            .padding(bottom = dimensionResource(R.dimen.spacing_huge))

        OutlinedTextField(
            value = identifier,
            label = { Text(stringResource(R.string.login_identifier)) },
            placeholder = { Text(stringResource(R.string.login_identifier_placeholder)) },
            enabled = !isWaitingForRandomCode,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboard?.hide()

                if (canSubmit) {
                    viewModel.submit()
                }
            }),
            onValueChange = viewModel::updateIdentifier,
            modifier = fieldModifier
                .focusRequester(identifierFocus)
                .sharedElement(
                    rememberSharedContentState(key = "first-field"),
                    visibilityScope
                )
        )

        AnimatedVisibility(isWaitingForRandomCode) {
            RandomCodeInput(
                randomCode = randomCode,
                onValueChange = viewModel::updateRandomCode,
                modifier = fieldModifier
            )
        }

        SubmitOrCancel(
            submitLabel = stringResource(R.string.login_submit),
            canSubmit = canSubmit,
            canCancel = isWaitingForRandomCode,
            isLoading = isLoading,
            onSubmit = viewModel::submit,
            onCancel = viewModel::cancel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.spacing_huge))
                .sharedElement(
                    rememberSharedContentState(key = "submit"),
                    visibilityScope
                )
        )

        LaunchedEffect(deepLinkRandomCode) {
            delay(100.milliseconds)
            deepLinkRandomCode?.let(viewModel::trySubmitDeepLinkRandomCode)
        }

        LaunchedEffect(Unit) {
            delay(100.milliseconds)

            if (identifier.isBlank()) {
                identifierFocus.requestFocus()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LoginScreenPreview() {
    AppTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                val storeResolver = FakeStoreResolver()
                LoginScreen(
                    visibilityScope = this,
                    viewModel = LoginViewModel(
                        state = SavedStateHandle(),
                        eventBus = FakeEventBus(),
                        resourceResolver = ContextResourceResolver(LocalContext.current),
                        storeResolver = storeResolver,
                        secretsHandler = FakeSecretsHandler(),
                        apiResolver = FakeApiResolver()
                    ),
                    environmentViewModel = EnvironmentViewModel(
                        storeResolver = storeResolver
                    )
                )
            }
        }
    }
}
