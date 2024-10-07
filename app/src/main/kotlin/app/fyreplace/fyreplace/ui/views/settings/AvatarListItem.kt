package app.fyreplace.fyreplace.ui.views.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fyreplace.api.data.User
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.activity
import app.fyreplace.fyreplace.ui.views.Avatar
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AvatarListItem(
    user: User?,
    onUpdateAvatar: (File) -> Unit,
    onRemoveAvatar: () -> Unit
) {
    val activity = activity
    val haptics = LocalHapticFeedback.current
    val dateJoined = user?.dateCreated
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT) }
    var isAvatarMenuExpanded by remember { mutableStateOf(false) }
    val avatarDropTarget = remember { activity?.makeFileDropTarget(onUpdateAvatar) }
    val dropModifier = when (avatarDropTarget) {
        null -> Modifier
        else -> Modifier
            .dragAndDropTarget(
                shouldStartDragAndDrop = { dropEvent ->
                    dropEvent
                        .mimeTypes()
                        .any { it.startsWith("image/") }
                },
                target = avatarDropTarget
            )
            .background(if (avatarDropTarget.isReady) Color.Gray.copy(alpha = 0.25f) else Color.Transparent)
    }

    fun selectImage() {
        activity?.selectImage(onUpdateAvatar)
    }

    Box(modifier = dropModifier) {
        ListItem(
            headlineContent = { Text(user?.username ?: stringResource(R.string.loading)) },
            supportingContent = {
                Text(
                    when (dateJoined) {
                        null -> stringResource(R.string.loading)
                        else -> stringResource(
                            R.string.settings_profile_date_joined,
                            dateFormatter.format(dateJoined)
                        )
                    }
                )
            },
            leadingContent = { Avatar(user = user, modifier = Modifier.size(56.dp)) },
            modifier = Modifier.combinedClickable(
                onClick = ::selectImage,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    isAvatarMenuExpanded = true
                }
            )
        )

        DropdownMenu(
            isAvatarMenuExpanded,
            onDismissRequest = { isAvatarMenuExpanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.settings_profile_avatar_change)) },
                leadingIcon = { Icon(Icons.Outlined.Upload, null) },
                onClick = {
                    selectImage()
                    isAvatarMenuExpanded = false
                }
            )
            DropdownMenuItem(
                enabled = !user?.avatar.isNullOrEmpty(),
                text = { Text(stringResource(R.string.settings_profile_avatar_remove)) },
                leadingIcon = { Icon(Icons.Outlined.Delete, null) },
                onClick = {
                    onRemoveAvatar()
                    isAvatarMenuExpanded = false
                }
            )
        }
    }
}
