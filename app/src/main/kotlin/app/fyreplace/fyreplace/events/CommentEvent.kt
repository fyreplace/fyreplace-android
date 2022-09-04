package app.fyreplace.fyreplace.events

import app.fyreplace.protos.Comment
import com.google.protobuf.ByteString

class CommentCreationEvent(item: Comment, val postId: ByteString) : ItemEvent<Comment>(item)

class CommentDeletionEvent(item: Comment, val postId: ByteString) : ItemEvent<Comment>(item)

class CommentSeenEvent(item: Comment) : ItemEvent<Comment>(item)
