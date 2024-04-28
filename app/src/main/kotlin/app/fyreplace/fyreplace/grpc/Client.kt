package app.fyreplace.fyreplace.grpc

import app.fyreplace.protos.client

val defaultClient = client {
    hardware = "mobile"
    software = "android"
}
