package app.fyreplace.fyreplace.legacy.grpc

import app.fyreplace.protos.client

val defaultClient = client {
    hardware = "mobile"
    software = "android"
}
