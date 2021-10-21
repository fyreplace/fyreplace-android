package app.fyreplace.client.grpc

import app.fyreplace.protos.client

val defaultClient = client {
    hardware = "mobile"
    software = "android"
}
