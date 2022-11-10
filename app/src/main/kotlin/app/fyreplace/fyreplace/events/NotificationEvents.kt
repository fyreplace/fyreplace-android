package app.fyreplace.fyreplace.events

import app.fyreplace.protos.Notification

class NotificationWasCreatedEvent(item: Notification) : ItemEvent<Notification>(item)

class NotificationWasUpdatedEvent(item: Notification) : ItemEvent<Notification>(item)

class NotificationWasDeletedEvent(item: Notification) : ItemEvent<Notification>(item)
