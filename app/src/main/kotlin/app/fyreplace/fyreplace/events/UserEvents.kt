package app.fyreplace.fyreplace.events

import app.fyreplace.protos.Profile

class UserWasBlockedEvent(item: Profile) : ItemEvent<Profile>(item)

class UserWasUnblockedEvent(item: Profile) : ItemEvent<Profile>(item)

class UserWasBannedEvent(item: Profile) : ItemEvent<Profile>(item)
