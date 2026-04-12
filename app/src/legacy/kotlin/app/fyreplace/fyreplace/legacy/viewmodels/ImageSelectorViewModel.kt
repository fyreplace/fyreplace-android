package app.fyreplace.fyreplace.legacy.viewmodels

import android.content.SharedPreferences
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageSelectorViewModel @Inject constructor(
    override val preferences: SharedPreferences
) : BaseViewModel() {
    private val imageUris = mutableListOf<Uri>()

    fun push(uri: Uri) = imageUris.add(uri)

    fun pop() = imageUris.removeAt(imageUris.size - 1)
}
