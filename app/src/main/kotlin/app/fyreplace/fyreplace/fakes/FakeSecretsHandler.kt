package app.fyreplace.fyreplace.fakes

import app.fyreplace.fyreplace.data.SecretsHandler
import com.google.protobuf.ByteString

class FakeSecretsHandler : SecretsHandler {
    override suspend fun encrypt(value: String): ByteString = ByteString.copyFromUtf8(value)

    override suspend fun decrypt(data: ByteString): String = data.toStringUtf8()
}
