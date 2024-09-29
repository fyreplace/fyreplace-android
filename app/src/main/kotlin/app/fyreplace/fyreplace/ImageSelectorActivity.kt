package app.fyreplace.fyreplace

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import app.fyreplace.fyreplace.events.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

abstract class ImageSelectorActivity : SecureActivity() {
    @Inject
    lateinit var eventBus: EventBus

    private lateinit var getContentLauncher: ActivityResultLauncher<String>

    private var onImageUri: (Uri) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val getContentContract = ActivityResultContracts.GetContent()
        getContentLauncher = registerForActivityResult(getContentContract) {
            onImageUri(it ?: return@registerForActivityResult)
        }
    }

    fun selectImage(onImage: (File) -> Unit) {
        this.onImageUri = { uri -> lifecycleScope.launch { onImage(makeFileFromUri(uri)) } }
        getContentLauncher.launch("image/*")
    }


    private suspend fun makeFileFromUri(uri: Uri) = withContext(Dispatchers.IO) {
        val file = File.createTempFile("image", ".tmp")

        contentResolver.openInputStream(uri)?.use { stream ->
            file.outputStream().use(stream::copyTo)
        }

        return@withContext file
    }
}
