package app.fyreplace.fyreplace.legacy.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.protos.Bio
import app.fyreplace.protos.UserServiceClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class BioViewModel @AssistedInject constructor(
    override val preferences: SharedPreferences,
    private val userService: UserServiceClient,
    @Assisted initialBio: String
) :
    TextInputViewModel(initialBio) {
    suspend fun update(text: String) = whileLoading {
        userService.UpdateBio().executeFully(Bio(bio = text))
    }

    companion object {
        fun provideFactory(assistedFactory: BioViewModelFactory, bio: String) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    assistedFactory.create(bio) as T
            }
    }
}
