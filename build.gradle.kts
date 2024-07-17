plugins {
    alias(libs.plugins.android) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.sentry) apply false
    alias(libs.plugins.openapi) apply false
    alias(libs.plugins.protobuf) apply false
}

buildscript {
    dependencies {
        classpath(libs.eclipse.jgit)
    }
}
