package app.fyreplace.fyreplace.fakes

import app.fyreplace.api.data.Color
import app.fyreplace.api.data.Rank
import app.fyreplace.api.data.User
import java.time.OffsetDateTime
import java.util.UUID

val User.Companion.placeholder
    get() = User(
        id = UUID.randomUUID(),
        dateCreated = OffsetDateTime.now(),
        username = "random_user",
        rank = Rank.CITIZEN,
        avatar = "",
        bio = "Hello there",
        banned = false,
        blocked = false,
        tint = Color(0x7F, 0x7F, 0x7F)
    )
