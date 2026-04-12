package app.fyreplace.fyreplace.legacy.events

import okio.ByteString

class ChapterWasUpdatedEvent(val postId: ByteString, val position: Int, val text: String) : Event
