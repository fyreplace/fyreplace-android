package app.fyreplace.fyreplace.ui

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.FileProvider.getUriForFile
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.viewmodels.ImageSelectorViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt

class ImageSelector @AssistedInject constructor(
    @Assisted private val fragment: Fragment,
    @Assisted private val failureHandler: FailureHandler,
    @Assisted private val listener: Listener,
    @Assisted private val maxImageByteSize: Int
) {
    private val vm by fragment.viewModels<ImageSelectorViewModel>()
    private val imagesDirectory = File(fragment.requireContext().filesDir, "images")
    private val photoImageFile = File(imagesDirectory, "image.data")
    private var snackbar: Snackbar? = null
    private lateinit var getContentLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    fun onCreate() {
        val getContentContract = ActivityResultContracts.GetContent()
        getContentLauncher = fragment.registerForActivityResult(getContentContract) {
            it?.let { failureHandler.launch { useImageUri(it) } }
        }

        val takePictureContract = ActivityResultContracts.TakePicture()
        takePictureLauncher = fragment.registerForActivityResult(takePictureContract) {
            if (!it) {
                return@registerForActivityResult
            }

            failureHandler.launch {
                vm.pop().let { uri ->
                    useImageUri(uri)
                    photoImageFile.delete()
                }
            }
        }
    }

    fun showImageChooser(@StringRes title: Int, canRemove: Boolean) {
        val items = mutableListOf(
            R.string.image_selector_action_file,
            R.string.image_selector_action_photo
        )

        if (canRemove) {
            items += R.string.image_selector_action_remove
        }

        failureHandler.showSelectionAlert(
            title,
            items.map { fragment.resources.getString(it) }.toTypedArray()
        ) {
            failureHandler.launch {
                when (items[it]) {
                    R.string.image_selector_action_remove -> listener.onImageRemoved()
                    R.string.image_selector_action_file -> selectImage(false)
                    R.string.image_selector_action_photo -> selectImage(true)
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    private suspend fun selectImage(picture: Boolean) = if (picture) {
        val imageFile = photoImageFile
        withContext(Dispatchers.IO) { imageFile.parentFile?.mkdirs() }
        val imageUri = getUriForFile(
            fragment.requireContext(),
            fragment.resources.getString(R.string.file_provider_authority),
            imageFile
        )
        imageUri?.let { vm.push(it) }
        takePictureLauncher.launch(imageUri)
    } else {
        getContentLauncher.launch("image/*")
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun useImageUri(uri: Uri) = withContext(Dispatchers.Main) {
        try {
            onImageLoadingBegin()
            val resolver = fragment.context?.contentResolver ?: return@withContext
            val mimeType = resolver.getType(uri)
                ?: throw IOException(fragment.resources.getString(R.string.image_error_unknown_type_message))

            withContext(Dispatchers.IO) {
                val transformations = resolver.openInputStream(uri)
                    ?.use { extractTransformations(it) }
                    ?: Matrix()

                resolver.openInputStream(uri)?.use {
                    useBytes(it.readBytes(), transformations, mimeType)
                }
            }
        } finally {
            onImageLoadingEnd()
        }
    }

    private suspend fun onImageLoadingBegin() {
        val view = fragment.view ?: return
        val context = coroutineContext
        snackbar = Snackbar.make(
            view, R.string.image_selector_snackbar_upload,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.cancel) { context.cancel() }
            .apply { show() }
    }

    private fun onImageLoadingEnd() {
        snackbar?.dismiss()
        snackbar = null
    }

    private suspend fun useBytes(
        bytes: ByteArray,
        transformations: Matrix,
        mimeType: String
    ) {
        var compressedBytes = bytes
        val downscaleFactor = compressedBytes.size.toFloat() / maxImageByteSize
        val isTooBig = downscaleFactor >= 1
        val isUnknownMime = mimeType !in listOf("jpeg", "png", "webp").map { "image/$it" }
        val isRotated = !transformations.isIdentity

        if (isTooBig || isUnknownMime || isRotated) withContext(Dispatchers.Default) {
            coroutineContext.ensureActive()
            val os = ByteArrayOutputStream()
            val bitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
            var quality = 100
            var compressFormat = when (mimeType.split('/').last()) {
                "webp" -> when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> when {
                        isTooBig -> CompressFormat.WEBP_LOSSY
                        else -> CompressFormat.WEBP_LOSSLESS
                    }
                    else -> @Suppress("DEPRECATION") CompressFormat.WEBP
                }
                "png" -> CompressFormat.PNG
                else -> CompressFormat.JPEG
            }
            var uprightBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                transformations,
                true
            )

            if (isTooBig) {
                coroutineContext.ensureActive()
                uprightBitmap = uprightBitmap.downscaled(downscaleFactor)
                quality = 50
            }

            fun compress() {
                coroutineContext.ensureActive()
                os.reset()
                uprightBitmap.compress(compressFormat, quality, os)
            }

            compress()

            if (os.size() > maxImageByteSize && compressFormat != CompressFormat.JPEG) {
                compressFormat = CompressFormat.JPEG
                compress()
            }

            compressedBytes = os.toByteArray()
            coroutineContext.ensureActive()
        }

        withContext(Dispatchers.Main) { listener.onImage(compressedBytes) }
    }

    companion object {
        const val IMAGE_CHUNK_SIZE = 100 * 1024
    }

    interface Listener {
        suspend fun onImage(image: ByteArray)

        suspend fun onImageRemoved() = Unit

        suspend fun onImageSelectionCancelled() = Unit
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun extractTransformations(source: InputStream) = withContext(Dispatchers.IO) {
    val transformations = Matrix()
    val exif = ExifInterface(source)

    when (exif.getAttributeInt(TAG_ORIENTATION, ORIENTATION_UNDEFINED)) {
        ORIENTATION_ROTATE_90 -> transformations.postRotate(90f)
        ORIENTATION_ROTATE_180 -> transformations.postRotate(180f)
        ORIENTATION_ROTATE_270 -> transformations.postRotate(270f)
        ORIENTATION_FLIP_HORIZONTAL -> transformations.postScale(-1f, 1f)
        ORIENTATION_FLIP_VERTICAL -> transformations.postScale(1f, -1f)
    }

    return@withContext transformations
}

private fun Bitmap.downscaled(factor: Float): Bitmap {
    if (factor <= 1) {
        return this
    }

    val sideFactor = sqrt(factor)
    return Bitmap.createScaledBitmap(
        this,
        (width / sideFactor).toInt(),
        (height / sideFactor).toInt(),
        true
    )
}
