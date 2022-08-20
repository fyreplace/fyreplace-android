package app.fyreplace.fyreplace.events

import app.fyreplace.protos.Comment
import com.google.protobuf.ByteString

class CommentCreationEvent(
    override val item: Comment,
    val postId: ByteString
) : ItemEvent<Comment>

class CommentDeletionEvent(
    override val item: Comment,
    override val position: Int,
    val postId: ByteString
) : ItemPositionalEvent<Comment>

class CommentSeenEvent(
    override val item: Comment
) : ItemEvent<Comment>
