package app.fyreplace.fyreplace.viewmodels

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageSelectorViewModel @Inject constructor() : BaseViewModel() {
    private val imageUris = mutableListOf<Uri>()

    fun push(uri: Uri) = imageUris.add(uri)

    fun pop() = imageUris.removeAt(imageUris.size - 1)
}
