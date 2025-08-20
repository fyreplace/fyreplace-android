package app.fyreplace.fyreplace.legacy.extensions

import app.fyreplace.protos.Comment
import com.google.protobuf.ByteString

fun Comment.notificationTag(postId: ByteString) =
    "${postId.base64ShortString}:${id.base64ShortString}"
