package app.fyreplace.fyreplace.data

import android.content.SharedPreferences
import androidx.core.content.edit
import app.fyreplace.protos.Token

fun SharedPreferences.storeAuthToken(token: Token) = edit { putString("auth.token", token.token) }
