package app.fyreplace.fyreplace.fakes.api

import app.fyreplace.api.UsersEndpointApi
import app.fyreplace.api.data.BlockUpdate
import app.fyreplace.api.data.Profile
import app.fyreplace.api.data.ReportUpdate
import app.fyreplace.api.data.User
import app.fyreplace.api.data.UserCreation
import app.fyreplace.fyreplace.fakes.badRequest
import app.fyreplace.fyreplace.fakes.conflict
import app.fyreplace.fyreplace.fakes.created
import app.fyreplace.fyreplace.fakes.forbidden
import app.fyreplace.fyreplace.fakes.placeholder
import retrofit2.Response
import java.io.File
import java.util.UUID

class FakeUsersEndpointApi : UsersEndpointApi {
    override suspend fun countBlockedUsers(): Response<Long> =
        throw NotImplementedError()

    override suspend fun createUser(
        userCreation: UserCreation,
        customDeepLinks: Boolean?
    ) = when {
        userCreation.username == BAD_USERNAME -> badRequest()
        userCreation.username == RESERVED_USERNAME -> forbidden()
        userCreation.username == USED_USERNAME -> conflict()
        userCreation.username == PASSWORD_USERNAME -> conflict()
        userCreation.email == BAD_EMAIL -> badRequest()
        userCreation.email == USED_EMAIL -> conflict()
        else -> created(User.placeholder)
    }

    override suspend fun deleteCurrentUser(): Response<Unit> =
        throw NotImplementedError()

    override suspend fun deleteCurrentUserAvatar(): Response<Unit> =
        throw NotImplementedError()

    override suspend fun getCurrentUser(): Response<User> =
        throw NotImplementedError()

    override suspend fun getUser(id: UUID): Response<User> =
        throw NotImplementedError()

    override suspend fun listBlockedUsers(page: Int?): Response<List<Profile>> =
        throw NotImplementedError()

    override suspend fun setCurrentUserAvatar(body: File): Response<String> =
        throw NotImplementedError()

    override suspend fun setCurrentUserBio(body: String): Response<String> =
        throw NotImplementedError()

    override suspend fun setUserBanned(id: UUID): Response<Unit> =
        throw NotImplementedError()

    override suspend fun setUserBlocked(id: UUID, blockUpdate: BlockUpdate): Response<Unit> =
        throw NotImplementedError()

    override suspend fun setUserReported(id: UUID, reportUpdate: ReportUpdate): Response<Unit> =
        throw NotImplementedError()

    companion object {
        const val BAD_USERNAME = "bad-username"
        const val RESERVED_USERNAME = "reserved-username"
        const val USED_USERNAME = "used-username"
        const val PASSWORD_USERNAME = "password-username"
        const val GOOD_USERNAME = "good-username"
        const val BAD_EMAIL = "bad-email"
        const val USED_EMAIL = "used-email"
        const val GOOD_EMAIL = "good-email"
    }
}
