package app.fyreplace.fyreplace.viewmodels.screens

import androidx.compose.runtime.mutableStateListOf
import app.fyreplace.api.data.Email
import app.fyreplace.fyreplace.api.ApiResolver
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.viewmodels.ApiViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmailsViewModel @Inject constructor(
    eventBus: EventBus,
    storeResolver: StoreResolver,
    private val apiResolver: ApiResolver,
) : ApiViewModelBase(eventBus, storeResolver) {
    val emails = mutableStateListOf<Email>()

    init {
        loadEmails()
    }

    private fun loadEmails() = call(apiResolver::emails) {
        emails.clear()
        var page = 0

        do {
            val emailsPage = listEmails(page).require() ?: return@call
            emails.addAll(emailsPage)
            page++
        } while (emailsPage.isNotEmpty())
    }
}
