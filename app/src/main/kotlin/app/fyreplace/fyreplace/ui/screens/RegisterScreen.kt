package app.fyreplace.fyreplace.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.ContextResourceResolver
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.extensions.activity
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.settings.EnvironmentSelector
import app.fyreplace.fyreplace.viewmodels.screens.EnvironmentViewModel
import app.fyreplace.fyreplace.viewmodels.screens.RegisterViewModel

@ExperimentalSharedTransitionApi
@Composable
fun SharedTransitionScope.RegisterScreen(
    visibilityScope: AnimatedVisibilityScope,
    viewModel: RegisterViewModel = hiltViewModel(),
    environmentViewModel: EnvironmentViewModel = hiltViewModel(requireNotNull(activity))
) {
    val environment by environmentViewModel.environment.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val canSubmit by viewModel.canSubmit.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    val usernameFocus = remember(::FocusRequester)
    val emailFocus = remember(::FocusRequester)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .imePadding()
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.logo),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = 32.dp)
                .size(96.dp)
                .sharedElement(
                    rememberSharedContentState(key = "image"),
                    visibilityScope
                )
        )

        val textFieldModifier = Modifier
            .widthIn(
                integerResource(R.integer.form_min_width).dp,
                integerResource(R.integer.form_max_width).dp
            )
            .padding(bottom = 32.dp)

        Box(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .sharedElement(
                    rememberSharedContentState(key = "environment-selector"),
                    visibilityScope
                )
        ) {
            EnvironmentSelector(
                environment = environment,
                onEnvironmentChange = environmentViewModel::updateEnvironment
            )
        }

        OutlinedTextField(
            value = username,
            label = { Text(text = stringResource(R.string.register_username)) },
            placeholder = { Text(text = stringResource(R.string.register_username_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next
            ),
            onValueChange = viewModel::updateUsername,
            modifier = textFieldModifier
                .focusRequester(usernameFocus)
                .focusProperties { next = emailFocus }
                .sharedElement(
                    rememberSharedContentState(key = "first-field"),
                    visibilityScope
                )
        )

        OutlinedTextField(
            value = email,
            label = { Text(text = stringResource(R.string.register_email)) },
            placeholder = { Text(text = stringResource(R.string.register_email_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
            onValueChange = viewModel::updateEmail,
            modifier = textFieldModifier.focusRequester(emailFocus)
        )

        Box(
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "submit"),
                visibilityScope
            )
        ) {
            Button(
                enabled = canSubmit,
                onClick = {},
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(stringResource(R.string.register_submit), maxLines = 1)
            }
        }

        LaunchedEffect(true) {
            when {
                username.isBlank() -> usernameFocus.requestFocus()
                email.isBlank() -> emailFocus.requestFocus()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterScreenPreview() {
    AppTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                RegisterScreen(
                    visibilityScope = this,
                    viewModel = RegisterViewModel(
                        eventBus = EventBus(),
                        resourceResolver = ContextResourceResolver(LocalContext.current)
                    ),
                    environmentViewModel = EnvironmentViewModel(
                        storeResolver = FakeStoreResolver()
                    )
                )
            }
        }
    }
}
