package app.fyreplace.fyreplace.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.settings.EnvironmentSelector
import app.fyreplace.fyreplace.viewmodels.screens.RegisterViewModel

@ExperimentalSharedTransitionApi
@Composable
fun SharedTransitionScope.RegisterScreen(visibilityScope: AnimatedVisibilityScope) {
    val viewModel = hiltViewModel<RegisterViewModel>()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val canSubmit by viewModel.canSubmit.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    val emailFocus = FocusRequester()

    fun submit() {
        keyboard?.hide()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .imePadding()
            .testTag("screen:${Destination.REGISTER}")
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
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

        val baseModifier = Modifier
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
            EnvironmentSelector()
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
            keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
            onValueChange = viewModel::updateUsername,
            modifier = baseModifier
                .sharedElement(
                    rememberSharedContentState(key = "first-field"),
                    visibilityScope
                )
                .testTag("register:username")
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
            keyboardActions = KeyboardActions(onDone = { submit() }),
            onValueChange = viewModel::updateEmail,
            modifier = baseModifier
                .focusRequester(emailFocus)
                .testTag("register:email")
        )

        Box(
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "submit"),
                visibilityScope
            )
        ) {
            Button(
                enabled = canSubmit,
                onClick = ::submit,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .testTag("register:submit")
            ) {
                Text(stringResource(R.string.register_submit), maxLines = 1)
            }
        }
    }
}
