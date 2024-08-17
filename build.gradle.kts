plugins {
    alias(libs.plugins.android) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.sentry) apply false
    alias(libs.plugins.openapi) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.ksp) apply false
}

buildscript {
    dependencies {
        classpath(libs.eclipse.jgit)
    }
}
