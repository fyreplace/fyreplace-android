package app.fyreplace.client.viewmodels

import android.net.Uri

class ImageSelectorViewModel : BaseViewModel() {
    private val imageUris = mutableListOf<Uri>()

    fun push(uri: Uri) = imageUris.add(uri)

    fun pop() = imageUris.removeAt(imageUris.size - 1)
}
