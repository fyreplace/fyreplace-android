package app.fyreplace.fyreplace.events

import app.fyreplace.protos.Comment
import com.google.protobuf.ByteString

abstract class CommentEvent(item: Comment, val postId: ByteString) : ItemEvent<Comment>(item)

class CommentCreationEvent(item: Comment, postId: ByteString, val byCurrentUser: Boolean) :
    CommentEvent(item, postId)

class CommentDeletionEvent(item: Comment, postId: ByteString) : CommentEvent(item, postId)

class CommentSeenEvent(item: Comment, postId: ByteString, val commentsLeft: Int) :
    CommentEvent(item, postId)
