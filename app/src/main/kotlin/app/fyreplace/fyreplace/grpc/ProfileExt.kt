package app.fyreplace.fyreplace.grpc

import app.fyreplace.protos.Profile

val Profile.isAvailable get() = !isBanned && username.isNotEmpty()
