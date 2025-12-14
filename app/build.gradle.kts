import java.io.ByteArrayOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.sentry)
    alias(libs.plugins.openapi)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use(localProperties::load)
}

enum class VersionSuffix(val value: Int) {
    DEV(0),
    RELEASE(1),
    HOTFIX(2),
    MAIN(3)
}

interface Git {
    @get:Inject
    val operations: ExecOperations

    fun exec(vararg args: String): String {
        val outputStream = ByteArrayOutputStream()
        operations.exec {
            commandLine("git", *args)
            standardOutput = outputStream
        }
        return outputStream.toString().trim()
    }
}

fun getValue(name: String): String? =
    System.getenv(name.replace('.', '_').uppercase())
        ?: localProperties.getProperty(name)

fun git(vararg args: String) = project.objects.newInstance<Git>().exec(*args)

fun getVersionNumberSuffix(): VersionSuffix {
    val ref = git("describe", "--tags", "--always")
    val branch = git("rev-parse", "--abbrev-ref", "HEAD")
    return when {
        ref.matches(Regex("v?\\d+\\.\\d+\\.\\d+")) -> VersionSuffix.MAIN
        branch.startsWith("hotfix") -> VersionSuffix.HOTFIX
        branch.startsWith("release") -> VersionSuffix.RELEASE
        else -> VersionSuffix.DEV
    }
}

fun getVersionNumber(): Int {
    return (git("rev-list", "--count", "HEAD") + getVersionNumberSuffix().value).toInt()
}

fun getVersionString(variant: String? = null): String {
    val parts = git("describe", "--tags", "--always").split('-')
    val build = if (parts.size > 1) getVersionNumber().toString() else ""
    val result = StringBuilder()
    result.append(parts.first(), '+')

    if (build.isNotEmpty()) {
        result.append(build, '.')
    }

    if (variant != null) {
        result.append(variant)
    }

    return result.toString()
        .removePrefix("v")
        .removeSuffix("+")
        .removeSuffix(".")
}

android {
    namespace = "app.fyreplace.fyreplace"
    compileSdk = 36
    compileSdkMinor = 1

    defaultConfig {
        applicationId = "app.fyreplace.fyreplace"
        minSdk = 24
        targetSdk = 36
        versionCode = getVersionNumber()
        versionName = getVersionString()
        testInstrumentationRunner = "app.fyreplace.fyreplace.androidtest.TestRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "sentry_dsn", getValue("sentry.dsn").orEmpty())
        resValue("string", "sentry_environment", getVersionNumberSuffix().name.lowercase())
        resValue("string", "sentry_release", "$applicationId@${getVersionString()}")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = getValue("keystore.path")

            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
            }

            storePassword = getValue("keystore.password")
            keyAlias = getValue("key.alias")
            keyPassword = getValue("key.password")
        }
    }

    buildTypes {
        val environmentType = "app.fyreplace.fyreplace.protos.Environment"

        debug {
            applicationIdSuffix = ".debug"
            buildConfigField(
                environmentType,
                "ENVIRONMENT_DEFAULT",
                "$environmentType.LOCAL"
            )
        }

        release {
            val environment = if (getVersionNumberSuffix() == VersionSuffix.DEV) "DEV" else "MAIN"

            buildConfigField(
                environmentType,
                "ENVIRONMENT_DEFAULT",
                "$environmentType.$environment"
            )

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += listOf("version", "ecosystem")

    productFlavors {
        create("next") {
            dimension = "version"
        }

        create("legacy") {
            dimension = "version"
        }

        create("libre") {
            dimension = "ecosystem"
        }

        create("google") {
            dimension = "ecosystem"
            versionName = getVersionString(name)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }

    testOptions.unitTests.isIncludeAndroidResources = true

    sourceSets {
        named("main") {
            java.srcDir("build/openapi/src/main")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xannotation-default-target=param-property")
    }
}

composeCompiler {
    val outputDirectory = layout.buildDirectory.dir("compose_compiler")
    reportsDestination = outputDirectory
    metricsDestination = outputDirectory
}

sentry {
    org = getValue("sentry.org")
    projectName = getValue("sentry.project")
    authToken = getValue("sentry.auth.token")
    includeSourceContext = true
    ignoredFlavors = setOf("legacy", "libre")
}

openApiGenerate {
    generatorName = "kotlin"
    inputSpec = "$projectDir/src/main/assets/openapi.yaml"
    outputDir = "$projectDir/build/openapi"
    apiPackage = "app.fyreplace.api"
    modelPackage = "app.fyreplace.api.data"
    validateSpec = false
    generateModelTests = false
    generateApiTests = false
    library = "jvm-retrofit2"
    configOptions = mapOf(
        "useCoroutines" to "true",
        "moshiCodeGen" to "true",
        "serializableModel" to "true",
        "additionalModelTypeAnnotations" to "@androidx.compose.runtime.Immutable"
    )
}

tasks.named {
    it.matches(
        Regex(
            "((compile|ksp).*kotlin)|(.*sentry.*)",
            RegexOption.IGNORE_CASE
        )
    )
}.configureEach {
    dependsOn(tasks.named("openApiGenerate"))
}

protobuf {
    fun artifactName(dependency: MinimalExternalModuleDependency) =
        with(dependency) { "${group}:${name}:${version}" }

    protoc {
        artifact = artifactName(libs.protobuf.protoc.get())
    }

    plugins {
        create("grpc") {
            artifact = artifactName(libs.grpc.protoc.java.get())
        }

        create("grpckt") {
            artifact = artifactName(libs.grpc.protoc.kotlin.get())
        }
    }

    generateProtoTasks {
        all().configureEach {
            builtins {
                create("java")
                create("kotlin")
            }

            plugins {
                create("grpc")
                create("grpckt")
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(platform(libs.androidx.compose.bom))
    implementation(kotlin("reflect"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.adaptive)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.coil.compose)
    implementation(libs.hilt)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.moshi)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.kotlin)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.retrofit.converter.scalars)

    "legacyImplementation"(libs.androidx.coordinatorlayout)
    "legacyImplementation"(libs.androidx.exifinterface)
    "legacyImplementation"(libs.androidx.fragment)
    "legacyImplementation"(libs.androidx.navigation.fragment)
    "legacyImplementation"(libs.androidx.navigation.ui)
    "legacyImplementation"(libs.androidx.paging)
    "legacyImplementation"(libs.androidx.preference)
    "legacyImplementation"(libs.androidx.recyclerview)
    "legacyImplementation"(libs.androidx.recyclerview.selection)
    "legacyImplementation"(libs.androidx.swiperefreshlayout)
    "legacyImplementation"(libs.material)
    "legacyImplementation"(libs.glide)
    "legacyImplementation"(libs.glide.okhttp)
    "legacyImplementation"(libs.grpc.okhttp)
    "legacyImplementation"(libs.grpc.protobuf)
    "legacyImplementation"(libs.grpc.stub)
    "legacyImplementation"(libs.grpc.stub.kotlin)
    "legacyImplementation"(libs.kotlinx.coroutines)

    "libreImplementation"(libs.conscrypt)

    "googleImplementation"(platform(libs.firebase.bom))
    "googleImplementation"(libs.play.services.base)
    "googleImplementation"(libs.firebase.messaging)

    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.testing)

    ksp(libs.androidx.hilt.compiler)
    ksp(libs.glide.compiler)
    ksp(libs.hilt.compiler)
    ksp(libs.moshi.compiler)
}
