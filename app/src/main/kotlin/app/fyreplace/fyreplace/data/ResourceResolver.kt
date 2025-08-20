package app.fyreplace.fyreplace.data

import android.content.Context
import androidx.annotation.IntegerRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ResourceResolver {
    fun getInteger(@IntegerRes resId: Int): Int
}

class ContextResourceResolver @Inject constructor(
    @ApplicationContext private val context: Context
) : ResourceResolver {
    override fun getInteger(resId: Int) = context.resources.getInteger(resId)
}
