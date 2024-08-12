package app.fyreplace.fyreplace.test.fakes

import app.fyreplace.fyreplace.data.ResourceResolver

class ResourceResolverFake(private val map: Map<Int, Int>) : ResourceResolver {
    override fun getInteger(resId: Int) = map[resId]!!
}
