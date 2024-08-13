package app.fyreplace.fyreplace.fakes

import app.fyreplace.fyreplace.data.ResourceResolver

class FakeResourceResolver(private val map: Map<Int, Int>) : ResourceResolver {
    override fun getInteger(resId: Int) = map[resId]!!
}
