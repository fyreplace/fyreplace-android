package app.fyreplace.fyreplace.fakes

import app.fyreplace.fyreplace.data.SecretsHandler
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

class FakeSecretsHandler : SecretsHandler {
    override suspend fun encrypt(value: String): ByteString = value.encodeUtf8()

    override suspend fun decrypt(data: ByteString): String = data.utf8()
}
