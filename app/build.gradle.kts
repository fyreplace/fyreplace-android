import com.google.common.collect.Iterables
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use(localProperties::load)
}

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
    alias(libs.plugins.ksp)
}

enum class VersionSuffix(val value: Int) {
    DEV(0),
    RELEASE(1),
    HOTFIX(2),
    MAIN(3)
}

fun <R> useGitRepository(block: (Repository, Git) -> R) = RepositoryBuilder()
    .readEnvironment()
    .setWorkTree(File("."))
    .findGitDir()
    .build()
    .use { repository -> Git(repository).use { git -> block(repository, git) } }

fun getVersionNumberSuffix() = useGitRepository { repository, git ->
    val ref = git.describe().setTags(true).call()
    val branch = repository.branch
    return@useGitRepository when {
        ref.matches(Regex("v?\\d+\\.\\d+\\.\\d+")) -> VersionSuffix.MAIN
        branch.startsWith("hotfix") -> VersionSuffix.HOTFIX
        branch.startsWith("release") -> VersionSuffix.RELEASE
        else -> VersionSuffix.DEV
    }
}

fun getVersionNumber() = useGitRepository { _, git ->
    (Iterables.size(git.log().call()).toString() + getVersionNumberSuffix().value).toInt()
}

fun getVersionString(variant: String? = null) =
    useGitRepository { _, git ->
        val parts = git.describe().setTags(true).call().trim().split('-')
        val build = if (parts.size > 1) getVersionNumber().toString() else ""
        val result = StringBuilder()
        result.append(parts.first(), '+')

        if (build.isNotEmpty()) {
            result.append(build, '.')
        }

        if (variant != null) {
            result.append(variant)
        }

        return@useGitRepository result.toString()
            .removePrefix("v")
            .removeSuffix("+")
            .removeSuffix(".")
    }

android {
    namespace = "app.fyreplace.fyreplace"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.fyreplace.fyreplace"
        minSdk = 24
        targetSdk = 35
        versionCode = getVersionNumber()
        versionName = getVersionString()
        testInstrumentationRunner = "app.fyreplace.fyreplace.androidtest.TestRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "sentry_dsn", System.getenv("SENTRY_DSN").orEmpty())
        resValue("string", "sentry_environment", getVersionNumberSuffix().name.lowercase())
        resValue("string", "sentry_release", "$applicationId@${getVersionString()}")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("KEYSTORE_PATH")
                ?: localProperties.getProperty("keystore.path")

            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
            }

            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: localProperties.getProperty("keystore.password")
            keyAlias = System.getenv("KEY_ALIAS")
                ?: localProperties.getProperty("key.alias")
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: localProperties.getProperty("key.password")
        }
    }

    buildTypes {
        val environmentType = "app.fyreplace.fyreplace.protos.Environment"

        debug {
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

    flavorDimensions += "ecosystem"

    productFlavors {
        create("google") {
            dimension = "ecosystem"
            versionName = getVersionString(name)
        }

        create("libre") {
            dimension = "ecosystem"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = JvmTarget.JVM_17.target
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
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

composeCompiler {
    val outputDirectory = layout.buildDirectory.dir("compose_compiler")
    reportsDestination = outputDirectory
    metricsDestination = outputDirectory
}

sentry {
    org = System.getenv("SENTRY_ORG")
    projectName = System.getenv("SENTRY_PROJECT")
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
    includeSourceContext = true
    ignoredFlavors = setOf("libre")

    if (localProperties.getProperty("sentry.disabled") == "true") {
        ignoredBuildTypes = setOf("debug", "release")
    }
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
        "additionalModelTypeAnnotations" to "@androidx.compose.runtime.Stable"
    )
}

tasks.named {
    it.matches(
        Regex(
            "((compile|ksp).*kotlin)|(.*sentry.*)",
            RegexOption.IGNORE_CASE
        )
    )
}
    .all { dependsOn(tasks.named("openApiGenerate")) }

protobuf {
    protoc {
        artifact = with(libs.protobuf.protoc.get()) { "${group}:${name}:${version}" }
    }

    generateProtoTasks {
        all().all {
            builtins {
                create("java") { option("lite") }
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
    implementation(libs.kotlin.serialization.json)
    implementation(libs.moshi)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.protobuf.java)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.retrofit.converter.scalars)
    "googleImplementation"(libs.play.services.base)
    "libreImplementation"(libs.conscrypt)
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
    ksp(libs.hilt.compiler)
    ksp(libs.moshi.codegen)
}
