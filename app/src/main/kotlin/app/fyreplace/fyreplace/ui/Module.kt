package app.fyreplace.fyreplace.ui

import androidx.fragment.app.Fragment
import dagger.assisted.AssistedFactory

@AssistedFactory
@Suppress("unused")
interface ImageSelectorFactory {
    fun create(
        fragment: Fragment,
        failureHandler: FailureHandler,
        listener: ImageSelector.Listener,
        maxImageSize: Float
    ): ImageSelector
}
