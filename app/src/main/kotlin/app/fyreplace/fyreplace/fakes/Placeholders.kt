package app.fyreplace.fyreplace.fakes

import app.fyreplace.api.data.Color
import app.fyreplace.api.data.Email
import app.fyreplace.api.data.Rank
import app.fyreplace.api.data.User
import java.time.OffsetDateTime
import java.util.UUID

val User.Companion.placeholder
    get() = make("random_user")

fun User.Companion.make(username: String) = User(
    id = UUID.randomUUID(),
    dateCreated = OffsetDateTime.now(),
    username = username,
    rank = Rank.CITIZEN,
    avatar = "",
    bio = "Hello there",
    banned = false,
    blocked = false,
    tint = Color(0x7F, 0x7F, 0x7F)
)

fun Email.Companion.make(main: Boolean = false, verified: Boolean = true): Email {
    val id = UUID.randomUUID()
    return Email(
        id = id,
        email = "$id@example.org",
        main = main,
        verified = verified
    )
}
