package app.fyreplace.fyreplace.viewmodels.events

import app.fyreplace.protos.Post

class PostSubscriptionEvent(override val item: Post, override val position: Int) :
    ItemPositionalEvent<Post>

class PostUnsubscriptionEvent(override val position: Int) :
    PositionalEvent

class PostDeletionEvent(override val position: Int) :
    PositionalEvent

class DraftCreationEvent(override val item: Post) :
    ItemEvent<Post>

class DraftUpdateEvent(override val item: Post, override val position: Int) :
    ItemPositionalEvent<Post>

class DraftDeletionEvent(override val position: Int) :
    PositionalEvent

class DraftPublicationEvent(override val item: Post, override val position: Int) :
    ItemPositionalEvent<Post>
