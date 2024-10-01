package app.fyreplace.fyreplace

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.toAndroidDragEvent
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


    suspend fun makeFileFromUri(uri: Uri): File = withContext(Dispatchers.IO) {
        val file = File.createTempFile("image", null)
        file.deleteOnExit()

        contentResolver.openInputStream(uri)?.use { stream ->
            file.outputStream().use(stream::copyTo)
        }

        return@withContext file
    }

    fun makeFileDropTarget(onFile: suspend (File) -> Unit) = FileDropTarget(onFile)

    inner class FileDropTarget(private val onFile: suspend (File) -> Unit) : DragAndDropTarget {
        var isReady by mutableStateOf(false)
            private set

        override fun onDrop(event: DragAndDropEvent): Boolean {
            val dragEvent = event.toAndroidDragEvent()
            val uri = dragEvent.clipData?.getItemAt(0)?.uri ?: return false

            lifecycleScope.launch {
                val permissions = requestDragAndDropPermissions(dragEvent)

                try {
                    onFile(makeFileFromUri(uri))
                } finally {
                    permissions.release()
                }
            }

            return true
        }

        override fun onEnded(event: DragAndDropEvent) {
            isReady = false
        }

        override fun onEntered(event: DragAndDropEvent) {
            isReady = true
        }

        override fun onExited(event: DragAndDropEvent) {
            isReady = false
        }
    }
}
