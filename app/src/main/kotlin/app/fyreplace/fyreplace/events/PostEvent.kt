package app.fyreplace.fyreplace.events

import app.fyreplace.protos.Post

class PostSubscriptionEvent(item: Post) : ItemEvent<Post>(item)

class PostUnsubscriptionEvent(item: Post) : ItemEvent<Post>(item)

class PostDeletionEvent(item: Post) : ItemEvent<Post>(item)

class DraftCreationEvent(item: Post) : ItemEvent<Post>(item)

class DraftUpdateEvent(item: Post) : ItemEvent<Post>(item)

class DraftDeletionEvent(item: Post) : ItemEvent<Post>(item)

class DraftPublicationEvent(item: Post) : ItemEvent<Post>(item)
