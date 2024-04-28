package app.fyreplace.fyreplace.events

import com.google.protobuf.ByteString

class ChapterWasUpdatedEvent(val postId: ByteString, val position: Int, val text: String) : Event
