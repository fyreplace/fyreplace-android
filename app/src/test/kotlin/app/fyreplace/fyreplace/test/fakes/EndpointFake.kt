package app.fyreplace.fyreplace.test.fakes

import app.fyreplace.fyreplace.api.Endpoint

class EndpointFake<T>(private val construct: () -> T) : Endpoint<T> {
    override suspend fun get() = construct()
}
