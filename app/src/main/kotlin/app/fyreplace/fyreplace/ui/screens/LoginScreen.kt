package app.fyreplace.fyreplace.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.ContextResourceResolver
import app.fyreplace.fyreplace.extensions.activity
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeSecretsHandler
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.fakes.FakeTokensEndpointApi
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.SmallCircularProgressIndicator
import app.fyreplace.fyreplace.ui.views.settings.EnvironmentSelector
import app.fyreplace.fyreplace.viewmodels.screens.EnvironmentViewModel
import app.fyreplace.fyreplace.viewmodels.screens.LoginViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@ExperimentalSharedTransitionApi
@Composable
fun SharedTransitionScope.LoginScreen(
    visibilityScope: AnimatedVisibilityScope,
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
            .padding(horizontal = dimensionResource(R.dimen.spacing_medium))
            .imePadding()
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.logo),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = dimensionResource(R.dimen.spacing_large))
                .size(dimensionResource(R.dimen.logo_size))
                .sharedElement(
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

        val textFieldModifier = Modifier
            .widthIn(
                dimensionResource(R.dimen.form_min_width),
                dimensionResource(R.dimen.form_max_width)
            )
            .padding(bottom = dimensionResource(R.dimen.spacing_large))

        OutlinedTextField(
            value = identifier,
            label = { Text(stringResource(R.string.login_identifier)) },
            placeholder = { Text(stringResource(R.string.login_identifier_placeholder)) },
            enabled = !isWaitingForRandomCode,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboard?.hide()

                if (canSubmit) {
                    viewModel.submit()
                }
            }),
            onValueChange = viewModel::updateIdentifier,
            modifier = textFieldModifier
                .focusRequester(identifierFocus)
                .sharedElement(
                    rememberSharedContentState(key = "first-field"),
                    visibilityScope
                )
        )

        AnimatedVisibility(isWaitingForRandomCode) {
            OutlinedTextField(
                value = randomCode,
                label = { Text(stringResource(R.string.account_random_code)) },
                placeholder = { Text(stringResource(R.string.account_random_code_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                onValueChange = viewModel::updateRandomCode,
                modifier = textFieldModifier
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.spacing_large))
                .sharedElement(
                    rememberSharedContentState(key = "submit"),
                    visibilityScope
                )
        ) {
            Button(
                enabled = canSubmit,
                onClick = viewModel::submit
            ) {
                Box {
                    Text(
                        stringResource(R.string.login_submit),
                        color = if (isLoading) Color.Transparent else Color.Unspecified,
                        maxLines = 1
                    )

                    if (isLoading) {
                        SmallCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }

            AnimatedVisibility(
                visible = isWaitingForRandomCode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FilledTonalButton(
                    enabled = !isLoading,
                    onClick = viewModel::cancel
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            }

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
                        tokensEndpoint = FakeTokensEndpointApi()
                    ),
                    environmentViewModel = EnvironmentViewModel(
                        storeResolver = storeResolver
                    )
                )
            }
        }
    }
}
