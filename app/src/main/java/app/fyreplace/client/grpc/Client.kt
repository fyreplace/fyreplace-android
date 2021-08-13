package app.fyreplace.client.grpc

import app.fyreplace.protos.Client

val defaultClient: Client = Client.newBuilder()
    .setHardware("mobile")
    .setSoftware("android")
    .build()
