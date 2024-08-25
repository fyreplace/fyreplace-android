package app.fyreplace.fyreplace.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.BLOCK_MODE_CBC
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_PKCS7
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import android.util.Base64
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

interface SecretsHandler {
    suspend fun encrypt(value: String): ByteString

    suspend fun decrypt(data: ByteString): String
}

class EncryptedSecretsHandler @Inject constructor() : SecretsHandler {
    private val keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER).apply { load(null) }

    override suspend fun encrypt(value: String): ByteString = withContext(Dispatchers.Default) {
        val cipher = getCipher().apply { init(Cipher.ENCRYPT_MODE, getKey()) }
        val data = cipher.doFinal(value.toByteArray())
        return@withContext ByteString.copyFromUtf8("${data.encodedString}:${cipher.iv.encodedString}")
    }

    override suspend fun decrypt(data: ByteString): String = withContext(Dispatchers.Default) {
        val string = data.toStringUtf8()
        val (bytes, iv) = string.split(":").map { it.decodedBytes }
        val cipher = getCipher().apply { init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv)) }
        return@withContext String(cipher.doFinal(bytes))
    }

    private fun getCipher() = Cipher.getInstance(TRANSFORMATION.toString())

    private fun getKey() = when (val entry = keyStore.getEntry(ALIAS, null)) {
        !is KeyStore.SecretKeyEntry -> createKey()
        else -> entry.secretKey
    }

    private fun createKey() = KeyGenerator.getInstance(
        TRANSFORMATION.algorithm,
        KEY_STORE_PROVIDER
    ).run {
        init(
            KeyGenParameterSpec.Builder(ALIAS, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                .setBlockModes(TRANSFORMATION.blockMode)
                .setEncryptionPaddings(TRANSFORMATION.padding)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return@run generateKey()
    }

    private companion object {
        const val KEY_STORE_PROVIDER = "AndroidKeyStore"
        const val ALIAS = "secrets"
        val TRANSFORMATION =
            Transformation(KEY_ALGORITHM_AES, BLOCK_MODE_CBC, ENCRYPTION_PADDING_PKCS7)

        val ByteArray.encodedString: String
            get() = Base64.encodeToString(this, Base64.NO_WRAP)

        val String.decodedBytes: ByteArray
            get() = Base64.decode(this, Base64.NO_WRAP)
    }

    private data class Transformation(
        val algorithm: String,
        val blockMode: String,
        val padding: String
    ) {
        override fun toString() = "$algorithm/$blockMode/$padding"
    }
}
