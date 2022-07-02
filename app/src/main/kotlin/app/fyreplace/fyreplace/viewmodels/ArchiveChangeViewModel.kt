package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.protos.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArchiveChangeViewModel @Inject constructor() : ItemChangeViewModel<Post>()
