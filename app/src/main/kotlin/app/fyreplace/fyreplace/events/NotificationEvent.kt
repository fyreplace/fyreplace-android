package app.fyreplace.fyreplace.events

import app.fyreplace.protos.Notification

class NotificationCreationEvent(item: Notification) : ItemEvent<Notification>(item)

class NotificationUpdateEvent(item: Notification) : ItemEvent<Notification>(item)

class NotificationDeletionEvent(item: Notification) : ItemEvent<Notification>(item)
