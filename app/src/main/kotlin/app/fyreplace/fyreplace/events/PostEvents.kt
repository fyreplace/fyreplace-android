package app.fyreplace.fyreplace.events

import app.fyreplace.protos.Post

class PostWasSubscribedToEvent(item: Post) : ItemEvent<Post>(item)

class PostWasUnsubscribedFromEvent(item: Post) : ItemEvent<Post>(item)

class PostWasDeletedEvent(item: Post) : ItemEvent<Post>(item)

class DraftWasCreatedEvent(item: Post) : ItemEvent<Post>(item)

class DraftWasUpdatedEvent(item: Post) : ItemEvent<Post>(item)

class DraftWasDeletedEvent(item: Post) : ItemEvent<Post>(item)

class DraftWasPublishedEvent(item: Post) : ItemEvent<Post>(item)
