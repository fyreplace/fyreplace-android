package app.fyreplace.fyreplace.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import kotlin.math.min

@ExperimentalSharedTransitionApi
@Composable
fun SharedTransitionScope.RegisterScreen(visibilityScope: AnimatedVisibilityScope) {
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

        var username by rememberSaveable { mutableStateOf("") }
        var email by rememberSaveable { mutableStateOf("") }
        val usernameMaxLength = integerResource(R.integer.username_max_length)
        val emailMaxLength = integerResource(R.integer.email_max_length)
        val keyboard = LocalSoftwareKeyboardController.current
        val emailFocus = FocusRequester()

        fun submit() {
            keyboard?.hide()
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
            onValueChange = { username = it.substring(0, min(usernameMaxLength, it.length)) },
            modifier = Modifier
                .widthIn(
                    integerResource(R.integer.form_min_width).dp,
                    integerResource(R.integer.form_max_width).dp
                )
                .padding(bottom = 32.dp)
                .sharedElement(
                    rememberSharedContentState(key = "field"),
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
            onValueChange = { email = it.substring(0, min(emailMaxLength, it.length)) },
            modifier = Modifier
                .widthIn(
                    integerResource(R.integer.form_min_width).dp,
                    integerResource(R.integer.form_max_width).dp
                )
                .padding(bottom = 32.dp)
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
                enabled = username.isNotBlank()
                        && username.length >= integerResource(R.integer.username_min_length)
                        && email.isNotBlank()
                        && email.length >= integerResource(R.integer.email_min_length)
                        && email.contains('@'),
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

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RegisterScreenPreview() {
    AppTheme {
        SharedTransitionLayout {
            val state = remember { 0 }
            AnimatedContent(state, label = "Preview") {
                RegisterScreen(this)
            }
        }
    }
}
