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
import app.fyreplace.fyreplace.fakes.make
import app.fyreplace.fyreplace.fakes.noContent
import app.fyreplace.fyreplace.fakes.ok
import app.fyreplace.fyreplace.fakes.payloadTooLarge
import app.fyreplace.fyreplace.fakes.placeholder
import app.fyreplace.fyreplace.fakes.unsupportedMediaType
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
        else -> created(User.make(userCreation.username))
    }

    override suspend fun deleteCurrentUser(): Response<Unit> =
        throw NotImplementedError()

    override suspend fun deleteCurrentUserAvatar() = noContent()

    override suspend fun getCurrentUser() = ok(User.placeholder)

    override suspend fun getUser(id: UUID): Response<User> =
        throw NotImplementedError()

    override suspend fun listBlockedUsers(page: Int?): Response<List<Profile>> =
        throw NotImplementedError()

    override suspend fun setCurrentUserAvatar(body: File) = when (body.path) {
        NORMAL_IMAGE_FILE.path -> created(NORMAL_IMAGE_FILE.path)
        LARGE_IMAGE_FILE.path -> payloadTooLarge()
        else -> unsupportedMediaType()
    }

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

        const val BAD_EMAIL = "bad@email"
        const val USED_EMAIL = "used@email"
        const val GOOD_EMAIL = "good@email"

        val NOT_IMAGE_FILE = File("text.txt")
        val LARGE_IMAGE_FILE = File("large.png")
        val NORMAL_IMAGE_FILE = File("normal.png")
    }
}
