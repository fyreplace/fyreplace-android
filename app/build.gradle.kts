import com.google.common.collect.Iterables
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose)
    alias(libs.plugins.sentry)
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
    compileSdk = 34

    defaultConfig {
        applicationId = "app.fyreplace.fyreplace"
        minSdk = 24
        targetSdk = 34
        versionCode = getVersionNumber()
        versionName = getVersionString()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "sentry_dsn", System.getenv("SENTRY_DSN").orEmpty())
        resValue("string", "sentry_environment", getVersionNumberSuffix().name.lowercase())
        resValue("string", "sentry_release", "$applicationId@${getVersionString()}")
    }

    signingConfigs {
        create("release") {
            if (!System.getenv("KEYSTORE_PATH").isNullOrBlank()) {
                storeFile = file(System.getenv("KEYSTORE_PATH"))
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
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
    }

    kotlinOptions {
        jvmTarget = JvmTarget.JVM_17.target
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

sentry {
    org = System.getenv("SENTRY_ORG")
    projectName = System.getenv("SENTRY_PROJECT")
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
    includeSourceContext = true
    ignoredFlavors = setOf("libre")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.adaptive)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
