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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.views.SmallCircularProgressIndicator
import app.fyreplace.fyreplace.ui.views.settings.EnvironmentSelector
import app.fyreplace.fyreplace.viewmodels.screens.LoginViewModel

@ExperimentalSharedTransitionApi
@Composable
fun SharedTransitionScope.LoginScreen(visibilityScope: AnimatedVisibilityScope) {
    val viewModel = hiltViewModel<LoginViewModel>()
    val identifier by viewModel.identifier.collectAsStateWithLifecycle()
    val canSubmit by viewModel.canSubmit.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    val identifierFocus = FocusRequester()

    fun submit() {
        keyboard?.hide()

        if (canSubmit) {
            viewModel.sendEmail()
        }
    }

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
            value = identifier,
            label = { Text(text = stringResource(R.string.login_identifier)) },
            placeholder = { Text(text = stringResource(R.string.login_identifier_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            onValueChange = viewModel::updateIdentifier,
            modifier = Modifier
                .focusRequester(identifierFocus)
                .widthIn(
                    integerResource(R.integer.form_min_width).dp,
                    integerResource(R.integer.form_max_width).dp
                )
                .padding(bottom = 32.dp)
                .sharedElement(
                    rememberSharedContentState(key = "first-field"),
                    visibilityScope
                )
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
                modifier = Modifier.padding(bottom = 32.dp)
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
        }

        LaunchedEffect(true) {
            if (identifier.isBlank()) {
                identifierFocus.requestFocus()
            }
        }
    }
}
