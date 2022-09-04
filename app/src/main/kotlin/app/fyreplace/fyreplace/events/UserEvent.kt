package app.fyreplace.fyreplace.events

import app.fyreplace.protos.Profile

class UserBlockEvent(item: Profile) : ItemEvent<Profile>(item)

class UserUnblockEvent(item: Profile) : ItemEvent<Profile>(item)

class UserBanEvent(item: Profile) : ItemEvent<Profile>(item)
