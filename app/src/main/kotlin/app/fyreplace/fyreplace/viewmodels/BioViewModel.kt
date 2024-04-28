package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.protos.UserServiceGrpcKt
import app.fyreplace.protos.bio
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class BioViewModel @AssistedInject constructor(
    @Assisted initialBio: String,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub
) :
    TextInputViewModel(initialBio) {
    suspend fun update(text: String): Unit = whileLoading {
        userStub.updateBio(bio { bio = text })
    }

    companion object {
        fun provideFactory(
            assistedFactory: BioViewModelFactory,
            bio: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                assistedFactory.create(bio) as T
        }
    }
}
