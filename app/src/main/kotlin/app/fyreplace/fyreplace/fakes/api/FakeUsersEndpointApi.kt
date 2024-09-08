package app.fyreplace.fyreplace.fakes.api

import app.fyreplace.api.UsersEndpointApi
import app.fyreplace.api.data.BlockUpdate
import app.fyreplace.api.data.Profile
import app.fyreplace.api.data.Rank
import app.fyreplace.api.data.ReportUpdate
import app.fyreplace.api.data.User
import app.fyreplace.api.data.UserCreation
import app.fyreplace.fyreplace.fakes.badRequest
import app.fyreplace.fyreplace.fakes.conflict
import app.fyreplace.fyreplace.fakes.created
import app.fyreplace.fyreplace.fakes.forbidden
import retrofit2.Response
import java.io.File
import java.time.OffsetDateTime
import java.util.UUID

class FakeUsersEndpointApi : UsersEndpointApi {
    override suspend fun countBlockedUsers(): Response<Long> {
        TODO("Not yet implemented")
    }

    override suspend fun createUser(userCreation: UserCreation) = when {
        userCreation.username == BAD_USERNAME -> badRequest()
        userCreation.username == RESERVED_USERNAME -> forbidden()
        userCreation.username == USED_USERNAME -> conflict()
        userCreation.email == BAD_EMAIL -> badRequest()
        userCreation.email == USED_EMAIL -> conflict()
        else -> created(
            User(
                id = UUID.randomUUID(),
                dateCreated = OffsetDateTime.now(),
                username = userCreation.username,
                rank = Rank.CITIZEN,
                avatar = "",
                bio = "",
                banned = false,
                blocked = false,
                tint = "#7F7F7F"
            )
        )
    }

    override suspend fun deleteCurrentUser(): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCurrentUserAvatar(): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUser(): Response<User> {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(id: UUID): Response<User> {
        TODO("Not yet implemented")
    }

    override suspend fun listBlockedUsers(page: Int?): Response<List<Profile>> {
        TODO("Not yet implemented")
    }

    override suspend fun setCurrentUserAvatar(body: File): Response<String> {
        TODO("Not yet implemented")
    }

    override suspend fun setCurrentUserBio(body: String): Response<String> {
        TODO("Not yet implemented")
    }

    override suspend fun setUserBanned(id: UUID): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun setUserBlocked(id: UUID, blockUpdate: BlockUpdate): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun setUserReported(id: UUID, reportUpdate: ReportUpdate): Response<Unit> {
        TODO("Not yet implemented")
    }

    companion object {
        const val BAD_USERNAME = "bad-username"
        const val RESERVED_USERNAME = "reserved-username"
        const val USED_USERNAME = "used-username"
        const val GOOD_USERNAME = "good-username"
        const val BAD_EMAIL = "bad-email"
        const val USED_EMAIL = "used-email"
        const val GOOD_EMAIL = "good-email"
    }
}
