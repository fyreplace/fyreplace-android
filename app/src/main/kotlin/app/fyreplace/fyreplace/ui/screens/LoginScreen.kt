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
fun SharedTransitionScope.LoginScreen(visibilityScope: AnimatedVisibilityScope) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .imePadding()
            .testTag("screen:${Destination.LOGIN}")
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 32.dp, bottom = 32.dp)
                .size(96.dp)
                .sharedElement(
                    rememberSharedContentState(key = "image"),
                    visibilityScope
                )
        )

        var identifier by rememberSaveable { mutableStateOf("") }
        val maxLength = integerResource(R.integer.email_max_length)
        val keyboard = LocalSoftwareKeyboardController.current

        fun submit() {
            keyboard?.hide()
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
            onValueChange = { identifier = it.substring(0, min(maxLength, it.length)) },
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
                .testTag("login:identifier")
        )

        Box(
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "submit"),
                visibilityScope
            )
        ) {
            Button(
                enabled = identifier.isNotBlank() && identifier.length >= integerResource(R.integer.username_min_length),
                onClick = ::submit,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .testTag("login:submit")
            ) {
                Text(stringResource(R.string.login_submit), maxLines = 1)
            }
        }
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LoginScreenPreview() {
    AppTheme {
        SharedTransitionLayout {
            val state = remember { 0 }
            AnimatedContent(state, label = "Preview") {
                LoginScreen(this)
            }
        }
    }
}
