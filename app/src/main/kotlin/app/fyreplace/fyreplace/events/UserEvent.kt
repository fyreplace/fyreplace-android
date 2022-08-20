package app.fyreplace.fyreplace.events

import app.fyreplace.protos.Profile

class UserBlockEvent(override val item: Profile, override val position: Int) :
    ItemPositionalEvent<Profile>

class UserUnblockEvent(override val position: Int) :
    PositionalEvent

class UserBanEvent(override val item: Profile, override val position: Int) :
    ItemPositionalEvent<Profile>
