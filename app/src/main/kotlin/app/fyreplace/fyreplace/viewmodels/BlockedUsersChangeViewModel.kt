package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.protos.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BlockedUsersChangeViewModel @Inject constructor() : ItemChangeViewModel<Profile>()
