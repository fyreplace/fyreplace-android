package app.fyreplace.client.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.FileProvider.getUriForFile
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.*
import androidx.fragment.app.Fragment
import app.fyreplace.client.R
import app.fyreplace.client.data.ImageData
import app.fyreplace.client.viewmodels.ImageSelectorViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ImageSelector<F>(
    private val fragment: F,
    maxImageSize: Float
) where F : Fragment, F : FailureHandler, F : ImageSelector.Listener {
    private var snackbar: Snackbar? = null
    private val vm by fragment.viewModel<ImageSelectorViewModel>()
    private val imagesDirectory = File(fragment.requireContext().filesDir, "images")
    private val photoImageFile = File(imagesDirectory, "image.data")
    private val maxImageByteSize = (maxImageSize * 1024 * 1024).roundToInt()
    private lateinit var getContentContract: ActivityResultLauncher<String>
    private lateinit var takePictureContract: ActivityResultLauncher<Uri>

    fun onCreate() {
        getContentContract =
            fragment.registerForActivityResult(ActivityResultContracts.GetContent()) {
                it?.let { fragment.launch { useImageUri(it) } }
            }

        takePictureContract =
            fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) {
                if (!it) {
                    return@registerForActivityResult
                }

                fragment.launch {
                    vm.pop().let {
                        useImageUri(it)
                        photoImageFile.delete()
                    }
                }
            }
    }

    fun showImageChooser(@StringRes title: Int, canRemove: Boolean) {
        val items = mutableListOf(
            R.string.image_selector_dialog_file,
            R.string.image_selector_dialog_photo
        )

        if (canRemove) {
            items.add(R.string.image_selector_dialog_remove)
        }

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(title)
            .setItems(items.map { fragment.resources.getString(it) }.toTypedArray()) { _, i ->
                fragment.launch {
                    when (items[i]) {
                        R.string.image_selector_dialog_remove -> fragment.onImageRemoved()
                        R.string.image_selector_dialog_file -> selectImage(false)
                        R.string.image_selector_dialog_photo -> selectImage(true)
                        else -> throw IllegalArgumentException()
                    }
                }
            }
            .show()
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
        takePictureContract.launch(imageUri)
    } else {
        getContentContract.launch("image/*")
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun useImageUri(uri: Uri) = withContext(Dispatchers.Main) {
        try {
            onImageLoadingBegin()
            val resolver = fragment.context?.contentResolver ?: return@withContext
            val mimeType = resolver.getType(uri)
                ?: throw IOException(fragment.resources.getString(R.string.image_failure_unknown_type))

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
        snackbar?.setAction(R.string.cancel) { context.cancel() }
        snackbar?.show()
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
        var compressedMimeType = mimeType
        val isTooBig = compressedBytes.size > maxImageByteSize
        val isUnknownMime = mimeType !in listOf("jpeg", "png").map { "image/$it" }
        val isPng = mimeType == "image/png"
        val isRotated = !transformations.isIdentity

        if (isTooBig || isUnknownMime || isRotated) withContext(Dispatchers.Default) {
            coroutineContext.ensureActive()
            val os = ByteArrayOutputStream()
            val bitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
            var quality = 100
            var compressFormat = if (isPng) CompressFormat.PNG else CompressFormat.JPEG
            var rotatedBitmap = Bitmap.createBitmap(
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
                rotatedBitmap = downscaleBitmap(rotatedBitmap)
                quality = 50
            }

            fun compress() {
                coroutineContext.ensureActive()
                os.reset()
                rotatedBitmap.compress(compressFormat, quality, os)
            }

            compress()

            if (os.size() > maxImageByteSize && isPng) {
                compressFormat = CompressFormat.JPEG
                compress()
            }

            compressedBytes = os.toByteArray()
            @SuppressLint("NewApi")
            compressedMimeType = "image/" + when (compressFormat) {
                CompressFormat.JPEG -> "jpeg"
                CompressFormat.PNG -> "png"
                CompressFormat.WEBP,
                CompressFormat.WEBP_LOSSY,
                CompressFormat.WEBP_LOSSLESS -> "webp"
            }

            coroutineContext.ensureActive()
        }

        withContext(Dispatchers.Main) {
            if (compressedBytes.size <= maxImageByteSize) {
                val extension =
                    MimeTypeMap.getSingleton().getExtensionFromMimeType(compressedMimeType)
                val image = ImageData("image.${extension}", compressedMimeType, compressedBytes)
                fragment.onImage(image)
            } else {
                throw IOException(fragment.resources.getString(R.string.image_failure_file_size))
            }
        }
    }

    companion object {
        const val IMAGE_MAX_AREA = 1920 * 1080
        const val IMAGE_CHUNK_SIZE = 4096
    }

    interface Listener {
        suspend fun onImage(image: ImageData) = Unit

        suspend fun onImageRemoved() = Unit
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

private fun downscaleBitmap(bitmap: Bitmap): Bitmap {
    val areaFactor = (bitmap.width * bitmap.height).toFloat() / ImageSelector.IMAGE_MAX_AREA

    if (areaFactor > 1) {
        val sideFactor = sqrt(areaFactor)
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width / sideFactor).toInt(),
            (bitmap.height / sideFactor).toInt(),
            true
        )
    }

    return bitmap
}
