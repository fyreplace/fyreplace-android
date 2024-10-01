package app.fyreplace.fyreplace.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fyreplace.api.data.User
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.activity
import app.fyreplace.fyreplace.fakes.FakeApiResolver
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.fakes.placeholder
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.Avatar
import app.fyreplace.fyreplace.viewmodels.screens.SettingsViewModel
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = dimensionResource(R.dimen.spacing_medium))
    ) {
        val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

        UserInfo(user = currentUser, onAvatarFile = viewModel::updateAvatar)
        Button(onClick = viewModel::logout) {
            Text(stringResource(R.string.settings_logout))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserInfo(user: User?, onAvatarFile: (File) -> Unit) {
    val activity = activity
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT) }
    val loading = stringResource(R.string.loading)
    val avatarSize = 128.dp
    val avatarInteraction = remember { MutableInteractionSource() }
    val isAvatarHovered by avatarInteraction.collectIsHoveredAsState()
    val avatarDropTarget = remember { requireNotNull(activity).makeFileDropTarget(onAvatarFile) }
    val isAvatarUpdatable = isAvatarHovered || avatarDropTarget.isReady
    val avatarBlur by animateDpAsState(
        targetValue = if (isAvatarUpdatable) 1.dp else 0.dp,
        label = "Avatar blur"
    )

    Box(contentAlignment = Alignment.Center) {
        Avatar(
            user = user,
            tinted = true,
            size = avatarSize,
            modifier = Modifier
                .blur(avatarBlur)
                .hoverable(avatarInteraction)
                .clickable { activity?.selectImage(onAvatarFile) }
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { dropEvent ->
                        dropEvent
                            .mimeTypes()
                            .any { it.startsWith("image/") }
                    },
                    target = avatarDropTarget
                )
        )

        AnimatedVisibility(
            visible = isAvatarUpdatable,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(avatarSize / 4)
            )
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = user?.username ?: loading,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = when (user) {
                null -> loading
                else -> stringResource(
                    R.string.settings_date_joined,
                    dateFormatter.format(user.dateCreated)
                )
            },
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            viewModel = SettingsViewModel(
                SavedStateHandle().apply {
                    this[SettingsViewModel::currentUser.name] = User.placeholder
                },
                FakeEventBus(),
                FakeStoreResolver(),
                FakeApiResolver()
            )
        )
    }
}
