package app.fyreplace.fyreplace.legacy.ui

import androidx.fragment.app.Fragment
import dagger.assisted.AssistedFactory

@AssistedFactory
interface ImageSelectorFactory {
    fun create(
        fragment: Fragment,
        failureHandler: FailureHandler,
        listener: ImageSelector.Listener,
        maxImageByteSize: Int
    ): ImageSelector
}
